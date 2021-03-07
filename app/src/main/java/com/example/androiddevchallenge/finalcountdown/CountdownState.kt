/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    scrollState: ScrollState,
) {
    val value = countdownState.value
    if (value is CountDownState.InProgress) {
        LaunchedEffect(
            scrollState.value,
            block = {
                while (isActive && scrollState.value > 0) {
                    delay(1)
                    scrollState.scrollTo(
                        (
                            200 * (((value.totalAmount) - (System.currentTimeMillis() - value.startedAt).toFloat() / 1000f)).coerceAtLeast(
                                0f
                            )
                            ).toInt().coerceAtLeast(0)
                    )

                    if (scrollState.value <= 0f) {
                        countdownState.value = CountDownState.NotStarted
                    }
                }
            }
        )
    }
}
