package se.accepted.watcher.ui.main

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.delay
import kotlin.random.Random

interface Api {
    suspend fun login(username: String, password: String): UserState
}

/**
 * Fakes the implementation of an API call
 */
class ApiImpl : Api {
    override suspend fun login(username: String, password: String): UserState {
        delay(Random.nextLong(500, 2000))
        return when(Random.nextBoolean()) {
            true -> UserState.User("Bob", "secret")
            false -> UserState.Error(Error("We got error"))
        }
    }
}