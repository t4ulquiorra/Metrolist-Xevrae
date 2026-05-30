package com.metrolist.music.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.window.DialogProperties
import com.metrolist.music.R
import com.metrolist.music.ui.theme.xevrae.seed
import com.metrolist.music.ui.theme.xevrae.typo

@Composable
@ExperimentalMaterial3Api
fun ReviewDialog(
    onDismissRequest: () -> Unit,
    onDoneReview: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        onDismissRequest = {
            onDismissRequest.invoke()
        },
        confirmButton = {
            TextButton(onClick = {
                onDoneReview.invoke()
                uriHandler.openUri("https://github.com/t4ulquiorra/Metrolist")
            }) {
                Text(
                    "Give a star",
                    style = typo().bodySmall,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest.invoke()
            }) {
                Text(
                    "Later",
                    style = typo().bodySmall,
                )
            }
        },
        icon = {
            Icon(painterResource(R.drawable.app_logo), "App Icon")
        },
        title = {
            Text(
                "Enjoying Metrolist?",
                style = typo().labelSmall,
            )
        },
        text = {
            Text(
                buildAnnotatedString {
                    append("If you enjoy using Metrolist, star it on GitHub or leave a review on")
                    withLink(
                        LinkAnnotation.Url(
                            "https://www.producthunt.com/products/xevrae",
                            TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline, color = seed)),
                        ) {
                            onDoneReview.invoke()
                            onDismissRequest.invoke()
                            uriHandler.openUri("https://www.producthunt.com/products/xevrae")
                        },
                    ) {
                        append(" ProductHunt")
                    }
                    append("\n")
                    append("If you love my work, consider")
                    withLink(
                        LinkAnnotation.Url(
                            "https://buymeacoffee.com/t4ulquiorra",
                            TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline, color = seed)),
                        ) {
                            onDoneReview.invoke()
                            onDismissRequest.invoke()
                            uriHandler.openUri("https://buymeacoffee.com/t4ulquiorra")
                        },
                    ) {
                        append(" buying me a coffee")
                    }
                },
                textAlign = TextAlign.Center,
                style = typo().bodySmall,
            )
        },
    )
}
