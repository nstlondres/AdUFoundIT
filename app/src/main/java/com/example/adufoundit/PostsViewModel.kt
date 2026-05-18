package com.example.adufoundit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.adufoundit.api.ApiClient
import com.example.adufoundit.api.ApiService
import com.example.adufoundit.api.ItemApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.adufoundit.api.SimpleResponse
import com.example.adufoundit.api.DeleteItemRequest

class PostsViewModel : ViewModel() {

    private val api: ApiService =
        ApiClient.retrofit.create(ApiService::class.java)

    private val _posts = MutableLiveData<List<Post>>(emptyList())
    val posts: LiveData<List<Post>> = _posts

    fun loadPostsFromApi() {
        api.getItems().enqueue(object : Callback<List<ItemApiResponse>> {

            override fun onResponse(
                call: Call<List<ItemApiResponse>>,
                response: Response<List<ItemApiResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {

                    val mapped = response.body()!!.map { item ->
                        Post(
                            postId = item.ItemID.toString(),
                            itemName = item.ItemName,
                            userName = item.UserName,
                            userId=item.UserID,
                            itemDescription = item.ItemDescription ?: "",
                            category = item.CategoryName,
                            location = item.ItemLocation ?: "",
                            date = item.DateReported,
                            status = item.ItemStatus,
                            contactNumber = null,
                            dropOffPoint = null,

                            foundByName = item.UserName,
                            foundByContact = null
                        )
                    }

                    _posts.postValue(mapped)

                } else {
                    _posts.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<List<ItemApiResponse>>, t: Throwable) {
                t.printStackTrace()
                _posts.postValue(emptyList())
            }
        })
    }

    fun deletePost(postId: String) {
        api.deleteItem(DeleteItemRequest(postId))
            .enqueue(object : Callback<SimpleResponse> {

                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    if (response.isSuccessful && response.body()?.ok == true) {
                        loadPostsFromApi()
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }
}

