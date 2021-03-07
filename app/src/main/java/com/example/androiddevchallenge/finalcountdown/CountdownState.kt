package com.example.androiddevchallenge.finalcountdown

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun UpdateCountdownStateByScroll(
    countdownState: MutableState<CountDownState>,
    scrollState: ScrollState
) {
    val value = countdownState.value
    if (value is CountDownState.InProgress) {
        LaunchedEffect(scrollState.value, block = {
            while (isActive && scrollState.value > 0) {
                delay(1)
                scrollState.scrollTo(
                    (200 * (((value.totalAmount) - (System.currentTimeMillis() - value.startedAt).toFloat() / 1000f)).coerceAtLeast(
                        0f
                    )).toInt().coerceAtLeast(0)
                )

                if (scrollState.value <= 0f) {
                    countdownState.value = CountDownState.NotStarted
                }
            }

        })
    }
}