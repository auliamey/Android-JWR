package com.example.pbd_jwr.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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

        transaction?.let { displayTransactionDetails(it) }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDelete.setOnClickListener {
            transaction?.let { deleteTransaction(it) }
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
