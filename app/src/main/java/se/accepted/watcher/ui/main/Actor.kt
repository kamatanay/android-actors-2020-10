package se.accepted.watcher.ui.main

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ActorScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.collect
import se.accepted.watcher.AppStream
import se.accepted.watcher.AppStream.messages
import se.accepted.watcher.Message
import kotlin.reflect.KSuspendFunction2

typealias ActorFunction<S> = KSuspendFunction2<TheActorScope,S, Unit>


class TheActorScope(private val scope:ActorScope<Message>):ActorScope<Message> by scope{
    fun send(message: Message) = AppStream.send(message)
}

data class TheActor<S> private constructor(val actorFunction:ActorFunction<S>, val initialState: S){

    companion object{

        fun <S> ActorFunction<S>.toActor(state:S): TheActor<S> {
            return TheActor(this, state)
        }

    }

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    fun start() = scope.launch {
        val actor = actor<Message>(scope.coroutineContext) {
            val actorScope = TheActorScope(this)
            actorFunction(actorScope, initialState)
        }
        messages.collect(actor::send)
    }

    fun stop() {
        scope.cancel()
    }

}