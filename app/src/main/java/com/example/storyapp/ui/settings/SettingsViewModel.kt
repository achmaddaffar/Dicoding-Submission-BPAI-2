package com.example.storyapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.repository.StoryRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: StoryRepository) : ViewModel() {

    fun logout() = viewModelScope.launch { repository.deleteUser() }

    fun getThemeSetting() = repository.getTheme()

    fun saveThemeSetting(isDarkModeActive: Boolean) =
        viewModelScope.launch { repository.saveTheme(isDarkModeActive) }

}