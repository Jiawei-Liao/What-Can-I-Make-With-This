package com.example.whatcanimakewiththis

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ReviewImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_image)

        val byteArray = intent.getByteArrayExtra("capturedImage")

        val imageView = findViewById<ImageView>(R.id.imageView)
        val acceptButton = findViewById<Button>(R.id.acceptButton)
        val retakeButton = findViewById<Button>(R.id.retakeButton)

        // decode byte array image to bitmap and classify it with imageProcessor
        if (byteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            imageView.setImageBitmap(bitmap)
            ImageProcessing.processImage(byteArray)
        }

        acceptButton.setOnClickListener{ acceptImage() }
        retakeButton.setOnClickListener{ retakeImage() }
    }

    private fun acceptImage() {
        val intent = Intent(this, GetRecipesActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun retakeImage() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
        finish()
    }
}