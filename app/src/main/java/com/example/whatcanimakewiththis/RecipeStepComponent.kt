package com.example.whatcanimakewiththis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipeStepComponent(private var steps: List<Step>) : RecyclerView.Adapter<RecipeStepComponent.RecipeStepViewHolder>() {
    class RecipeStepViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stepNumber = view.findViewById<TextView>(R.id.stepNumber)
        val stepDescription = view.findViewById<TextView>(R.id.stepDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeStepViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recipe_step_component, parent, false)
        return RecipeStepViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeStepViewHolder, position: Int) {
        val step = steps[position]
        holder.stepNumber.text = "Step ${step.number}"
        holder.stepDescription.text = step.description
    }

    override fun getItemCount(): Int = steps.size

    fun updateItems(newItems: List<Step>) {
        this.steps = newItems
        notifyDataSetChanged()
    }
}