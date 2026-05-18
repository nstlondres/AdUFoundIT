package com.example.adufoundit.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

data class LoginRequest(
    val Email: String,
    val PasswordHash: String
)

data class User(
    val UserID: String,
    val FullName: String,
    val Email: String
)

data class LoginResponse(
    val ok: Boolean,
    val user: User?,
    val error: String? = null
)

data class RegisterRequest(
    val FullName: String,
    val Email: String,
    val PasswordHash: String
)

data class RegisterResponse(
    val ok: Boolean,
    val error: String?
)

data class Category(
    val CategoryID: String,
    val CategoryName: String
)

data class ItemApiResponse(
    val ItemID: String,
    val UserID: String,
    val UserName: String,
    val ItemName: String,
    val ItemDescription: String?,
    val ItemStatus: String,
    val ItemLocation: String?,
    val CategoryName: String,
    val DateReported: String
)

data class CreateItemRequest(
    val UserID: String,
    val ItemName: String,
    val ItemDescription: String?,
    val CategoryName: String,
    val ItemLocation: String?,
    val ItemStatus: String
)

data class CreateItemResponse(
    val ok: Boolean,
    val ItemID: Int?,
    val error: String?
)

data class SimpleResponse(
    val ok: Boolean,
    val error: String? = null,
    val message: String? = null
)

data class UpdateItemRequest(
    val ItemID: String,
    val ItemName: String,
    val ItemDescription: String,
    val ItemLocation: String,
    val ItemStatus: String
)

data class DeleteItemRequest(
    val ItemID: String
)

data class ItemActionRequest(
    val ItemID: String,
    val ContactNumber: String? = null
)

data class UpdateProfileRequest(
    val UserID: String,
    val FullName: String,
    val Email: String,
    val ContactNumber: String?
)

data class UpdatePasswordRequest(
    val UserID: String,
    val NewPassword: String
)

interface ApiService {

    

    @POST("login.php")
    fun loginUser(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("register.php")
    fun registerUser(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    

    @GET("items.php")
    fun getItems(): Call<List<ItemApiResponse>>

    @GET("categories.php")
    fun getCategories(): Call<List<Category>>

    
    @POST("report_item.php")
    fun createItem(
        @Body request: CreateItemRequest
    ): Call<CreateItemResponse>

    
    @Multipart
    @POST("upload_photo.php")
    fun uploadPhoto(
        @Part("ItemID") itemId: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<SimpleResponse>

    
    @POST("items_delete.php")
    fun deleteItem(
        @Body request: DeleteItemRequest
    ): Call<SimpleResponse>

    
    @POST("items_update.php")
    fun updateItem(
        @Body request: UpdateItemRequest
    ): Call<SimpleResponse>

    
    @POST("items_found.php")
    fun markFound(
        @Body request: ItemActionRequest
    ): Call<SimpleResponse>

    
    @POST("items_claim.php")
    fun claimItem(
        @Body request: ItemActionRequest
    ): Call<SimpleResponse>

    

    @PUT("users_update.php")
    fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Call<SimpleResponse>

    @PUT("users_update_password.php")
    fun updatePassword(
        @Body request: UpdatePasswordRequest
    ): Call<SimpleResponse>
}
