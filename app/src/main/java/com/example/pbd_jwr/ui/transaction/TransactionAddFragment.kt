package com.example.pbd_jwr.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.databinding.FragmentTransactionAddBinding
import java.util.Date

class TransactionAddFragment : Fragment() {

    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var mTransactionViewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mTransactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)

        _binding = FragmentTransactionAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnSubmit.setOnClickListener {
            onSubmitClicked()
        }


        return root
    }

    private fun onSubmitClicked() {
        val title = binding.editTextTitle.text.toString()
        val category = binding.editTextCategory.text.toString()
        val amount = binding.editTextAmount.text.toString().toDoubleOrNull() ?: return // Handle invalid input
        val location = binding.editTextLocation.text.toString()
        val date = Date().time // You can get the date from a date picker or another source

        // Call the addTransaction method from TransactionViewModel
        mTransactionViewModel.addTransaction(Transaction(14,1, title, category, amount, location, date))

        findNavController().popBackStack()
//        mTransactionViewModel.transactionSubmitted.observe(viewLifecycleOwner) { transactionSubmitted ->
//            if (transactionSubmitted) {
//
//            }
//        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
