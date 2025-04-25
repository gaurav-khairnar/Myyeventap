package com.example.myyevent.models
import com.google.firebase.Timestamp


data class Token(
    val tokenId: String = "",
    val userId: String = "",
    val isActive: Boolean = true,
    val status: String = "",  // ✅ Ensure this exists
//    val expiryDate: String = "",
    val expiryDate: Timestamp? = null,
    val tokenType: String = "",
    var isExpiringSoon: Boolean = false,
    var linkedEventId: String = ""

)