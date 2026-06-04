package com.metrolist.music.extensions

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

/** Returns true when the item at [index] is currently visible in the list. */
@Composable
fun LazyListState.isElementVisible(index: Int): Boolean =
    remember(this) {
        derivedStateOf {
            layoutInfo.visibleItemsInfo.any { it.index == index }
        }
    }.value
