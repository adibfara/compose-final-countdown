package com.example.androiddevchallenge.finalcountdown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.isActive
import java.lang.Math.floor
import java.text.DecimalFormat
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */

sealed class CountDownState {
    class InProgress(val totalAmount: Float, val startedAt: Long = System.currentTimeMillis()) :
        CountDownState() {

    }

    object NotStarted : CountDownState()
}

@Composable
fun FinalCountDown() {
    Surface {
        val timerValue = remember {
            mutableStateOf(0f)
        }
        val timerState: MutableState<CountDownState> = remember {
            mutableStateOf(CountDownState.NotStarted)
        }

        val value = timerState.value
        if (value is CountDownState.InProgress) {
            LaunchedEffect(timerState.value, block = {
                while (isActive && timerValue.value > 0) {
                    delay(1)
                    timerValue.value =
                        (((value.totalAmount) - (System.currentTimeMillis() - value.startedAt).toFloat() / 1000f).toFloat()).coerceAtLeast(
                            0f
                        )
                    if (timerValue.value <= 0f) {
                        timerState.value = CountDownState.NotStarted
                    }
                }

            })
        }
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {

            TimerGauge(
                0.5f,
                currentValue = timerValue.value,
                onValueChanged = {
                    if (timerState.value is CountDownState.NotStarted)
                        timerValue.value = it
                },
                Modifier.fillMaxSize()
            )

            TimerGUI(timerValue.value) {
                ToggleButton(timerState.value) {
                    if (timerState.value is CountDownState.NotStarted) {
                        timerState.value = CountDownState.InProgress(timerValue.value)
                    } else {
                        timerState.value = CountDownState.NotStarted
                        timerValue.value = 0f

                    }
                }

            }


        }
    }
}

@Composable
private fun ToggleButton(
    timerState: CountDownState,
    onClick: () -> Unit
) {
    Button(onClick) {
        val text = if (timerState is CountDownState.NotStarted) "START" else "STOP"
        Text(text)
    }
}

@Composable
private fun TimerGUI(timerValue: Float, content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row {
                val formatter = remember { DecimalFormat("00") }
                val style = MaterialTheme.typography.h4
                Text(
                    text = formatter.format(floor(timerValue.toInt() / 60.0)),
                    style = style
                )
                Text(
                    text = ":",
                    style = style
                )
                Text(
                    formatter.format(timerValue.toInt() % 60),
                    style = style
                )
            }
            content()

        }


    }
}


@Composable
fun TimerGauge(
    diameterPercentage: Float,
    currentValue: Float,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {

    val scrollState = rememberScrollState(currentValue.toInt())
    /*if (scrollState.value > 200 * 60) {
        LaunchedEffect(Unit, block = {
            scrollState.scrollTo(200 * 60)
        })
    }*/
    onValueChanged(scrollState.value.toFloat() / 200f)
    Canvas(
        modifier = modifier
            .scrollable(
                scrollState,
                orientation = Orientation.Vertical,
            ),
        onDraw = drawHalfCircle(
            currentValue,
            diameterPercentage,
            MaterialTheme.colors.background,
            MaterialTheme.colors.onBackground,
            MaterialTheme.colors.secondary,
            MaterialTheme.colors.primary,
            MaterialTheme.colors.primaryVariant
        )
    )
}

@Composable
private fun drawHalfCircle(
    seconds: Float,
    diameterPercentage: Float,
    backgroundColor: Color,
    ticksColor: Color,
    handleColor: Color,
    primaryColor: Color,
    primaryVariant: Color,
): DrawScope.() -> Unit = {
    val radius = (size.height * diameterPercentage) / 2
    val offset = radius * 0.2f
    val center = size.center.copy(x = size.width + offset)
    val strokeSize = 20.dp
    val topLeft = center.copy(
        center.x - radius - (strokeSize.toPx() / 2),
        center.y - radius - (strokeSize.toPx() / 2)
    )
    val size = Size((2 * radius) + (strokeSize.toPx()), (2 * radius) + (strokeSize.toPx()))

    val ticksCount = 30
    val secondsPerTick = 60 / ticksCount
    val lineLength = 30.dp.toPx()
    val degree = 360 / ticksCount


    val (o1, o2) = getTickPosition(
        180f,
        center,
        radius,
        lineLength,
        lineLength + 8.dp.toPx()
    )
    drawLine(
        handleColor,
        o1, o2,
        4.dp.toPx(),
        StrokeCap.Round,

        )
    val addedTicks =
        ((seconds / secondsPerTick).roundToInt() + (ticksCount / 4) + 1).coerceAtMost(ticksCount)
    (0 until addedTicks).forEach {

        val value = (seconds % 60) * (degree / secondsPerTick)
        val theta = 180 + -value + it * degree
        val (o1, o2) = getTickPosition(theta, center, radius, lineLength)
        drawLine(
            ticksColor,
            o1,
            o2,
            2.dp.toPx(),
            StrokeCap.Round
        )
    }

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

    drawCircle(
        Brush.radialGradient(
            listOf(
                backgroundColor,
                Color(0xFF04283D)
            ),
            center = center
        ), radius, center
    )
}

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
fun TimerText(timerValue: Int, timerState: CountDownState, modifier: Modifier = Modifier) {
    Text(timerValue.toString())
}


@Composable
@Preview
fun regularPreview() {
    MyTheme(darkTheme = true) {
        FinalCountDown()
    }
}