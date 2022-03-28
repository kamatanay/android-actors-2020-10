package se.accepted.watcher

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ActorScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.junit.Test

import org.junit.Assert.*
import kotlin.reflect.KSuspendFunction2

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

data class State(val count:Int){
    fun increment() = copy(count + 1)
    fun decrement() = copy(count - 1)
}

sealed class Messages{
    object Increment:Messages()
    object Decrement:Messages()
    data class GetState(val state: CompletableDeferred<State> = CompletableDeferred()):Messages()
}


typealias ActorFunction<M,S> = KSuspendFunction2<ActorScope<M>, S, Unit>


fun <M,S> ActorFunction<M,S>.toActorWithInitialState(state:S, scope:CoroutineScope):SendChannel<M>{
    val function = this
    return scope.actor<M>{
        function(this, state)
    }
}


tailrec suspend fun ActorScope<Messages>.counter(state:State):Unit{
    when(val message = channel.receive()){
        is Messages.Increment -> {

            val newState = state.increment()
            counter(newState)
        }
        is Messages.Decrement -> {
            counter(state.decrement())
        }
        is Messages.GetState -> {
            message.state.complete(state)
        }
    }
}


class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun actorTest() = runBlocking{

        val actor = ActorScope<Messages>::counter.toActorWithInitialState(State(0), this)

        suspend fun SendChannel<Messages>.getState():State{
            val deferredState = CompletableDeferred<State>()
            send(Messages.GetState(deferredState))
            return deferredState.await()
        }

        actor.send(Messages.Increment)
        actor.send(Messages.Increment)
        actor.send(Messages.Increment)
        actor.send(Messages.Increment)
        actor.send(Messages.Decrement)
        val state = actor.getState()
        assertEquals(3, state.count)
    }

}
