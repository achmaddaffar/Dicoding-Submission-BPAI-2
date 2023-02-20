package com.example.storyapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.utils.Event

class ListStoryViewModel(
    repository: StoryRepository
) :
    ViewModel() {

    private val mIsLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = mIsLoading

    private val mSnackBarText = MutableLiveData<Event<String>>()
    val snackBarText: LiveData<Event<String>> = mSnackBarText

    val story: LiveData<PagingData<ListStoryItem>> =
        repository.getStory().cachedIn(viewModelScope)
}