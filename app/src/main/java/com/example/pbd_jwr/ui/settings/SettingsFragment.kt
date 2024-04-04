package com.example.pbd_jwr.ui.settings

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.pbd_jwr.databinding.FragmentSettingsBinding
import com.example.pbd_jwr.encryptedSharedPref.EncryptedSharedPref
import com.example.pbd_jwr.ui.transaction.TransactionViewModel
import com.example.pbd_jwr.data.entity.Transaction
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Locale


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val transactionViewModel: TransactionViewModel by viewModels()
    private lateinit var encryptedSharedPref: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        encryptedSharedPref = EncryptedSharedPref.create(requireContext(), "login")

        setupListeners()
        return binding.root
    }

    private fun setupListeners() {
        val toEmail = encryptedSharedPref.getString("email", "")
        binding.sendEmailButton.setOnClickListener {
            sendEmailWithAttachment(toEmail)
        }

        binding.saveBtn.setOnClickListener {
            saveTransactionsToExcel()
        }

        val randomizeButton = binding.randomTransactionBtn
        randomizeButton.setOnClickListener {
            val intent = Intent()
            intent.setAction("com.example.pbd_jwr.RANDOMIZE_TRANSACTION")

            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
    }

    private fun sendEmailWithAttachment(toEmail: String?) {
        val subject = "Comprehensive Account Transaction History Report"
        val content = """
            Attached is a comprehensive report detailing all transactions associated with your account. Should you have any questions or require further assistance, please don't hesitate to reach out.

            Best regards,

            JWR App
        """.trimIndent()

        transactionViewModel.getAllTransactions().observe(viewLifecycleOwner) { transactions ->
            exportTransactionsToExcel(transactions, requireContext())?.let { uri ->
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "vnd.android.cursor.dir/email"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, content)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(emailIntent, "Send email with..."))
            }
        }
    }

    private fun saveTransactionsToExcel() {
        transactionViewModel.getAllTransactions().observe(viewLifecycleOwner) { transactions ->
            exportTransactionsToExcel(transactions, requireContext())
        }
    }

    private fun exportTransactionsToExcel(transactions: List<Transaction>, context: Context): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Transactions.xlsx")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)

        val workbook = XSSFWorkbook()

        try {
            val sheet = workbook.createSheet("Transactions")

            // Membuat header
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("Tanggal")
            header.createCell(1).setCellValue("Kategori Transaksi")
            header.createCell(2).setCellValue("Nominal Transaksi")
            header.createCell(3).setCellValue("Nama Transaksi")
            header.createCell(4).setCellValue("Latitude")
            header.createCell(5).setCellValue("Longitude")

            // Format tanggal
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // Mengisi data ke dalam sheet
            transactions.forEachIndexed { index, transaction ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(dateFormat.format(transaction.date))
                row.createCell(1).setCellValue(transaction.category.toString())
                row.createCell(2).setCellValue(transaction.amount)
                row.createCell(3).setCellValue(transaction.title)
                row.createCell(4).setCellValue(transaction.latitude)
                row.createCell(5).setCellValue(transaction.longitude)
            }

            // Menyimpan file
            uri?.let {
                resolver.openOutputStream(it).use { outputStream ->
                    workbook.write(outputStream)
                }
            }

            Toast.makeText(context, "Transaksi berhasil diekspor ke Excel", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor transaksi: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            workbook.close()
        }
        return uri
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
