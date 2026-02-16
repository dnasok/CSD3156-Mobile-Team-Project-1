package com.example.team16_mobile_team_project_1.game

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    val playerX = mutableStateOf(0f)
    val playerY = mutableStateOf(0f)
}
