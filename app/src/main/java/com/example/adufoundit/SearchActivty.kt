package com.example.adufoundit

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import android.util.Log

class SearchActivity : AppCompatActivity() {

    private var dateFrom: String? = null
    private var dateTo: String? = null
    private var selectedDate: String? = null
    private var selectedCategory: String? = null
    private val TAG = "SearchActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnApplyFilter = findViewById<Button>(R.id.btnApplyFilter)
        val spinnerSort = findViewById<Spinner>(R.id.spinnerSort)
        val rvPosts = findViewById<RecyclerView>(R.id.rvPosts)
        val etDateFrom = findViewById<EditText>(R.id.etDateFrom)
        val etDateTo = findViewById<EditText>(R.id.etDateTo)

        
        rvPosts.layoutManager = LinearLayoutManager(this)

        
        val sortOptions = resources.getStringArray(R.array.sort_options)
        spinnerSort.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sortOptions
        )

        etDateFrom.setOnClickListener {
            Log.d(TAG, "Date FROM clicked")
            pickDate { date ->
                Log.d(TAG, "Date FROM selected: $date")
                dateFrom = date
                etDateFrom.setText(date)
            }
        }

        etDateTo.setOnClickListener {
            Log.d(TAG, "Date TO clicked")
            pickDate { date ->
                Log.d(TAG, "Date TO selected: $date")
                dateTo = date
                etDateTo.setText(date)
            }
        }

        btnApplyFilter.setOnClickListener {
            val query = etSearch.text.toString()
            val sortBy = spinnerSort.selectedItem.toString()

            Toast.makeText(
                this,
                "Searching from $dateFrom to $dateTo",
                Toast.LENGTH_SHORT
            ).show()

            
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun pickDate(onPicked: (String) -> Unit) {
        val cal = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val formatted =
                    "%04d-%02d-%02d".format(year, month + 1, day)
                onPicked(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
