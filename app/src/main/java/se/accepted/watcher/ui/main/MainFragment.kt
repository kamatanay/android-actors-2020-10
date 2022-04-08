package se.accepted.watcher.ui.main

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import se.accepted.watcher.R

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    fun setLoadingVisibility(visibility: Int){
        loader.visibility = visibility
        val messageVisibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
        message.visibility = messageVisibility
        error.visibility = messageVisibility
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        lifecycleScope.launchWhenResumed {
            viewModel.uiMessages.collect {
                when(it){
                    is ShowLoading -> setLoadingVisibility(View.VISIBLE)
                    is HideLoading -> setLoadingVisibility(View.GONE)
                    is LoginSuccessful ->{
                        message.text = "Is the user logged in: true"
                        error.text = ""
                    }
                    is LoginFailed -> {
                        message.text = "Is the user logged in: false"
                        error.text = "We got error"
                    }
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.checkCurrentUser()
        }

        login_button.setOnClickListener {
            viewModel.viewModelScope.launch {
                viewModel.login("", "")
            }
        }
    }

}