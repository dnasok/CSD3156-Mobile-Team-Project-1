package com.example.team16_mobile_team_project_1.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.team16_mobile_team_project_1.database.ScoreRepository

/**
 * Factory for creating instances of [GameManager] with a [ScoreRepository] dependency.
 * This is required to pass arguments to the ViewModel's constructor when using `viewModel()`.
 *
 * @param repository The repository for accessing score data, to be passed to the GameManager.
 */
class GameManagerFactory(private val repository: ScoreRepository) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given `Class`.
     *
     * @param modelClass a `Class` whose instance is requested
     * @param <T> The type parameter for the ViewModel.
     * @return a newly created ViewModel
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameManager::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameManager(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
