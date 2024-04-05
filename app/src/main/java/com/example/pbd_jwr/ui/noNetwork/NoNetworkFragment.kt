package com.example.pbd_jwr.ui.noNetwork

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pbd_jwr.databinding.FragmentNonetworkBinding


class NoNetworkFragment : Fragment() {


    private var _binding: FragmentNonetworkBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNonetworkBinding.inflate(inflater, container, false)
        return binding.root
    }




}
