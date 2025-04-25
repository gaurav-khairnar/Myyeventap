package com.example.myyevent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var mobileEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        mobileEditText = findViewById(R.id.mobileEditText)
        ageEditText = findViewById(R.id.ageEditText)
        signupButton = findViewById(R.id.signupButton)
        progressBar = findViewById(R.id.progressBar)

        signupButton.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val mobile = mobileEditText.text.toString().trim()
        val age = ageEditText.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || mobile.isEmpty() || age.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        signupButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUserId = auth.currentUser?.uid
                    if (firebaseUserId != null) {
                        val userId = generateUserId()
                        saveUserData(firebaseUserId, userId, name, email, mobile, age)
                    } else {
                        showError("User creation failed. Please try again.")
                    }
                } else {
                    showError(task.exception?.message ?: "Signup failed")
                }
            }
    }

    private fun saveUserData(firebaseUserId: String, userId: String, name: String, email: String, mobile: String, age: String) {
        val userMap = hashMapOf(
            "userId" to userId,  // ✅ Unique formatted user ID
            "name" to name,
            "email" to email,
            "mobile" to mobile,
            "age" to age
        )

        db.collection("users").document(firebaseUserId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Signup Successful! Please Login.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                showError("Error saving data: ${e.message}")
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.GONE
        signupButton.isEnabled = true
    }

    private fun generateUserId(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = sdf.format(Date())
        val randomPart = UUID.randomUUID().toString().take(6).uppercase()
        return "USER-$date-$randomPart"
    }
}