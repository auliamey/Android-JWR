package com.example.pbd_jwr.ui.transaction

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.databinding.FragmentTransactionAddBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mTransactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        _binding = FragmentTransactionAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inisialisasi FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Menampilkan lokasi saat ini di EditText
        showCurrentLocation()

        binding.btnSubmit.setOnClickListener {
            onSubmitClicked()
        }

        val editMode = arguments?.getBoolean("editMode", false) ?: false
        if (editMode) {
            val transaction = arguments?.getParcelable<Transaction>("transaction")
            transaction?.let {
                binding.editTextTitle.setText(it.title)
                binding.editTextCategory.setText(it.category)
                binding.editTextAmount.setText(it.amount.toString())
                binding.editTextLocation.setText(it.location)
            }
        }


        return root
    }

    private fun showCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            println("granted")
            // Jika izin diberikan, dapatkan lokasi saat ini
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Jika lokasi berhasil didapatkan, set nilai EditText lokasi
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        val currentLocation = "Latitude: $latitude, Longitude: $longitude"
                        println(currentLocation)
                        binding.editTextLocation.setText(currentLocation)
                    }
                }
                .addOnFailureListener { e ->
                    // Jika gagal mendapatkan lokasi, tampilkan pesan kesalahan
                    println("haha")
                    Toast.makeText(
                        requireContext(),
                        "Failed to get location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 10000 // Interval pembaruan lokasi dalam milidetik (contoh: 10 detik)
                fastestInterval = 5000 // Interval tercepat untuk pembaruan lokasi dalam milidetik (contoh: 5 detik)
            }

            // Meminta pembaruan lokasi secara real-time
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        // Mendapatkan lokasi saat ini dari locationResult
                        val lastLocation = locationResult.lastLocation
                        val latitude = lastLocation.latitude
                        val longitude = lastLocation.longitude
                        val currentLocation = "Latitude: $latitude, Longitude: $longitude"
                        binding.editTextLocation.setText(currentLocation)
                    }
                },
                null
            )
        } else {
            println("not granted")
            // Jika izin lokasi belum diberikan, minta izin
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
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
                // Izin lokasi diberikan, panggil showCurrentLocation untuk menampilkan lokasi
                showCurrentLocation()
            } else {
                // Izin lokasi ditolak, tampilkan pesan kesalahan
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
        val category = binding.editTextCategory.text.toString().trim()
        val amountString = binding.editTextAmount.text.toString().trim()
        val location = binding.editTextLocation.text.toString().trim()

        // Validate input fields
        if (title.isEmpty() || category.isEmpty() || amountString.isEmpty() || location.isEmpty()) {
            showError("All fields are required")
            return
        }

        val amount = amountString.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            showError("Invalid amount")
            return
        }

        val date = Date().time


        val editMode = arguments?.getBoolean("editMode", false) ?: false
        println("editMode")
        println(editMode)
        if (editMode) {
            // Editing mode
            val transactionId = arguments?.getLong("transactionId", -1L) ?: -1L
            editTransaction(transactionId, title, category, amount, location, date)
            findNavController().popBackStack()
        } else {
            // Adding mode
            addTransaction(title, category, amount, location, date)
        }

        findNavController().popBackStack()
    }

    private fun addTransaction(title: String, category: String, amount: Double, location: String, date: Long) {
        mTransactionViewModel.addTransaction(Transaction(userId = 1, title = title, category = category, amount = amount, location = location, date = date))
    }

    private fun editTransaction(transactionId: Long, title: String, category: String, amount: Double, location: String, date: Long) {
        mTransactionViewModel.updateTransaction(Transaction(transactionId, userId = 1, title = title, category = category, amount = amount, location = location, date = date))
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
