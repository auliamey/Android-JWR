package com.example.pbd_jwr

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.ListView
import androidx.core.content.FileProvider
import com.example.pbd_jwr.encryptedSharedPref.EncryptedSharedPref
import com.example.pbd_jwr.ui.transaction.TransactionDummyAdapter
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ScanActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private var currentPhotoPath: String = ""
    private lateinit var sharedPreferences: SharedPreferences
    var transactionDummyData: String = ""

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_CAMERA_PERMISSION = 2
        const val PICK_IMAGE = 3
        const val REQUEST_STORAGE_PERMISSION = 4
    }

    data class TransactionDummy(
        val name: String,
        val qty: Int,
        val price: Double
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        sharedPreferences = EncryptedSharedPref.create(applicationContext,"login")

        val scanButton: Button = findViewById(R.id.scanBtn)
        val galleryBtn: Button = findViewById(R.id.galleryBtn)
        val uploadButton: Button = findViewById(R.id.uploadBtn)
        val saveBtn: Button = findViewById(R.id.saveTransactionsBtn)
        val backBtn: Button = findViewById(R.id.backBtn)

        val placeholderImage: ImageView = findViewById(R.id.imageView)
        placeholderImage.setImageResource(R.drawable.baseline_insert_photo_24)
        addDummyTransactions("{\"items\":{\"items\":[{\"name\":\"none\",\"qty\":0,\"price\":0}]}}")

        backBtn.setOnClickListener {
            finish()
        }

        scanButton.setOnClickListener {
            checkAndRequestPermissions()
        }

        galleryBtn.setOnClickListener {
            openGallery()
        }

        uploadButton.setOnClickListener {
            if (imageUri != null) {
                uploadImage(imageUri!!)
            } else {
                Toast.makeText(this, "No image selected or taken", Toast.LENGTH_SHORT).show()
            }
        }


        saveBtn.setOnClickListener {
            handleSave()
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

    @SuppressLint("InflateParams")
    private fun handleSave() {
        if (transactionDummyData == "") {
            Toast.makeText(this, "No transactions read", Toast.LENGTH_SHORT).show()
        } else {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_transaction, null)
            val dialogBuilder = AlertDialog.Builder(this).apply {
                setView(dialogView)
                setCancelable(true) // Memungkinkan dialog untuk dibatalkan dengan tombol back atau mengetuk di luar
            }
            val dialog = dialogBuilder.create()

            dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btnOkay).setOnClickListener {
                val resultIntent = Intent()
                resultIntent.putExtra("transactionDummyData", transactionDummyData)
                setResult(Activity.RESULT_OK, resultIntent)
                dialog.dismiss()
                finish()
            }

            dialog.show()
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
                    Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraIntent()
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Token not found.", Toast.LENGTH_SHORT).show()
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
                    runOnUiThread { Toast.makeText(this@ScanActivity, "Failed to upload image", Toast.LENGTH_SHORT).show() }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()

                            Log.d("Response", "response body: $responseBody")

                            if (responseBody != null) {
                                transactionDummyData = responseBody
                                addDummyTransactions(responseBody)
                                Log.d("Response", "transactionDummyData: $transactionDummyData")
                            }

                            Toast.makeText(this@ScanActivity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ScanActivity, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })

        } else {
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun addDummyTransactions(response: String) {
        val transactions = parseTransactions(response)

        // Tampilkan data transaksi pada ListView
        val listView: ListView = findViewById(R.id.listDummyTransaction)
        val adapter = TransactionDummyAdapter(this, transactions)
        listView.adapter = adapter
    }

    private fun parseTransactions(responseBody: String): MutableList<TransactionDummy> {
        val transactions = mutableListOf<TransactionDummy>()
        responseBody.let {
            val jsonObject = JSONObject(it)
            val itemsObject = jsonObject.getJSONObject("items")
            val itemsArray = itemsObject.getJSONArray("items")

            for (i in 0 until itemsArray.length()) {
                val itemObject = itemsArray.getJSONObject(i)
                val name = itemObject.getString("name")
                val qty = itemObject.getInt("qty")
                val price = itemObject.getDouble("price")
                val transaction = TransactionDummy(name, qty, price)
                transactions.add(transaction)
            }
        }
        return transactions
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

