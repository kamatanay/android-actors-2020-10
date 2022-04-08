package se.accepted.watcher.ui.main

import se.accepted.watcher.Message


class LogAttemptMessage(val count:Int):Message{
    override fun toString(): String {
        return "Login Attempts: $count"
    }
}

tailrec suspend fun TheActorScope.LoginAttemptCounterActor(count:Int){

    when(channel.receive()){
        is LoginMessage -> {
            send(LogAttemptMessage(count + 1))
            LoginAttemptCounterActor(count + 1)
        }
        else -> LoginAttemptCounterActor(count)
    }

}