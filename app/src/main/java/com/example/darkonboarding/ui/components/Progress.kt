package com.example.darkonboarding.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.darkonboarding.ui.theme.AccentCyan
import com.example.darkonboarding.ui.theme.AccentPurple
import com.example.darkonboarding.ui.theme.TextSecondary

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

@Composable
fun LoadingDots() {
    var visibleDots by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            visibleDots = if (visibleDots == 3) 1 else visibleDots + 1
        }
    }

    Row {
        repeat(visibleDots) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(TextSecondary)
            )
            if (it != visibleDots - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
