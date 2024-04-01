package com.example.pbd_jwr.ui.settings

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pbd_jwr.databinding.FragmentSettingsBinding
import com.example.pbd_jwr.ui.transaction.TransactionViewModel
import com.example.pbd_jwr.data.entity.Transaction
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Locale


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        binding.saveBtn.setOnClickListener {
            transactionViewModel.getAllTransactions().observe(viewLifecycleOwner) { transactions ->
                exportTransactionsToExcel(transactions, requireContext())
            }
        }

        return root
    }

    private fun exportTransactionsToExcel(transactions: List<Transaction>, context: Context) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Transactions.xlsx") // Nama file
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") // MIME Type
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // Direktori tujuan
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values) // Mendapatkan URI

        val workbook = XSSFWorkbook()

        try {
            val sheet = workbook.createSheet("Transactions")

            // Membuat header
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("Tanggal")
            header.createCell(1).setCellValue("Kategori Transaksi")
            header.createCell(2).setCellValue("Nominal Transaksi")
            header.createCell(3).setCellValue("Nama Transaksi")
            header.createCell(4).setCellValue("Lokasi")

            // Format tanggal
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // Mengisi data ke dalam sheet
            transactions.forEachIndexed { index, transaction ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(dateFormat.format(transaction.date))
                row.createCell(1).setCellValue(transaction.category)
                row.createCell(2).setCellValue(transaction.amount)
                row.createCell(3).setCellValue(transaction.title)
                row.createCell(4).setCellValue(transaction.location)
            }

            // Menyimpan file
            uri?.let {
                resolver.openOutputStream(it).use { outputStream ->
                    workbook.write(outputStream) // Menulis ke file
                }
            }

            Toast.makeText(context, "Transaksi berhasil diekspor ke Excel", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor transaksi: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            workbook.close()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}