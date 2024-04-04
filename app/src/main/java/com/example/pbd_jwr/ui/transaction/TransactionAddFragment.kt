package com.example.pbd_jwr.ui.transaction

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.core.content.ContextCompat
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.data.model.Category
import com.example.pbd_jwr.databinding.FragmentTransactionAddBinding
import com.example.pbd_jwr.encryptedSharedPref.EncryptedSharedPref
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import java.util.Date

class TransactionAddFragment : Fragment() {

    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var mTransactionViewModel: TransactionViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var encryptedSharedPref: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mTransactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        _binding = FragmentTransactionAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mTransactionViewModel.transactionSubmitted.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Transaction submitted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Transaction submission failed", Toast.LENGTH_SHORT).show()
            }
        }

        val filter = IntentFilter(TransactionAddFragment.ACTION_RANDOMIZE_TRANSACTION)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(randomizeTransactionReceiver, filter)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        showCurrentLocation()

        val spinnerCategory = binding.spinnerCategory
        val categories = arrayOf(Category.INCOME, Category.EXPENSE)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                Toast.makeText(requireContext(), "Selected category: $selectedCategory", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle jika tidak ada item yang dipilih
            }
        }

        binding.btnSubmit.setOnClickListener {
            onSubmitClicked()
        }

        val editMode = arguments?.getBoolean("editMode", false) ?: false
        if (editMode) {
            val transaction = arguments?.getParcelable<Transaction>("transaction")
            transaction?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                val latitudeString = "$latitude"
                val longitudeString = "$longitude"

                binding.editTextTitle.setText(it.title)
                binding.editTextAmount.setText(it.amount.toString())
                binding.editTextLatitude.setText(latitudeString)
                binding.editTextLongitude.setText(longitudeString)

                val category = it.category
                if (category != null) {
                    val categoryIndex = categories.indexOf(category)
                    if (categoryIndex != -1) {
                        spinnerCategory.setSelection(categoryIndex)
                    }
                }
            }
        }


        return root
    }

    private fun handleRandomizeTransaction() {
        val editMode = arguments?.getBoolean("editMode", false) ?: false
        if (!editMode) {
            println("random random")
            fillRandomField()
        }
    }

    private val randomizeTransactionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("Intent diterima oleh broadcast receiver")
            if (intent?.action == "com.example.pbd_jwr.RANDOMIZE_TRANSACTION") {
                handleRandomizeTransaction()
            }
        }
    }

    private fun showCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        val latitudeString = "$latitude"
                        val longitudeString = "$longitude"
                        val currentLocation = "Latitude: $latitude, Longitude: $longitude"
                        binding.editTextLatitude.setText(latitudeString)
                        binding.editTextLongitude.setText(longitudeString)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to get location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            val latitude = "6.8915"
            val longitude = "107.6107"
            val currentLocation = "Latitude: $latitude, Longitude: $longitude"
            binding.editTextLatitude.setText(latitude)
            binding.editTextLongitude.setText(longitude)
        }
    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission denied. Some functionality may be limited.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun onSubmitClicked() {

        val title = binding.editTextTitle.text.toString().trim()
        val amountString = binding.editTextAmount.text.toString().trim()
        val latitudeString = binding.editTextLatitude.text.toString().trim() // Ambil nilai latitude dari EditText
        val longitudeString = binding.editTextLongitude.text.toString().trim()

        val category = binding.spinnerCategory.selectedItem as Category

        // Validate input fields
        if (title.isEmpty() || amountString.isEmpty() || latitudeString.isEmpty() || longitudeString.isEmpty()) {
            showError("All fields are required")
            return
        }

        // Validate category
        if (category == null) {
            showError("Please select a category")
            return
        }

        val amount = amountString.toDoubleOrNull()
        val latitude = latitudeString.toDoubleOrNull()
        val longitude = longitudeString.toDoubleOrNull()

        if (amount == null || amount <= 0 || latitude == null || longitude == null) {
            showError("Invalid amount , latitude, or longitude")
            return
        }

        val date = Date().time
        val editMode = arguments?.getBoolean("editMode", false) ?: false

        encryptedSharedPref = EncryptedSharedPref.create(requireContext(), "login")
        val currentUserEmail = encryptedSharedPref.getString("email", "") ?: ""

        if (editMode) {
            val transactionId = arguments?.getLong("transactionId", -1L) ?: -1L
            editTransaction(transactionId, title, category, amount, latitude, longitude, date, currentUserEmail)
            findNavController().popBackStack()
        } else {
            addTransaction(title, category, amount, latitude, longitude, date, currentUserEmail)
        }

        findNavController().popBackStack()
    }

    private fun addTransaction(title: String, category: Category, amount: Double, latitude: Double, longitude: Double, date: Long, email: String) {
        mTransactionViewModel.addTransaction(Transaction(title = title, category = category, amount = amount, latitude = latitude, longitude = longitude, date = date, email = email))
    }

    private fun editTransaction(transactionId: Long, title: String, category: Category, amount: Double, latitude: Double, longitude: Double, date: Long, currentUserEmail: String) {
        mTransactionViewModel.updateTransaction(Transaction(id = transactionId, title = title, category = category, amount = amount, latitude = latitude, longitude = longitude, date = date, email = currentUserEmail))
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun fillRandomField() {
        val randomTitle = generateRandomTitle()
        binding.editTextTitle.setText(randomTitle)
    }

    private fun generateRandomTitle(): String {
        return "Random Transaction ${System.currentTimeMillis()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(randomizeTransactionReceiver)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val ACTION_RANDOMIZE_TRANSACTION = "com.example.pbd_jwr.RANDOMIZE_TRANSACTION"
    }

}
