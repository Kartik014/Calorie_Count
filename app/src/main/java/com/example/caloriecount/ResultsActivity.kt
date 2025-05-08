package com.example.caloriecount

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val data = intent.getStringExtra("data")

        val peanutsTextView: TextView = findViewById(R.id.peanutsCalories)
//        val chiaTextView: TextView = findViewById(R.id.chiaCalories)
//        val sunflowerTextView: TextView = findViewById(R.id.sunflowerCalories)
//        val raisinsTextView: TextView = findViewById(R.id.raisinsCalories)
//        val totalCaloriesTextView: TextView = findViewById(R.id.totalCalories)

        // Example values (ideally get these from intent or backend)
        peanutsTextView.text = data ?: "No data available"
//        chiaTextView.text = "Chia Seeds: 137 kcal"
//        sunflowerTextView.text = "Sunflower Seeds: 160 kcal"
//        raisinsTextView.text = "Raisins: 85 kcal"
//        totalCaloriesTextView.text = "Total: 542 kcal"
    }
}
