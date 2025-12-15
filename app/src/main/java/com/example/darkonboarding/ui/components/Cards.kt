package com.example.darkonboarding.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.ui.theme.CardBg
import com.example.darkonboarding.ui.theme.Stroke

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 18.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    gradientStroke: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(corner)
    val border = if (gradientStroke) {
        BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(Color(0xFF22D3EE), Color(0xFF8B5CF6))
            )
        )
    } else {
        BorderStroke(1.dp, Stroke.copy(alpha = 0.85f))
    }

    Surface(
        modifier = modifier,
        color = CardBg.copy(alpha = 0.92f),
        shape = shape,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
            content()
        }
    }
}
