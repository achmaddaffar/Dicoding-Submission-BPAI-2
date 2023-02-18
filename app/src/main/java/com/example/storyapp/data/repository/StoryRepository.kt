package com.example.storyapp.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.*
import com.example.storyapp.data.database.StoryDatabase
import com.example.storyapp.data.local.UserModel
import com.example.storyapp.data.local.UserPreference
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.data.remote.retrofit.ApiService
import com.example.storyapp.utils.Helper.Companion.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    context: Context
) {
    private val pref = UserPreference.getInstance(context.dataStore)

    private fun getToken(): String {
        val result = runBlocking {
            pref.getUser().first()
        }
        return result.token
    }

    fun getStory(): LiveData<PagingData<ListStoryItem>> {
        val token = getToken()

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = { storyDatabase.storyDao().getAllStory() }
        ).liveData
    }

    fun getUser() = pref.getUser().asLiveData()

    suspend fun deleteUser() = pref.deleteUser()

    suspend fun saveUser(user: UserModel) = pref.saveUser(user)

    suspend fun saveTheme(isDarkModeActive: Boolean) = pref.saveThemeSetting(isDarkModeActive)

    fun getTheme() = pref.getThemeSetting().asLiveData()

    companion object {
        private const val TAG = "StoryRepository"
    }
}