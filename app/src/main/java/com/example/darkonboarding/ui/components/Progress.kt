package com.example.darkonboarding.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.ui.theme.AccentCyan
import com.example.darkonboarding.ui.theme.AccentPurple

@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1D2431))
    ) {
        val p = progress.coerceIn(0f, 1f)
        Box(
            Modifier
                .fillMaxWidth(p)
                .height(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.horizontalGradient(listOf(AccentPurple, AccentCyan)))
        )
    }
}
