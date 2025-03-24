package com.dev.kosongdua.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.kosongdua.databinding.ItemContainerUserBinding
import com.dev.kosongdua.model.Users

class ListUserAdapter(
private val userList: MutableList<Users>, // Pastikan ini MutableList
private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ListUserAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(user: Users) // Menggunakan objek Users
    }

    fun clearList() {
        val size = userList.size
        userList.clear() // Mengosongkan daftar
        notifyItemRangeRemoved(0, size) // Notifikasi bahwa item telah dihapus
    }

    class MyViewHolder(private val binding: ItemContainerUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Users, clickListener: OnItemClickListener) {
            binding.tvNamae.text = user.name // Menggunakan name
            binding.tvEmail.text = user.email
            // Pastikan mengirim UID:
            itemView.setOnClickListener { clickListener.onItemClick(user) } // Mengirim objek Users
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.context),
        parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(userList[position], itemClickListener)
    }

    override fun getItemCount(): Int = userList.size
}