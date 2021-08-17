package com.openclassrooms.realestatemanager.ui.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.realestatemanager.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        val animation = AnimationUtils.loadAnimation(this, R.anim.scale_up)

        createAccountButton.setOnClickListener {
            createAccountButton.startAnimation(animation)
            registerUser(email = editTextEnterEmail.text.toString(),
                    password = editTextEnterPassword.text.toString())
        }

        loginButton.setOnClickListener {
            loginButton.startAnimation(animation)
            login()
        }

        forgotPasswordTextView.setOnClickListener {
            forgotPasswordTextView.startAnimation(animation)
            resetPasswordDialog()
        }

    }

    private fun login() {
        val email = editTextEnterEmail.text.toString()
        val password = editTextEnterPassword.text.toString()


        if (isValidEmail(email) && password.isNotEmpty()) {
            loginProgressBar.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()
                        editTextEnterEmail.text?.clear()
                        editTextEnterPassword.text?.clear()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("justLoggedIn", "justLoggedIn")
                        loginProgressBar.visibility = View.GONE
                        startActivity(intent)
                    }
                    else  -> {
                        loginProgressBar.visibility = View.GONE
                        Toast.makeText(this, "Login error: ${it.exception}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        else {
            Toast.makeText(this, "Please enter a valid email address and a password", Toast.LENGTH_LONG).show()
        }

    }

    private fun registerUser(email: String, password: String) {
        if (isValidEmail(email) && password.isNotEmpty()) {
            loginProgressBar.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        loginProgressBar.visibility = View.GONE
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        loginProgressBar.visibility = View.GONE
                        Toast.makeText(this, "Failed creating new account", Toast.LENGTH_LONG).show()
                        editTextEnterEmail.text?.clear()
                        editTextEnterPassword.text?.clear()
                    }
                }
            }
        }
        else {
            Toast.makeText(this, "Please enter a valid email address and a password", Toast.LENGTH_LONG).show()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun resetPasswordDialog() {
        val inputEditTextField = EditText(this)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Please enter email address")
                .setView(inputEditTextField)
                .setCancelable(false)
                .setPositiveButton("Confirm") { _, _ ->
                    if (isValidEmail(inputEditTextField.text.toString())) {
                        firebaseAuth.sendPasswordResetEmail(inputEditTextField.text.toString())
                        Toast.makeText(this, "Reset password sent to ${inputEditTextField.text}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
        val alert = dialogBuilder.create()
        alert.setTitle("Reset Password")
        alert.show()
    }
}

