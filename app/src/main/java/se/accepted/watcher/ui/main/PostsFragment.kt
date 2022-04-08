package se.accepted.watcher.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_posts.*
import kotlinx.android.synthetic.main.fragment_posts.view.*
import se.accepted.watcher.R
import java.util.*
import kotlin.text.Typography.nbsp


class PostsFragment : Fragment() {
    private val viewModel: PostsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.fetchPosts()
        }
        lifecycleScope.launchWhenResumed {
            viewModel.sharedPostUiFlow.collect {
                when (it) {
                    PostsFetchFailed -> {
                        tv_posts.text = "Posts Fetch Failed"
                    }
                    is PostsFetched -> {
                        val bodyList = it.posts.mapIndexed { index, item ->
                            "${index + 1}: ${item.body}\n\n"
                        }
                        tv_posts.text =
                            bodyList.joinToString().replace("[", "").replace("]", "").replace(",", "")
                    }

                }
            }
        }
    }
}