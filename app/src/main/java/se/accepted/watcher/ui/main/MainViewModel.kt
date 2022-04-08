package se.accepted.watcher.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import se.accepted.watcher.AppStream.send
import se.accepted.watcher.AppStream.states
import se.accepted.watcher.UIMessage

object ShowLoading:UIMessage
object HideLoading:UIMessage
object LoginSuccessful:UIMessage
object LoginFailed:UIMessage

class MainViewModel : ViewModel() {

    private val uiMessageFlow:MutableSharedFlow<UIMessage> = MutableSharedFlow(extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val uiMessages = uiMessageFlow.asSharedFlow()

//    private val userFlow: Flow<UserState> = flow {
//        emitAll(states.filterIsInstance())
//    }

//
//    private val _loading = MutableLiveData<Boolean>(false)
//    val loading: LiveData<Boolean> = _loading

    suspend fun login(username:String, password:String) {
        uiMessageFlow.emit(ShowLoading)
        send(LoginMessage(username, password))
        checkCurrentUser()
    }

    suspend fun checkCurrentUser() {
        when (getCurrentUser()) {
            is UserState.User -> uiMessageFlow.emit(LoginSuccessful)
            is UserState.Error -> uiMessageFlow.emit(LoginFailed)
        }
        uiMessageFlow.emit(HideLoading)
    }

    private suspend fun getCurrentUser():UserState{
        val userState = CompletableDeferred<UserState>()
        send(GetState(state = userState))
        return userState.await()
    }
}