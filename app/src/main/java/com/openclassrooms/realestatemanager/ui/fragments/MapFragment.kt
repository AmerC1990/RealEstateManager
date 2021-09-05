package com.openclassrooms.realestatemanager.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.slider.RangeSlider
import com.google.gson.Gson
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.databinding.FilterScreenBinding
import com.openclassrooms.realestatemanager.filter.FilterParams
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.android.synthetic.main.filter_screen.*
import kotlinx.android.synthetic.main.fragment_all_listings.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private val viewModel: ListingsViewModel by inject()
    private var binding: FilterScreenBinding? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(false)
        val view = inflater.inflate(R.layout.filter_screen, null)
        binding = FilterScreenBinding.bind(view)
//        val progressBar = inflater.findViewById<ProgressBar>(R.id.mapFragmentProgressBar)
//        progressBar.visibility = View.VISIBLE
        return inflater.inflate(R.layout.fragment_map, container, false)

    }

    override fun onStart() {
        super.onStart()
        val sharedPrefs = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val filter = sharedPrefs.getString("filter", null)
        val filterParams = sharedPrefs.getString("filterParams", null)
        if (filter.isNullOrEmpty()) {
            viewModel.fetchListings()
        } else {
            val objectMapper = ObjectMapper()
            val filterParamsObject = objectMapper.readValue(filterParams, FilterParams::class.java)
            viewModel.filter(filterParams = filterParamsObject)
        }
    }
    // TODO()
    // filter options menu click not working from map fragment, fix it
    // add search functionality to map fragment as well
    // add appropriate progress bars throughout app

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val filterIcon = menu.findItem(R.id.action_filter)
        filterIcon?.setOnMenuItemClickListener {

            val popupWindow = PopupWindow(
                    binding?.root, // Custom view to show in popup window
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

            val types = resources.getStringArray(R.array.type_of_listing_spinner)
            val statuses = resources.getStringArray(R.array.status_of_property_spinner)

            val typeAdapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, types)
            binding?.typeOfPropertySpinnerFilter?.adapter = typeAdapter


            val statusAdapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, statuses)
            binding?.statusOfPropertySpinnerFilter?.adapter = statusAdapter

            popupWindow.isFocusable = true
            popupWindow.update()
            binding?.buttonClose?.setOnClickListener {
                // Dismiss the popup window
                popupWindow.dismiss()
            }

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            binding?.beenOnMarketSinceDatePicker?.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                        requireContext(),
                        DatePickerDialog.OnDateSetListener { view, Year, Month, Day ->
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
                        DatePickerDialog.OnDateSetListener { view, Year, Month, Day ->
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

            binding?.buttonApply?.setOnClickListener {
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
                    val objectMapper = ObjectMapper()
                    val filterParamsAsString = objectMapper.writeValueAsString(filterParams)
                    val sharedPrefs = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPrefs.edit()
                    editor.apply {
                        putString("filter", "filter")
                        putString("filterParams", filterParamsAsString)
                    }
                            .apply()
                }
                popupWindow.dismiss()
            }
            binding?.resetFiltersTextview?.setOnClickListener {
                viewModel.fetchListings()
                val sharedPrefs = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                val editor = sharedPrefs.edit()
                editor.apply {
                    remove("filter")
                    remove("filterParams")
                }
                        .apply()
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
//                    recyclerViewAdapter.setListData(filteredData as ArrayList<ListingEntity>)
//                    recyclerViewAdapter.notifyDataSetChanged()
                    when (searchView.query.toString()) {
                        "" -> {
//                            recyclerViewAdapter.setListData((viewModel.uiState.value as ListingsViewModel.ListingState.Success).listing as ArrayList<ListingEntity>)
//                            recyclerViewAdapter.notifyDataSetChanged()
                        }
                    }
                }
                return false
            }
        }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        isLocationOn()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            map = googleMap
        }
        lifecycleScope.launch(IO) {
            getLocationAccess()
            attachObservers()
        }
    }

    private fun attachObservers() {
        lifecycleScope.launchWhenCreated {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is ListingsViewModel.ListingState.Loading -> {
                        mapFragmentProgressBar.visibility = View.VISIBLE
                    }
                    is ListingsViewModel.ListingState.Success -> {
                        mapFragmentProgressBar.visibility = View.GONE
                        val listingData = uiState.listing
                        val builder = LatLngBounds.builder()
                        for (item in listingData) {
                            if (item.address.isNotEmpty()) {
                                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                                val address = geocoder.getFromLocationName(item.address, 1)
                                var markers: Marker?
                                for (element in address) {
                                    val latLng = LatLng(element.latitude, element.longitude)
                                    markers = map.addMarker(
                                            latLng.let {
                                                MarkerOptions().position(it)
                                                        .title(item.address)
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                            }
                                    )
                                    builder.include(markers.position)
                                    val bounds = builder.build()
                                    val width = resources.displayMetrics.widthPixels
                                    val height = resources.displayMetrics.heightPixels
                                    val padding = (width * 0.10).toInt()
                                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                                            bounds,
                                            width,
                                            height,
                                            padding
                                    )
                                    map.animateCamera(cameraUpdate)
                                    geolocalizationButton.setOnClickListener {
                                        map.animateCamera(cameraUpdate)
                                    }
                                }
                            }
                            map.setOnMarkerClickListener { it ->
                                val markerName = it.title
                                if (markerName?.toString() == "My Location") {
                                    it.showInfoWindow()
                                } else {
                                    val fragment = ViewAndUpdateListingFragment()
                                    val transaction = activity?.supportFragmentManager?.beginTransaction()
                                    val bundle = Bundle()
                                    for (listings in listingData) {
                                        if (listings.address == markerName) {
                                            bundle.putString("listing_id", listings.id.toString())
                                            fragment.arguments = bundle
                                            transaction?.replace(R.id.fragmentContainer, fragment)
                                            transaction?.addToBackStack(null)
                                            transaction?.commit()
                                        }
                                    }
                                }
                                true
                            }
                        }
                    }
                    is ListingsViewModel.ListingState.Error -> {
                        mapFragmentProgressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private suspend fun getLocationAccess() {
        val getLocation = checkPermissionAndGetLocation()
        getLocation?.addOnSuccessListener { location: Location? ->
            if (location != null) {
                geolocalizationButton.visibility = View.VISIBLE
                val position = LatLng(location.latitude, location.longitude)
                map.addMarker(
                        position.let {
                            MarkerOptions().position(it)
                                    .title("My Location")
                                    .icon(
                                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                                    )
                        }
                )
            }
        }
        withContext(Main) {
            if (!checkPermission()) {
                map.isMyLocationEnabled = false
                val mapFragment =
                        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                val mapContainer = mapFragment?.map
                mapContainer?.view?.visibility = View.GONE
                geolocalizationButton.visibility = View.GONE
                deniedPermissionMessage.visibility = View.VISIBLE
            }
        }
    }


    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun checkPermissionAndGetLocation(): Task<Location>? {
        if (checkPermission()) {
            val locationRequest = LocationRequest.create()
            locationRequest.interval = 60000
            locationRequest.fastestInterval = 5000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            return LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
        }
        return null
    }

    private fun isLocationOn() {
        val locationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(R.string.location_disabled_message.toString())
                    .setCancelable(false)
                    .setPositiveButton(
                            "Yes"
                    ) { _, _ ->
                        startActivityForResult(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 11
                        )
                    }
            val alert: AlertDialog = builder.create()
            alert.show()
        }
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
}