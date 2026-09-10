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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun CircularReveal(
    progress: Float,
    originInRoot: Offset,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }

    val isRevealing = progress < 1f
    val localOrigin = if (originInRoot.isSpecified) {
        originInRoot - positionInRoot
    } else {
        Offset.Unspecified
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                positionInRoot = coordinates.boundsInRoot().topLeft
            }
            .then(
                if (isRevealing) {
                    Modifier
                        .clip(CircularRevealShape(progress = progress, origin = localOrigin))
                        .background(MaterialTheme.colorScheme.background)
                } else {
                    Modifier
                }
            )
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

        val fullRadius = listOf(
            Offset.Zero,
            Offset(size.width, 0f),
            Offset(0f, size.height),
            Offset(size.width, size.height)
        ).maxOf { corner -> (corner - center).getDistance() }

        val path = Path().apply {
            addOval(Rect(center = center, radius = fullRadius * progress))
        }

        return Outline.Generic(path)
    }
}
