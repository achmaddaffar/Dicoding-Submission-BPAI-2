package com.example.storyapp.data.di

import android.content.Context
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.data.database.StoryDatabase
import com.example.storyapp.data.remote.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService, context)
    }
}