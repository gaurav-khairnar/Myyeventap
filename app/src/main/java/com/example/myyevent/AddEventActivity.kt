package com.example.myyevent

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private lateinit var editTextEventName: EditText
    private lateinit var editTextEventDescription: EditText
    private lateinit var editTextEventDate: EditText
    private lateinit var editTextEventLocation: EditText
    private lateinit var buttonSaveEvent: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        editTextEventName = findViewById(R.id.editTextEventName)
        editTextEventDescription = findViewById(R.id.editTextEventDescription)
        editTextEventDate = findViewById(R.id.editTextEventDate)
        editTextEventLocation = findViewById(R.id.editTextEventLocation)
        buttonSaveEvent = findViewById(R.id.buttonSaveEvent)

        buttonSaveEvent.setOnClickListener {
            linkTokenAndSaveEvent()
        }
    }

    private fun linkTokenAndSaveEvent() {
        val name = editTextEventName.text.toString().trim()
        val description = editTextEventDescription.text.toString().trim()
        val date = editTextEventDate.text.toString().trim()
        val location = editTextEventLocation.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (name.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔍 Step 1: Check for available token
        db.collection("tokens")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "Available")
            .whereEqualTo("isActive", true)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(this, "No valid token available to create event", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val tokenDoc = querySnapshot.documents.first()
                val tokenId = tokenDoc.getString("tokenId") ?: ""
                val tokenType = tokenDoc.getString("tokenType") ?: "General"

                // Step 2: Generate event and store in Firestore
                val eventId = generateEventId()
                val eventRef = db.collection("events").document(eventId)

                val eventData = hashMapOf(
                    "eventId" to eventId,
                    "name" to name,
                    "description" to description,
                    "date" to date,
                    "location" to location,
                    "organizerId" to userId,
                    "tokenId" to tokenId,
                    "tokenType" to tokenType,
                    "status" to "Ongoing", // Optional field for token regeneration rules
                    "timestamp" to FieldValue.serverTimestamp()
                )

                eventRef.set(eventData)
                    .addOnSuccessListener {
                        // Assign the current user as organizer under the roles subcollection
                        val organizerData = hashMapOf(
                            "role" to "Organizer",
                            "name" to (auth.currentUser?.displayName ?: "Organizer")
                        )
                        eventRef.collection("roles").document(userId).set(organizerData)

                        // Step 3: Update token status and link to event
                        db.collection("tokens").document(tokenId)
                            .update(
                                "status", "Used",
                                "linkedEventId", eventId
                            )

                        Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error adding event", e)
                        Toast.makeText(this, "Failed to add event: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            }
            .addOnFailureListener { e ->
                Log.e("TokenLink", "Failed to get token: ${e.message}")
                Toast.makeText(this, "Token fetch error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun generateEventId(): String {
        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val randomPart = UUID.randomUUID().toString().take(6).uppercase()
        return "EVENT-$timestamp-$randomPart"
    }
}
