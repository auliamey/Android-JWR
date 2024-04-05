package com.example.pbd_jwr.ui.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pbd_jwr.R
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.data.model.Category
import com.example.pbd_jwr.databinding.FragmentScanBinding
import com.example.pbd_jwr.encryptedSharedPref.EncryptedSharedPref
import com.example.pbd_jwr.ui.transaction.TransactionDummyAdapter
import com.example.pbd_jwr.ui.transaction.TransactionViewModel
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ScanFragment : Fragment() {

    private var imageUri: Uri? = null
    private var currentPhotoPath: String = ""
    private lateinit var sharedPreferences: SharedPreferences
    var transactionDummyData: String = ""
    private var _binding: FragmentScanBinding? = null
    private lateinit var mTransactionViewModel: TransactionViewModel
    private lateinit var connectivityManager: ConnectivityManager

    private val binding get() = _binding!!

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 101
    }

    data class TransactionDummy(
        val name: String,
        val qty: Int,
        val price: Double
    )

    private val startCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = Uri.parse(currentPhotoPath)
            binding.imageView.setImageURI(imageUri)
            binding.uploadBtn.isEnabled = true
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.imageView.setImageURI(it)
            imageUri = it
            binding.uploadBtn.isEnabled = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        connectivityManager = activity?.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uploadBtn.isEnabled = false
        binding.saveTransactionsBtn.isEnabled = false

        mTransactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        sharedPreferences = EncryptedSharedPref.create(requireContext(), "login")
        resetTransaction()

        binding.scanBtn.setOnClickListener {
            checkAndRequestPermissions(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION) {
                openCamera()
            }
        }

        binding.galleryBtn.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.uploadBtn.setOnClickListener {
            if(isNetworkAvailable()){
                imageUri?.let { uri ->
                    uploadImage(uri)
                } ?: run {
                    Toast.makeText(context, "No image selected or taken", Toast.LENGTH_SHORT).show()
                }
            }else{
                findNavController().navigate(R.id.navigation_nonetwork)
            }
        }

        binding.saveTransactionsBtn.setOnClickListener {
            handleSave()
        }
    }

    private fun checkAndRequestPermissions(permission: String, requestCode: Int, grantedAction: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED -> {
                requestPermissions(arrayOf(permission), requestCode)
            }
            else -> grantedAction()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_CAMERA_PERMISSION -> openCamera()
                // Handle other permissions if necessary
            }
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity?.packageManager?.let { packageManager ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(requireContext(), "JWR.provider", it)
                    startCamera.launch(Intent(takePictureIntent).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    })
                    imageUri = photoURI
                }
            }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        val token = sharedPreferences.getString("token", null)
        if (token.isNullOrEmpty()) {
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Token not found.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val bitmap = getBitmapFromUri(imageUri, requireContext())
        if (bitmap != null) {
            val compressedFile = compressAndSaveBitmap(bitmap, requireContext())

            // Setup request with OkHttpClient as before
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
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    activity?.runOnUiThread {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
//
                            Log.d("Response", "response body: $responseBody")

                            if (responseBody != null) {
                                transactionDummyData = responseBody
                                addDummyTransactions(transactionDummyData)
                                Log.d("Response", "transactionDummyData: $transactionDummyData")
                            }

                            binding.saveTransactionsBtn.isEnabled = true
                            binding.uploadBtn.isEnabled = false

                            Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } else {
            Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("InflateParams")
    private fun handleSave() {
        if (transactionDummyData.isEmpty()) {
            Toast.makeText(context, "No transactions read", Toast.LENGTH_SHORT).show()
        } else {
            // Inflate the dialog layout
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_transaction, null)
            val dialogBuilder = AlertDialog.Builder(requireContext()).apply {
                setView(dialogView)
                setCancelable(true) // Allows dialog to be canceled with back button or by tapping outside
            }

            val dialog = dialogBuilder.create()

            dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btnOkay).setOnClickListener {

                val jsonObject = JSONObject(transactionDummyData)
                val itemsArray = jsonObject.getJSONObject("items").getJSONArray("items")

                for (i in 0 until itemsArray.length()) {
                    val itemObject = itemsArray.getJSONObject(i)
                    val name = itemObject.getString("name")
                    val category = Category.EXPENSE
                    val price = itemObject.getDouble("price")
                    val qty = itemObject.getInt("qty")
                    val amount = (qty * price * 1000).roundToInt() / 1000.0
                    val latitude = 6.8915
                    val longitude = 107.6107
                    val currentUserEmail = sharedPreferences.getString("email", "") ?: ""
                    val date = Date().time

                    mTransactionViewModel.addTransaction(Transaction(email = currentUserEmail, title = name, category = category, amount = amount, latitude = latitude, longitude = longitude, date = date))
                }
                dialog.dismiss()
                resetTransaction()
                Toast.makeText(context, "Transactions saved", Toast.LENGTH_SHORT).show()
            }

            dialog.show()
        }
    }

    private fun resetTransaction() {
        binding.uploadBtn.isEnabled = false
        binding.saveTransactionsBtn.isEnabled = false

        binding.imageView.setImageResource(R.drawable.baseline_insert_photo_24)
        addDummyTransactions("{\"items\":{\"items\":[{\"name\":\"none\",\"qty\":0,\"price\":0}]}}")
    }


    private fun getBitmapFromUri(uri: Uri, context: Context): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }
    private fun compressAndSaveBitmap(bitmap: Bitmap, context: Context): File {
        val compressedFile = File(context.externalCacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(compressedFile).use { outStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream)
            outStream.flush()
        }
        return compressedFile
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).also {
            currentPhotoPath = it.absolutePath
        }
    }


    private fun addDummyTransactions(response: String) {
        val transactions = parseTransactions(response)

        // Pastikan view sudah dibuat dan context tersedia sebelum mengakses binding atau requireContext()
        val adapter = TransactionDummyAdapter(requireContext(), transactions)

        // Gunakan binding untuk mengakses ListView dan menetapkan adapter
        binding.listDummyTransaction.adapter = adapter
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

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                )
    }

}
