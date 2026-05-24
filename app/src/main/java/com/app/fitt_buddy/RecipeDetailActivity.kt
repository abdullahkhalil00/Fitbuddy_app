package com.app.fitt_buddy

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class RecipeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val meal = intent.getSerializableExtra("MEAL_DATA") as? Meal

        val ivImage = findViewById<ImageView>(R.id.iv_detail_image)
        val tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        val tvCategory = findViewById<TextView>(R.id.tv_detail_category)
        val tvInstructions = findViewById<TextView>(R.id.tv_detail_instructions)
        val btnBack = findViewById<ImageButton>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        meal?.let {
            tvTitle.text = it.label
            tvCategory.text = it.source
            tvInstructions.text = it.instructions ?: "No instructions available."

            Glide.with(this)
                .load(it.image)
                .centerCrop()
                .into(ivImage)
        }
    }
}
