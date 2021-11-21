package com.openclassrooms.realestatemanager.ui.activities


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.widget.*
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.Utils.euroOrDollar
import com.openclassrooms.realestatemanager.databinding.FilterScreenBinding
import com.openclassrooms.realestatemanager.databinding.LoanSimulatorScreenBinding
import com.openclassrooms.realestatemanager.databinding.LoanSimulatorScreenTabletBinding
import com.openclassrooms.realestatemanager.ui.fragments.AllListingsFragment
import com.openclassrooms.realestatemanager.ui.fragments.ListingFormFragment
import com.openclassrooms.realestatemanager.ui.fragments.MapFragment
import com.openclassrooms.realestatemanager.ui.fragments.ViewAndUpdateListingFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.bottomNavigation
import kotlinx.android.synthetic.main.fragment_all_listings.*
import kotlinx.android.synthetic.main.fragment_all_listings.allListingsConstraintLayout
import kotlinx.android.synthetic.main.fragment_all_listings_tablet.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map_tablet.*
import kotlinx.android.synthetic.main.main_activity_land.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var binding: LoanSimulatorScreenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupMainLayout()
        setupLoanSimulatorLayout()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        showOnlineStatus()
        val mapFragment = MapFragment()
        val allListingsFragment = AllListingsFragment()
        val viewAndUpdateListingFragment = ViewAndUpdateListingFragment()
        makeCurrentFragment(fragmentAllListings = allListingsFragment, fragmentDetails = viewAndUpdateListingFragment)
        isNotificationReceived(viewAndUpdateListingFragment = viewAndUpdateListingFragment)
        setUpBottomNavClicks(allListingsFragment = allListingsFragment, mapFragment = mapFragment, listingDetailsFragment = viewAndUpdateListingFragment)
    }

    private fun setupLoanSimulatorLayout() {
        if (this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val view = layoutInflater.inflate(R.layout.loan_simulator_screen_tablet, null)
            binding = LoanSimulatorScreenBinding.bind(view)
        }
        else if (this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            val view = layoutInflater.inflate(R.layout.loan_simulator_screen_tablet_landscape, null)
            binding = LoanSimulatorScreenBinding.bind(view)
        }
        else if (this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Mobile") {
            val view = layoutInflater.inflate(R.layout.loan_simulator_screen, null)
            binding = LoanSimulatorScreenBinding.bind(view)
        }
    }

    private fun setupMainLayout() {
        if (this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.main_activity_land)
        }
        else  {
            setContentView(R.layout.activity_main)
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

    private fun isNotificationReceived(viewAndUpdateListingFragment: ViewAndUpdateListingFragment) {
        if (this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val bundle = Bundle()
            val id = intent.extras?.getInt("id_from_notification")
            if (id != null && id.toString() != "0") {
                bundle.putString("listing_id", id.toString())
                viewAndUpdateListingFragment.arguments = bundle
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.containerForListingDetails, viewAndUpdateListingFragment)
                    commit()
                }
            }
        } else {
            val bundle = Bundle()
            val id = intent.extras?.getInt("id_from_notification")
            if (id != null && id.toString() != "0") {
                bundle.putString("listing_id", id.toString())
                viewAndUpdateListingFragment.arguments = bundle
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragmentContainer, viewAndUpdateListingFragment)
                    commit()
                }
            }
        }
    }

    private fun makeCurrentFragment(fragmentAllListings: Fragment, fragmentDetails: Fragment) {
        if (this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.containerForAllListings, fragmentAllListings)
                commit()
            }
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.containerForListingDetails, fragmentDetails)
                commit()
            }
        }
        else  {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, fragmentAllListings)
                commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val userEmailItem = menu?.findItem(R.id.userEmailItem)
        userEmailItem?.title = firebaseAuth.currentUser?.email?.toString() ?: "Guest"
        val logoutItem = menu?.findItem(R.id.logoutItem)
        val loanSimulatorItem = menu?.findItem(R.id.loanSimulatorItem)
        if (firebaseAuth.currentUser?.email?.toString().isNullOrEmpty()) {
            logoutItem?.title = "Login"
        }
        else if(!firebaseAuth.currentUser?.email?.toString().isNullOrEmpty()) {
            logoutItem?.title = "Logout"
        }

        logoutItem?.setOnMenuItemClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            true
        }

        loanSimulatorItem?.setOnMenuItemClickListener {
            val popupWindow = PopupWindow(
                        binding?.root,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                )

            setUpPopupWindow(popupWindow)
            popupWindow.isFocusable = true
            setUpLoanHints()
            popupWindow.update()
            clearInput()

            binding?.cancelLoanCalculatorButton?.setOnClickListener {
                popupWindow.dismiss()
            }
            binding?.calculateLoanButton?.setOnClickListener {
                if (binding?.loanPropertyPriceEditText?.text.isNullOrEmpty() ||
                        binding?.loanDownPaymentEditText?.text.isNullOrEmpty() ||
                        binding?.loanNumberOfYearsEdittext?.text.isNullOrEmpty() ||
                        binding?.loanInterestRateEditText?.text.isNullOrEmpty()) {
                    Toast.makeText(this, "Please enter all inputs", Toast.LENGTH_LONG).show()
                }
                else {
                    val propertyPrice = binding?.loanPropertyPriceEditText?.text.toString().toDouble()
                    val downPayment = binding?.loanDownPaymentEditText?.text.toString().toDouble()
                    val howManyMonths = binding?.loanNumberOfYearsEdittext?.text.toString().toDouble() * 12
                    val interestRatePercent = binding?.loanInterestRateEditText?.text.toString().toDouble()

                    val percent = interestRatePercent / 100
                    val totalInterestBeingPaid = propertyPrice * percent

                    val totalLoanAmount = propertyPrice + totalInterestBeingPaid - downPayment
                    val monthlyPayment = totalLoanAmount / howManyMonths
                    val decimalFormat = DecimalFormat("#.##")
                    decimalFormat.roundingMode = RoundingMode.CEILING
                    val finalMonthlyPaymentSummary = euroOrDollar() + decimalFormat.format(monthlyPayment).toString() + "/month for " + decimalFormat.format(howManyMonths).toString() + " months"

                    binding?.totalInterestBeingPaidDisplay?.text = euroOrDollar() + decimalFormat.format(totalInterestBeingPaid).toString()
                    binding?.totalLoanAmountDisplay?.text = euroOrDollar() + decimalFormat.format(totalLoanAmount).toString()
                    binding?.monthlyPaymentDisplay?.text = finalMonthlyPaymentSummary
                }
            }
            true
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun clearInput() {
        binding?.totalLoanAmountDisplay?.text = ""
       binding?.monthlyPaymentDisplay?.text = ""
        binding?.totalInterestBeingPaidDisplay?.text = ""
        binding?.loanInterestRateEditText?.text?.clear()
        binding?.loanNumberOfYearsEdittext?.text?.clear()
        binding?.loanDownPaymentEditText?.text?.clear()
       binding?.loanPropertyPriceEditText?.text?.clear()
    }

    private fun setUpLoanHints() {
        if (Utils.doesLocaleSubscribeToEuroCurrency()) {
            binding?.loanPropertyPriceEditText?.hint = R.string.price_of_property_euro.toString()
            binding?.loanDownPaymentEditText?.hint = R.string.down_payment_euro.toString()
        }
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

    private fun setUpBottomNavClicks(allListingsFragment: Fragment, mapFragment: Fragment, listingDetailsFragment: Fragment) {
        bottomNavigation.setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.ic_mapview -> {
                        makeCurrentFragment(fragmentAllListings = mapFragment, fragmentDetails = listingDetailsFragment)
                    }
                    R.id.ic_listview -> {
                        makeCurrentFragment(fragmentAllListings = allListingsFragment, fragmentDetails = listingDetailsFragment)
                    }
                }
                true
            }
    }

    private fun showOnlineStatus() {
        val fromLogin = intent.getStringExtra("justLoggedIn")
        if (fromLogin != null) {
            if (this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                    resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (Utils.isOnline(this)) {
                    Snackbar
                            .make(mainActivityConstraintLayoutLandscape, "Connected", Snackbar.LENGTH_SHORT)
                            .withColor(resources.getColor(R.color.myGreen))
                            .show()
                } else if (!Utils.isOnline(this)) {
                    Snackbar
                            .make(mainActivityConstraintLayoutLandscape, "Offline", Snackbar.LENGTH_SHORT)
                            .withColor(resources.getColor(R.color.myRed))
                            .show()
                }
            }
            else  {
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

    }

    private fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar {
        this.view.setBackgroundColor(colorInt)
        return this
    }

    private fun setUpPopupWindow(popupWindow: PopupWindow) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10.0F
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow.enterTransition = slideIn

            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut
        }

        if(this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Mobile"){
            Log.d("debuggerz", "line 357")
                TransitionManager.beginDelayedTransition(mainActivityConstraintLayout)
                popupWindow.showAtLocation(
                        mainActivityConstraintLayout,
                        Gravity.CENTER,
                        0,
                        0
                )

        }else if (this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                this.findViewById<ConstraintLayout>(R.id.allListingsConstraintLayout) != null) {
            Log.d("debuggerz", "line 368")
                        TransitionManager.beginDelayedTransition(allListingsConstraintLayout)
                        popupWindow.showAtLocation(
                                allListingsConstraintLayout,
                                Gravity.CENTER,
                                0,
                                0
                        )
        }
        else if (this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                this.findViewById<FrameLayout>(R.id.mainActivityConstraintLayout) != null) {
            Log.d("debuggerz", "line 379")
            TransitionManager.beginDelayedTransition(mainActivityConstraintLayout)
            popupWindow.showAtLocation(
                    allListingsConstraintLayout,
                    Gravity.CENTER,
                    0,
                    0
            )
        }
        else if (this.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                this.findViewById<ConstraintLayout>(R.id.mapFragmentTablet) != null) {
            Log.d("debuggerz", "this is the one")
            TransitionManager.beginDelayedTransition(mapFragmentTablet)
            popupWindow.showAtLocation(
                    mapFragmentTablet,
                    Gravity.CENTER,
                    0,
                    0
            )
        }


    }
}