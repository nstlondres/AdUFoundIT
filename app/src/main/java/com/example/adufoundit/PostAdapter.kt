package com.example.adufoundit

import android.app.AlertDialog
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.adufoundit.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostAdapter(
    private val currentUserId: String,
    private val onEdit: (Post) -> Unit,
    private val onDelete: (Post) -> Unit,
    private val onRefresh: () -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvUser = itemView.findViewById<TextView>(R.id.tvUserName)
        private val tvItem = itemView.findViewById<TextView>(R.id.tvItemName)
        private val tvCategory = itemView.findViewById<TextView>(R.id.tvCategory)
        private val tvLocation = itemView.findViewById<TextView>(R.id.tvLocation)
        private val tvDesc = itemView.findViewById<TextView>(R.id.tvPostContent)
        private val btnAction = itemView.findViewById<Button>(R.id.btnAction)
        private val editLayout = itemView.findViewById<LinearLayout>(R.id.editDeleteLayout)
        private val btnEdit = itemView.findViewById<Button>(R.id.btnEdit)
        private val btnDelete = itemView.findViewById<Button>(R.id.btnDelete)

        private val api = ApiClient.retrofit.create(ApiService::class.java)

        fun bind(post: Post) {

            tvUser.text = post.userName
            tvItem.text = post.itemName
            tvCategory.text = post.category
            tvLocation.text = post.location
            tvDesc.text = post.itemDescription

            
            
            
            when (post.status) {

                
                "Lost" -> {
                    btnAction.text = "FOUND IT"
                    btnAction.isEnabled = true
                    btnAction.setOnClickListener {
                        showActionDialog(
                            title = "Found Item",
                            buttonText = "CONFIRM FOUND"
                        ) { mobile ->
                            api.markFound(
                                ItemActionRequest(
                                    ItemID = post.postId,
                                    ContactNumber = mobile
                                )
                            ).enqueue(actionCallback("Item marked as FOUND"))
                        }
                    }
                }

                
                "Found" -> {
                    btnAction.text = "CLAIM IT"
                    btnAction.isEnabled = true
                    btnAction.setOnClickListener {
                        showClaimInfoDialog(post)
                    }
                }

                
                "Claimed" -> {
                    btnAction.text = "CLAIMED"
                    btnAction.isEnabled = false
                    btnAction.setOnClickListener(null)
                }
            }

            
            
            
            if (post.userId == currentUserId) {
                editLayout.visibility = View.VISIBLE

                btnEdit.setOnClickListener { onEdit(post) }

                btnDelete.setOnClickListener {
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Delete Post")
                        .setMessage("Delete this post?")
                        .setPositiveButton("Delete") { _, _ ->
                            onDelete(post)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            } else {
                editLayout.visibility = View.GONE
            }
        }

        
        
        
        private fun showActionDialog(
            title: String,
            buttonText: String,
            onConfirm: (String) -> Unit
        ) {
            val input = EditText(itemView.context).apply {
                hint = "Enter mobile number"
                inputType = InputType.TYPE_CLASS_PHONE
            }

            AlertDialog.Builder(itemView.context)
                .setTitle(title)
                .setView(input)
                .setPositiveButton(buttonText) { _, _ ->
                    val number = input.text.toString().trim()
                    if (number.isNotEmpty()) {
                        onConfirm(number)
                    } else {
                        Toast.makeText(
                            itemView.context,
                            "Mobile number required",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        
        
        
        private fun showClaimInfoDialog(post: Post) {
            val message = """
                Found by: ${post.foundByName ?: "Anonymous"}
                Contact: ${post.foundByContact ?: "Not provided"}
            """.trimIndent()

            AlertDialog.Builder(itemView.context)
                .setTitle("Item Found Details")
                .setMessage(message)
                .setPositiveButton("CLAIM ITEM") { _, _ ->
                    api.claimItem(
                        ItemActionRequest(ItemID = post.postId)
                    ).enqueue(actionCallback("Item CLAIMED"))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        
        
        
        private fun actionCallback(msg: String) =
            object : Callback<SimpleResponse> {
                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    if (response.isSuccessful && response.body()?.ok == true) {
                        Toast.makeText(itemView.context, msg, Toast.LENGTH_SHORT).show()
                        onRefresh()
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    Toast.makeText(itemView.context, "Action failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(old: Post, new: Post) = old.postId == new.postId
    override fun areContentsTheSame(old: Post, new: Post) = old == new
}
