package com.example.whatcanimakewiththis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class RecipeListComponent(
    private var items: List<Recipe>,
    private val onClick: (id: Int, title: String, imageUrl: String) -> Unit
) : RecyclerView.Adapter<RecipeListComponent.RecipeViewHolder>() {
    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button = view.findViewById<Button>(R.id.recipeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_list_component, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = items[position]
        holder.button.text = recipe.title
        holder.button.setOnClickListener { onClick(recipe.id, recipe.title, recipe.imageUrl) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(newItems: List<Recipe>) {
        items = newItems
        notifyDataSetChanged()
    }
}