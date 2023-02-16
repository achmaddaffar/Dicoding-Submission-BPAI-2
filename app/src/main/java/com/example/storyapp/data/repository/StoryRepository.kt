package com.example.storyapp.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.*
import com.example.storyapp.data.database.StoryDatabase
import com.example.storyapp.data.local.UserModel
import com.example.storyapp.data.local.UserPreference
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.data.remote.retrofit.ApiService
import com.example.storyapp.utils.Helper.Companion.dataStore

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val context: Context
) {
    private val pref = UserPreference.getInstance(context.dataStore)

    fun getStory(): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, context),
            pagingSourceFactory = { storyDatabase.storyDao().getAllStory() }
        ).liveData
    }

    fun getUser() = pref.getUser().asLiveData()

    suspend fun deleteUser() = pref.deleteUser()

    suspend fun saveUser(user: UserModel) = pref.saveUser(user)

    suspend fun saveTheme(isDarkModeActive: Boolean) = pref.saveThemeSetting(isDarkModeActive)

    fun getTheme() = pref.getThemeSetting().asLiveData()

    // UBAH PARAMETER SMUA VM JADI APPLICATION CONTEXT
    // APAKAH DATA CLASS ENTITIY BUAT DATABASE SAMA RESPONSE REPOSITORY ITU BOLEH JADI 1?
}