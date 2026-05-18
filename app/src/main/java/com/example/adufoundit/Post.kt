package com.example.adufoundit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Post(
    val postId: String = UUID.randomUUID().toString(), 
    val userId: String, 
    val userName: String,
    val itemName: String,
    val category: String,
    val itemDescription: String,
    val location: String,
    val date: String,
    val status: String,
    val contactNumber: String?,
    val dropOffPoint: String?,

    val foundByName: String?,
    val foundByContact: String?

) : Parcelable
