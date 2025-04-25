package com.example.myyevent

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myyevent.models.Token
import com.example.myyevent.adapters.TokenAdapter
import java.util.Calendar
import java.util.UUID

class TokenDashboardFragment : Fragment(R.layout.fragment_token_dashboard) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TokenAdapter
    private var fullTokenList = mutableListOf<Token>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.tokenRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TokenAdapter(mutableListOf())
        recyclerView.adapter = adapter

        val generateButton: Button = view.findViewById(R.id.btnGenerateToken)
        generateButton.setOnClickListener {
            generateTokenIfAllowed()
        }

        // 🔘 Filter Buttons
        view.findViewById<Button>(R.id.btnShowAll).setOnClickListener { filterTokens("ALL") }
        view.findViewById<Button>(R.id.btnShowActive).setOnClickListener { filterTokens("ACTIVE") }
        view.findViewById<Button>(R.id.btnShowInactive).setOnClickListener { filterTokens("INACTIVE") }

        fetchTokens()
    }

    private fun fetchTokens() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("tokens")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val newTokenList = mutableListOf<Token>()

                for (document in documents) {
                    val token = document.toObject(Token::class.java)

                    // 🟨 Check if token expires in next 5 days
                    val expiry = token.expiryDate?.toDate()
                    if (expiry != null) {
                        val fiveDaysFromNow = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, 5)
                        }.time
                        token.isExpiringSoon = expiry.before(fiveDaysFromNow)
                    }

                    newTokenList.add(token)
                }

                fullTokenList = newTokenList
                adapter.updateTokenList(fullTokenList) // Show all tokens by default
            }
    }

    private fun filterTokens(filter: String) {
        val filteredList = when (filter) {
            "ACTIVE" -> fullTokenList.filter { it.isActive }
            "INACTIVE" -> fullTokenList.filter { !it.isActive }
            else -> fullTokenList
        }
        adapter.updateTokenList(filteredList)
    }

    private fun generateTokenIfAllowed() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val activeTokens = fullTokenList.filter { it.status == "Available" && it.isActive }

        if (activeTokens.size >= 2) {
            Toast.makeText(requireContext(), "You already have 2 active tokens!", Toast.LENGTH_SHORT).show()
            return
        }

        val generatedTokenId = UUID.randomUUID().toString().take(8)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val expiryDate = Timestamp(calendar.time)

        val tokenData = hashMapOf(
            "tokenId" to generatedTokenId,
            "userId" to userId,
            "isActive" to true,
            "status" to "Available",
            "tokenType" to "General",
            "expiryDate" to expiryDate,
            "linkedEventId" to ""
        )

        FirebaseFirestore.getInstance().collection("tokens")
            .document(generatedTokenId)
            .set(tokenData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Token Generated!", Toast.LENGTH_SHORT).show()
                fetchTokens()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to generate token", Toast.LENGTH_SHORT).show()
            }
    }
}