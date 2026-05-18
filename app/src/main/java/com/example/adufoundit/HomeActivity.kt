package com.example.adufoundit

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adufoundit.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var postAdapter: PostAdapter
    private val postsViewModel: PostsViewModel by viewModels()

    private var currentUserId: String? = null

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    private val postItemLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show()
                postsViewModel.loadPostsFromApi()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
        val prefs = getSharedPreferences("AUTH", MODE_PRIVATE)
        currentUserId = prefs.getString("USER_ID", null)

        if (currentUserId == null) {
            
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
            return
        }

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        
        requestNotificationPermission()

        setupRecyclerView()
        setupObservers()
        setupUI()

        
        postsViewModel.loadPostsFromApi()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun setupRecyclerView() {
        val userId = currentUserId ?: return

        postAdapter = PostAdapter(
            currentUserId = userId,
            onEdit = { post ->
                val intent = Intent(this, PostItemActivity::class.java)
                intent.putExtra("edit_mode", true)
                intent.putExtra("post_to_edit", post)
                postItemLauncher.launch(intent)
            },
            onDelete = { post ->
                postsViewModel.deletePost(post.postId)
            },
            onRefresh = {
                postsViewModel.loadPostsFromApi()
            }
        )

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = postAdapter
    }

    private fun setupObservers() {
        postsViewModel.posts.observe(this) { posts ->
            postAdapter.submitList(posts)
            binding.tvNoResults.visibility =
                if (posts.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupUI() {
        val categories =
            resources.getStringArray(R.array.category_filters)

        binding.spinnerCategoryFilter.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )

        binding.btnSearch.setOnClickListener {
            performSearch(binding.etSearch.text.toString())
        }

        binding.btnFilter.setOnClickListener {
            binding.filterLayout.visibility =
                if (binding.filterLayout.visibility == View.VISIBLE)
                    View.GONE else View.VISIBLE
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.fabPostItem.setOnClickListener {
            postItemLauncher.launch(Intent(this, PostItemActivity::class.java))
        }
    }

    private fun performSearch(query: String) {
        val posts = postsViewModel.posts.value ?: emptyList()

        val filtered = posts.filter {
            it.itemName.contains(query, true) ||
                    it.category.contains(query, true) ||
                    it.itemDescription.contains(query, true) ||
                    it.location.contains(query, true)
        }

        postAdapter.submitList(filtered)
        binding.tvNoResults.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
