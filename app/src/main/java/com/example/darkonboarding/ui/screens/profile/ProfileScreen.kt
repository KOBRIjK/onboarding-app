package com.example.darkonboarding.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.ui.components.GlassCard
import com.example.darkonboarding.ui.components.SelectChip
import com.example.darkonboarding.ui.theme.AccentBlue
import com.example.darkonboarding.ui.theme.AccentCyan
import com.example.darkonboarding.ui.theme.AccentPurple
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val role = remember { mutableStateOf("Backend") }
    val level = remember { mutableStateOf("Middle") }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(listOf(AccentPurple, AccentCyan)),
                            RoundedCornerShape(18.dp)
                        )
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("AI", color = Color.White)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Александр Иванов", color = TextPrimary)
                Text("alex.ivanov@company.com", color = TextSecondary)
            }
        }

        // Role
        Text("⌄  Роль", color = TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Backend", "Frontend", "Data", "QA").forEach { r ->
                SelectChip(text = r, selected = role.value == r) { role.value = r }
            }
        }

        // Team card
        Text("👥  Команда", color = TextSecondary)
        GlassCard(padding = PaddingValues(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Platform Team A", color = TextPrimary)
                    Text("8 участников", color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf("1", "2", "3", "4").forEach { n ->
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFF1A2130),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Brush.linearGradient(listOf(AccentCyan, AccentPurple))
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(n, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Level
        Text("📈  Уровень", color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SelectChip("Junior", selected = level.value == "Junior") { level.value = "Junior" }
            SelectChip("Middle", selected = level.value == "Middle") { level.value = "Middle" }
            SelectChip("Senior", selected = level.value == "Senior") { level.value = "Senior" }
        }

        // Experience
        Text("🧑‍💼  Опыт работы", color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassCard(modifier = Modifier.weight(1f), padding = PaddingValues(16.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AccentPurple)
                Spacer(Modifier.height(10.dp))
                Text("3.5", color = TextPrimary)
                Text("года опыта", color = TextSecondary)
            }
            GlassCard(modifier = Modifier.weight(1f), padding = PaddingValues(16.dp)) {
                Icon(Icons.Default.Code, contentDescription = null, tint = AccentCyan)
                Spacer(Modifier.height(10.dp))
                Text("12", color = TextPrimary)
                Text("проектов", color = TextSecondary)
            }
        }

        // Tech
        Text("Технологии", color = TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Python", "Django", "PostgreSQL", "Redis", "Docker", "Kubernetes", "AWS").forEach {
        TechChip(it)
    }

    GlassCard(padding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Выйти из аккаунта", color = TextPrimary)
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onLogout() }
            )
        }
    }
}
    }
}

@Composable
private fun TechChip(text: String) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3240))
    ) {
        Text(text, color = TextPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}
