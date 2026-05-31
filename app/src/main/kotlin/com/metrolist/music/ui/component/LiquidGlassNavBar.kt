package com.metrolist.music.ui.component

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import androidx.core.graphics.scale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.metrolist.music.utils.Logger
import com.metrolist.music.ui.utils.PlatformBackdrop
import com.metrolist.music.ui.utils.drawBackdropCustomShape
import com.metrolist.music.ui.navigation.xevrae.destination.home.HomeDestination
import com.metrolist.music.ui.navigation.xevrae.destination.library.LibraryDestination
import com.metrolist.music.ui.navigation.xevrae.destination.search.SearchDestination
import com.metrolist.music.ui.screens.xevrae.XevraeMiniPlayer
import com.metrolist.music.ui.theme.xevrae.bottomBarSeedDark
import com.metrolist.music.ui.theme.xevrae.customDarkGray
import com.metrolist.music.ui.theme.xevrae.customGray
import com.metrolist.music.ui.theme.xevrae.transparent
import com.metrolist.music.ui.theme.xevrae.typo
import com.metrolist.music.ui.theme.xevrae.white
import com.metrolist.music.viewmodels.xevrae.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.sign
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds
import androidx.compose.ui.graphics.lerp as colorLerp

private const val TAG = "LiquidGlassNavBar"

enum class BottomNavScreen(val title: String, val destination: Any) {
    Home("Home", HomeDestination),
    Search("Search", SearchDestination),
    Library("Library", LibraryDestination)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiquidGlassNavBar(
    startDestination: Any,
    navController: NavController,
    backdrop: PlatformBackdrop,
    viewModel: SharedViewModel,
    isScrolledToTop: Boolean,
    onOpenNowPlaying: () -> Unit,
    reloadDestinationIfNeeded: (KClass<*>) -> Unit
) {
    val density = LocalDensity.current
    val layer = rememberGraphicsLayer()
    val luminanceAnimation = remember { Animatable(0f) }

    val customGrayColor by animateColorAsState(
        targetValue = colorLerp(customGray, customDarkGray, luminanceAnimation.value * 1.25f),
        animationSpec = tween(1000),
        label = "CustomGrayColorAnimation",
    )

    LaunchedEffect(layer) {
        val buffer = java.nio.IntBuffer.allocate(25)
        while (isActive) {
            try {
                val imageBitmap = layer.toImageBitmap()
                val thumbnail = withContext(Dispatchers.IO) {
                    imageBitmap
                        .asAndroidBitmap()
                        .scale(5, 5, false)
                        .copy(Bitmap.Config.ARGB_8888, false)
                }
                buffer.rewind()
                thumbnail.copyPixelsToBuffer(buffer)
            } catch (e: Exception) {
                Logger.e(TAG, "Error getting pixels from layer: ${e.localizedMessage}")
            }
            val averageLuminance =
                (0 until 25).sumOf { index ->
                    val color = buffer.get(index)
                    val r = (color shr 16 and 0xFF) / 255f
                    val g = (color shr 8 and 0xFF) / 255f
                    val b = (color and 0xFF) / 255f
                    0.2126 * r + 0.7152 * g + 0.0722 * b
                } / 25
            luminanceAnimation.animateTo(
                averageLuminance.coerceAtMost(0.8).toFloat(),
                tween(500),
            )
            delay(1.seconds)
        }
    }

    val nowPlayingData by viewModel.nowPlayingState.collectAsStateWithLifecycle()
    var isShowMiniPlayer by rememberSaveable {
        mutableStateOf(true)
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val bottomNavScreens =
        listOf(
            BottomNavScreen.Home,
            BottomNavScreen.Search,
            BottomNavScreen.Library,
        )
    var selectedIndex by rememberSaveable {
        mutableIntStateOf(
            when (startDestination) {
                is HomeDestination -> BottomNavScreen.Home.ordinal
                is SearchDestination -> BottomNavScreen.Search.ordinal
                is LibraryDestination -> BottomNavScreen.Library.ordinal
                else -> BottomNavScreen.Home.ordinal
            },
        )
    }
    var previousSelectedIndex by rememberSaveable {
        mutableIntStateOf(selectedIndex)
    }
    var isExpanded by rememberSaveable {
        mutableStateOf(true)
    }

    var isInSearchDestination by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(nowPlayingData) {
        isShowMiniPlayer = nowPlayingData?.mediaItem != null
    }

    LaunchedEffect(currentBackStackEntry) {
        currentBackStackEntry?.destination?.let { current ->
            Logger.d(TAG, "LiquidGlassNavBar: current route: ${current.route}")
            isInSearchDestination = current.hasRoute(SearchDestination::class)
        }
    }

    LaunchedEffect(isInSearchDestination) {
        isExpanded = !isInSearchDestination
    }

    var updateConstraints by remember {
        mutableStateOf(true)
    }

    var constraintSet by remember {
        mutableStateOf(
            decoupledConstraints(isShowMiniPlayer, isExpanded),
        )
    }

    LaunchedEffect(isShowMiniPlayer, isExpanded) {
        constraintSet = decoupledConstraints(isShowMiniPlayer, isExpanded)
        updateConstraints = false
    }

    LaunchedEffect(updateConstraints) {
        if (updateConstraints) {
            constraintSet = decoupledConstraints(isShowMiniPlayer, isExpanded)
            updateConstraints = false
        }
    }

    LaunchedEffect(isScrolledToTop) {
        Logger.d(TAG, "isScrolledToTop: $isScrolledToTop")
        if (!isInSearchDestination) {
            isExpanded = isScrolledToTop
        }
    }

    ConstraintLayout(
        constraintSet = constraintSet,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    WindowInsets.navigationBars.asPaddingValues(),
                )
                .padding(
                    bottom = 8.dp,
                )
                .imePadding(),
        animateChangesSpec = tween(300),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .padding(start = 16.dp)
                    .wrapContentSize()
                    .layoutId("toolbar"),
        ) {
            HorizontalFloatingToolbar(
                modifier =
                    Modifier
                        .drawBackdropCustomShape(
                            backdrop,
                            layer,
                            luminanceAnimation.value,
                            CircleShape,
                        )
                        .then(
                            if (!isExpanded) {
                                Modifier.size(48.dp)
                            } else {
                                Modifier.wrapContentSize()
                            },
                        )
                        .onGloballyPositioned {
                            updateConstraints = true
                        },
                contentPadding =
                    PaddingValues(
                        horizontal = if (isExpanded) 4.dp else 0.dp,
                    ),
                colors =
                    FloatingToolbarDefaults
                        .standardFloatingToolbarColors()
                        .copy(
                            toolbarContainerColor = transparent,
                        ),
                expanded = isExpanded,
                trailingContent = {
                    var buttonSize by remember { mutableStateOf(0.dp to 0.dp) }
                    bottomNavScreens.filter { it != BottomNavScreen.Search }.forEach { screen ->
                        Box {
                            if (selectedIndex == screen.ordinal) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(buttonSize.first, buttonSize.second)
                                            .clip(CircleShape)
                                            .blur(8.dp),
                                )
                            }
                            Button(
                                modifier =
                                    Modifier.onGloballyPositioned {
                                        if (selectedIndex == screen.ordinal) {
                                            buttonSize = with(density) { it.size.width.toDp() to it.size.height.toDp() }
                                        }
                                    },
                                onClick = {
                                    if (selectedIndex == screen.ordinal) {
                                        if (currentBackStackEntry?.destination?.hierarchy?.any {
                                                it.hasRoute(screen.destination::class)
                                            } == true
                                        ) {
                                            reloadDestinationIfNeeded(
                                                screen.destination::class,
                                            )
                                        } else {
                                            navController.navigate(screen.destination)
                                        }
                                    } else {
                                        previousSelectedIndex = selectedIndex
                                        selectedIndex = screen.ordinal
                                        navController.navigate(screen.destination) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                shape = CircleShape,
                                colors =
                                    ButtonDefaults
                                        .buttonColors()
                                        .copy(
                                            disabledContainerColor = transparent,
                                            containerColor =
                                                if (selectedIndex == screen.ordinal) {
                                                    customGrayColor
                                                } else {
                                                    transparent
                                                },
                                            contentColor =
                                                if (selectedIndex == screen.ordinal) {
                                                    bottomBarSeedDark
                                                } else {
                                                    white
                                                },
                                        ),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    // Simplified Icons for now since resources are not yet migrated/available in the same way
                                    Icon(
                                        imageVector = if (screen == BottomNavScreen.Home) Icons.Outlined.Search else Icons.Outlined.Search, // Placeholder
                                        contentDescription = screen.title,
                                    )
                                    Text(
                                        screen.title,
                                        style =
                                            if (selectedIndex == screen.ordinal) {
                                                typo().bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            } else {
                                                typo().bodySmall
                                            },
                                        color =
                                            if (selectedIndex == screen.ordinal) {
                                                bottomBarSeedDark
                                            } else {
                                                white
                                            },
                                    )
                                }
                            }
                        }
                    }
                },
            ) {
                if (!isExpanded) {
                    val screen = bottomNavScreens.find { it.ordinal == selectedIndex } ?: return@HorizontalFloatingToolbar
                    val previousScreen =
                        bottomNavScreens.find { it.ordinal == previousSelectedIndex } ?: return@HorizontalFloatingToolbar
                    IconButton(
                        modifier =
                            Modifier.size(
                                FloatingToolbarDefaults.ContainerSize.value.dp,
                            ),
                        shape = CircleShape,
                        onClick = {
                            if (screen == BottomNavScreen.Search) {
                                val destination =
                                    when (previousScreen) {
                                        BottomNavScreen.Home -> HomeDestination
                                        BottomNavScreen.Library -> LibraryDestination
                                        else -> HomeDestination
                                    }
                                selectedIndex = previousSelectedIndex
                                previousSelectedIndex = BottomNavScreen.Search.ordinal
                                navController.navigate(destination) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                isExpanded = true
                            }
                        },
                        colors =
                            IconButtonDefaults.iconButtonColors().copy(
                                contentColor = bottomBarSeedDark,
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search, // Placeholder
                            contentDescription = "",
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(Modifier.size(12.dp))
            }

            val searchColor by animateColorAsState(
                targetValue = if (luminanceAnimation.value > 0.6f) Color(0xFF121212) else Color.White,
                label = "MiniPlayerTextColor",
                animationSpec = tween(500),
            )

            AnimatedVisibility(
                visible = !isInSearchDestination && isExpanded,
                enter =
                    slideInHorizontally(
                        tween(100),
                    ) { it / 2 },
                exit =
                    slideOutHorizontally(
                        tween(100),
                    ) { -it / 2 },
            ) {
                FloatingActionButton(
                    modifier =
                        Modifier.drawBackdropCustomShape(
                            backdrop,
                            layer,
                            luminanceAnimation.value,
                            CircleShape,
                        ),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    onClick = {
                        previousSelectedIndex = selectedIndex
                        selectedIndex = BottomNavScreen.Search.ordinal
                        navController.navigate(BottomNavScreen.Search.destination) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    shape = CircleShape,
                    containerColor = transparent,
                    contentColor = transparent,
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        "",
                        tint = searchColor,
                    )
                }
            }
        }
        XevraeMiniPlayer(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(60.dp)
                .layoutId("miniPlayer"),
            backdrop = backdrop,
            onClick = {
                onOpenNowPlaying()
            },
            onClose = {
                // To be wired to PlayerConnection later
            },
        )
    }
}

private fun decoupledConstraints(
    isMiniplayerShow: Boolean = true,
    isExpanded: Boolean,
): ConstraintSet =
    ConstraintSet {
        val toolbar = createRefFor("toolbar")
        constrain(toolbar) {
            bottom.linkTo(parent.bottom)
            width = Dimension.wrapContent
            height = Dimension.wrapContent
            if (!isExpanded) {
                start.linkTo(parent.start)
            } else {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        }
        val miniPlayer = createRefFor("miniPlayer")
        constrain(miniPlayer) {
            if (!isExpanded) {
                start.linkTo(toolbar.end)
                end.linkTo(parent.end)
                top.linkTo(toolbar.top)
                bottom.linkTo(toolbar.bottom)
                width = if (isMiniplayerShow) Dimension.fillToConstraints else Dimension.wrapContent
            } else {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(toolbar.top, margin = 12.dp)
                width = if (isMiniplayerShow) Dimension.matchParent else Dimension.wrapContent
            }
            visibility =
                if (isMiniplayerShow) {
                    Visibility.Visible
                } else {
                    Visibility.Gone
                }
        }
    }
