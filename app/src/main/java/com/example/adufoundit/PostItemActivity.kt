package com.example.adufoundit

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.adufoundit.api.*
import com.example.adufoundit.databinding.ActivityPostItemBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Calendar
import android.util.Log

class PostItemActivity : AppCompatActivity() {

        private lateinit var binding: ActivityPostItemBinding
        private val api = ApiClient.retrofit.create(ApiService::class.java)

        private var selectedImageFile: File? = null
        private var isEditMode = false
        private var postToEdit: Post? = null

        

        private val imagePicker =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri ?: return@registerForActivityResult

                val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                } ?: "upload.jpg"

                val file = File(cacheDir, fileName)
                contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                selectedImageFile = file
                binding.tvAttachmentName.text = fileName
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            }

        

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityPostItemBinding.inflate(layoutInflater)
            setContentView(binding.root)

            loadCategories()
            setupDatePicker()
            setupStatusRadioGroup()

            
            isEditMode = intent.getBooleanExtra("edit_mode", false)

            if (isEditMode) {
                setupEditMode()
            }

            binding.btnUpload.setOnClickListener {
                imagePicker.launch("image/*")
            }

            binding.btnSubmitPost.setOnClickListener {
                if (isEditMode) updatePost() else submitPost()
            }

            binding.btnBack.setOnClickListener { finish() }
        }

        

        private fun setupEditMode() {
            postToEdit = intent.getParcelableExtra("post_to_edit")

            postToEdit?.let { post ->
                binding.etItemName.setText(post.itemName)
                binding.etItemDescription.setText(post.itemDescription)
                binding.etItemLocation.setText(post.location)
                binding.etDateFound.setText(post.date)

                if (post.status.equals("Found", true)) {
                    binding.rbFound.isChecked = true
                } else {
                    binding.rbLost.isChecked = true
                }

                binding.btnSubmitPost.text = "UPDATE ITEM"
            }
        }

        private fun updatePost() {
            val post = postToEdit ?: return

            val request = UpdateItemRequest(
                ItemID = post.postId,
                ItemName = binding.etItemName.text.toString(),
                ItemDescription = binding.etItemDescription.text.toString(),
                ItemLocation = binding.etItemLocation.text.toString(),
                ItemStatus = if (binding.rbFound.isChecked) "Found" else "Lost"
            )

            api.updateItem(request).enqueue(object : Callback<SimpleResponse> {
                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    if (response.isSuccessful && response.body()?.ok == true) {

                        if (selectedImageFile != null) {
                            uploadPhoto(post.postId.toInt())
                        } else {
                            Toast.makeText(
                                this@PostItemActivity,
                                "Item updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                    } else {
                        Toast.makeText(
                            this@PostItemActivity,
                            "Update failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                    override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    Toast.makeText(
                        this@PostItemActivity,
                        "Update failed: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        

        private fun submitPost() {

            val itemName = binding.etItemName.text.toString().trim()
            val itemDesc = binding.etItemDescription.text.toString().trim()
            val itemLoc = binding.etItemLocation.text.toString().trim()
            val status = if (binding.rbFound.isChecked) "Found" else "Lost"
            val categoryName = binding.spinnerCategory.selectedItem.toString()

            if (itemName.isEmpty()) {
                Toast.makeText(this, "Item name required", Toast.LENGTH_SHORT).show()
                return
            }

            val userId = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("USER_ID", null)
                ?: run {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                    return
                }

            val request = CreateItemRequest(
                UserID = userId,
                ItemName = itemName,
                ItemDescription = itemDesc,
                CategoryName = categoryName,
                ItemLocation = itemLoc,
                ItemStatus = status
            )

            api.createItem(request).enqueue(object : Callback<CreateItemResponse> {

                override fun onResponse(
                    call: Call<CreateItemResponse>,
                    response: Response<CreateItemResponse>
                ) {
                    Toast.makeText(
                        this@PostItemActivity,
                        "HTTP ${response.code()} body=${response.body()}",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.d("API_CREATE", "code=${response.code()} body=${response.body()}")

                    if (!response.isSuccessful || response.body()?.ok != true) {
                        Toast.makeText(
                            this@PostItemActivity,
                            "Create failed: ${response.body()?.error}",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val itemId = response.body()!!.ItemID!!
                    if (selectedImageFile != null) uploadPhoto(itemId) else finishSuccess()
                }

                override fun onFailure(call: Call<CreateItemResponse>, t: Throwable) {
                    Toast.makeText(
                        this@PostItemActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        private fun loadCategories() {
            api.getCategories().enqueue(object : Callback<List<Category>> {

                override fun onResponse(
                    call: Call<List<Category>>,
                    response: Response<List<Category>>
                ) {
                    if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                        Toast.makeText(
                            this@PostItemActivity,
                            "Failed to load categories",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val categoryNames = response.body()!!.map { it.CategoryName }

                    binding.spinnerCategory.adapter =
                        ArrayAdapter(
                            this@PostItemActivity,
                            android.R.layout.simple_spinner_item,
                            categoryNames
                        ).also {
                            it.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                            )
                        }
                }

                override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                    Toast.makeText(
                        this@PostItemActivity,
                        "Category error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        private fun uploadPhoto(itemId: Int) {

            val file = selectedImageFile ?: return

            val reqFile =
                file.asRequestBody("image/*".toMediaType())

            val photoPart =
                MultipartBody.Part.createFormData("file", file.name, reqFile)

            val itemIdBody =
                itemId.toString().toRequestBody("text/plain".toMediaType())

            api.uploadPhoto(itemIdBody, photoPart)
                .enqueue(object : Callback<SimpleResponse> {

                    override fun onResponse(
                        call: Call<SimpleResponse>,
                        response: Response<SimpleResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.ok == true) {
                            finishSuccess()
                        } else {
                            Toast.makeText(
                                this@PostItemActivity,
                                "Photo upload failed on server",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                        Toast.makeText(
                            this@PostItemActivity,
                            "Photo upload failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }

        private fun finishSuccess() {
            Toast.makeText(this, "Item posted successfully", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }

        

        private fun setupDatePicker() {
            binding.etDateFound.setOnClickListener {
                val cal = Calendar.getInstance()
                DatePickerDialog(
                    this,
                    { _, y, m, d -> binding.etDateFound.setText("$d/${m + 1}/$y") },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        private fun setupStatusRadioGroup() {
            binding.rgItemStatus.setOnCheckedChangeListener { _, id ->
                binding.layoutDropOff.visibility =
                    if (id == R.id.rbFound) View.VISIBLE else View.GONE
            }
        }
    }