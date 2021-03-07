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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

@Composable
fun TimerGUI(timerValue: Float, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Header()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.widthIn(140.dp)) {

                Row(verticalAlignment = Alignment.Bottom) {
                    val formatter = remember { DecimalFormat("00") }
                    val style = MaterialTheme.typography.h4
                    Text(
                        text = formatter.format(Math.floor(timerValue.toInt() / 60.0)),
                        style = style,

                    )
                    Text(
                        text = ":",
                        style = style
                    )
                    Text(
                        formatter.format(timerValue.toInt() % 60),
                        style = style
                    )
                    val milliseconds = ((timerValue - timerValue.toInt()) * 1000).toInt()
                    Text(
                        ".$milliseconds",
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                content()
            }
        }

        Footer()
    }
}

@Composable
fun ToggleButton(
    timerState: CountDownState,
    onClick: () -> Unit,
) {
    Button(onClick) {
        val text = if (timerState is CountDownState.NotStarted) "START" else "STOP"
        Text(text)
    }
}

@Composable
private fun Header() {
    Row {
        Text(
            "Final Countdown",
            style = MaterialTheme.typography.h5
        )
        Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxWidth()) {
            val currentTheme = LocalThemeComposition.current
            val isChecked = currentTheme.value == Theme.Night
            IconToggleButton(
                checked = isChecked,
                onCheckedChange = {
                    currentTheme.value = if (it) Theme.Night else Theme.Day
                }
            ) {
                Text(text = if (isChecked) "Light" else "Dark")
            }
        }
    }
}

@Composable
private fun Footer() {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Made with ❤ by Adib Faramarzi",
                style = MaterialTheme.typography.body2,
            )
            Text(
                text = "https://github.com/adibfara/compose-final-countdown",
                style = MaterialTheme.typography.caption
            )
        }
    }
}
