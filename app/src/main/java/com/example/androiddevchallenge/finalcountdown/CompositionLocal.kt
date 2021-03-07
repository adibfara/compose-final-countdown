package com.example.androiddevchallenge.finalcountdown

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf


enum class Theme {
    Night,
    Day
}
val LocalThemeComposition = compositionLocalOf { mutableStateOf<Theme>(Theme.Night) }
