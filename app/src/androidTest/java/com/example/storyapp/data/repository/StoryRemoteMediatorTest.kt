package com.example.storyapp.data.repository

import androidx.paging.*
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.storyapp.data.database.StoryDatabase
import com.example.storyapp.data.remote.response.*
import com.example.storyapp.data.remote.retrofit.ApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalPagingApi
@RunWith(AndroidJUnit4::class)
class StoryRemoteMediatorTest {

    private var mockApi: ApiService = FakeApiService()
    private val mockDb: StoryDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        StoryDatabase::class.java
    ).allowMainThreadQueries().build()
    private val token = "12345"

    @Test
    fun refreshLoadReturnsSuccessResultWhenMoreDataIsPresent() = runTest {
        val remoteMediator = StoryRemoteMediator(
            mockDb,
            mockApi,
            token
        )
        val pagingState = PagingState<Int, ListStoryItem>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @After
    fun tearDown() {
        mockDb.clearAllTables()
    }
}

class FakeApiService : ApiService {
    override suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return RegisterResponse(false, "success")
    }

    override suspend fun login(email: String, password: String): LoginResponse {
        return LoginResponse(LoginResult("name", "1", "12345"), false, "success")
    }

    override suspend fun uploadStory(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: Float?,
        lon: Float?
    ): UploadStoryResponse {
        return UploadStoryResponse(false, "success")
    }

    override suspend fun getAllStory(
        token: String,
        page: Int?,
        size: Int?,
        location: Int?
    ): StoryResponse {
        val item: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                "https://w7.pngwing.com/pngs/798/436/png-transparent-computer-icons-user-profile-avatar-profile-heroes-black-profile-thumbnail.png",
                "created at $i",
                "user $i",
                "description $i",
                i.toDouble(),
                "$i",
                i.toDouble()
            )
            item.add(story)
        }
        return StoryResponse(item, false, "success")
    }
}