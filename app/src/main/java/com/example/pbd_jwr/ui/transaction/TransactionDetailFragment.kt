package com.example.pbd_jwr.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.pbd_jwr.R
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.databinding.FragmentTransactionDetailBinding
//import com.example.pbd_jwr.ui.map.MapsFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionDetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var mTransactionViewModel: TransactionViewModel

    private lateinit var googleMap: GoogleMap

    private var transaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mTransactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        _binding = FragmentTransactionDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val transaction = arguments?.getParcelable<Transaction>("transaction")

        if (transaction != null) {
            displayTransactionDetails(transaction)

            binding.btnDelete.setOnClickListener {
                transaction?.let { deleteTransaction(it) }
            }

            binding.btnEdit.setOnClickListener {
                val bundle = Bundle().apply {
                    putParcelable("transaction", transaction)
                    putLong("transactionId", transaction.id) // Pass the transaction ID
                    putBoolean("editMode", true) // Set edit mode to true
                }
                findNavController().navigate(R.id.action_transactionDetailFragment_to_transactionAddFragment, bundle)
            }

        } else {
            Toast.makeText(requireContext(), "Transaction not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displayTransactionDetails(transaction: Transaction) {
        binding.textViewTitle.text = transaction.title
        binding.textViewCategory.text = transaction.category.toString()
        binding.textViewAmount.text = transaction.amount.toString()
        binding.textViewLocation.text = "${transaction.latitude.toString()}, ${transaction.longitude.toString()}"
        binding.textViewDate.text = formatDate(transaction.date)

        binding.textViewCategoryLabel.text = "Category: "
        binding.textViewAmountLabel.text = "Amount: "
        binding.textViewLocationLabel.text = "Location: "
        binding.textViewDateLabel.text = "Date: "
    }

    private fun formatDate(milliseconds: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        return sdf.format(calendar.time)
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Yes") { _, _ ->
                transaction?.let { deleteTransaction(it) }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        mTransactionViewModel.deleteTransaction(transaction)
        findNavController().popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        transaction?.let { addMarker(it.latitude, it.longitude) }
    }

    private fun addMarker(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)
        googleMap.addMarker(MarkerOptions().position(location).title("Transaction Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}
