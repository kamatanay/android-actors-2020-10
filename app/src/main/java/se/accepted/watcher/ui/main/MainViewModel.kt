package se.accepted.watcher.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.*
import se.accepted.watcher.AppStream.send
import se.accepted.watcher.AppStream.states

class MainViewModel : ViewModel() {

    private val userFlow: Flow<UserState> = flow {
        emitAll(states.filterIsInstance())
    }

    val loggedIn = userFlow
        .map { it is UserState.User }
        .onStart { false }
        .onEach { _loading.postValue(false) }
        .asLiveData()

    val errorMessage = userFlow
        .filter { it is UserState.Error }
        .map { it as UserState.Error }
        .map { it.throwable.message ?: "" }
        .onStart { "" }
        .asLiveData()

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    fun login(username:String, password:String) {
        _loading.postValue(true)
        send(LoginMessage(username, password))
    }
}