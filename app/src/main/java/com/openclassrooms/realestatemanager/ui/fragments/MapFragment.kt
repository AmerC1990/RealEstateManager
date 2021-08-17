package com.openclassrooms.realestatemanager.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.Dispatchers
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
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val inflater = inflater.inflate(R.layout.fragment_map, container, false)
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(false)
        val progressBar = inflater.findViewById<ProgressBar>(R.id.mapFragmentProgressBar)
        progressBar.visibility = View.VISIBLE
        return inflater

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overrideOnBackPressed()
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
}