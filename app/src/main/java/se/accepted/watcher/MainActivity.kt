package se.accepted.watcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import se.accepted.watcher.ui.main.MainFragment
import se.accepted.watcher.ui.main.PostsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, PostsFragment())
                    .commitNow()
        }
    }
}