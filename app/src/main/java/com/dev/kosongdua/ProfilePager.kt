package com.dev.kosongdua

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dev.kosongdua.databinding.IdentProfileBinding
//import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.QuerySnapshot
import java.util.Locale

class ProfilePager : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var binding: IdentProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private var isDarkMode: Boolean = false // Menyimpan status tema

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Inisialisasi sharedPref
        sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        isDarkMode = sharedPref.getBoolean("DarkMode", false)

        // Set tema berdasarkan preferensi pengguna
        if (isDarkMode) {
            setTheme(R.style.Theme_KosongDua_Light)
        } else {
            setTheme(R.style.Theme_KosongDua_Dark)
        }

        super.onCreate(savedInstanceState)
        binding = IdentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil UID dari intent
        val currentUserUid = intent.getStringExtra("EXTRA_USER_UID") ?: run {
            Toast.makeText(this, "User UID not found!", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan data pengguna berdasarkan UID
        loadUserData(currentUserUid)

        // Setup tombol untuk mengubah nama, email, dan password
        setupButtons()
    }

    private fun setupButtons() {
        binding.nombolNamae.setOnClickListener {
            val inputName = toProperCase(binding.ubahNamae.text.toString().trim())
            if (inputName.isEmpty()) {
                Toast.makeText(this, "Fill name please!", Toast.LENGTH_LONG).show()
            } else {
                confirmNameChange(inputName)
            }
        }

        binding.nombolEmail.setOnClickListener {
            val inputEmail = binding.ubahEmail.text.toString().trim()
            if (inputEmail.isEmpty()) {
                Toast.makeText(this, "Fill email please!", Toast.LENGTH_SHORT).show()
            } else {
                confirmEmailChange(inputEmail)
            }
        }

        binding.nombolSandi.setOnClickListener {
            val inputPassword = binding.ubahSandi.text.toString().trim()
            if (inputPassword.isEmpty()) {
                Toast.makeText(this, "Fill password please!", Toast.LENGTH_SHORT).show()
            } else {
                confirmPasswordChange()
            }
        }
    }

    private fun confirmNameChange(inputName: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Name Change")
            .setMessage("Are you sure you want to change your name to '$inputName'?")
            .setPositiveButton("Yes") { dialog, _ ->
                updateNameInFirestore(inputName)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun confirmEmailChange(inputEmail: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Email Change")
            .setMessage("Are you sure you want to update your email to '$inputEmail'?")
            .setPositiveButton("Yes") { dialog, _ ->
                updateEmailInFirestore(inputEmail)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun confirmPasswordChange() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Password Change")
            .setMessage("Are you sure you want to change your password?")
            .setPositiveButton("Yes") { dialog, _ ->
                updatePasswordInFirestore()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun loadUserData(userUid: String) {
        db.collection("users").document(userUid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "No Name"
                    val email = document.getString("email") ?: "No Email"
                    val password = document.getString("password") ?: "No Password"

                    // Set data ke TextView
                    binding.tandaNamae.text = name
                    binding.tandaEmail.text = email
                    binding.tandaSandi.text = password

                    // Ambil daftar teman berdasarkan UID
                    loadFriendList(userUid)
                } else {
                    Toast.makeText(this, "User is not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFriendList(userUid: String) {
        db.collection("frenlist").document(userUid).get()
            .addOnSuccessListener { friendListDoc ->
                if (friendListDoc.exists()) {
                    val rawFriends = friendListDoc.get("friends") as? List<*>
                    val friendUids = rawFriends?.filterIsInstance<String>() ?: emptyList()

                    // Ambil username berdasarkan UID
                    loadUsernames(friendUids)
                } else {
                    Toast.makeText(this, "Friend list not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUsernames(friendUids: List<String>) {
        val tempUsernames = mutableListOf<String>()
        var loadedCount = 0

        for (uid in friendUids) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { userDoc ->
                    userDoc?.let {
                        val username = it.getString("name") ?: "No Name"
                        tempUsernames.add(username)
                    }
                    loadedCount++
                    if (loadedCount == friendUids.size) {
                        // Tampilkan username yang didapat
                        displayUsernames(tempUsernames)
                    }
                }
                .addOnFailureListener { e ->
                    loadedCount++
                    Log.e("ERROR", "Failed getting username for $uid: ${e.message}")
                    if (loadedCount == friendUids.size) {
                        // Tampilkan username yang didapat meskipun ada yang gagal
                        displayUsernames(tempUsernames)
                    }
                }
        }
    }

    private fun displayUsernames(usernames: List<String>) {
        // Tampilkan username di UI, misalnya di TextView
    }

    private fun updateNameInFirestore(inputName: String) {
        // Implementasi untuk memperbarui nama di Firestore
    }

    private fun updateEmailInFirestore(inputEmail: String) {
        // Implementasi untuk memperbarui email di Firestore
    }

    private fun updatePasswordInFirestore() {
        // Implementasi untuk memperbarui password di Firestore
    }

    // Mengubah setiap kata menjadi huruf kapital pada huruf pertamanya
    private fun toProperCase(input: String): String {
        return input.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { char -> char.uppercase(Locale.getDefault()) }
        }
    }
    // Input gak boleh angka
    fun String.isNumber(): Boolean {
        return this.all { it.isDigit() }
    }
}