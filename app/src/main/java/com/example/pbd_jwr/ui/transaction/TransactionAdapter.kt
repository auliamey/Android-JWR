package com.example.pbd_jwr.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
//import androidx.navigation.common.ktx.R
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.databinding.TransactionItemBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.pbd_jwr.R

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = TransactionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TransactionViewHolder(private val binding: TransactionItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            with(binding) {
                textViewTitle.text = transaction.title
                textViewCategory.text = transaction.category
                textViewAmount.text = transaction.amount.toString()
            }

            itemView.setOnClickListener {
                val bundle = Bundle().apply {
                    putParcelable("transaction", transaction)
                }
                itemView.findNavController().navigate(R.id.action_transactionFragment_to_transactionDetailFragment, bundle)
            }

        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}

