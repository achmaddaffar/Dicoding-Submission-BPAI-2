package com.example.storyapp.ui.login

import androidx.lifecycle.*
import com.example.storyapp.data.local.UserModel
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.utils.Event
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: StoryRepository
) :
    ViewModel() {

    private val mIsAnimate = MutableLiveData<Event<Boolean>>()
    val isAnimate: LiveData<Event<Boolean>> = mIsAnimate

    init {
        mIsAnimate.value = Event(false)
    }

    fun login(email: String, password: String) = repository.login(email, password).asLiveData()

    fun saveUser(user: UserModel) = viewModelScope.launch { repository.saveUser(user) }
}