package com.example.team16_mobile_team_project_1.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Player(x: Float, y: Float) {
    Canvas(modifier = Modifier
        .size(50.dp)
        .offset(x.dp, y.dp)) {
        drawCircle(color = Color.Blue, radius = 50f, center = Offset(size.width / 2, size.height / 2))
    }
}
