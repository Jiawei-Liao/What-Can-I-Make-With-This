package com.example.whatcanimakewiththis

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RecipeActivity : AppCompatActivity() {
    private val SPOONACULAR_API_KEY = BuildConfig.SPOONACULAR_API_KEY
    private lateinit var recipeId: String
    private lateinit var stepsRecyclerView: RecyclerView
    private lateinit var stepsAdapter: RecipeStepComponent
    private lateinit var ingredientsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        recipeId = intent.getIntExtra("recipeId", 0).toString()
        val recipeTitle = intent.getStringExtra("recipeTitle")
        val recipeImageUrl = intent.getStringExtra("recipeImageUrl")

        val title = findViewById<TextView>(R.id.title)
        val recipeImage = findViewById<ImageView>(R.id.recipeImage)
        stepsRecyclerView = findViewById(R.id.steps)
        ingredientsTextView = findViewById(R.id.ingredientsList)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener{ finish() }

        title.text = recipeTitle
        Glide.with(this)
            .load(recipeImageUrl)
            .into(recipeImage)

        stepsRecyclerView.layoutManager = LinearLayoutManager(this)
        stepsAdapter = RecipeStepComponent(emptyList())
        stepsRecyclerView.adapter = stepsAdapter

        getIngredients()
        getSteps()
    }

    private fun getIngredients() {
        if (recipeId.isEmpty()) {
            return
        }
        val url =
            "https://api.spoonacular.com/recipes/$recipeId/information?apiKey=$SPOONACULAR_API_KEY"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@RecipeActivity,
                        "Failed to fetch ingredients",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.let { responseBody ->
                    val responseString = responseBody.string()
                    val ingredients = parseIngredients(responseString)

                    runOnUiThread {
                        ingredientsTextView.text = ingredients
                    }
                } ?: run {
                    runOnUiThread {
                        Toast.makeText(
                            this@RecipeActivity,
                            "No response received",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun parseIngredients(responseString: String): String {
        val ingredients = StringBuilder()

        try {
            val jsonObject = JSONObject(responseString)
            val ingredientsArray = jsonObject.getJSONArray("extendedIngredients")

            for (i in 0 until ingredientsArray.length()) {
                val ingredient = ingredientsArray.getJSONObject(i)

                var amount = ingredient.getDouble("amount")
                val formattedAmount = if (amount % 1 == 0.0) amount.toInt() else amount
                val unit = ingredient.optString("unit", "").takeIf { it.isNotBlank() } ?: ""
                val name = ingredient.optString("nameClean", ingredient.getString("name"))

                val ingredientString = if (unit.isNotEmpty()) {
                    "$formattedAmount $unit $name"
                } else {
                    "$formattedAmount $name"
                }
                ingredients.append(ingredientString).append("\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ingredients.toString().trim()
    }

    private fun getSteps() {
        if (recipeId.isEmpty()) {
            return
        }

        val url = "https://api.spoonacular.com/recipes/$recipeId/analyzedInstructions?apiKey=$SPOONACULAR_API_KEY"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@RecipeActivity,
                        "Failed to fetch instructions",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.let { responseBody ->
                    val responseString = responseBody.string()
                    val recipes = parseRecipes(responseString)

                    runOnUiThread {
                        stepsAdapter.updateItems(recipes)
                    }
                } ?: run {
                    runOnUiThread {
                        Toast.makeText(
                            this@RecipeActivity,
                            "No response received",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun parseRecipes(responseString: String): List<Step> {
        val steps = mutableListOf<Step>()

        try {
            val jsonArray = JSONArray(responseString)

            if (jsonArray.length() == 0) {
                return steps
            }

            val instructionObject = jsonArray.getJSONObject(0)
            val stepsArray = instructionObject.getJSONArray("steps")

            for (i in 0 until stepsArray.length()) {
                val stepObject = stepsArray.getJSONObject(i)

                val number = stepObject.getInt("number")
                val description = stepObject.getString("step")

                steps.add(Step(number, description))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return steps
    }
}