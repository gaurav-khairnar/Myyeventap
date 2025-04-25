package com.example.myyevent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.example.myyevent.EventAdapter
import com.example.myyevent.Event

class MyEventsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_events, container, false)
        recyclerView = view.findViewById(R.id.myEventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        eventAdapter = EventAdapter(mutableListOf(), object : EventAdapter.EventJoinListener {
            override fun onJoinEvent(eventId: String) {
                // Optional: allow edit or view
            }
        })

        recyclerView.adapter = eventAdapter

        fetchMyEvents()
        return view
    }

    private fun fetchMyEvents() {
        if (userId == null) return

        db.collection("events")
            .whereEqualTo("organizerId", userId)
            .get()
            .addOnSuccessListener { docs ->
                val events = docs.mapNotNull { doc ->
                    Event(
                        eventId = doc.getString("eventId") ?: "",
                        eventName = doc.getString("name") ?: "",
                        eventDescription = doc.getString("description") ?: "",
                        eventDate = doc.getString("date") ?: "",
                        eventLocation = doc.getString("location") ?: "",
                        timestamp = doc.getTimestamp("timestamp")
                    )
                }
                eventAdapter.updateEvents(events.toMutableList())
            }
    }
}