package com.example.pbd_jwr

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd_jwr.R
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.pbd_jwr.encryptedSharedPref.EncryptedSharedPref
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ScanActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private var currentPhotoPath: String = ""
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_CAMERA_PERMISSION = 2
        const val PICK_IMAGE = 3
        const val REQUEST_STORAGE_PERMISSION = 4
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        sharedPreferences = EncryptedSharedPref.create(applicationContext,"login")

        val scanButton: Button = findViewById(R.id.scanBtn)
        val uploadButton: Button = findViewById(R.id.uploadBtn)
        val sendButton: Button = findViewById(R.id.sendBtn)
        val backBtn: Button = findViewById(R.id.backBtn)

        backBtn.setOnClickListener {
            finish()
        }

        scanButton.setOnClickListener {
            checkAndRequestPermissions()
        }

        uploadButton.setOnClickListener {
            openGallery()
        }

        sendButton.setOnClickListener {
            if (imageUri != null) {
                uploadImage(imageUri!!)
            } else {
                Toast.makeText(this, "Tidak ada gambar yang dipilih atau diambil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_STORAGE_PERMISSION)
        } else {
            // Semua izin sudah diberikan
            openCamera()
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else  {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {

                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        null
                    }

                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "JWR.provider",
                            it
                        )
                        Log.d("ScanActivity", "Uri: $photoURI")
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        imageUri = photoURI // Simpan Uri untuk digunakan nanti
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }


    private fun startCameraIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Izin penyimpanan diperlukan", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraIntent()
                } else {
                    Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }


    private fun getBitmapFromUri(uri: Uri, context: Context): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }


    private fun compressAndSaveBitmap(bitmap: Bitmap, context: Context): File {
        // Membuat file output di direktori cache eksternal
        val compressedFile = File(context.externalCacheDir, "compressed_${System.currentTimeMillis()}.jpg")

        FileOutputStream(compressedFile).use { outStream ->
            // Kompres bitmap ke format JPEG dengan kualitas 75% (sesuaikan sesuai kebutuhan)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream)
            outStream.flush()
        }

        return compressedFile
    }


    @SuppressLint("Recycle")
    private fun uploadImage(imageUri: Uri) {
        val token = sharedPreferences.getString("token", null)
        if (token.isNullOrEmpty()) {
            runOnUiThread {
                Toast.makeText(this, "Token tidak ditemukan. $token.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val bitmap = getBitmapFromUri(imageUri, this)
        if (bitmap != null) {
            // Kompres dan simpan bitmap ke file baru
            val compressedFile = compressAndSaveBitmap(bitmap, this)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", compressedFile.name, compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()

            val request = Request.Builder()
                .url("https://pbd-backend-2024.vercel.app/api/bill/upload")
                .addHeader("Authorization", "Bearer $token")
                .post(requestBody)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread { Toast.makeText(this@ScanActivity, "Gagal mengirim gambar", Toast.LENGTH_SHORT).show() }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ScanActivity, "Gambar berhasil diupload", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ScanActivity, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })

        } else {
            Toast.makeText(this, "Gagal membaca gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        // Ini harus berhasil untuk Uri yang diberikan oleh FileProvider
        Log.d("ScanActivity", "Uri: $uri")

        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        tempFile.deleteOnExit()

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open URI");

        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageUri = Uri.parse(currentPhotoPath)
            imageUri?.let {
                findViewById<ImageView>(R.id.imageView).setImageURI(it)
            }
        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                findViewById<ImageView>(R.id.imageView).setImageURI(uri)
                this.imageUri = uri // Simpan Uri dari gambar yang dipilih
            }
        }
    }
}

