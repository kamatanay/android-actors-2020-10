package se.accepted.watcher.ui.main

import android.app.Application
import kotlinx.coroutines.ObsoleteCoroutinesApi
import se.accepted.watcher.network.APIHelper

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
        TheActor.run {
            postsActor(APIHelper.postsService).toActor(PostsState.NotFetched)
        }.start()
    }

    private fun createApi(): Api = ApiImpl()
}