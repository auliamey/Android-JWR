package com.example.pbd_jwr.ui.transaction

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pbd_jwr.databinding.FragmentTransactionBinding
import com.example.pbd_jwr.R

class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var mTransactionViewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mTransactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]



        mTransactionViewModel.getAllTransactions().observe(viewLifecycleOwner, Observer { transactions ->
            transactions.forEach { transaction ->
                Log.d("Transaction", "Title: ${transaction.title}, Category: ${transaction.category}, Amount: ${transaction.amount}")
            }
        })

        transactionAdapter = TransactionAdapter()

        binding.recyclerViewTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_transactionFragment_to_transactionAddFragment)
        }

        mTransactionViewModel.getAllTransactions().observe(viewLifecycleOwner, Observer { transactions ->
            transactionAdapter.submitList(transactions)
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}