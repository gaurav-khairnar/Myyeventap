package com.example.myyevent

import com.google.firebase.Timestamp

data class Event(
    val eventId: String = "",  // ✅ Ensure eventId exists
    val eventName: String = "",
    val eventDescription: String = "",
    val eventDate: String = "", // String for UI display
    val eventLocation: String = "",
    val timestamp: Timestamp? = null // Firestore Timestamp for sorting
)
