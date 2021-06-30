package com.openclassrooms.realestatemanager.ui.fragments

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.annotation.ColorInt
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.slider.RangeSlider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.adapters.RecyclerViewAdapter
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.android.synthetic.main.filter_screen.*
import kotlinx.android.synthetic.main.fragment_all_listings.*
import kotlinx.android.synthetic.main.fragment_listing_form.*
import kotlinx.android.synthetic.main.fragment_listing_form.statusOfPropertySpinner
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
        Log.d("lifecycle", "onCreateView")
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_all_listings, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("lifecycle", "onAttach")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("lifecycle", "onCreate")

    }

    override fun onPause() {
        super.onPause()
        Log.d("lifecycle", "onPause")

    }

    override fun onStop() {
        super.onStop()
        Log.d("lifecycle", "onStop")

    }

    override fun onResume() {
        super.onResume()
        Log.d("lifecycle", "onResume")

    }

    override fun onStart() {
        super.onStart()
        Log.d("lifecycle", "onStart")

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
        Log.d("lifecycle", "onActivityCreated")
        showOnlineStatus()
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
                priceMinText.text = rangeSlider.values[0].toString()
                priceMaxText.text = rangeSlider.values[1].toString()
                priceChanged = true
            }
            var surfaceAreaChanged = false
            surfaceAreaSlider.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
                surfaceMinText.text = rangeSlider.values[0].toString()
                surfaceMaxText.text = rangeSlider.values[1].toString()
                surfaceAreaChanged = true
            }
            photoSlider.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
                photosMixText.text = rangeSlider.values[0].toString()
                photosMaxText.text = rangeSlider.values[1].toString()
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
                    val filteredData = (viewModel.uiState.value as ListingsViewModel.ListingState.Success)
                            .listing.filter {

                                ((
                                        it.photoCount in minimumPhotos..maximumPhotos
                                                &&
                                                it.pointsOfInterest.replace("[", "").replace("]", "").split(",").map { it.trim() }
                                                        .containsAll(getCheckedCategories(bar = barCheckbox,
                                                                park = parkCheckbox,
                                                                school = schoolCheckbox,
                                                                restaurant = restaurantCheckbox,
                                                                hospital = hospitalCheckbox)))
                                        &&
                                        it.status.contains(statusOfPropertyText, ignoreCase = true)
                                        && it.typeOfListing.contains(typeOfListingText, ignoreCase = true)
                                        && it.address.contains(enterLocationEditText.text.toString()))
                                        &&
                                        if (it.dateOnMarket.isNotEmpty() && !beenOnMarketTextview.text.toString().contains("date", ignoreCase = true)) {
                                            return@filter (LocalDate.parse(it.dateOnMarket.filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(beenOnMarketTextview.text.toString().filter { !it.isWhitespace() }, formatter)))
                                        } else {
                                            return@filter it.dateOnMarket.isNotEmpty()
                                        }
                                        &&
                                        if (it.saleDate.isNotEmpty() && !beenSoldSinceTextview.text.toString().contains("date", ignoreCase = true)) {
                                            return@filter (LocalDate.parse(it.saleDate.filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(beenSoldSinceTextview.text.toString().filter { !it.isWhitespace() }, formatter)))
                                        } else {
                                            return@filter it.saleDate.isNotEmpty()
                                        }
                                        &&
                                        if (it.price.isNotEmpty() && priceChanged) {
                                            it.price.toDouble() in priceMinText.text.toString().toDouble()..priceMaxText.text.toString().toDouble()
                                        } else {
                                            it.price.isNotEmpty()
                                        }
                                        &&
                                        if (it.surfaceArea.isNotEmpty() && surfaceAreaChanged) {
                                            it.surfaceArea.toDouble() in surfaceMinText.text.toString().toDouble()..surfaceMaxText.text.toString().toDouble()
                                        } else {
                                            it.surfaceArea.isNotEmpty()
                                        }
                                        &&
                                        if (editTextEnterLocation.text.toString().isNotEmpty()) {
                                            it.address.contains(editTextEnterLocation.text.toString())
                                        } else {
                                            it.address.isNotEmpty()
                                        }
                            }
                    viewModel.insertListings(filteredData)
                    recyclerViewAdapter.setListData(filteredData as ArrayList<ListingEntity>)
                    recyclerViewAdapter.notifyDataSetChanged()
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
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Loading Listings")
        lifecycleScope.launchWhenCreated {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is ListingsViewModel.ListingState.Loading -> {
                        progressDialog.show()
                    }
                    is ListingsViewModel.ListingState.Success -> {
                        recyclerViewAdapter.setListData(uiState.listing as ArrayList<ListingEntity>)
                        recyclerViewAdapter.notifyDataSetChanged()
                        progressDialog.dismiss()
                    }
                    is ListingsViewModel.ListingState.Error -> {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun changeFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
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

    private fun showOnlineStatus() {
        val fromLogin = arguments?.getString("justLoggedIn", null)
        if (fromLogin != null) {
            if (Utils.isOnline(requireContext())) {
                Snackbar
                        .make(allListingsConstraintLayout, "Connected", Snackbar.LENGTH_SHORT)
                        .withColor(resources.getColor(R.color.myGreen))
                        .show()
            } else if (!Utils.isOnline(requireContext())) {
                Snackbar
                        .make(allListingsConstraintLayout, "Offline", Snackbar.LENGTH_SHORT)
                        .withColor(resources.getColor(R.color.myRed))
                        .show()
            }
        }
    }

    private fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar {
        this.view.setBackgroundColor(colorInt)
        return this
    }

    private fun overrideOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback {
        }
    }

    private fun getCheckedCategories(bar: CheckBox, school: CheckBox, park: CheckBox, restaurant: CheckBox, hospital: CheckBox): List<String> = listOfNotNull(
            "Bar".takeIf { bar.isChecked },
            "School".takeIf { school.isChecked },
            "Park".takeIf { park.isChecked },
            "Restaurant".takeIf { restaurant.isChecked },
            "Hospital".takeIf { hospital.isChecked }
    )

}