package se.accepted.watcher.ui.main

import android.app.Application
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ActorScope
import se.accepted.watcher.Message

@ObsoleteCoroutinesApi
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val api = createApi()

        TheActor.run {
            userActor(api).toActor(UserState.NotLoggedIn)
        }.start()

        TheActor.run {
            TheActorScope::logActor.toActor(Unit)
        }.start()

        TheActor.run {
            TheActorScope::LoginAttemptCounterActor.toActor(0)
        }.start()
    }

    private fun createApi(): Api = ApiImpl()
}