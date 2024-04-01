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

class TransactionDetailFragment : Fragment() {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var mTransactionViewModel: TransactionViewModel


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


            binding.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

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
        binding.textViewCategory.text = transaction.category
        binding.textViewAmount.text = transaction.amount.toString()
        binding.textViewLocation.text = transaction.location
        binding.textViewDate.text = transaction.date.toString()
    }

    private fun deleteTransaction(transaction: Transaction) {
        mTransactionViewModel.deleteTransaction(transaction)
        findNavController().popBackStack()
    }
}
