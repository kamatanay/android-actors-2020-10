package se.accepted.watcher.ui.main

import android.util.Log
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ActorScope
import se.accepted.watcher.Message
import se.accepted.watcher.State

tailrec suspend fun TheActorScope.logActor(state:Unit){
    val message = channel.receive()
    val tag = if (message is State) "<<" else ">>"
    Log.i(tag, message.toString())
    logActor(Unit)
}