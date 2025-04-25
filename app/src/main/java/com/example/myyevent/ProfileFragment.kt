package com.example.myyevent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
        val emailTextView = view.findViewById<TextView>(R.id.emailTextView)
        val mobileTextView = view.findViewById<TextView>(R.id.mobileTextView)
        val ageTextView = view.findViewById<TextView>(R.id.ageTextView)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user != null) {
            emailTextView.text = "Email: ${user.email}"

            // Fetch user details from Firestore
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        nameTextView.text = "Name: ${document.getString("name") ?: "N/A"}"
                        mobileTextView.text = "Mobile: ${document.getString("mobile") ?: "N/A"}"
                        ageTextView.text = "Age: ${document.getString("age") ?: "N/A"}"
                    }
                }
                .addOnFailureListener {
                    nameTextView.text = "Name: Error loading"
                    mobileTextView.text = "Mobile: Error loading"
                    ageTextView.text = "Age: Error loading"
                }
        } else {
            nameTextView.text = "Name: Not Logged In"
            emailTextView.text = "Email: Not Logged In"
            mobileTextView.text = "Mobile: Not Logged In"
            ageTextView.text = "Age: Not Logged In"
        }

        // Logout button functionality
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }
}