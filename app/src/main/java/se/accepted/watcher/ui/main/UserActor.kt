package se.accepted.watcher.ui.main

import com.github.michaelbull.result.getError
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ActorScope
import se.accepted.watcher.Message
import se.accepted.watcher.State


data class LoginMessage(val username:String, val password: String) : Message
data class GetState(val state:CompletableDeferred<UserState> = CompletableDeferred()) : Message

sealed class UserState:State{
    data class User(val username: String, val token: String):UserState()
    data class Error(val throwable: Throwable):UserState()
    object NotLoggedIn:UserState()
}


fun userActor(api:Api):ActorFunction<UserState>{

    suspend fun login(username: String, password: String):UserState {
        return api.login(username, password)
    }
    tailrec suspend fun TheActorScope.function(state:UserState){
        when(val message = channel.receive()){
            is LoginMessage -> {
                val user = login(message.username, message.password)
                send(user)
                function(user)
            }

            is GetState -> {
                message.state.complete(state)
                function(state)
            }

            else -> function(state)
        }
    }
    return TheActorScope::function
}