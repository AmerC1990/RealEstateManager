package com.openclassrooms.realestatemanager.ui.activities

import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.os.Bundle
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils

class MainActivity : AppCompatActivity() {
    private var textViewMain: TextView? = null
    private var textViewQuantity: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Bug #1
//        Find view by Id was using a reference to the textview in the second activity
        // which isnt the activity being launched so it was giving us a NPE
        // I just modified the code to use the textview from main activity

//        textViewMain = findViewById(R.id.activity_second_activity_text_view_main)
        textViewMain = findViewById(R.id.activity_main_activity_text_view_main)

        textViewQuantity = findViewById(R.id.activity_main_activity_text_view_quantity)
        configureTextViewMain()
        configureTextViewQuantity()
    }

    private fun configureTextViewMain() {
        textViewMain!!.textSize = 15f
        textViewMain!!.text = "Le premier bien immobilier enregistré vaut "
    }

    private fun configureTextViewQuantity() {
        val quantity = Utils.convertDollarToEuro(100)
        textViewQuantity!!.textSize = 20f
        //  Bug #2
        // setText takes in a string but quantity is an Int so I just converted the
        // Int to a String with Kotlin
        textViewQuantity!!.setText(quantity.toString())

//        textViewQuantity!!.setText(quantity)
    }
}