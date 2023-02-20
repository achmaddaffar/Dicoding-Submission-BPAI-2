package com.example.storyapp.ui.addStory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.storyapp.data.repository.StoryRepository
import java.io.File

class AddStoryViewModel(
    private val repository: StoryRepository
) :
    ViewModel() {

    private val mTempFile = MutableLiveData<File>()
    val tempFile: LiveData<File> = mTempFile

    fun setFile(file: File) {
        mTempFile.value = file
    }

    fun uploadStory(file: File, desc: String, lat: Double? = null, lon: Double? = null) =
        repository.uploadStory(file, desc, lat, lon).asLiveData()
}