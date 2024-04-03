package com.example.pbd_jwr.ui.transaction

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.pbd_jwr.ScanActivity
import com.example.pbd_jwr.databinding.TransactionDummyDetailBinding

class TransactionDummyAdapter(context: Context, transactions: List<ScanActivity.TransactionDummy>) :
    ArrayAdapter<ScanActivity.TransactionDummy>(context, 0, transactions) {

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val transaction = getItem(position)
        val binding: TransactionDummyDetailBinding

        if (view == null) {
            binding = TransactionDummyDetailBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = view.tag as TransactionDummyDetailBinding
        }

        transaction?.let {
            binding.itemName.text = it.name
            binding.itemName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            binding.itemName.isAllCaps = true
            binding.itemQty.text = "Quantity: " + it.qty.toString()
            binding.itemPrice.text = "Price: " + it.price.toString()
        }

        return view
    }
}