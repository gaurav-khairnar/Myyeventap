package com.example.myyevent

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(private val eventList: MutableList<Event>, private val listener: EventJoinListener) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    interface EventJoinListener {
        fun onJoinEvent(eventId: String)
    }

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.eventNameTextView)
        val descTextView: TextView = view.findViewById(R.id.eventDescriptionTextView)
        val dateTextView: TextView = view.findViewById(R.id.eventDateTextView)
        val locationTextView: TextView = view.findViewById(R.id.eventLocationTextView)
        val eventIdTextView: TextView = view.findViewById(R.id.eventIdTextView) // ✅ Fix: Added missing eventIdTextView
        val joinButton: Button = view.findViewById(R.id.btnJoinEvent) // ✅ Fix: Ensure button exists in XML
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]

        holder.nameTextView.text = event.eventName.ifEmpty { "No Name" }
        holder.descTextView.text = event.eventDescription.ifEmpty { "No Description" }

        holder.locationTextView.text = holder.itemView.context.getString(
            R.string.event_location_placeholder,
            event.eventLocation.ifEmpty { "No Location" }
        )

        val sdf = SimpleDateFormat("EEE, MMM dd yyyy HH:mm", Locale.getDefault())
        val formattedDate = event.timestamp?.toDate()?.let { sdf.format(it) } ?: "No Date"

        holder.dateTextView.text = holder.itemView.context.getString(
            R.string.event_date_placeholder,
            formattedDate
        )

        // ✅ Fix: Display Event ID
        holder.eventIdTextView.text = "Event ID: ${event.eventId}"

        // ✅ Handle Join Button Click
        holder.joinButton.setOnClickListener {
            listener.onJoinEvent(event.eventId)
        }

        // ✅ Handle Click to Open Event Details Page
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, EventDetailsActivity::class.java)
            intent.putExtra("eventId", event.eventId)
            holder.itemView.context.startActivity(intent)
        }
    }

    fun updateEvents(newEvents: List<Event>) {
        eventList.clear()
        eventList.addAll(newEvents)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}