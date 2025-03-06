package com.example.whatcanimakewiththis

import ImageProcessing
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class GetRecipesActivity : AppCompatActivity() {
    private lateinit var itemListRecyclerView: RecyclerView
    private lateinit var itemListComponent: ItemListComponent
    private lateinit var recipeListRecyclerView: RecyclerView
    private lateinit var recipeListComponent: RecipeListComponent
    private lateinit var addItemBox: EditText
    private val SPOONACULAR_API_KEY = BuildConfig.SPOONACULAR_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_recipes)

        val useCameraButton = findViewById<Button>(R.id.useCamera)
        useCameraButton.setOnClickListener{ useCamera() }

        val addItemButton = findViewById<Button>(R.id.addItem)
        addItemButton.setOnClickListener{ addItem() }

        addItemBox = findViewById(R.id.textInput)

        itemListRecyclerView = findViewById(R.id.itemsList)
        itemListRecyclerView.layoutManager = LinearLayoutManager(this)

        // Update live data items list when deleting
        itemListComponent = ItemListComponent(emptyList()) { position ->
            ImageProcessing.removeItem(position)
            getRecipes()
        }
        itemListRecyclerView.adapter = itemListComponent

        recipeListRecyclerView = findViewById(R.id.recipesList)
        recipeListRecyclerView.layoutManager = LinearLayoutManager(this)

        recipeListComponent = RecipeListComponent(emptyList()) { recipeId, recipeTitle, recipeImageUrl ->
            val intent = Intent(this@GetRecipesActivity, RecipeActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            intent.putExtra("recipeTitle", recipeTitle)
            intent.putExtra("recipeImageUrl", recipeImageUrl)
            startActivity(intent)
        }
        recipeListRecyclerView.adapter = recipeListComponent

        ImageProcessing.isProcessing.observe(this) { isProcessing ->
            //loadingIndicator.visibility = if (isProcessing) View.VISIBLE else View.GONE
        }

        // Add new detections to the list
        ImageProcessing.classificationResult.observe(this) { result ->
            result?.let {
                if (it == "UNKNOWN") {
                    Toast.makeText(this, "No Items Found", Toast.LENGTH_LONG).show()
                } else {
                    ImageProcessing.addItem(it)
                }
            }
        }

        // Observe items list live data and update adapter when it changes
        ImageProcessing.itemsList.observe(this) { items ->
            itemListComponent.updateItems(items)
        }
    }

    private fun useCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun addItem() {
        val newItem = addItemBox.text.toString()
        ImageProcessing.addItem(newItem)
        addItemBox.setText("")
        getRecipes()
    }

    private fun getRecipes() {
        val ingredients = ImageProcessing.itemsList.value?.joinToString(",") ?: ""

        if (ingredients.isEmpty()) {
            return
        }

        val url = "https://api.spoonacular.com/recipes/findByIngredients?ingredients=$ingredients&apiKey=$SPOONACULAR_API_KEY"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@GetRecipesActivity,
                        "Failed to fetch recipes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body().let { responseBody ->
                    val responseString = responseBody.string()
                    val recipes = parseRecipes(responseString)

                    runOnUiThread {
                        recipeListComponent.updateItems(recipes)
                    }
                }
            }
        })
    }

    private fun parseRecipes(responseString: String): List<Recipe> {
        val recipesList = mutableListOf<Recipe>()
        val jsonArray = JSONArray(responseString)

        for (i in 0 until jsonArray.length()) {
            val recipe = jsonArray.getJSONObject(i)
            val id = recipe.getInt("id")
            val title = recipe.getString("title")
            val imageUrl = recipe.getString("image")

            recipesList.add(Recipe(id, title, imageUrl))
        }

        return recipesList
    }
}