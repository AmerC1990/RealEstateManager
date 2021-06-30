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
import androidx.fragment.app.Fragment
import com.google.android.material.slider.RangeSlider
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.adapters.RecyclerViewAdapter
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.ui.fragments.AllListingsFragment
import com.openclassrooms.realestatemanager.ui.fragments.ViewAndUpdateListingFragment
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.filter_screen.*
import org.koin.android.ext.android.inject
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val allListingsFragment = AllListingsFragment()
        val viewAndUpdateListingsFragment = ViewAndUpdateListingFragment()
        val id = intent.extras?.getInt("id_from_notification")
        if (id != null) {
            passBundleMakeFragment(viewAndUpdateListingsFragment, id.toString())
        } else {
            makeCurrentFragment(allListingsFragment)
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putString("justLoggedIn", "justLoggedIn")
        fragment.arguments = bundle
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

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu, menu)
//        val filterIcon = menu?.findItem(R.id.action_filter)
//        val userEmailItem = menu?.findItem(R.id.userEmailItem)
//        userEmailItem?.title = firebaseAuth.currentUser?.email?.toString()
//        val logoutItem = menu?.findItem(R.id.logoutItem)
//
//        logoutItem?.setOnMenuItemClickListener {
//            firebaseAuth.signOut()
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            true
//        }
//        filterIcon?.setOnMenuItemClickListener {
//            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            val view = inflater.inflate(R.layout.filter_screen,null)
//            val popupWindow = PopupWindow(
//                    view, // Custom view to show in popup window
//                    LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
//                    LinearLayout.LayoutParams.MATCH_PARENT // Window height
//            )
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                popupWindow.elevation = 10.0F
//            }
//
//
//            // If API level 23 or higher then execute the code
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//                // Create a new slide animation for popup window enter transition
//                val slideIn = Slide()
//                slideIn.slideEdge = Gravity.TOP
//                popupWindow.enterTransition = slideIn
//
//                // Slide animation for popup window exit transition
//                val slideOut = Slide()
//                slideOut.slideEdge = Gravity.RIGHT
//                popupWindow.exitTransition = slideOut
//            }
//            val filterTitleTextview = view.findViewById<TextView>(R.id.titleTextview)
//            filterTitleTextview.paintFlags = Paint.UNDERLINE_TEXT_FLAG
//            val buttonApply = view.findViewById<Button>(R.id.button_apply)
//            val buttonClose = view.findViewById<Button>(R.id.button_close)
//            val priceSlider = view.findViewById<RangeSlider>(R.id.price_slider)
//            val surfaceAreaSlider = view.findViewById<RangeSlider>(R.id.surfaceArea_slider)
//            val photoSlider = view.findViewById<RangeSlider>(R.id.numberOfPhotos_slider)
//            val beenOnMarketSinceDatePickerImageView = view.findViewById<ImageView>(R.id.beenOnMarketSinceDatePicker)
//            val beenOnMarketTextview = view.findViewById<TextView>(R.id.beenOnMarketDateTextview)
//            val beenSoldSinceDatePickerImageView = view.findViewById<ImageView>(R.id.beenSoldSinceDatePicker)
//            val beenSoldSinceTextview = view.findViewById<TextView>(R.id.beenSoldSinceTextview)
//            val barCheckbox = view.findViewById<CheckBox>(R.id.barCheckbox)
//            val schoolCheckbox = view.findViewById<CheckBox>(R.id.schoolCheckbox)
//            val parkCheckbox = view.findViewById<CheckBox>(R.id.parkCheckbox)
//            val restaurantCheckbox = view.findViewById<CheckBox>(R.id.restaurantCheckbox)
//            val hospitalCheckbox = view.findViewById<CheckBox>(R.id.hospitalCheckbox)
//            val enterLocationEditText = view.findViewById<EditText>(R.id.editTextEnterLocation)
//            val priceMinText = view.findViewById<TextView>(R.id.priceMinTextview)
//            val priceMaxText = view.findViewById<TextView>(R.id.priceMaxTextview)
//            val surfaceMinText = view.findViewById<TextView>(R.id.surfaceMinTextview)
//            val surfaceMaxText = view.findViewById<TextView>(R.id.surfaceMaxTextview)
//            val photosMixText = view.findViewById<TextView>(R.id.photosMinTextview)
//            val photosMaxText = view.findViewById<TextView>(R.id.photosMaxTextview)
//
//            popupWindow.setFocusable(true);
//            popupWindow.update();
//            buttonClose.setOnClickListener{
//                // Dismiss the popup window
//                popupWindow.dismiss()
//            }
//
//            val c = Calendar.getInstance()
//            val year = c.get(Calendar.YEAR)
//            val month = c.get(Calendar.MONTH)
//            val day = c.get(Calendar.DAY_OF_MONTH)
//
//            beenOnMarketSinceDatePickerImageView.setOnClickListener {
//                val datePickerDialog = DatePickerDialog(
//                        this,
//                        DatePickerDialog.OnDateSetListener { view, Year, Month, Day ->
//                            beenOnMarketTextview.visibility = View.VISIBLE
//                            var month = Month.toString()
//                            var day = Day.toString()
//                            if (Month.toString().length < 2) {
//                                month = "0$Month"
//                            }
//                            if (Day.toString().length < 2) {
//                                day = "0$Day"
//                            }
//                            beenOnMarketTextview.text = "$month/$day/$Year"
//                        },
//                        year,
//                        month,
//                        day
//                )
//                datePickerDialog.show()
//            }
//
//            beenSoldSinceDatePickerImageView.setOnClickListener {
//                val datePickerDialog = DatePickerDialog(
//                        this,
//                        DatePickerDialog.OnDateSetListener { view, Year, Month, Day ->
//                            beenSoldSinceTextview.visibility = View.VISIBLE
//                            var month = Month.toString()
//                            var day = Day.toString()
//                             if (Month.toString().length < 2) {
//                                month = "0$Month"
//                             }
//                            if (Day.toString().length < 2) {
//                                day = "0$Day"
//                            }
//                            beenSoldSinceTextview.text = "$month/$day/$Year"
//                        },
//                        year,
//                        month,
//                        day
//                )
//                datePickerDialog.show()
//            }
//
//            priceSlider.addOnChangeListener{ rangeSlider: RangeSlider, fl: Float, b: Boolean ->
//                priceMinText.text = rangeSlider.values[0].toString()
//                priceMaxText.text = rangeSlider.values[1].toString()
//            }
//
//            surfaceAreaSlider.addOnChangeListener{ rangeSlider: RangeSlider, fl: Float, b: Boolean ->
//                surfaceMinText.text = rangeSlider.values[0].toString()
//                surfaceMaxText.text = rangeSlider.values[1].toString()
//            }
//
//            photoSlider.addOnChangeListener{ rangeSlider: RangeSlider, fl: Float, b: Boolean ->
//                photosMixText.text = rangeSlider.values[0].toString()
//                photosMaxText.text = rangeSlider.values[1].toString()
//            }
//
//            buttonApply.setOnClickListener {
//                if (viewModel.uiState.value is ListingsViewModel.ListingState.Success) {
//                    val filteredData = (viewModel.uiState.value as ListingsViewModel.ListingState.Success)
//                            .listing.filter {
//                        it.price.toInt() >= priceMinText.text.toString().toInt() &&
//                                it.price.toInt() <= priceMaxText.text.toString().toInt()
//                    }
//                    recyclerViewAdapter.setListData(filteredData as ArrayList<ListingEntity>)
//                    recyclerViewAdapter.notifyDataSetChanged()
//                }
//
//
//             }
//
//
//            TransitionManager.beginDelayedTransition(mainActivityConstraintLayout)
//            popupWindow.showAtLocation(
//                    mainActivityConstraintLayout, // Location to display popup window
//                    Gravity.CENTER, // Exact position of layout to display popup
//                    0, // X offset
//                    0 // Y offset
//            )
//          true
//        }
//        return super.onCreateOptionsMenu(menu)
//    }
}