package com.example.team16_mobile_team_project_1.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun KillZone() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = Color.Red,
            style = Stroke(width = 10f)
        )
    }
}
