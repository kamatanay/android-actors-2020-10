package se.accepted.watcher

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

interface UIMessage

object UIStream {

    private val uiStream: Channel<UIMessage> = Channel(capacity = 1000)

    suspend fun update(message:UIMessage) = uiStream.send(message)

    fun uiMessages() = uiStream.receiveAsFlow()


}