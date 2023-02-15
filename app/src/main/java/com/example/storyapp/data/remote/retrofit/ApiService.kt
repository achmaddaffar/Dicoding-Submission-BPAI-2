package com.example.storyapp.data.remote.retrofit

import androidx.paging.PagingSource
import com.example.storyapp.data.remote.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @Multipart
    @POST("stories")
    fun uploadStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): Call<UploadStoryResponse>

    @Multipart
    @POST("stories")
    fun uploadStoryAsGuest(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): Call<UploadStoryResponse>

    @GET("stories")
    fun getAllStory(
        @Header("Authorization") token: String,
        @Query("page") page: String? = null,
        @Query("size") size: String? = null,
        @Query("location") location: Int? = null
    ): PagingSource<Int, ListStoryItem>
}
