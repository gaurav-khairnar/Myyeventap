package com.example.myyevent

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventDetailsActivity : AppCompatActivity() {

    private lateinit var eventName: TextView
    private lateinit var eventId: TextView
    private lateinit var eventDesc: TextView
    private lateinit var eventDate: TextView
    private lateinit var eventLocation: TextView
    private lateinit var userIdText: TextView
    private lateinit var joinButton: Button
    private lateinit var eventTokenTextView: TextView   // ✅ For token display

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        eventName = findViewById(R.id.eventNameDetailTextView)
        eventId = findViewById(R.id.eventIdDetailTextView)
        eventDesc = findViewById(R.id.eventDescriptionDetailTextView)
        eventDate = findViewById(R.id.eventDateDetailTextView)
        eventLocation = findViewById(R.id.eventLocationDetailTextView)
        userIdText = findViewById(R.id.userIdTextView)
        joinButton = findViewById(R.id.btnJoinEvent)
        eventTokenTextView = findViewById(R.id.eventTokenTextView) // ✅ Bind token TextView

        val eventIdStr = intent.getStringExtra("eventId") ?: return
        loadEventDetails(eventIdStr)

        val user = auth.currentUser
        if (user != null) {
            userIdText.text = "User ID: ${user.uid}"
        }

        joinButton.setOnClickListener {
            joinEvent(eventIdStr)
        }
    }

    private fun loadEventDetails(eventIdStr: String) {
        db.collection("events").document(eventIdStr).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    eventName.text = document.getString("name")
                    eventId.text = "Event ID: $eventIdStr"
                    eventDesc.text = document.getString("description")
                    eventDate.text = document.getString("date")
                    eventLocation.text = document.getString("location")

                    // ✅ Load linked token details
                    loadTokenDetails(eventIdStr)
                }
            }
    }

    private fun loadTokenDetails(eventId: String) {
        db.collection("tokens")
            .whereEqualTo("linkedEventId", eventId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val tokenDoc = documents.first()
                    val tokenId = tokenDoc.getString("tokenId") ?: "N/A"
                    val tokenType = tokenDoc.getString("tokenType") ?: "N/A"

                    eventTokenTextView.text = "Used Token: $tokenId ($tokenType)"
                } else {
                    eventTokenTextView.text = "No token linked with this event."
                }
            }
            .addOnFailureListener {
                eventTokenTextView.text = "Failed to load token info"
            }
    }

    private fun joinEvent(eventId: String) {
        val userId = auth.currentUser?.uid ?: return
        val roleRef = db.collection("events").document(eventId).collection("roles").document(userId)

        roleRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                Toast.makeText(this, "Already joined!", Toast.LENGTH_SHORT).show()
            } else {
                roleRef.set(mapOf("role" to "Audience", "name" to auth.currentUser?.displayName))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Joined event!", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}