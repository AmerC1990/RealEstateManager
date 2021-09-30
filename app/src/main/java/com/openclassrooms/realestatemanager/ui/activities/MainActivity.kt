package com.openclassrooms.realestatemanager.ui.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.google.android.material.slider.RangeSlider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.adapters.RecyclerViewAdapter
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.ui.fragments.AllListingsFragment
import com.openclassrooms.realestatemanager.ui.fragments.ListingFormFragment
import com.openclassrooms.realestatemanager.ui.fragments.MapFragment
import com.openclassrooms.realestatemanager.ui.fragments.ViewAndUpdateListingFragment
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.filter_screen.*
import kotlinx.android.synthetic.main.fragment_all_listings.*
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        showOnlineStatus()
        val mapFragment = MapFragment()
        val allListingsFragment = AllListingsFragment()
        val viewAndUpdateListingFragment = ViewAndUpdateListingFragment()
        redirectToLogin()
        isNotificationReceived(allListingsFragment = allListingsFragment, viewAndUpdateListingFragment = viewAndUpdateListingFragment)
        setUpBottomNavClicks(allListingsFragment = allListingsFragment, mapFragment = mapFragment)
    }

    private fun isNotificationReceived(viewAndUpdateListingFragment: ViewAndUpdateListingFragment, allListingsFragment: AllListingsFragment) {
        val id = intent.extras?.getInt("id_from_notification")
        if (id != null && id.toString() != "0") {
            passBundleMakeFragment(viewAndUpdateListingFragment, id.toString())
        } else {
            makeCurrentFragment(allListingsFragment)
        }
    }

    private fun redirectToLogin() {
        if (firebaseAuth.currentUser?.email.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
            commit()
        }
    }

    private fun passBundleMakeFragment(fragment: Fragment, idOrFromLogin: String) {
        val bundle = Bundle()
        bundle.putString("listing_id", idOrFromLogin.toString())
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
            commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val userEmailItem = menu?.findItem(R.id.userEmailItem)
        userEmailItem?.title = firebaseAuth.currentUser?.email?.toString()
        val logoutItem = menu?.findItem(R.id.logoutItem)

        logoutItem?.setOnMenuItemClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val allListingsFragment = AllListingsFragment()
        val listingFormFragment = ListingFormFragment()
        val viewAndUpdateListingFragment = ViewAndUpdateListingFragment()
        if (listingFormFragment.isResumed) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, allListingsFragment)
                commit()
            }
        }
        if (viewAndUpdateListingFragment.isResumed) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, allListingsFragment)
                commit()
            }
        }
    }

    private fun setUpBottomNavClicks(allListingsFragment: Fragment, mapFragment: Fragment) {
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_mapview -> {
                    makeCurrentFragment(mapFragment)
                }
                R.id.ic_listview -> {
                    makeCurrentFragment(allListingsFragment)
                }
            }
            true
        }
    }

    private fun showOnlineStatus() {
        val fromLogin = intent.getStringExtra("justLoggedIn")
        if (fromLogin != null) {
            if (Utils.isOnline(this)) {
                Snackbar
                        .make(mainActivityConstraintLayout, "Connected", Snackbar.LENGTH_SHORT)
                        .withColor(resources.getColor(R.color.myGreen))
                        .show()
            } else if (!Utils.isOnline(this)) {
                Snackbar
                        .make(mainActivityConstraintLayout, "Offline", Snackbar.LENGTH_SHORT)
                        .withColor(resources.getColor(R.color.myRed))
                        .show()
            }
        }
    }

    private fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar {
        this.view.setBackgroundColor(colorInt)
        return this
    }
}