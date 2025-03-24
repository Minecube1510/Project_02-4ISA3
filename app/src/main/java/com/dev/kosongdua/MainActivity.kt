package com.dev.kosongdua

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
//import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.kosongdua.adapter.ListUserAdapter
import com.dev.kosongdua.databinding.ActivityMainBinding
import com.dev.kosongdua.model.Users
import com.google.firebase.firestore.FirebaseFirestore
//import androidx.core.content.edit

class MainActivity : AppCompatActivity(), ListUserAdapter.OnItemClickListener {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var adapter: ListUserAdapter
    private val userList = mutableListOf<Users>()
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Inisialisasi sharedPref
        sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)

        // Set tema berdasarkan preferensi pengguna
        if (isDarkMode) {
            setTheme(R.style.Theme_KosongDua_Light)
        } else {
            setTheme(R.style.Theme_KosongDua_Dark)
        }

        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sakelarEnvor.isChecked = isDarkMode
        binding.sakelarEnvor.setOnClickListener {
            val newMode = !isDarkMode
            sharedPref.edit { putBoolean("DarkMode", newMode) }
            recreate() // Restart activity agar tema berubah
        }

        // Setup RecyclerView
        binding.liatList.layoutManager = LinearLayoutManager(this)
        adapter = ListUserAdapter(userList, this)
        binding.liatList.adapter = adapter

        // Cek apakah pengguna sudah login
        val userUid = intent.getStringExtra("EXTRA_USER_UID")
        if (userUid == null) {
            Toast.makeText(this, "Let's make some friends!", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data friend list dari Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("frenlist").document(userUid)
            .get()
            .addOnSuccessListener { friendListDoc ->
                if (friendListDoc.exists()) {
                    val rawFriends = friendListDoc.get("friends") as? List<*>
                    val validFriends = rawFriends?.filterIsInstance<String>()
                    if (validFriends != null) {
                        loadUserData(validFriends) // Pass userUid ke loadUser Data
                    } else {
                        Log.d("DEBUG", "Invalid friend list format")
                    }
                } else {
                    Log.d("DEBUG", "The friend list is not found!")
                    Toast.makeText(this, "The friend list is not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        binding.liaTeman.setOnClickListener {
            Log.d("DEBUG", "Liat list")
            val intent = Intent(this, FriendList::class.java).apply {
                putExtra("EXTRA_USER_UID", userUid) // Mengirim UID
            }
            startActivity(intent)
        }
    }

    // Fungsi untuk memuat data pengguna berdasarkan daftar teman
    @SuppressLint("NotifyDataSetChanged")
    private fun loadUserData(friendNames: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val tempList = mutableListOf<Users>()
        var loadedCount = 0

        // Clear the current list in the adapter before loading new data
        adapter.clearList() // Clear the list in the adapter

        for (friendName in friendNames) {
            // Cari pengguna berdasarkan nama di koleksi 'users'
            db.collection("users")
                .whereEqualTo("name", friendName)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (userDoc in querySnapshot.documents) {
                        val name = userDoc.getString("name") ?: "No Name"
                        val email = userDoc.getString("email") ?: "No Email"
                        val uid = userDoc.id // Ambil UID dari dokumen

                        // Tambahkan pengguna ke daftar
                        tempList.add(Users(uid = uid, name = name, email = email))
                    }
                    loadedCount++
                    if (loadedCount == friendNames.size) {
                        // Update adapter setelah semua data teman telah diambil
                        userList.clear()
                        userList.addAll(tempList)
                        adapter.notifyDataSetChanged() // Memperbarui tampilan adapter
                    }
                }
                .addOnFailureListener { e ->
                    loadedCount++
                    Log.e("ERROR", "Failed getting user for $friendName: ${e.message}")
                    if (loadedCount == friendNames.size) {
                        userList.clear()
                        userList.addAll(tempList)
                        adapter.notifyDataSetChanged()
                    }
                }

        }
    }

    // Navigasi ke ChattingAct (mengirimkan data pengguna jika perlu)
    override fun onItemClick(user: Users) {
        Log.d("DEBUG", "Sending UID: ${user.uid}")
        val userUid = intent.getStringExtra("EXTRA_USER_UID") // Ambil UID dari intent
        val intent = Intent(this, ChattingAct::class.java).apply {
            putExtra("USER_UID", user.uid) // Menggunakan UID teman yang di-klik
            putExtra("CURRENT_USER_UID", userUid) // Mengirim UID pengguna yang sedang login
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.liatList.adapter = null
    }
}