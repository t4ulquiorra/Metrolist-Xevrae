package com.metrolist.music.ui.screens.xevrae.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metrolist.music.ui.utils.DiscordWebView
import com.metrolist.music.ui.utils.rememberWebViewState
import com.metrolist.music.extensions.getStringBlocking
import com.metrolist.music.ui.component.DevLogInBottomSheet
import com.metrolist.music.ui.component.DevLogInType
import com.metrolist.music.ui.component.RippleIconButton
import com.metrolist.music.ui.theme.xevrae.typo
import com.metrolist.music.viewmodels.xevrae.LogInViewModel
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscordLoginScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: LogInViewModel = hiltViewModel(),
    hideBottomNavigation: () -> Unit,
    showBottomNavigation: () -> Unit,
) {
    var devLoginSheet by rememberSaveable {
        mutableStateOf(false)
    }
    // Hide bottom navigation when entering this screen
    LaunchedEffect(Unit) {
        hideBottomNavigation()
    }

    // Show bottom navigation when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            showBottomNavigation()
        }
    }

    val state = rememberWebViewState()
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Spacer(
                Modifier
                    .size(
                        innerPadding.calculateTopPadding() + 64.dp,
                    ),
            )
            // WebView for Discord login
            DiscordWebView(
                state,
                aboveContent = {
                    if (devLoginSheet) {
                        DevLogInBottomSheet(
                            onDismiss = {
                                devLoginSheet = false
                            },
                            onDone = { token, _ ->
                                devLoginSheet = false
                                viewModel.saveDiscordToken(token)
                                viewModel.makeToast(getStringBlocking(com.metrolist.music.R.string.login_success))
                                navController.navigateUp()
                            },
                            type = DevLogInType.Discord,
                        )
                    }
                }
            ) { token ->
                viewModel.saveDiscordToken(token)
                viewModel.makeToast(getStringBlocking(com.metrolist.music.R.string.login_success))
                navController.navigateUp()
            }
        }
        TopAppBar(
            modifier =
                Modifier
                    .align(Alignment.TopCenter),
            title = {
                Text(
                    text = stringResource(com.metrolist.music.R.string.log_in_to_discord),
                    style = typo().titleMedium,
                )
            },
            navigationIcon = {
                Box(Modifier.padding(horizontal = 5.dp)) {
                    RippleIconButton(
                        com.metrolist.music.R.drawable.baseline_arrow_back_ios_new_24,
                        Modifier.size(32.dp),
                        true,
                    ) {
                        navController.navigateUp()
                    }
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        devLoginSheet = true
                    },
                ) {
                    Icon(
                        Icons.Default.LogoDev,
                        "Developer Mode",
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
        )
    }
}