package com.openclassrooms.realestatemanager.ui.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.slider.RangeSlider
import com.google.android.material.textview.MaterialTextView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.adapters.RecyclerViewAdapter
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.filter.FilterContext
import com.openclassrooms.realestatemanager.filter.FilterParams
import com.openclassrooms.realestatemanager.filter.PriceStrategy
import com.openclassrooms.realestatemanager.filter.SurfaceStrategy
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.android.synthetic.main.filter_screen.*
import kotlinx.android.synthetic.main.fragment_all_listings.*
import kotlinx.android.synthetic.main.fragment_listing_form.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class AllListingsFragment : Fragment() {
    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private val viewModel: ListingsViewModel by inject()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(false)
        return inflater.inflate(R.layout.fragment_all_listings, container, false)
    }

    override fun onStart() {
        super.onStart()
        overrideOnBackPressed()
        val listingFormFragment = ListingFormFragment()
        if (Utils.isOnline(requireContext())) {
            addListingButton.setOnClickListener {
                changeFragment(listingFormFragment)
            }
        } else if (!Utils.isOnline(requireContext())) {
            addListingButton.visibility = View.GONE
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerview()
        attachObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val filterIcon = menu.findItem(R.id.action_filter)
        filterIcon?.setOnMenuItemClickListener {
            val inflater: LayoutInflater = activity?.layoutInflater as LayoutInflater
            val view = inflater.inflate(R.layout.filter_screen, null)
            val popupWindow = PopupWindow(
                    view, // Custom view to show in popup window
                    LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                    LinearLayout.LayoutParams.MATCH_PARENT // Window height
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }


            // If API level 23 or higher then execute the code
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut
            }

            // TODO() ViewBinding
            val typeOfListingSpinner = view.findViewById<Spinner>(R.id.typeOfPropertySpinnerFilter)
            val statusOfPropertySpinner = view.findViewById<Spinner>(R.id.statusOfPropertySpinnerFilter)
            val buttonApply = view.findViewById<Button>(R.id.button_apply)
            val buttonClose = view.findViewById<Button>(R.id.button_close)
            val priceSlider = view.findViewById<RangeSlider>(R.id.price_slider)
            val surfaceAreaSlider = view.findViewById<RangeSlider>(R.id.surfaceArea_slider)
            val photoSlider = view.findViewById<RangeSlider>(R.id.numberOfPhotos_slider)
            val beenOnMarketSinceDatePickerImageView = view.findViewById<ImageView>(R.id.beenOnMarketSinceDatePicker)
            val beenOnMarketTextview = view.findViewById<MaterialTextView>(R.id.beenOnMarketDateTextview)
            val beenSoldSinceDatePickerImageView = view.findViewById<ImageView>(R.id.beenSoldSinceDatePicker)
            val beenSoldSinceTextview = view.findViewById<MaterialTextView>(R.id.beenSoldSinceTextview)
            val barCheckbox = view.findViewById<CheckBox>(R.id.barCheckbox)
            val schoolCheckbox = view.findViewById<CheckBox>(R.id.schoolCheckbox)
            val parkCheckbox = view.findViewById<CheckBox>(R.id.parkCheckbox)
            val restaurantCheckbox = view.findViewById<CheckBox>(R.id.restaurantCheckbox)
            val hospitalCheckbox = view.findViewById<CheckBox>(R.id.hospitalCheckbox)
            val enterLocationEditText = view.findViewById<EditText>(R.id.editTextEnterLocation)
            val priceMinText = view.findViewById<MaterialTextView>(R.id.priceMinTextview)
            val priceMaxText = view.findViewById<MaterialTextView>(R.id.priceMaxTextview)
            val surfaceMinText = view.findViewById<MaterialTextView>(R.id.surfaceMinTextview)
            val surfaceMaxText = view.findViewById<MaterialTextView>(R.id.surfaceMaxTextview)
            val photosMixText = view.findViewById<MaterialTextView>(R.id.photosMinTextview)
            val photosMaxText = view.findViewById<MaterialTextView>(R.id.photosMaxTextview)

            val types = resources.getStringArray(R.array.type_of_listing_spinner)
            val statuses = resources.getStringArray(R.array.status_of_property_spinner)

            val typeAdapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, types)
            typeOfListingSpinner.adapter = typeAdapter


            val statusAdapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, statuses)
            statusOfPropertySpinner.adapter = statusAdapter

            popupWindow.isFocusable = true
            popupWindow.update()
            buttonClose.setOnClickListener {
                // Dismiss the popup window
                popupWindow.dismiss()
            }
            // TODO() Naming fix up
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            beenOnMarketSinceDatePickerImageView.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                        requireContext(),
                        DatePickerDialog.OnDateSetListener { view, Year, Month, Day ->
                            beenOnMarketTextview.visibility = View.VISIBLE
                            var myMonth = (Month + 1).toString()
                            var myDay = Day.toString()
                            if (myMonth.length < 2) {
                                myMonth = "0$myMonth"
                            }
                            if (myDay.length < 2) {
                                myDay = "0$myDay"
                            }
                            beenOnMarketTextview.text = "   $myMonth/$myDay/$Year"
                        },
                        year,
                        month,
                        day
                )
                datePickerDialog.show()
            }

            beenSoldSinceDatePickerImageView.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                        requireContext(),
                        DatePickerDialog.OnDateSetListener { view, Year, Month, Day ->
                            beenSoldSinceTextview.visibility = View.VISIBLE
                            var month = (Month + 1).toString()
                            var day = Day.toString()
                            if (Month.toString().length < 2) {
                                month = "0$Month"
                            }
                            if (Day.toString().length < 2) {
                                day = "0$Day"
                            }
                            beenSoldSinceTextview.text = "   $month/$day/$Year"
                        },
                        year,
                        month,
                        day
                )
                datePickerDialog.show()
            }
            var priceChanged = false
            priceSlider.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
                priceMinText.text = rangeSlider.values[0].toString() + "0"
                priceMaxText.text = rangeSlider.values[1].toString() + "0"
                priceChanged = true
            }
            var surfaceAreaChanged = false
            surfaceAreaSlider.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
                surfaceMinText.text = rangeSlider.values[0].toString().substringBefore(".")
                surfaceMaxText.text = rangeSlider.values[1].toString().substringBefore(".")
                surfaceAreaChanged = true
            }
            photoSlider.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
                photosMixText.text = rangeSlider.values[0].toString().substringBefore(".")
                photosMaxText.text = rangeSlider.values[1].toString().substringBefore(".")
            }

            buttonApply.setOnClickListener {
                if (viewModel.uiState.value is ListingsViewModel.ListingState.Success) {
                    val minimumPhotos = photosMixText.text.toString().substringBefore(".").toInt()
                    val maximumPhotos = photosMaxText.text.toString().substringBefore(".").toInt()

                    var typeOfListingText = typeOfListingSpinner?.selectedItem.toString()
                    var statusOfPropertyText = statusOfPropertySpinner?.selectedItem.toString()

                    if (typeOfListingText.contains("Select")) {
                        typeOfListingText = ""
                    }
                    if (statusOfPropertyText.contains("Select")) {
                        statusOfPropertyText = ""
                    }
                    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

                    var finalFilteredData = ((viewModel.uiState.value as ListingsViewModel.ListingState.Success).listing)
//                    val filters = FilterParams(minPrice = priceMinText.text.toString().toDouble(),
//                            maxPrice = priceMaxText.text.toString().toDouble(),
//                            minSurfaceArea  = surfaceMinText.text.toString().toDouble(),
//                            maxSurfaceArea = surfaceMaxText.text.toString().toDouble(),
//                            minNumberOfPhotos = minimumPhotos.toDouble(),
//                            maxNumberOfPhotos = maximumPhotos.toDouble(),
//                            status = statusOfPropertyText,
//                            type = typeOfListingText,
//                            location = editTextEnterLocation.text.toString(),
//                            onMarketSince = beenOnMarketTextview.text.toString(),
//                            soldSince = beenSoldSinceTextview.text.toString(),
//                            pointsOfInterest = getCheckedCategories(bar = barCheckbox,
//                                                                park = parkCheckbox,
//                                                                school = schoolCheckbox,
//                                                                restaurant = restaurantCheckbox,
//                                                                hospital = hospitalCheckbox))

                    val filters = FilterParams.Builder()
                            .location(editTextEnterLocation.text.toString())
                            .build()

                    val filteredData = FilterContext()
                    if (priceChanged) {
                        // TODO() Builder pattern filter param
                        // TODO() Unconditionally extract all values from filter forms, sliders, etc filter param object
                        val priceFilterParam = FilterParams(minPrice = priceMinText.text.toString().toDouble(), maxPrice = priceMaxText.text.toString().toDouble())
                        val priceFilterContext = FilterContext(PriceStrategy())
                        finalFilteredData = priceFilterContext.executeStrategy(priceFilterParam, ((viewModel.uiState.value as ListingsViewModel.ListingState.Success).listing))
                    }
                    if (surfaceAreaChanged) {
                        val surfaceAreaFilterParam = FilterParams(minSurfaceArea = surfaceMinText.text.toString().toDouble(), maxSurfaceArea = surfaceMaxText.text.toString().toDouble())
                        val surfaceAreaFilterContext = FilterContext(SurfaceStrategy())
                        finalFilteredData = surfaceAreaFilterContext.executeStrategy(surfaceAreaFilterParam, finalFilteredData)
                    }


//                    val filteredData = (viewModel.uiState.value as ListingsViewModel.ListingState.Success)
//                            .listing.filter {
//
//                                ((
//                                        it.photoCount in minimumPhotos..maximumPhotos
//                                                &&
//                                                it.pointsOfInterest.replace("[", "").replace("]", "").split(",").map { it.trim() }
//                                                        .containsAll(getCheckedCategories(bar = barCheckbox,
//                                                                park = parkCheckbox,
//                                                                school = schoolCheckbox,
//                                                                restaurant = restaurantCheckbox,
//                                                                hospital = hospitalCheckbox)))
//                                        &&
//                                        it.status.contains(statusOfPropertyText, ignoreCase = true)
//                                        && it.typeOfListing.contains(typeOfListingText, ignoreCase = true)
//                                        && it.address.contains(enterLocationEditText.text.toString()))
//                                        &&
//                                        if (it.dateOnMarket.isNotEmpty() && !beenOnMarketTextview.text.toString().contains("date", ignoreCase = true)) {
//                                            return@filter (LocalDate.parse(it.dateOnMarket.filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(beenOnMarketTextview.text.toString().filter { !it.isWhitespace() }, formatter)))
//                                        } else {
//                                            return@filter it.dateOnMarket.isNotEmpty()
//                                        }
//                                        &&
//                                        if (it.saleDate.isNotEmpty() && !beenSoldSinceTextview.text.toString().contains("date", ignoreCase = true)) {
//                                            return@filter (LocalDate.parse(it.saleDate.filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(beenSoldSinceTextview.text.toString().filter { !it.isWhitespace() }, formatter)))
//                                        } else {
//                                            return@filter it.saleDate.isNotEmpty()
//                                        }
//                                        &&
//                                        if (it.price.isNotEmpty() && priceChanged) {
//                                            it.price.toDouble() in priceMinText.text.toString().toDouble()..priceMaxText.text.toString().toDouble()
//                                        } else {
//                                            it.price.isNotEmpty()
//                                        }
//                                        &&
//                                        if (it.surfaceArea.isNotEmpty() && surfaceAreaChanged) {
//                                            it.surfaceArea.toDouble() in surfaceMinText.text.toString().toDouble()..surfaceMaxText.text.toString().toDouble()
//                                        } else {
//                                            it.surfaceArea.isNotEmpty()
//                                        }
//                                        &&
//                                        if (editTextEnterLocation.text.toString().isNotEmpty()) {
//                                            it.address.contains(editTextEnterLocation.text.toString())
//                                        } else {
//                                            it.address.isNotEmpty()
//                                        }
//                            }
//                    viewModel.insertListings(filteredData)
//                    recyclerViewAdapter.setListData(filteredData as ArrayList<ListingEntity>)
//                    recyclerViewAdapter.notifyDataSetChanged()
                }
                popupWindow.dismiss()
            }




            TransitionManager.beginDelayedTransition(allListingsConstraintLayout)
            popupWindow.showAtLocation(
                    allListingsConstraintLayout, // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
            )
            true
        }
        val searchItem = menu.findItem(R.id.action_search)
        val searchView: androidx.appcompat.widget.SearchView = searchItem?.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Search Listings"
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (viewModel.uiState.value is ListingsViewModel.ListingState.Success) {
                    val filteredData =
                            (viewModel.uiState.value as ListingsViewModel.ListingState.Success).listing.filter {
                                it.descriptionOfListing.contains(
                                        searchView.query,
                                        ignoreCase = true)
                                        ||
                                        it.address.contains(searchView.query,
                                                ignoreCase = true)
                                        || it.pointsOfInterest.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.typeOfListing.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.price.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.photoDescription.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.surfaceArea.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.status.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.realEstateAgent.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.dateOnMarket.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.saleDate.contains(searchView.query,
                                        ignoreCase = true)
                            }
                    recyclerViewAdapter.setListData(filteredData as ArrayList<ListingEntity>)
                    recyclerViewAdapter.notifyDataSetChanged()
                    when (searchView.query.toString()) {
                        "" -> {
                            recyclerViewAdapter.setListData((viewModel.uiState.value as ListingsViewModel.ListingState.Success).listing as ArrayList<ListingEntity>)
                            recyclerViewAdapter.notifyDataSetChanged()
                        }
                    }
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
                        allListingsProgressBar.visibility = View.VISIBLE
                    }
                    is ListingsViewModel.ListingState.Success -> {
                        recyclerViewAdapter.setListData(uiState.listing as ArrayList<ListingEntity>)
                        recyclerViewAdapter.notifyDataSetChanged()
                        allListingsProgressBar.visibility = View.GONE
                    }
                    is ListingsViewModel.ListingState.Error -> {
                        allListingsProgressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun changeFragment(fragment: Fragment) {
        allListingsProgressBar.visibility = View.VISIBLE
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            allListingsProgressBar.visibility = View.GONE
            commit()
        }
    }

    private fun initRecyclerview() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            recyclerViewAdapter = RecyclerViewAdapter()
            adapter = recyclerViewAdapter
            val divider = DividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
            divider.setDrawable(context.resources.getDrawable(R.drawable.list_divider))
            addItemDecoration(divider)
        }
    }

    private fun overrideOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback {
        }
    }

    fun getCheckedCategories(bar: CheckBox, school: CheckBox, park: CheckBox, restaurant: CheckBox, hospital: CheckBox): List<String> = listOfNotNull(
            "Bar".takeIf { bar.isChecked },
            "School".takeIf { school.isChecked },
            "Park".takeIf { park.isChecked },
            "Restaurant".takeIf { restaurant.isChecked },
            "Hospital".takeIf { hospital.isChecked }
    )

    interface FilterStrategy {
        fun filter(vararg param: Any): Flow<ListingEntity>
    }

}