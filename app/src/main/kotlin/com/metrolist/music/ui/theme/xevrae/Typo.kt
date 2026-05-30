package com.metrolist.music.ui.theme.xevrae

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.metrolist.music.R

@Composable
fun fontFamily(): FontFamily =
    FontFamily(
        Font(R.font.nunito, FontWeight.Medium, FontStyle.Normal),
    )

@Composable
fun typo(): Typography {
    val fontFamily = fontFamily()

    val typo =
        Typography(
            /***
             * This typo().is use for the title of the Playlist, Artist, Song, Album, etc. in Home, Mood, Genre, Playlist, etc.
             */
            titleSmall =
                TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontFamily,
                    color = Color.White,
                ),
            titleMedium =
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = fontFamily,
                    color = Color.White,
                ),
            titleLarge =
                TextStyle(
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = fontFamily,
                    color = Color.White,
                ),
            bodySmall =
                TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontFamily,
                    color = Color(0xFFB8B8B8),
                ),
            bodyMedium =
                TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontFamily,
                    color = Color(0xFFB8B8B8),
                ),
            bodyLarge =
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontFamily,
                    color = Color(0xFFB8B8B8),
                ),
            displayLarge =
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontFamily,
                    color = Color(0xFFB8B8B8),
                ),
            headlineMedium =
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = fontFamily,
                    color = Color(0xFFB8B8B8),
                ),
            headlineLarge =
                TextStyle(
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = fontFamily,
                    color = Color(0xFFB8B8B8),
                ),
            labelMedium =
                TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = fontFamily,
                    color = Color(0xFFB8B8B8),
                ),
            labelSmall =
                TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = fontFamily,
                    color = Color(0xFFB8B8B8),
                ),
            // ...
        )
    return typo
}