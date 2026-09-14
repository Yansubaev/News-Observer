package com.ians.observer.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun CircularReveal(
    progressProvider: () -> Float,
    originInRoot: Offset,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var positionInRoot by remember { mutableStateOf(Offset.Unspecified) }
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                val newPositionInRoot = coordinates.boundsInRoot().topLeft
                if (positionInRoot != newPositionInRoot) {
                    positionInRoot = newPositionInRoot
                }
            }
            .graphicsLayer {
                val coordinatesReady = positionInRoot.isSpecified
                val localOrigin = when {
                    !coordinatesReady -> Offset.Unspecified
                    originInRoot.isSpecified -> originInRoot - positionInRoot
                    else -> Offset(size.width, 0f)
                }
                val revealProgress = if (coordinatesReady) {
                    progressProvider().coerceIn(0f, 1f)
                } else {
                    0f
                }

                clip = revealProgress < 1f
                shape = CircularRevealShape(
                    progress = revealProgress,
                    origin = localOrigin
                )
            }
            .background(backgroundColor)
    ) {
        content()
    }
}

private class CircularRevealShape(
    private val progress: Float,
    private val origin: Offset,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val center = if (origin.isSpecified) origin else Offset(size.width, 0f)

        val farthestX = maxOf(center.x, size.width - center.x)
        val farthestY = maxOf(center.y, size.height - center.y)
        val fullRadius = Offset(farthestX, farthestY).getDistance()

        val path = Path().apply {
            addOval(Rect(center = center, radius = fullRadius * progress))
        }

        return Outline.Generic(path)
    }
}
