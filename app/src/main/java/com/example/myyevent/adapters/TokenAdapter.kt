package com.example.myyevent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myyevent.models.Token
import com.example.myyevent.R
import java.text.SimpleDateFormat
import java.util.Locale

class TokenAdapter(var tokenList: MutableList<Token>) : RecyclerView.Adapter<TokenAdapter.TokenViewHolder>() {

    class TokenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tokenIdTextView: TextView = itemView.findViewById(R.id.tokenIdTextView)
        val tokenStatusTextView: TextView = itemView.findViewById(R.id.tokenStatusTextView)
        val tokenExpiryTextView: TextView = itemView.findViewById(R.id.tokenExpiryTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_token, parent, false)
        return TokenViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TokenViewHolder, position: Int) {
        val currentToken = tokenList[position]

        holder.tokenIdTextView.text = "Token ID: ${currentToken.tokenId}"
        holder.tokenStatusTextView.text = "Status: ${currentToken.status}"

        // ✅ Format the expiry date if available
        val formattedDate = currentToken.expiryDate?.toDate()?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
        } ?: "N/A"

        holder.tokenExpiryTextView.text = "Expires on: $formattedDate"
    }

    override fun getItemCount(): Int {
        return tokenList.size
    }

    // ✅ Updates the list and refreshes the RecyclerView
    fun updateTokenList(newList: List<Token>) {
        tokenList.clear()
        tokenList.addAll(newList)
        notifyDataSetChanged() // Full refresh (OK for small lists)
    }
}