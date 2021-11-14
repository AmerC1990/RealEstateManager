package com.openclassrooms.realestatemanager.ui.activities


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.realestatemanager.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpLoginLayout()
        checkAndRequestLocationPermission()
        FirebaseApp.initializeApp(this)
        redirectIfLoggedIn()
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

        loginAsGuestButton.setOnClickListener {
            loginAsGuest()
        }
    }

    private fun setUpLoginLayout() {
        if (getDeviceInfo(applicationContext, Device.DEVICE_TYPE) == "Tablet") {
            setContentView(R.layout.login_activity_tablet)
        }
        else if (getDeviceInfo(applicationContext, Device.DEVICE_TYPE) == "Mobile") {
            setContentView(R.layout.activity_login)
        }
    }

    enum class Device {
        DEVICE_TYPE
    }

    private fun getDeviceInfo(context: Context, device: Device?): String? {
        try {
            when (device) {
                Device.DEVICE_TYPE -> return if (isTablet(context)) {
                    if (getDevice5Inch(context)) {
                        "Tablet"
                    } else {
                        "Mobile"
                    }
                } else {
                    "Mobile"
                }
                else -> {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getDevice5Inch(context: Context): Boolean {
        return try {
            val displayMetrics: DisplayMetrics = context.resources.displayMetrics
            val yinch = displayMetrics.heightPixels / displayMetrics.ydpi
            val xinch = displayMetrics.widthPixels / displayMetrics.xdpi
            val diagonalinch = Math.sqrt((xinch * xinch + yinch * yinch).toDouble())
            diagonalinch >= 7
        } catch (e: Exception) {
            false
        }
    }

    private fun isTablet(context: Context): Boolean {
        return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
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
    private fun loginAsGuest() {
        loginProgressBar.visibility = View.VISIBLE
        val intent = Intent(this, MainActivity::class.java)
        loginProgressBar.visibility = View.GONE
        startActivity(intent)
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

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun checkAndRequestLocationPermission() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE)
        }
    }

    private fun redirectIfLoggedIn() {
        if(!firebaseAuth.currentUser?.email?.toString().isNullOrEmpty()) {
            login_constraint_layout.visibility = View.INVISIBLE
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("justLoggedIn", "justLoggedIn")
            startActivity(intent)
        }
    }
}

