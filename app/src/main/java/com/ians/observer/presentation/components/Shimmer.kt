package com.ians.observer.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Paints an animated shimmer gradient behind the content.
 *
 * The gradient offset is read inside [drawBehind], so the animation only invalidates the draw
 * phase instead of recomposing on every frame. The highlight band is sized relative to the actual
 * element width, which keeps the sweep speed consistent for both wide and narrow placeholders.
 */
@Composable
fun Modifier.shimmer(shape: Shape = ShimmerShape): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ShimmerDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surfaceContainerHighest

    return this
        .clip(shape)
        .drawBehind {
            val bandWidth = size.width * ShimmerBandFraction
            val bandStart = -bandWidth + progress * (size.width + bandWidth)

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(baseColor, highlightColor, baseColor),
                    start = Offset(bandStart, 0f),
                    end = Offset(bandStart + bandWidth, 0f)
                )
            )
        }
}

private const val ShimmerBandFraction = 0.6f
private const val ShimmerDurationMillis = 1400

private val ShimmerShape = RoundedCornerShape(4.dp)
