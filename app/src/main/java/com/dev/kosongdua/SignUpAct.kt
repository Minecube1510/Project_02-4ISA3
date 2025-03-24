package com.dev.kosongdua

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.kosongdua.databinding.SignUpPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import androidx.core.content.edit
import com.google.firebase.Firebase
import java.util.Locale

class SignUpAct : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var binding: SignUpPageBinding
    private lateinit var authSignUp: FirebaseAuth
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inisialisasi sharedPref dan ambil pengaturan tema
        sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)
        if (isDarkMode) {
            setTheme(R.style.Theme_KosongDua_Light)
        } else {
            setTheme(R.style.Theme_KosongDua_Dark)
        }

        super.onCreate(savedInstanceState)
        binding = SignUpPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.aikonLogo.setOnClickListener {
            // Toggle mode dan simpan ke shared preferences
            val newMode = !isDarkMode
            sharedPref.edit {
                putBoolean("DarkMode", newMode)
            }
            recreate() // Restart activity agar tema berubah
        }

        authSignUp = FirebaseAuth.getInstance()

        binding.creatIngA1.setOnClickListener {
            val iName = toProperCase(binding.nulisAiden.text.toString().trim())
            val email = binding.nulisEmail.text.toString().trim()
            val password = binding.nulisSandi.text.toString().trim()

            if (!validateInput(iName, email, password)) {
                return@setOnClickListener
            }

            db.collection("users").whereEqualTo("name", iName)
                .get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        Toast.makeText(this, "Username has added!", Toast.LENGTH_SHORT).show()
                    } else {
                        authSignUp.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val user = it.result?.user
                                    if (user != null) {
                                        val uid = user.uid

                                        // Simpan data user sesuai rules (name, email, password, uid)
                                        val userMap = hashMapOf(
                                            "name" to iName,
                                            "email" to email,
                                            "password" to password,
                                            "uid" to uid
                                        )

                                        db.collection("users").document(uid).set(userMap)
                                            .addOnSuccessListener {
                                                // Panggil fungsi createFriendList dengan parameter lengkap
                                                createFriendList(uid, iName)
                                            }
                                            .addOnFailureListener { e ->
                                                // Hapus user auth jika gagal simpan data
                                                user.delete()
                                                Toast.makeText(this, "Failed save data: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(this, "Error: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
        }

        binding.entryIngA1.setOnClickListener {
            startActivity(Intent(this, LogInAct::class.java))
        }
    }

    // Fungsi validasi input yang lebih sederhana
    private fun validateInput(name: String, email: String, password: String): Boolean {
        var isValid = true

        if (name.isBlank()) {
            binding.nulisAiden.error = "Fill the Name!"
            binding.nulisAiden.requestFocus()
            isValid = false
        }
        if (email.isBlank()) {
            binding.nulisEmail.error = "Fill the Email!"
            binding.nulisEmail.requestFocus()
            isValid = false
        }
        if (password.isBlank()) {
            binding.nulisSandi.error = "Fill the Password!"
            binding.nulisSandi.requestFocus()
            isValid = false
        }
        if (name.contains("@") || name.contains(".") || name.isNumber()) {
            binding.nulisAiden.error = "Name is invalid!"
            binding.nulisAiden.requestFocus()
            isValid = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.nulisEmail.error = "Email is invalid!"
            binding.nulisEmail.requestFocus()
            isValid = false
        }
        if (password.length < 6) {
            binding.nulisSandi.error = "Password must be at least 6 characters!"
            binding.nulisSandi.requestFocus()
            isValid = false
        }
        return isValid
    }

    // Fungsi untuk membuat friend list berdasarkan uid
    private fun createFriendList(uid: String, iName: String) {
        // Perbaikan: tambahkan field 'uid' ke dalam friendListMap sesuai rules
        val friendListMap = hashMapOf(
            "name" to iName,
            "friends" to listOf<String>(),
        )

        db.collection("frenlist").document(uid)
            .set(friendListMap)
            .addOnSuccessListener {
                // Navigasi ke MainActivity
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra("EXTRA_USER_INPUT", uid)
                })
                finish()
            }
            .addOnFailureListener { e ->
                // Hapus data user jika gagal bikin frenlist
                db.collection("users").document(uid).delete()
                authSignUp.currentUser?.delete()
                Toast.makeText(this, "Failed made friendlist: ${e.message}", Toast.LENGTH_SHORT).show()
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
