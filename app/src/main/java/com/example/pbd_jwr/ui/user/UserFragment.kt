package com.example.pbd_jwr.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pbd_jwr.R
import com.example.pbd_jwr.data.entity.User
import com.example.pbd_jwr.data.repository.UserRepository
import com.example.pbd_jwr.databinding.FragmentTransactionBinding
import com.example.pbd_jwr.databinding.FragmentUserBinding

class UserFragment(private val userRepository: UserRepository) : Fragment() {

    private lateinit var mUserViewModel: UserViewModel

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter

    private var _binding: FragmentUserBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        // Initialize RecyclerView and adapter
        userRecyclerView = view.findViewById(R.id.recyclerViewUsers)
        userAdapter = UserAdapter(emptyList())

        // Set up RecyclerView
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = userAdapter

        // Observe LiveData from UserRepository to get user list
        userRepository.getAllUsers().observe(viewLifecycleOwner, Observer { userList ->
            userAdapter.updateUserList(userList)
        })

//        _binding = FragmentUserBinding.inflate(inflater, container, false)
//
//        val view : View = binding.root

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
