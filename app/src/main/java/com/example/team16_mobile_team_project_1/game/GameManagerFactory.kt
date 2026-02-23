package com.example.team16_mobile_team_project_1.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.team16_mobile_team_project_1.database.ScoreRepository

class GameManagerFactory(private val repository: ScoreRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameManager::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameManager(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
