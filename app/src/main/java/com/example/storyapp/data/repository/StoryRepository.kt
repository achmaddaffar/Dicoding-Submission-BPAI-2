package com.example.storyapp.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.*
import com.example.storyapp.R
import com.example.storyapp.data.database.StoryDatabase
import com.example.storyapp.data.local.UserModel
import com.example.storyapp.data.local.UserPreference
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.data.remote.retrofit.ApiConfig
import com.example.storyapp.data.remote.retrofit.ApiService
import com.example.storyapp.utils.Helper.Companion.dataStore
import com.example.storyapp.utils.ScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val context: Context
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

    fun getStoryWithLocation() =
        flow {
            emit(ScreenState.Loading())
            try {
                val token = "Bearer ${getToken()}"
                val list = ApiConfig.getApiService().getAllStory(token, location = 1).listStory
                if (list != null) {
                    if (list.isEmpty())
                        emit(ScreenState.Error(context.getString(R.string.failed_to_connect)))
                    else
                        emit(ScreenState.Success(list))
                }
            } catch (e: Exception) {
                emit(e.localizedMessage?.let { ScreenState.Error(it) })
            }
        }.flowOn(Dispatchers.IO)


    fun getUser() = pref.getUser().asLiveData()

    suspend fun deleteUser() = pref.deleteUser()

    suspend fun saveUser(user: UserModel) = pref.saveUser(user)

    suspend fun saveTheme(isDarkModeActive: Boolean) = pref.saveThemeSetting(isDarkModeActive)

    fun getTheme() = pref.getThemeSetting().asLiveData()
}