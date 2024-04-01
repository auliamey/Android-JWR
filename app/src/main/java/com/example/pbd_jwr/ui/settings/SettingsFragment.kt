package com.example.pbd_jwr.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pbd_jwr.databinding.FragmentSettingsBinding
import com.example.pbd_jwr.encryptedSharedPref.EncryptedSharedPref

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var encryptedSharedPref : SharedPreferences;
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        encryptedSharedPref = EncryptedSharedPref.create(requireContext(),"login")

        val settingsViewModel =
            ViewModelProvider(this)[SettingsViewModel::class.java]

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val subjectEmail : String = "Comprehensive Account Transaction History Report"
        val contentEmail : String =
                "Attached is a comprehensive report detailing all transactions associated with your account. Should you have any questions or require further assistance, please don't hesitate to reach out.\n" +
                "\n" +
                "Best regards,\n" +
                "\n" +
                "JWR App\n" +
                "\n" +
                "\n" +
                "\n"

        binding.sendEmailButton.setOnClickListener{
            sendEmailIntent(subjectEmail,contentEmail, encryptedSharedPref.getString("email",""))
        }

        return root
    }

    private fun sendEmailIntent(subject : String, content : String, toEmail : String?){
        val emailIntent : Intent = Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL,arrayOf(toEmail));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        emailIntent.setType("message/rfcB22");
        startActivity(Intent.createChooser(emailIntent, "Send email with"))

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}