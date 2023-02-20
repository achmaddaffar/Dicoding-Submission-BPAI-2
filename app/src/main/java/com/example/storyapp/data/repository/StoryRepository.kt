package com.example.storyapp.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.*
import com.example.storyapp.data.database.StoryDatabase
import com.example.storyapp.data.local.UserModel
import com.example.storyapp.data.local.UserPreference
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.data.remote.retrofit.ApiConfig
import com.example.storyapp.data.remote.retrofit.ApiService
import com.example.storyapp.utils.Helper
import com.example.storyapp.utils.Helper.Companion.dataStore
import com.example.storyapp.utils.ScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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

    fun login(email: String, password: String) =
        flow {
            emit(ScreenState.Loading())
            try {
                val response = apiService.login(email, password)
                if (!(response.error as Boolean)) {
                    emit(ScreenState.Success(response))
                } else
                    emit(ScreenState.Error(response.message as String))
            } catch (e: Exception) {
                emit(e.localizedMessage?.let { ScreenState.Error(it) })
            }
        }

    fun register(name: String, email: String, password: String) =
        flow {
            emit(ScreenState.Loading())
            try {
                val response = apiService.register(name, email, password)
                if (!(response.error as Boolean))
                    emit(ScreenState.Success(response))
                else
                    emit(ScreenState.Error(response.message as String))
            } catch (e: Exception) {
                emit(e.localizedMessage?.let { ScreenState.Error(it) })
            }
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
                val response = ApiConfig.getApiService().getAllStory(token, location = 1)
                val list = response.listStory
                if (list != null) {
                    if (list.isEmpty())
                        emit(ScreenState.Error(response.message as String))
                    else
                        emit(ScreenState.Success(list))
                }
            } catch (e: Exception) {
                emit(e.localizedMessage?.let { ScreenState.Error(it) })
            }
        }.flowOn(Dispatchers.IO)

    fun uploadStory(file: File, desc: String, lat: Double? = null, lon: Double? = null) =
        flow {
            emit(ScreenState.Loading())
            try {
                val token = "Bearer ${getToken()}"
                val reducedFile = Helper.reduceFileImage(file)
                val description = desc.toRequestBody("text/plain".toMediaType())
                val requestImageFile = reducedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )
                val latitude = lat?.toFloat()
                val longitude = lon?.toFloat()

                val response =
                    ApiConfig.getApiService()
                        .uploadStory(token, imageMultipart, description, latitude, longitude)

                if (!(response.error as Boolean)) {
                    emit(ScreenState.Success(response))
                } else
                    emit(ScreenState.Error(response.message as String))
            } catch (e: Exception) {
                emit(e.localizedMessage?.let { ScreenState.Error(it) })
            }
        }

    fun getUser() = pref.getUser().asLiveData()

    suspend fun deleteUser() = pref.deleteUser()

    suspend fun saveUser(user: UserModel) = pref.saveUser(user)

    suspend fun saveTheme(isDarkModeActive: Boolean) = pref.saveThemeSetting(isDarkModeActive)

    fun getTheme() = pref.getThemeSetting().asLiveData()
}