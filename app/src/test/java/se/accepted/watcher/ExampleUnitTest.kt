package se.accepted.watcher

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

import app.cash.turbine.test
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import se.accepted.watcher.ui.main.*

class MainViewModelTest(){

    @Test
    fun itShouldShowSuccessfulMessageOnValidUserLogin() = runTest {
        val mainViewModel = MainViewModel()

        GlobalScope.launch {
            AppStream.messages.test {
                assertEquals(awaitItem(), LoginMessage("",""))
                val message = awaitItem()
                if (message is GetState){
                    message.state.complete(UserState.Error(Throwable("")))
                }
            }
        }

        mainViewModel.uiMessages.test {
            mainViewModel.login("","")
            assertEquals(awaitItem(), ShowLoading)
            assertEquals(awaitItem(), LoginFailed)
            assertEquals(awaitItem(), HideLoading)
            cancelAndConsumeRemainingEvents()
        }
    }

}

