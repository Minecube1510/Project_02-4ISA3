package com.dev.kosongdua.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dev.kosongdua.R
import com.dev.kosongdua.databinding.ListingUserfriendBinding
import com.dev.kosongdua.model.Users

class ListFriendAdapter(
    private val itemClickListener: OnItemClickListener,
    private val isDarkMode: Boolean // Tambahkan parameter untuk status tema
) : ListAdapter<Users, ListFriendAdapter.MyViewHolder>(UserDiffUtil()) {

    var selectedPosition = -1

    // Metode untuk meng-clear list
    fun clearList() {
        val size = currentList.size // Mendapatkan ukuran list saat ini
        submitList(emptyList()) // Mengosongkan daftar
        notifyItemRangeRemoved(0, size) // Notifikasi bahwa item telah dihapus
    }

    interface OnItemClickListener {
        fun onItemClick(user: Users, position: AdapterPosition)
    }

    inner class MyViewHolder(
        private val binding: ListingUserfriendBinding,
        private val clickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: Users, isSelected: Boolean) {
            binding.kawaNamae.text = user.name // Set nama pengguna
            binding.root.background = AppCompatResources.getDrawable(
                binding.root.context,
                getBackgroundResource(isSelected)
            ) // Set background berdasarkan status seleksi

            // Dapatkan warna berdasarkan tema saat ini
            val context = binding.root.context

            // Set color berdasarkan status seleksi
            binding.root.setBackgroundColor(
                if (isSelected) {
                    if (isDarkMode) {
                        ContextCompat.getColor(context, R.color.darkNess) // Warna teks saat terpilih di Dark Mode
                    } else {
                        ContextCompat.getColor(context, R.color.lightNess) // Warna teks saat terpilih di Light Mode
                    }
                } else {
                    if (isDarkMode) {
                        ContextCompat.getColor(context, R.color.lightNess) // Warna teks default di Dark Mode
                    } else {
                        ContextCompat.getColor(context, R.color.darkNess) // Warna teks default di Light Mode
                    }
                }
            )
            binding.kawaNamae.setTextColor(
                if (isSelected) {
                    if (isDarkMode) {
                        ContextCompat.getColor(context, R.color.lightNess) // Warna teks saat terpilih di Dark Mode
                    } else {
                        ContextCompat.getColor(context, R.color.darkNess) // Warna teks saat terpilih di Light Mode
                    }
                } else {
                    if (isDarkMode) {
                        ContextCompat.getColor(context, R.color.darkNess) // Warna teks default di Dark Mode
                    } else {
                        ContextCompat.getColor(context, R.color.lightNess) // Warna teks default di Light Mode
                    }
                }
            )

            // Set listener untuk item klik
            binding.root.setOnClickListener {
                if (isSelected) {
                    // Jika sudah terpilih, hapus seleksi
                    clickListener.onItemClick(user, AdapterPosition(-1)) // Menghapus seleksi
                    setSelectedPosition(AdapterPosition(-1)) // Menghapus seleksi
                } else {
                    // Jika belum terpilih, set sebagai terpilih
                    clickListener.onItemClick(user, AdapterPosition(adapterPosition))
                    setSelectedPosition(AdapterPosition(adapterPosition)) // Menetapkan seleksi
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListingUserfriendBinding.inflate(
                LayoutInflater.from(parent.context),
        parent,
        false
        )
        return MyViewHolder(binding, itemClickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition) // Bind data dan status seleksi
    }

    fun setSelectedPosition(position: AdapterPosition) {
        val prevSelected = selectedPosition
        selectedPosition = position.value
        if (prevSelected != -1) notifyItemChanged(prevSelected) // Notifikasi perubahan item sebelumnya
        notifyItemChanged(selectedPosition) // Notifikasi perubahan item baru
    }

    private fun getBackgroundResource(isSelected: Boolean) = when {
        isSelected -> R.drawable.listbox_pressed // Background untuk item terpilih
        else -> R.drawable.listbox_unpressed // Background untuk item tidak terpilih
    }

    class UserDiffUtil : DiffUtil.ItemCallback<Users>() {
        override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean =
            oldItem.uid == newItem.uid // Bandingkan berdasarkan uid

        override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean =
            oldItem == newItem // Bandingkan konten item
    }

    @JvmInline
    value class AdapterPosition(val value: Int) // Wrapper untuk posisi adapter
}