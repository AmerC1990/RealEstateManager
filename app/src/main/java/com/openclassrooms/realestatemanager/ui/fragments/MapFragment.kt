package com.openclassrooms.realestatemanager.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.transition.Slide
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.slider.RangeSlider
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.databinding.FilterScreenBinding
import com.openclassrooms.realestatemanager.filter.FilterParams
import com.openclassrooms.realestatemanager.filter.SearchParams
import com.openclassrooms.realestatemanager.ui.activities.MainActivity
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.android.synthetic.main.fragment_all_listings.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.deniedPermissionMessage
import kotlinx.android.synthetic.main.fragment_map.geolocalizationButton
import kotlinx.android.synthetic.main.fragment_map.map
import kotlinx.android.synthetic.main.fragment_map.mapFragmentProgressBar
import kotlinx.android.synthetic.main.fragment_map_tablet.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private val viewModel by sharedViewModel<ListingsViewModel>()
    private val REQUEST_CODE = 101
    private var binding: FilterScreenBinding? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(false)
        setupFilterScreenLayout(inflater)
        return if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet") {
            inflater.inflate(R.layout.fragment_map_tablet, container, false)

        }
        else {
            inflater.inflate(R.layout.fragment_map, container, false)
        }
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
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            map = googleMap
        }
        viewModel.filter(viewModel._filterParams.value)
        lifecycleScope.launch(IO) {
            getLocationAccess()
        }
        attachObservers()
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
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        activity?.let {
            ActivityCompat.requestPermissions(it,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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
        if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet") {
            TransitionManager.beginDelayedTransition(mapFragmentTablet)
            popupWindow.showAtLocation(
                    mapFragmentTablet,
                    Gravity.CENTER,
                    0,
                    0
            )
        }
        else if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Mobile") {
            TransitionManager.beginDelayedTransition(mapFragment)
            popupWindow.showAtLocation(
                    mapFragment,
                    Gravity.CENTER,
                    0,
                    0
            )
        }

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
        binding?.priceSliderTitle?.text = getString(R.string.price) + " (" + Utils.euroOrDollar() + ")"
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
        val menuItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = menuItem?.actionView as SearchView
        searchView.queryHint = "Search Listings"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        isLocationOn()
    }

    private fun attachObservers() {
        lifecycleScope.launchWhenCreated {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is ListingsViewModel.ListingState.Loading -> {
                        if (mapFragmentProgressBar != null) {
                            mapFragmentProgressBar.visibility = View.VISIBLE
                        }
                    }
                    is ListingsViewModel.ListingState.Success -> {
                        if (mapFragmentProgressBar != null) {
                            mapFragmentProgressBar.visibility = View.GONE
                        }
                        val listingData = uiState.listing
                        setMarkersAndClickListeners(listingData)
                    }
                        is ListingsViewModel.ListingState.Error -> {
                        if (mapFragmentProgressBar != null) {
                            mapFragmentProgressBar.visibility = View.GONE
                        }
                        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_LONG).show()
                    }
                }
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

    private fun setMarkersAndClickListeners(filteredData: List<ListingEntity>) {
        map.clear()
            val builder = LatLngBounds.builder()
            for (item in filteredData) {
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
                            geolocalizationButton?.setOnClickListener {
                                map.animateCamera(cameraUpdate)
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
                                if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                                        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                    for (listings in filteredData) {
                                        if (listings.address == markerName) {
                                            bundle.putString("listing_id", listings.id.toString())
                                            fragment.arguments = bundle
                                            transaction?.replace(R.id.containerForListingDetails, fragment)
                                            transaction?.addToBackStack(null)
                                            transaction?.commit()
                                        }
                                    }
                                }
                                else {
                                    for (listings in filteredData) {
                                        if (listings.address == markerName) {
                                            bundle.putString("listing_id", listings.id.toString())
                                            fragment.arguments = bundle
                                            transaction?.replace(R.id.fragmentContainer, fragment)
                                            transaction?.addToBackStack(null)
                                            transaction?.commit()
                                        }
                                    }
                                }

                            }
                            true
                        }

                    }
            }
    }
}