package com.openclassrooms.realestatemanager.ui.activities


import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.Menu
import android.widget.*
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.databinding.LoanSimulatorScreenBinding
import com.openclassrooms.realestatemanager.ui.fragments.AllListingsFragment
import com.openclassrooms.realestatemanager.ui.fragments.ListingFormFragment
import com.openclassrooms.realestatemanager.ui.fragments.MapFragment
import com.openclassrooms.realestatemanager.ui.fragments.ViewAndUpdateListingFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_all_listings.*

class MainActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var binding: LoanSimulatorScreenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val view = layoutInflater.inflate(R.layout.loan_simulator_screen, null)
        binding = LoanSimulatorScreenBinding.bind(view)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        showOnlineStatus()
        val mapFragment = MapFragment()
        val allListingsFragment = AllListingsFragment()
        val viewAndUpdateListingFragment = ViewAndUpdateListingFragment()
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
            binding?.cancelLoanCalculatorButton?.setOnClickListener {
                popupWindow.dismiss()
            }
            binding?.calculateLoanButton?.setOnClickListener {
                val propertyPrice = binding?.loanPropertyPriceEditText?.text.toString().toInt()
                val downPayment = binding?.loanDownPaymentEditText?.text.toString().toInt()
                val howManyMonths = binding?.loanNumberOfYearsEdittext?.text.toString().toInt() * 12
                val interestRatePercent = binding?.loanInterestRateEditText?.text.toString().toInt()

                val percent = interestRatePercent.toDouble() / 100
                val totalInterestBeingPaid = propertyPrice * percent

                val totalLoanAmount = propertyPrice + totalInterestBeingPaid - downPayment
                val monthlyPayment = totalLoanAmount / howManyMonths
                val finalMonthlyPaymentSummary = euroOrDollar() + monthlyPayment.toString() + "/month for " + howManyMonths.toString() + " months"

                binding?.totalInterestBeingPaidDisplay?.text = euroOrDollar() + totalInterestBeingPaid.toString()
                binding?.totalLoanAmountDisplay?.text = euroOrDollar() + totalLoanAmount.toString()
                binding?.monthlyPaymentDisplay?.text = finalMonthlyPaymentSummary
            }
            true
        }
        return super.onCreateOptionsMenu(menu)
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

        TransitionManager.beginDelayedTransition(allListingsConstraintLayout)
        popupWindow.showAtLocation(
                allListingsConstraintLayout,
                Gravity.CENTER,
                0,
                0
        )
    }

    private fun euroOrDollar(): String {
        if (Utils.doesLocaleSubscribeToEuroCurrency()) {
            return  "\u20ac"
        }
        else {
            return "$"
        }
    }
}