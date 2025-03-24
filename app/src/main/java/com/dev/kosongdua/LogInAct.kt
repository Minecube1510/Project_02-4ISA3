package com.dev.kosongdua

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.dev.kosongdua.databinding.LogInPageBinding
//import com.dev.kosongdua.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class LogInAct : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var binding: LogInPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inisialisasi sharedPref dan pengaturan tema
        sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)
        if (isDarkMode) {
            setTheme(R.style.Theme_KosongDua_Light)
        } else {
            setTheme(R.style.Theme_KosongDua_Dark)
        }

        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        binding = LogInPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toggle tema ketika logo diklik
        binding.aikonLogo.setOnClickListener {
            val newMode = !isDarkMode
            sharedPref.edit { putBoolean("DarkMode", newMode) }
            recreate() // Restart activity agar tema berubah
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.getLogInA2.setOnClickListener {
            val userInput = toProperCase(binding.mintaAiden.text.toString().trim())
            val password = binding.mintaSandi.text.toString().trim()

            if (userInput.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username/Email and password cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (userInput.isNumber()) {
                Toast.makeText(this, "Username/Email and password cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Jika input adalah email, langsung proses login
            if (Patterns.EMAIL_ADDRESS.matcher(userInput).matches()) {
                loginWithEmail(userInput, password)
            } else {
                // Jika input adalah username, cari email terkait di koleksi "users"
                firestore.collection("users")
                    .whereEqualTo("name", userInput)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            Toast.makeText(this, "Username not found!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Ambil email dari dokumen pertama yang ditemukan
                            val document = querySnapshot.documents[0]
                            val email = document.getString("email")
                            if (email.isNullOrEmpty()) {
                                Toast.makeText(this, "User email not found!", Toast.LENGTH_SHORT).show()
                            } else {
                                loginWithEmail(email, password)
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.toSignUpA2.setOnClickListener {
            startActivity(Intent(this, SignUpAct::class.java))
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val uid = user.uid
                        // Ambil friend list berdasarkan UID yang konsisten
                        fetchFriendList(uid) {
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java).apply {
                                putExtra("EXTRA_USER_UID", uid)
                            })
                        }
                    } else {
                        Toast.makeText(this, "User not found after login", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Fungsi untuk mengambil friend list berdasarkan UID
    private fun fetchFriendList(uid: String, onComplete: () -> Unit) {
        firestore.collection("frenlist").document(uid)
            .get()
            .addOnSuccessListener { document ->
                //if (document.exists()) {
                    //val rawFriends = document.get("friends")
                    //val friends = (rawFriends as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    // Proses friend list (misalnya, simpan atau kirim ke MainActivity) jika diperlukan
                //}
                onComplete()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching friend list: ${exception.message}", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }

    // Mengubah setiap kata menjadi huruf kapital pada huruf pertamanya
    private fun toProperCase(input: String): String {
        return input.split(" ").joinToString(" ") { word ->
            word.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() }
        }
    }
    // Input gak boleh angka
    fun String.isNumber(): Boolean {
        return this.all { it.isDigit() }
    }
}
