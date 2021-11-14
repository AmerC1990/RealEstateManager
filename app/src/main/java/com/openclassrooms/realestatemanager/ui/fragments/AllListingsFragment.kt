package com.openclassrooms.realestatemanager.ui.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.slider.RangeSlider
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.adapters.RecyclerViewAdapter
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.filter.FilterParams
import com.openclassrooms.realestatemanager.filter.SearchParams
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.android.synthetic.main.fragment_all_listings.*
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.sharedViewModel

import java.util.*

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.realestatemanager.Utils.euroOrDollar
import com.openclassrooms.realestatemanager.adapters.RecyclerViewAdapterTablet
import com.openclassrooms.realestatemanager.databinding.FilterScreenBinding
import com.openclassrooms.realestatemanager.databinding.FilterScreenTabletBinding
import com.openclassrooms.realestatemanager.ui.activities.LoginActivity
import com.openclassrooms.realestatemanager.ui.activities.MainActivity
import kotlinx.coroutines.delay
import kotlin.collections.ArrayList

class AllListingsFragment : Fragment() {
    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    lateinit var recyclerViewAdapterTablet: RecyclerViewAdapterTablet

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val viewModel by sharedViewModel<ListingsViewModel>()
    private var binding: FilterScreenBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(false)
        setupFilterScreenLayout(inflater)
        return inflater.inflate(R.layout.fragment_all_listings, container, false)
    }

    private fun setupFilterScreenLayout(inflater: LayoutInflater) {
        if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val view = inflater.inflate(R.layout.filter_screen_tablet, null)
            binding = FilterScreenBinding.bind(view)
        }
        else if(activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val view = inflater.inflate(R.layout.filter_screen_tablet_landscape, null)
            binding = FilterScreenBinding.bind(view)
        }
        else if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Mobile") {
            val view = inflater.inflate(R.layout.filter_screen, null)
            binding = FilterScreenBinding.bind(view)
        }
    }

    override fun onStart() {
        super.onStart()
        overrideOnBackPressed()
        isUserOnline()
        handleGuestUser()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerview()
        attachObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        viewModel.filter(viewModel._filterParams.value)
        val filterIcon = menu.findItem(R.id.action_filter)
        filterIcon?.setOnMenuItemClickListener {

            val popupWindow = PopupWindow(
                    binding?.root,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            )

            setUpPopupWindow(popupWindow)
            setUpSpinners()

            popupWindow.isFocusable = true
            popupWindow.update()
            binding?.buttonClose?.setOnClickListener {
                popupWindow.dismiss()
            }
            setUpDatePickers()

            setUpSliders()

            binding?.buttonApply?.setOnClickListener {
                applyFilters()
                popupWindow.dismiss()
            }
            binding?.resetFiltersTextview?.setOnClickListener {
                viewModel.clearFilters()
                binding?.barCheckbox?.isSelected = false
                binding?.schoolCheckbox?.isSelected = false
                binding?.parkCheckbox?.isSelected = false
                binding?.restaurantCheckbox?.isSelected = false
                binding?.hospitalCheckbox?.isSelected = false
                binding?.editTextEnterLocation?.setText("")
                binding?.editTextEnterLocation?.setHint(R.string.city_state_or_country)
                binding?.beenOnMarketDateTextview?.visibility = View.INVISIBLE
                binding?.beenOnMarketDateTextview?.text = R.string.date.toString()
                binding?.beenSoldSinceTextview?.visibility = View.INVISIBLE
                binding?.beenSoldSinceTextview?.text = R.string.date.toString()

                popupWindow.dismiss()
            }
            true
        }
        setUpSearchView(menu)
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

    private fun applyFilters() {
        if (viewModel.uiState.value is ListingsViewModel.ListingState.Success) {
            val minimumPhotos = binding?.photosMinTextview?.text.toString().substringBefore(".").toInt()
            val maximumPhotos = binding?.photosMaxTextview?.text.toString().substringBefore(".").toInt()
            var typeOfListingText = binding?.typeOfPropertySpinnerFilter?.selectedItem.toString()
            var statusOfPropertyText = binding?.statusOfPropertySpinnerFilter?.selectedItem.toString()

            if (typeOfListingText.contains("Select")) {
                typeOfListingText = ""
            }
            if (statusOfPropertyText.contains("Select")) {
                statusOfPropertyText = ""
            }

            val filterParams = FilterParams(minPrice = binding?.priceMinTextview?.text.toString().toDouble(),
                    maxPrice = binding?.priceMaxTextview?.text.toString().toDouble(),
                    minSurfaceArea = binding?.surfaceMinTextview?.text.toString().toDouble(),
                    maxSurfaceArea = binding?.surfaceMaxTextview?.text.toString().toDouble(),
                    minNumberOfPhotos = minimumPhotos.toDouble(),
                    maxNumberOfPhotos = maximumPhotos.toDouble(),
                    status = statusOfPropertyText,
                    type = typeOfListingText,
                    onMarketSince = binding?.beenOnMarketDateTextview?.text.toString(),
                    soldSince = binding?.beenSoldSinceTextview?.text.toString(),
                    pointsOfInterest = getCheckedCategories(),
                    location = binding?.editTextEnterLocation?.text.toString())

            viewModel.filter(filterParams = filterParams)
        }
    }

    private fun setUpSliders() {
        binding?.priceSliderTitle?.text = getString(R.string.price) + " (" + euroOrDollar() + ")"
        binding?.priceSlider?.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
            binding?.priceMinTextview?.text = rangeSlider.values[0].toString() + "0"
            binding?.priceMaxTextview?.text = rangeSlider.values[1].toString() + "0"
        }
        binding?.surfaceAreaSlider?.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
            binding?.surfaceMinTextview?.text = rangeSlider.values[0].toString().substringBefore(".")
            binding?.surfaceMaxTextview?.text = rangeSlider.values[1].toString().substringBefore(".")
        }
        binding?.numberOfPhotosSlider?.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
            binding?.photosMinTextview?.text = rangeSlider.values[0].toString().substringBefore(".")
            binding?.photosMaxTextview?.text = rangeSlider.values[1].toString().substringBefore(".")
        }
    }

    private fun setUpDatePickers() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        binding?.beenOnMarketSinceDatePicker?.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { view, Year, Month, Day ->
                        binding?.beenOnMarketDateTextview?.visibility = View.VISIBLE
                        var myMonth = (Month + 1).toString()
                        var myDay = Day.toString()
                        if (myMonth.length < 2) {
                            myMonth = "0$myMonth"
                        }
                        if (myDay.length < 2) {
                            myDay = "0$myDay"
                        }
                        binding?.beenOnMarketDateTextview?.text = "   $myMonth/$myDay/$Year"
                    },
                    year,
                    month,
                    day
            )
            datePickerDialog.show()
        }

        binding?.beenSoldSinceDatePicker?.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { view, Year, Month, Day ->
                        binding?.beenSoldSinceTextview?.visibility = View.VISIBLE
                        var myMonth = (Month + 1).toString()
                        var myDay = Day.toString()
                        if (myMonth.length < 2) {
                            myMonth = "0$myMonth"
                        }
                        if (myDay.length < 2) {
                            myDay = "0$myDay"
                        }
                        binding?.beenSoldSinceTextview?.text = "   $myMonth/$myDay/$Year"
                    },
                    year,
                    month,
                    day
            )
            datePickerDialog.show()
        }
    }

    private fun setUpSpinners() {
        val types = resources.getStringArray(R.array.type_of_listing_spinner)
        val statuses = resources.getStringArray(R.array.status_of_property_spinner)

        val typeAdapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, types)
        binding?.typeOfPropertySpinnerFilter?.adapter = typeAdapter

        val statusAdapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, statuses)
        binding?.statusOfPropertySpinnerFilter?.adapter = statusAdapter
    }

    private fun setUpSearchView(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView: androidx.appcompat.widget.SearchView = searchItem?.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Search Listings"
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (viewModel.uiState.value is ListingsViewModel.ListingState.Success) {
                    val searchParams = SearchParams(searchView.query.toString())
                    viewModel.search(searchParams)
                }
                return false
            }
        }
        )
    }
    private fun attachObservers() {
        lifecycleScope.launchWhenCreated {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is ListingsViewModel.ListingState.Loading -> {
                        if (allListingsProgressBar != null) {
                            allListingsProgressBar.visibility = View.VISIBLE
                        }
                    }
                    is ListingsViewModel.ListingState.Success -> {
                        if (allListingsProgressBar != null) {
                            allListingsProgressBar.visibility = View.GONE
                        }
                        if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            recyclerViewAdapterTablet.setListData(uiState.listing as ArrayList<ListingEntity>)
                            recyclerViewAdapterTablet.notifyDataSetChanged()
                        }
                        else  {
                            recyclerViewAdapter.setListData(uiState.listing as ArrayList<ListingEntity>)
                            recyclerViewAdapter.notifyDataSetChanged()
                        }


                    }
                    is ListingsViewModel.ListingState.Error -> {
                        if (allListingsProgressBar != null) {
                            allListingsProgressBar.visibility = View.GONE
                        }
                        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun changeFragment(fragment: Fragment) {
        if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.containerForAllListings, fragment)
                        .addToBackStack(null)
                commit()
            }
        }
        else {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                commit()
            }
        }

    }

    private fun initRecyclerview() {
        if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                recyclerViewAdapter = RecyclerViewAdapter()
                recyclerViewAdapterTablet = RecyclerViewAdapterTablet()
                adapter = recyclerViewAdapterTablet
                val divider = DividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
                divider.setDrawable(context.resources.getDrawable(R.drawable.list_divider))
                addItemDecoration(divider)
            }
        }
        else {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                recyclerViewAdapterTablet = RecyclerViewAdapterTablet()
                recyclerViewAdapter = RecyclerViewAdapter()
                adapter = recyclerViewAdapter
                val divider = DividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
                divider.setDrawable(context.resources.getDrawable(R.drawable.list_divider))
                addItemDecoration(divider)
            }
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

    private fun overrideOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback {
        }
    }

    private fun getCheckedCategories(): List<String> = listOfNotNull(
            "Bar".takeIf { binding!!.barCheckbox.isChecked },
            "School".takeIf { binding!!.schoolCheckbox.isChecked },
            "Park".takeIf { binding!!.parkCheckbox.isChecked },
            "Restaurant".takeIf { binding!!.restaurantCheckbox.isChecked },
            "Hospital".takeIf { binding!!.hospitalCheckbox.isChecked }
    )

    private fun isUserOnline() {
        val listingFormFragment = ListingFormFragment()
        if (Utils.isOnline(requireContext())) {
            addListingButton.setOnClickListener {
                changeFragment(listingFormFragment)
            }
        } else if (!Utils.isOnline(requireContext())) {
            addListingButton.visibility = View.GONE
        }
    }

    private fun handleGuestUser() {
        if (firebaseAuth.currentUser?.email.isNullOrEmpty()) {
            addListingButton.visibility = View.GONE
        }
    }
}