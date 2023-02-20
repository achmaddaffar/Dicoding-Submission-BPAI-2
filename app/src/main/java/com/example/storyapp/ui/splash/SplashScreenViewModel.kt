package com.example.storyapp.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.storyapp.data.local.UserModel
import com.example.storyapp.data.local.UserPreference
import com.example.storyapp.data.repository.StoryRepository

class SplashScreenViewModel(private val repository: StoryRepository): ViewModel() {
    fun getUser(): LiveData<UserModel> = repository.getUser()

    fun getTheme() = repository.getTheme()
}