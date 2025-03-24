package com.dev.kosongdua

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.kosongdua.adapter.PemesejanAdapter
import com.dev.kosongdua.databinding.GonnaChattingBinding
import com.dev.kosongdua.model.MeMassage
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ChattingAct : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var binding: GonnaChattingBinding
    private val db = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // Variabel untuk menyimpan UID pengguna yang sedang login dan UID teman
    private lateinit var currentUserUid: String
    private lateinit var targetUserUid: String

    private val messageList = mutableListOf<MeMassage>()
    private lateinit var adapter: PemesejanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi sharedPref
        sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)

        // Terapkan tema SEBELUM inflate layout
        if (isDarkMode) {
            setTheme(R.style.Theme_KosongDua_Light)
        } else {
            setTheme(R.style.Theme_KosongDua_Dark)
        }

        binding = GonnaChattingBinding.inflate(layoutInflater) // Inisialisasi View Binding
        setContentView(binding.root)

        // Menerima data dari Intent
        currentUserUid = intent.getStringExtra("CURRENT_USER_UID") ?: ""
        targetUserUid = intent.getStringExtra("USER_UID") ?: ""

        // Ambil nama pengguna berdasarkan UID
        val clickedUserUid = intent.getStringExtra("USER_UID") ?: "Unknown User"
        db.collection("users").document(clickedUserUid).get().addOnSuccessListener { userDoc ->
            if (userDoc.exists()) {
                val userName = userDoc.getString("name") ?: "Unknown User"
                binding.ketikPemesan.text = getString(R.string.testChattin, userName)
            }
        }

        // Inisialisasi adapter dan RecyclerView
        adapter = PemesejanAdapter(messageList, currentUserUid)
        binding.lagiChatting.adapter = adapter
        binding.lagiChatting.layoutManager = LinearLayoutManager(this)

        // Menampilkan UID pengguna yang sedang login
        binding.ketikPemesan.text = getString(R.string.testChattin, currentUserUid)

        // Mengirim userName ke MainActivity saat klik
        binding.ketikPemesan.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("EXTRA_USER_INPUT", currentUserUid) // Mengirim userName
            }
            startActivity(intent)
        }

        // Mengirim pesan
        binding.mauKirim.setOnClickListener {
            val messageText = binding.chatBerpesan.text.toString().trim()

            if (messageText.isEmpty()) {
                // Tampilkan toast jika pesan kosong
                Toast.makeText(this, "Type the chat!", Toast.LENGTH_SHORT).show()

            } else {
                val message = MeMassage(
                    mesejan = messageText,
                    sender = currentUserUid,
                    receiver = targetUserUid,
                    timestamp = System.currentTimeMillis() // Tambahkan timestamp
                )

                // Push ke path yang lebih terstruktur
                database.child("chats").child(generateChatId()).child("messages")
                    .push() // Generate unique key untuk setiap pesan
                    .setValue(message)
                    .addOnSuccessListener {
                        binding.chatBerpesan.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Setup listener untuk mendengarkan pesan baru
        setupMessageListener()
    }

    private fun setupMessageListener() {
        val chatRef = database.child("chats").child(generateChatId()).child("messages")

        // Gunakan ChildEventListener untuk update spesifik
        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(MeMassage::class.java)?.let { newMessage ->
                    messageList.add(newMessage)
                    adapter.notifyItemInserted(messageList.size - 1) // Update spesifik item baru
                    binding.lagiChatting.scrollToPosition(messageList.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Implementasi jika diperlukan untuk menangani perubahan pesan
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Implementasi jika diperlukan untuk menangani penghapusan pesan
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Implementasi jika diperlukan untuk menangani pemindahan pesan
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani error jika diperlukan
            }
        })
    }

    private fun generateChatId(): String {
        return if (currentUserUid < targetUserUid) {
            "${currentUserUid}_$targetUserUid"
        } else {
            "${targetUserUid}_$currentUserUid"
        }
    }
}