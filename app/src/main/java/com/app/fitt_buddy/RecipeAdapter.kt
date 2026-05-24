package com.app.fitt_buddy

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecipeAdapter(private val meals: List<Meal>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivRecipeImage: ImageView = view.findViewById(R.id.iv_recipe_image)
        val tvRecipeLabel: TextView = view.findViewById(R.id.tv_recipe_label)
        val tvRecipeSource: TextView = view.findViewById(R.id.tv_recipe_source)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val meal = meals[position]
        holder.tvRecipeLabel.text = meal.label
        holder.tvRecipeSource.text = meal.source
        
        Glide.with(holder.itemView.context)
            .load(meal.image)
            .centerCrop()
            .into(holder.ivRecipeImage)
            
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RecipeDetailActivity::class.java)
            intent.putExtra("MEAL_DATA", meal)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = meals.size
}