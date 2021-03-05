package com.example.androiddevchallenge.finalcountdown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Adib Faramarzi (adibfara@gmail.com)
 */

sealed class CountDownState {
    data class Started(val remainingTime: Int) : CountDownState()
    object NotStarted : CountDownState()
}

@Composable
fun FinalCountDown() {
    Surface {
        val timerValue = remember {
            mutableStateOf(10)
        }
        val timerState: MutableState<CountDownState.NotStarted> = remember {
            mutableStateOf(CountDownState.NotStarted)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            TimerText(timerValue.value, timerState.value)
            val second = remember { mutableStateOf(0) }
            TimerGauge(
                0.5f,
                currentValue = second.value,
                onValueChanged = { second.value = it },
                Modifier.fillMaxSize()
            )

        }
    }
}


@Composable
fun TimerGauge(
    diameterPercentage: Float,
    currentValue: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
    backgroundColor: Color = MaterialTheme.colors.secondary,
    ticksColor: Color = MaterialTheme.colors.primaryVariant.copy(alpha = 0.5f)
) {

    val scrollState = rememberScrollState()
    onValueChanged(scrollState.value)
    Canvas(
        modifier = modifier
            .scrollable(scrollState, orientation = Orientation.Vertical),
        onDraw = drawHalfCircle(
            currentValue.toFloat(),
            diameterPercentage,
            backgroundColor,
            color,
            ticksColor
        )
    )
}

@Composable
private fun drawHalfCircle(
    scrollAmount: Float,
    diameterPercentage: Float,
    backgroundColor: Color,
    color: Color,
    ticksColor: Color
): DrawScope.() -> Unit = {
    val radius = (size.height * diameterPercentage) / 2
    val offset = radius * 0.2f
    val center = size.center.copy(x = size.width + offset)
    val topLeft = center.copy(center.x - radius, center.y - radius)
    val size = Size(2 * radius, 2 * radius)
    val strokeSize = 30.dp

    val ticksCount = 12
    val lineLength = 30.dp.toPx()
    val degree = 360 / ticksCount

    val value = ((scrollAmount) / degree).toInt()

    val initialRotation = value

    val (o1, o2) = getTickPosition( 180.toInt(), center, radius, lineLength, lineLength + 8.dp.toPx())
    drawLine(
        ticksColor,
        o1, o2,
        4.dp.toPx(),
        StrokeCap.Round,

    )
    0.rangeTo(ticksCount - 1).forEach {
        val theta = 180 +  -initialRotation + it * degree
        val (o1, o2) = getTickPosition( theta.toInt(), center, radius, lineLength, )
        drawLine(
         ticksColor,
            o1, o2,
            2.dp.toPx(),
            StrokeCap.Round
        )
    }
    drawArc(
        color,
        180f,
        -value.toFloat(),
        false,
        topLeft,
        size,
        style = gaugeStroke(strokeSize, StrokeCap.Butt)
    )
    /* drawCircle(Color.Black, radius,, style = Stroke(
             10.dp.toPx(),
         ))*/
}

private fun getTickPosition(
    theta: Int,
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

@Preview
@Composable
fun regularPreview() {
    FinalCountDown()
}