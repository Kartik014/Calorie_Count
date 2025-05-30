package com.example.caloriecount

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var photoFile: File
    private val CAMERA_REQUEST_CODE = 1
    private val REQUEST_CAMERA_PERMISSION = 100
    private lateinit var buttonResults: Button
    private lateinit var data: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        val buttonCapture: Button = findViewById(R.id.buttonCapture)
        buttonResults = findViewById(R.id.buttonResults)

        buttonResults.isEnabled = false // Disable until upload completes

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }

        buttonCapture.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }

        buttonResults.setOnClickListener {
            val intent = Intent(this, ResultsActivity::class.java)
            intent.putExtra("calories", "542")
            intent.putExtra("proteins", "18g")
            intent.putExtra("vitamins", "A, C, D")
            intent.putExtra("data", data.toString())
            startActivity(intent)
        }
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = createImageFile()

        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(null)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageView.setImageBitmap(bitmap)
            uploadImage(photoFile)
        }
    }

    private fun uploadImage(file: File) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // connection timeout
            .readTimeout(60, TimeUnit.SECONDS)    // socket read timeout
            .writeTimeout(60, TimeUnit.SECONDS)   // socket write timeout
            .build()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            .build()

        val request = Request.Builder()
            .url("https://8c18-59-89-50-211.ngrok-free.app/upload") // Update to your IP
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("UploadError", "Image upload failed: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JSONObject(responseBody)
                    val forwardedResponse = json.optJSONObject("forwardedResponse")
                    data = forwardedResponse?.optString("result") ?: "No data in forwarded response"
                }
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Upload successful", Toast.LENGTH_SHORT).show()
                    Log.d("data", data)
                    buttonResults.isEnabled = true
                }
            }
        })
    }
}
