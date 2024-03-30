package com.example.pbd_jwr.ui.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pbd_jwr.R
import com.example.pbd_jwr.data.entity.User

class UserAdapter(private var userList: List<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.bind(currentUser)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUserList(newUserList: List<User>) {
        userList = newUserList
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fullNameTextView: TextView = itemView.findViewById(R.id.textViewFullName)
        private val emailTextView: TextView = itemView.findViewById(R.id.textViewEmail)

        fun bind(user: User) {
            fullNameTextView.text = user.fullName
            emailTextView.text = user.email
        }
    }
}
