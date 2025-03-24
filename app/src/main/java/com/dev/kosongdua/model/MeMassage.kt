// File: MeMassage.kt
package com.dev.kosongdua.model

import com.google.firebase.database.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.util.*

@IgnoreExtraProperties
data class MeMassage(
    var mesejan: String = "",
    var sender: String = "",
    var receiver: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    // Properti ini tidak perlu @Exclude karena tidak disimpan di Firebase
    val formattedTime: String
        get() = SimpleDateFormat("HH.mm", Locale.getDefault()).format(Date(timestamp))

    // Properti ini juga tidak perlu @Exclude
    val dayOfWeek: String
        get() = SimpleDateFormat("EEEE", Locale("id", "ID")).format(Date(timestamp))
}