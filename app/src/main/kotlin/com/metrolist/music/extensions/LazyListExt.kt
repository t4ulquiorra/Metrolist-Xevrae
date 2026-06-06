package com.metrolist.music.extensions

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned

/** Returns true when the item at [index] is currently visible in the list. */
@Composable
fun LazyListState.isElementVisible(index: Int): Boolean =
    remember(this) {
        derivedStateOf {
            layoutInfo.visibleItemsInfo.any { it.index == index }
        }
    }.value

/** Modifier extension: calls [onVisibilityChanged] with true/false as attachment changes. */
fun Modifier.isElementVisible(onVisibilityChanged: (Boolean) -> Unit): Modifier =
    this.onGloballyPositioned { coords ->
        onVisibilityChanged(coords.isAttached)
    }

@Composable
fun LazyGridState.isScrollingUp(): androidx.compose.runtime.State<Boolean> {
    var previousIndex by remember(this) { androidx.compose.runtime.mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { androidx.compose.runtime.mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }
}
