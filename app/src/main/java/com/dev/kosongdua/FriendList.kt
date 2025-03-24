package com.dev.kosongdua

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.kosongdua.adapter.ListFriendAdapter
import com.dev.kosongdua.databinding.FriendListedBinding
import com.dev.kosongdua.model.Users
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class FriendList : AppCompatActivity(), ListFriendAdapter.OnItemClickListener {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var binding: FriendListedBinding
    private lateinit var adapter: ListFriendAdapter
    private val friendList = mutableListOf<Users>()
    private val db = FirebaseFirestore.getInstance()
    private var isDarkMode: Boolean = false

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Inisialisasi sharedPref dan pengaturan tema
        sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        isDarkMode = sharedPref.getBoolean("DarkMode", false)
        if (isDarkMode) {
            setTheme(R.style.Theme_KosongDua_Light)
        } else {
            setTheme(R.style.Theme_KosongDua_Dark)
        }

        super.onCreate(savedInstanceState)
        binding = FriendListedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil UID pengguna dari intent
        val currentUserUid = intent.getStringExtra("EXTRA_USER_UID") ?: run {
            Toast.makeText(this, "User is invalid!", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil nama pengguna berdasarkan UID dan set ke header
        db.collection("users").document(currentUserUid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val userName = userDoc.getString("name") ?: "No Name"
                    binding.headerFrilist.text = getString(R.string.daFren, userName)
                } else {
                    binding.headerFrilist.text = getString(R.string.gakDaGuna)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Inisialisasi RecyclerView adapter
        adapter = ListFriendAdapter(this, isDarkMode)
        binding.listTeman.adapter = adapter
        binding.listTeman.layoutManager = LinearLayoutManager(this)

        // Load data teman
        adapter.clearList()
        loadFriendData()

        // Klik header untuk kembali ke MainActivity
        binding.headerFrilist.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("EXTRA_USER_UID", currentUserUid)
            })
        }

        // Set up listener untuk tombol
        binding.butanUnfren.setOnClickListener { removeFriend() }
        binding.butanProfil.setOnClickListener { goToProfile() }
        binding.butanTambah.setOnClickListener { addFriend() }
    }

    // Fungsi loadFriendData diperbarui agar langsung mengambil array nama teman
    private fun loadFriendData() {
        // Ambil UID pengguna dari intent
        val currentUserUid = intent.getStringExtra("EXTRA_USER_UID") ?: run {
            Toast.makeText(this, "User is invalid!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("frenlist").document(currentUserUid).get()
            .addOnSuccessListener { friendListDoc ->
                if (friendListDoc.exists()) {
                    val rawFriends = friendListDoc.get("friends") as? List<*>
                    val friendNames = rawFriends?.filterIsInstance<String>() ?: emptyList()

                    Log.d("FRIEND_DEBUG", "Friend names: $friendNames")

                    // Buat list Users dari nama teman (email dikosongkan, uid diisi dengan nama)
                    friendList.clear()
                    friendNames.forEach { name ->
                        friendList.add(Users(name = name, email = "", uid = name))
                    }
                    adapter.submitList(friendList.toList())

                    if (friendNames.isEmpty()) {
                        Toast.makeText(this, "Let's make some friends!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Friend list not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onItemClick(user: Users, position: ListFriendAdapter.AdapterPosition) {
        adapter.setSelectedPosition(position)
    }

    private fun removeFriend() {
        val selectedPosition = adapter.selectedPosition
        if (selectedPosition == -1) {
            Toast.makeText(this, "Please select a friend first!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserUid = intent.getStringExtra("EXTRA_USER_UID") ?: run {
            Toast.makeText(this, "User invalid!", Toast.LENGTH_SHORT).show()
            return
        }

        // Karena yang disimpan adalah nama teman, gunakan field name
        val friendToRemoveName = friendList[selectedPosition].name
        val friendListDocRef = db.collection("frenlist").document(currentUserUid)

        AlertDialog.Builder(this)
            .setTitle("Remove Friend")
            .setMessage("Are you sure you want to remove this friend?")
            .setPositiveButton("Yes") { _, _ ->
                friendListDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val friendsData = document.get("friends")
                        val mutableFriendsData = when (friendsData) {
                            is List<*> -> friendsData.filterIsInstance<String>().toMutableList()
                            else -> mutableListOf()
                        }
                        if (mutableFriendsData.remove(friendToRemoveName)) {
                            friendListDocRef.update("friends", mutableFriendsData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Friend removed successfully", Toast.LENGTH_SHORT).show()
                                    loadFriendData()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to remove friend: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Friend not found in your list", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Friend list not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun goToProfile() {
        val currentUserUid = intent.getStringExtra("EXTRA_USER_UID")
        if (currentUserUid != null) {
            // Ambil nama pengguna dari header atau dari Firestore jika perlu
            val userName = binding.headerFrilist.text.toString() // Ambil nama dari header

            // Pastikan nama tidak kosong
            if (userName.isNotEmpty()) {
                val intent = Intent(this, ProfilePager::class.java).apply {
                    putExtra("EXTRA_USER_UID", currentUserUid) // Kirim UID pengguna
                    putExtra("EXTRA_USER_NAME", userName) // Kirim nama pengguna
                    putExtra("DARK_MODE", isDarkMode) // Kirim status tema
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Username is not available!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User is not found", Toast.LENGTH_SHORT).show()
        }
    }

    // Pada fungsi addFriend(), yang ditambahkan ke array adalah nama teman (bukan UID)
    private fun addFriend() {
        val friendNameInput = toProperCase(binding.menCariTeman.text.toString().trim())
        if (friendNameInput.isEmpty()) {
            Toast.makeText(this, "Please enter the name!", Toast.LENGTH_SHORT).show()
            return
        }
        if (friendNameInput.isNumber()) {
            Toast.makeText(this, "Name is invalid!", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil UID pengguna saat ini
        val currentUserUid = intent.getStringExtra("EXTRA_USER_UID") ?: run {
            Toast.makeText(this, "User is invalid!", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil nama pengguna saat ini dari Firestore
        db.collection("users").document(currentUserUid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val currentUserName = userDoc.getString("name") ?: ""

                    // Cek jika nama yang dimasukkan sama dengan nama pengguna saat ini
                    if (friendNameInput.equals(currentUserName, ignoreCase = true)) {
                        Toast.makeText(this, "You can't type same name", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Tampilkan dialog konfirmasi untuk menambahkan teman
                    AlertDialog.Builder(this)
                        .setTitle("Add Friend")
                        .setMessage("Are you sure you want to add $friendNameInput as a friend?")
                        .setPositiveButton("Yes") { _, _ ->
                            // Cek apakah teman ada
                            db.collection("users")
                                .whereEqualTo("name", friendNameInput)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (querySnapshot.isEmpty) {
                                        Toast.makeText(this, "User is not found", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Logika untuk menambahkan teman
                                        val friendDoc = querySnapshot.documents[0]
                                        val friendName = friendDoc.getString("name")
                                        if (friendName != null) {
                                            val friendListDocRef = db.collection("frenlist").document(currentUserUid)
                                            friendListDocRef.get().addOnSuccessListener { document ->
                                                if (document.exists()) {
                                                    val friendsData = document.get("friends")
                                                    val mutableFriendsData = when (friendsData) {
                                                        is List<*> -> friendsData.filterIsInstance<String>().toMutableList()
                                                        else -> mutableListOf()
                                                    }
                                                    if (!mutableFriendsData.contains(friendName)) {
                                                        mutableFriendsData.add(friendName)
                                                        friendListDocRef.update("friends", mutableFriendsData)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(this, "Friend added successfully", Toast.LENGTH_SHORT).show()
                                                                loadFriendData()
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Toast.makeText(this, "Failed to add friend: ${e.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                    } else {
                                                        Toast.makeText(this, "Friend already exists in your list", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    // Buat dokumen baru untuk daftar teman
                                                    val newFrenData = hashMapOf(
                                                        "name" to "Friend List",
                                                        "friends" to listOf(friendName),
                                                        "friendUid" to currentUserUid
                                                    )
                                                    friendListDocRef.set(newFrenData)
                                                        .addOnSuccessListener {
                                                            Toast.makeText(this, "Friend list created and friend added", Toast.LENGTH_SHORT).show()
                                                            loadFriendData()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Toast.makeText(this, "Failed to add friend: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(this, "Friend data invalid!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .setNegativeButton("No", null)
                        .show()
                } else {
                    Toast.makeText(this, "User is not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Mengubah setiap kata menjadi huruf kapital pada huruf pertamanya
    private fun toProperCase(input: String): String {
        return input.split(" ").joinToString(" ") {
            it.lowercase(Locale.getDefault()).replaceFirstChar { char ->
                char.uppercase(Locale.getDefault())
            }
        }
    }
    // Input gak boleh angka
    fun String.isNumber(): Boolean {
        return this.all { it.isDigit() }
    }
}
