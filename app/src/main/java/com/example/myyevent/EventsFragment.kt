package com.example.myyevent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EventsFragment : Fragment(), EventAdapter.EventJoinListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        eventAdapter = EventAdapter(mutableListOf(), this)
        recyclerView.adapter = eventAdapter

        val fabAddEvent: FloatingActionButton = view.findViewById(R.id.fabAddEvent)
        fabAddEvent.setOnClickListener {
            startActivity(Intent(requireContext(), AddEventActivity::class.java))
        }

        fetchEventsFromFirestore()
        return view
    }

    private fun fetchEventsFromFirestore() {
        db.collection("events")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val events = mutableListOf<Event>()
                for (document in documents) {
                    val event = Event(
                        eventId = document.getString("eventId") ?: "",
                        eventName = document.getString("name") ?: "No Name",
                        eventDescription = document.getString("description") ?: "No Description",
                        eventLocation = document.getString("location") ?: "No Location",
                        timestamp = document.getTimestamp("timestamp")
                    )
                    events.add(event)
                }
                eventAdapter.updateEvents(events)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting events", e)
            }
    }

    override fun onJoinEvent(eventId: String) {
        joinEvent(eventId)
    }

    private fun joinEvent(eventId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        val roleRef = db.collection("events").document(eventId)
            .collection("roles").document(userId)

        roleRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                Toast.makeText(requireContext(), "You have already joined this event!", Toast.LENGTH_SHORT).show()
            } else {
                val userData = hashMapOf(
                    "role" to "Audience",
                    "name" to (FirebaseAuth.getInstance().currentUser?.displayName ?: "Unknown User")
                )

                roleRef.set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "You have joined the event!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error joining event", e)
                        Toast.makeText(requireContext(), "Failed to join event: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error checking event role", e)
            Toast.makeText(requireContext(), "Error checking event role: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}