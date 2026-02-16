package com.example.team16_mobile_team_project_1.game

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CannonViewModel : ViewModel() {
    val cannonX = mutableStateOf(0f)
    val cannonY = mutableStateOf(0f)
}
