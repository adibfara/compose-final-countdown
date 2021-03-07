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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

sealed class CountDownState {
    class InProgress(val totalAmount: Float, val startedAt: Long = System.currentTimeMillis()) :
        CountDownState()

    object NotStarted : CountDownState()
}

@Composable
fun FinalCountDown() {
    Surface {
        val scrollState = rememberScrollState()
        val timerValue = scrollState.value / 200f
        val countdownState: MutableState<CountDownState> = remember {
            mutableStateOf(CountDownState.NotStarted)
        }

        UpdateCountdownStateByScroll(countdownState, scrollState)
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {

            TimerGauge(
                scrollState,
                0.5f,
                currentValue = timerValue,
                Modifier.fillMaxSize(),
                countdownState.value
            )

            TimerGUI(timerValue) {
                val coroutineScope = rememberCoroutineScope()
                ToggleButton(countdownState.value) {
                    if (countdownState.value is CountDownState.NotStarted) {
                        if (timerValue > 0)
                            countdownState.value = CountDownState.InProgress(timerValue)
                    } else {
                        countdownState.value = CountDownState.NotStarted
                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerGauge(
    scrollState: ScrollState,
    diameterPercentage: Float,
    currentValue: Float,
    modifier: Modifier = Modifier,
    countdownState: CountDownState,
) {
    val seconds: Float = currentValue
    val ticksColor: Color = MaterialTheme.colors.onBackground
    val handleColor: Color = MaterialTheme.colors.secondary
    val primaryColor: Color = MaterialTheme.colors.primary
    val primaryVariant: Color = MaterialTheme.colors.primaryVariant
    TimerHandles(modifier, diameterPercentage, handleColor, seconds, ticksColor)

    TimerLine(
        modifier,
        scrollState,
        countdownState,
        diameterPercentage,
        seconds,
        primaryColor,
        primaryVariant
    )
    ClockBackground(modifier, diameterPercentage, currentValue, countdownState)
}

@Composable
private fun TimerLine(
    modifier: Modifier,
    scrollState: ScrollState,
    timerState: CountDownState,
    diameterPercentage: Float,
    seconds: Float,
    primaryColor: Color,
    primaryVariant: Color,
) {
    Canvas(
        modifier = modifier
            .scrollable(
                scrollState,
                orientation = Orientation.Vertical,
                enabled = timerState == CountDownState.NotStarted
            ),
        onDraw = {
            val radius = getClockSize(diameterPercentage)
            val offset = getClockOffset(radius)
            val center = getCenter(offset)
            val strokeSize = 20.dp
            val topLeft = getClockTopLeft(center, radius, strokeSize)
            val size = Size((2 * radius) + (strokeSize.toPx()), (2 * radius) + (strokeSize.toPx()))

            val ticksCount = ticksCount()
            val secondsPerTick = 60 / ticksCount
            val degree = 360 / ticksCount

            val value = seconds * (degree / secondsPerTick)
            val total = (seconds / 60).toInt()
            0.rangeTo(total).forEach {
                val sizeModifier = 0.5f.pow(it)
                drawArc(
                    Brush.verticalGradient(
                        listOf(
                            primaryColor.copy(alpha = 0.7f),
                            primaryVariant.copy(alpha = 0.7f),
                        )
                    ),
                    180f,
                    if (it < total) -value else -value % 360,
                    false,
                    topLeft,
                    size,
                    style = gaugeStroke(strokeSize * sizeModifier, StrokeCap.Round)
                )
            }
        }
    )
}

@Composable
private fun ClockBackground(
    modifier: Modifier,
    diameterPercentage: Float,
    second: Float,
    state: CountDownState,
) {
    val secondaryColor = animateColorAsState(
        targetValue = when {
            state is CountDownState.NotStarted || second % 1 > 0.2f -> MaterialTheme.colors.secondaryVariant
            else -> MaterialTheme.colors.secondary
        }
    ).value
    val radiusModifier = animateFloatAsState(
        targetValue = when {
            state is CountDownState.NotStarted || second % 1 > 0.2f -> 1f
            else -> 1.05f
        }
    ).value

    val backgroundColor = MaterialTheme.colors.background

    Canvas(
        modifier = modifier,
        onDraw = {
            val radius = getClockSize(diameterPercentage) * radiusModifier
            val offset = getClockOffset(radius)
            val center = getCenter(offset)
            drawCircle(
                Brush.radialGradient(
                    listOf(
                        backgroundColor,
                        secondaryColor
                    ),
                    center = center
                ),
                radius, center
            )
        }
    )
}

@Composable
private fun TimerHandles(
    modifier: Modifier,
    diameterPercentage: Float,
    handleColor: Color,
    seconds: Float,
    ticksColor: Color,
) {
    val lineLengthState = remember { mutableStateOf(24.dp) }
    LaunchedEffect(
        key1 = seconds,
        block = {
            lineLengthState.value = 28.dp
            delay(300)
            lineLengthState.value = 20.dp
        }
    )

    val lineLength = animateDpAsState(targetValue = lineLengthState.value).value

    Canvas(
        modifier = modifier,
        onDraw = {
            val radius = getClockSize(diameterPercentage)
            val offset = getClockOffset(radius)
            val center = getCenter(offset)

            val ticksCount = ticksCount()
            val secondsPerTick = 60 / ticksCount
            val degree = 360 / ticksCount

            val (lineStart, lineEnd) = getTickPosition(
                180f,
                center,
                radius,
                lineLength.toPx(),
                lineLength.toPx() + 8.dp.toPx()
            )
            drawLine(
                handleColor,
                lineStart, lineEnd,
                4.dp.toPx(),
                StrokeCap.Round,
            )
            val addedTicks =
                ((seconds / secondsPerTick).roundToInt() + (ticksCount / 4) + 1).coerceAtMost(
                    ticksCount
                )
            val highlightedTicksCount = seconds / secondsPerTick.toFloat()
            (0 until addedTicks).forEach {
                val distanceUntilHighlighted =
                    ((it.toFloat() - highlightedTicksCount) * -1).coerceIn(0f, 1f) * 2 + 1
                val value = (seconds % 60) * (degree / secondsPerTick)
                val theta = 180 + -value + it * degree
                val (tickStart, tickEnd) = getTickPosition(theta, center, radius, lineLength.toPx())
                drawLine(
                    ticksColor,
                    tickStart,
                    tickEnd,
                    2.dp.toPx() * distanceUntilHighlighted,
                    StrokeCap.Round
                )
            }
        }
    )
}

private fun ticksCount() = 30

private fun DrawScope.getClockTopLeft(
    center: Offset,
    radius: Float,
    strokeSize: Dp,
) = center.copy(
    center.x - radius - (strokeSize.toPx() / 2),
    center.y - radius - (strokeSize.toPx() / 2)
)

private fun DrawScope.getCenter(offset: Float) =
    size.center.copy(x = size.width + offset)

private fun getClockOffset(radius: Float) = radius * 0.2f

private fun DrawScope.getClockSize(diameterPercentage: Float) =
    (size.height * diameterPercentage) / 2

private fun getTickPosition(
    theta: Float,
    center: Offset,
    radius: Float,
    lineLength: Float,
    lineStart: Float = 0f,
): Pair<Offset, Offset> {
    val x1 = center.x + (radius + lineStart) * cos(Math.toRadians(theta.toDouble()))
    val y1 = center.y + (radius + lineStart) * sin(Math.toRadians(theta.toDouble()))
    val x2 = center.x + (radius + lineStart + lineLength) * cos(Math.toRadians(theta.toDouble()))
    val y2 = center.y + (radius + lineStart + lineLength) * sin(Math.toRadians(theta.toDouble()))
    val o1 = Offset(x1.toFloat(), y1.toFloat())
    val o2 = Offset(x2.toFloat(), y2.toFloat())
    return Pair(o1, o2)
}

private fun DrawScope.gaugeStroke(strokeSize: Dp, cap: StrokeCap) = Stroke(
    strokeSize.toPx(), cap = cap
)

@Composable
@Preview
fun RegularPreview() {
    MyTheme(darkTheme = true) {
        FinalCountDown()
    }
}
