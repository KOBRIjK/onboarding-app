package com.example.darkonboarding.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.ui.theme.AccentPurple
import com.example.darkonboarding.ui.theme.Stroke
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary

@Composable
fun SelectChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (selected) AccentPurple.copy(alpha = 0.22f) else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (selected) AccentPurple.copy(alpha = 0.55f) else Stroke.copy(alpha = 0.9f))
    ) {
        Row(Modifier.padding(PaddingValues(horizontal = 14.dp, vertical = 10.dp))) {
            Text(text, color = if (selected) TextPrimary else TextSecondary)
        }
    }
}
