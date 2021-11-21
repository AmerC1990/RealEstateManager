package com.openclassrooms.realestatemanager.ui.fragments

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.Utils.convertDateFromUSAToWorld
import com.openclassrooms.realestatemanager.Utils.convertDollarToEuro
import com.openclassrooms.realestatemanager.Utils.isLocaleInAmerica
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.receiver.AlarmReceiver
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import com.openclassrooms.realestatemanager.viewmodels.SingleListingViewModel
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.*
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.addImageIconUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.barCheckboxUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.buttonEditListingUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.buttonSaveListingUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.deleteEighthPhotoUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.deleteFifthPhotoUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.deleteFirstPhotoUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.deleteFourthPhotoUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.deleteNinthPhotoUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.deleteSecondPhotoUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.deleteSeventhPhotoUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.deleteSixthPhotoUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.deleteTenthPhotoUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.deleteThirdPhotoUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.editTextEnterAddressUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.editTextEnterDatePutOnMarketUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.editTextEnterDescriptionUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.editTextEnterImageDescriptionUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.editTextEnterNameOfAgentUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.editTextEnterNumberOfRoomsUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.editTextEnterPriceUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.editTextEnterSaleDateUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.editTextEnterSurfaceAreaUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.hospitalCheckboxUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.imageViewChooseImage10Update
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.imageViewChooseImage2Update
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.imageViewChooseImage3Update
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.imageViewChooseImage4Update
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.imageViewChooseImage5Update
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.imageViewChooseImage6Update
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.imageViewChooseImage7Update
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.imageViewChooseImage8Update
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.imageViewChooseImage9Update
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.imageViewChooseImageUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.parkCheckboxUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.restaurantCheckboxUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.schoolCheckboxUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.statusOfPropertySpinnerUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.typeOfListingSpinnerUpdate
import kotlinx.android.synthetic.main.fragment_view_and_update_listing.updateListingProgressBar
import kotlinx.android.synthetic.main.fragment_view_and_update_listing_tablet.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import java.util.*


class ViewAndUpdateListingFragment : Fragment() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var alarmManager: AlarmManager
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    private val channelId = "listing"
    private val description = "notification"
    lateinit var filePath: Uri
    private val singleListingViewModel: SingleListingViewModel by inject()
    private val listingsViewModel: ListingsViewModel by inject()
    private val allPhotos = mutableMapOf<Int, String>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        return if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet") {
            inflater.inflate(R.layout.fragment_view_and_update_listing_tablet, container, false)
        }
        else {
            inflater.inflate(R.layout.fragment_view_and_update_listing, container, false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overrideOnBackPressed()
        createNotificationChannel()
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

    override fun onStart() {
        super.onStart()
        handleGuestUser()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        disableEditing()
        attachObservers()
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
        val id = arguments?.getString("listing_id", null)
        if (Utils.isOnline(requireContext())) {
            if (!id.isNullOrEmpty()) {
                Log.d("single", id)
                singleListingViewModel.getSingleListing(id.toInt())
            } else if (id.isNullOrEmpty()) {
                 if (this.view?.findViewById<TextView>(R.id.pleaseSelectListingTextView) != null) {
                     pleaseSelectListingTextView.visibility = View.VISIBLE
                 }
                if (this.view?.findViewById<LinearLayout>(R.id.listingsDetailsTabletLinearLayout) != null) {
                    listingsDetailsTabletLinearLayout.visibility = View.GONE
                }
                if (this.view?.findViewById<ScrollView>(R.id.listingsDetailsTabletScrollview) != null) {
                    listingsDetailsTabletScrollview.visibility = View.GONE
                }
                if (this.view?.findViewById<ProgressBar>(R.id.updateListingProgressBar) != null) {
                    updateListingProgressBar.visibility = View.GONE
                }
            }
            buttonEditListingUpdate.setOnClickListener {
                buttonEditListingUpdate.startAnimation(animation)
                showDeletePhotoButtons()
                enableSaveListingButton()
                enableEditing()
                enterDates()
                addImageIconUpdate.visibility = View.VISIBLE
                id?.toInt()?.let { it1 -> insertListing(it1) }
                addImageIconUpdate.setOnClickListener {
                    startFileChooser()
                }
            }
        } else if (!Utils.isOnline(requireContext())) {
            if (!id.isNullOrEmpty()) {
                singleListingViewModel.getSingleListing(id.toInt())
            }  else if (id.isNullOrEmpty()) {
                if (this.view?.findViewById<TextView>(R.id.pleaseSelectListingTextView) != null) {
                    pleaseSelectListingTextView.visibility = View.VISIBLE
                }
                if (this.view?.findViewById<LinearLayout>(R.id.listingsDetailsTabletLinearLayout) != null) {
                    listingsDetailsTabletLinearLayout.visibility = View.GONE
                }
                if (this.view?.findViewById<ScrollView>(R.id.listingsDetailsTabletScrollview) != null) {
                    listingsDetailsTabletScrollview.visibility = View.GONE
                }
                if (this.view?.findViewById<ProgressBar>(R.id.updateListingProgressBar) != null) {
                    updateListingProgressBar.visibility = View.GONE
                }
            }
            setListingUpdateButtonVisibility()
        }
    }
    private fun attachObservers() {
        val typeOfListingSpinner = activity?.findViewById<Spinner>(R.id.typeOfListingSpinnerUpdate)
        val types = resources.getStringArray(R.array.type_of_listing_spinner)

        val statusOfPropertySpinner = activity?.findViewById<Spinner>(R.id.statusOfPropertySpinnerUpdate)
        val statuses = resources.getStringArray(R.array.status_of_property_spinner)


        val typeOfListingadapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, types)
        typeOfListingSpinner?.adapter = typeOfListingadapter


        val statusAdapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, statuses)
        statusOfPropertySpinner?.adapter = statusAdapter

        lifecycleScope.launchWhenCreated {
            singleListingViewModel.uiState.collect { uiState ->
                when (uiState) {
                    is SingleListingViewModel.SingleListingState.SuccessPhoto -> {
                        allPhotos.put(uiState.photoCount, uiState.photoReference)
                    }

                    is SingleListingViewModel.SingleListingState.Loading -> {
                        updateListingProgressBar.visibility = View.VISIBLE
                    }
                    is SingleListingViewModel.SingleListingState.Success -> {
                        updateListingProgressBar.visibility = View.GONE
                        editTextEnterImageDescriptionUpdate.setText(uiState.listing.photoDescription)
                        if (types.contains(uiState.listing.typeOfListing)) {
                            val spinnerPosition = typeOfListingadapter.getPosition(uiState.listing.typeOfListing)
                            typeOfListingSpinner?.setSelection(spinnerPosition)
                        } else {
                            typeOfListingSpinner?.setSelection(0)
                        }

                        if (Utils.doesLocaleSubscribeToEuroCurrency()) {
                            editTextEnterPriceUpdate.setText(convertDollarToEuro(uiState.listing.price.toInt()).toString())
                        }
                        else if (!Utils.doesLocaleSubscribeToEuroCurrency()) {
                            editTextEnterPriceUpdate.setText(uiState.listing.price)
                        }

                        editTextEnterSurfaceAreaUpdate.setText(uiState.listing.surfaceArea)
                        editTextEnterNumberOfRoomsUpdate.setText(uiState.listing.numberOfRooms)
                        editTextEnterDescriptionUpdate.setText(uiState.listing.descriptionOfListing)
                        editTextEnterAddressUpdate.setText(uiState.listing.address)
                        if (statuses.contains(uiState.listing.status)) {
                            val spinnerPosition = statusAdapter.getPosition(uiState.listing.status)
                            statusOfPropertySpinner?.setSelection(spinnerPosition)
                        } else {
                            statusOfPropertySpinner?.setSelection(0)
                        }
                        val checkBoxes = listOf<CheckBox>(barCheckboxUpdate,
                                restaurantCheckboxUpdate,
                                schoolCheckboxUpdate,
                                hospitalCheckboxUpdate,
                                parkCheckboxUpdate)
                        for (checkbox in checkBoxes) {
                            if (uiState.listing.pointsOfInterest.contains(checkbox.text.toString())) {
                                checkbox.isChecked = true
                            }
                        }
                        if (!isLocaleInAmerica()) {
                            editTextEnterDatePutOnMarketUpdate.setText(convertDateFromUSAToWorld(uiState.listing.dateOnMarket))
                            editTextEnterSaleDateUpdate.setText(convertDateFromUSAToWorld(uiState.listing.saleDate))
                        } else if (isLocaleInAmerica()) {
                            editTextEnterDatePutOnMarketUpdate.setText(uiState.listing.dateOnMarket)
                            editTextEnterSaleDateUpdate.setText(uiState.listing.saleDate)
                        }

                        editTextEnterNameOfAgentUpdate.setText(uiState.listing.realEstateAgent)
                        if (uiState.listing.photoReference.contains("firebasestorage")) {
                            allPhotos.put(0, uiState.listing.photoReference)
                            imageViewChooseImageUpdate.visibility = View.VISIBLE

                            Glide.with(imageViewChooseImageUpdate)
                                    .load(uiState.listing.photoReference)
                                    .fitCenter()
                                    .error(R.drawable.plus_icon)
                                    .into(imageViewChooseImageUpdate)

                            deleteFirstPhotoUpdate.setOnClickListener {
                                imageViewChooseImageUpdate.setImageDrawable(null)
                                imageViewChooseImageUpdate.visibility = View.GONE
                                deleteFirstPhotoUpdate.visibility = View.GONE
                                allPhotos[0] = ""
                            }
                        }
                        if (uiState.listing.photoReference2.contains("firebasestorage")) {
                            allPhotos.put(1, uiState.listing.photoReference2)
                            imageViewChooseImage2Update.visibility = View.VISIBLE

                            Glide.with(imageViewChooseImage2Update)
                                    .load(uiState.listing.photoReference2)
                                    .fitCenter()
                                    .error(R.drawable.plus_icon)
                                    .into(imageViewChooseImage2Update)

                            deleteSecondPhotoUpdate.setOnClickListener {
                                imageViewChooseImage2Update.setImageDrawable(null)
                                imageViewChooseImage2Update.visibility = View.GONE
                                deleteSecondPhotoUpdate.visibility = View.GONE
                                allPhotos[1] = ""
                            }
                        }
                        if (uiState.listing.photoReference3.contains("firebasestorage")) {
                            allPhotos.put(2, uiState.listing.photoReference3)
                            imageViewChooseImage3Update.visibility = View.VISIBLE

                            Glide.with(imageViewChooseImage3Update)
                                    .load(uiState.listing.photoReference3)
                                    .fitCenter()
                                    .error(R.drawable.plus_icon)
                                    .into(imageViewChooseImage3Update)

                            deleteThirdPhotoUpdate.setOnClickListener {
                                imageViewChooseImage3Update.setImageDrawable(null)
                                imageViewChooseImage3Update.visibility = View.GONE
                                deleteThirdPhotoUpdate.visibility = View.GONE
                                allPhotos[2] = ""
                            }
                        }
                        if (uiState.listing.photoReference4.contains("firebasestorage")) {
                            allPhotos.put(3, uiState.listing.photoReference4)
                            imageViewChooseImage4Update.visibility = View.VISIBLE

                            Glide.with(imageViewChooseImage4Update)
                                    .load(uiState.listing.photoReference4)
                                    .fitCenter()
                                    .error(R.drawable.plus_icon)
                                    .into(imageViewChooseImage4Update)

                            deleteFourthPhotoUpdate.setOnClickListener {
                                imageViewChooseImage4Update.setImageDrawable(null)
                                imageViewChooseImage4Update.visibility = View.GONE
                                deleteFourthPhotoUpdate.visibility = View.GONE
                                allPhotos[3] = ""
                            }
                        }
                        if (uiState.listing.photoReference5.contains("firebasestorage")) {
                            allPhotos.put(4, uiState.listing.photoReference5)
                            imageViewChooseImage5Update.visibility = View.VISIBLE

                            Glide.with(imageViewChooseImage5Update)
                                    .load(uiState.listing.photoReference5)
                                    .fitCenter()
                                    .error(R.drawable.plus_icon)
                                    .into(imageViewChooseImage5Update)

                            deleteFifthPhotoUpdate.setOnClickListener {
                                imageViewChooseImage5Update.setImageDrawable(null)
                                imageViewChooseImage5Update.visibility = View.GONE
                                deleteFifthPhotoUpdate.visibility = View.GONE
                                allPhotos[4] = ""
                            }
                        }
                        if (uiState.listing.photoReference6.contains("firebasestorage")) {
                            allPhotos.put(5, uiState.listing.photoReference6)
                            imageViewChooseImage6Update.visibility = View.VISIBLE

                            Glide.with(imageViewChooseImage6Update)
                                    .load(uiState.listing.photoReference6)
                                    .fitCenter()
                                    .error(R.drawable.plus_icon)
                                    .into(imageViewChooseImage6Update)

                            deleteSixthPhotoUpdate.setOnClickListener {
                                imageViewChooseImage6Update.setImageDrawable(null)
                                imageViewChooseImage6Update.visibility = View.GONE
                                deleteSixthPhotoUpdate.visibility = View.GONE
                                allPhotos[5] = ""
                            }
                        }
                        if (uiState.listing.photoReference7.contains("firebasestorage")) {
                            allPhotos.put(6, uiState.listing.photoReference7)
                            imageViewChooseImage7Update.visibility = View.VISIBLE

                            Glide.with(imageViewChooseImage7Update)
                                    .load(uiState.listing.photoReference7)
                                    .fitCenter()
                                    .error(R.drawable.plus_icon)
                                    .into(imageViewChooseImage7Update)

                            deleteSeventhPhotoUpdate.setOnClickListener {
                                imageViewChooseImage7Update.setImageDrawable(null)
                                imageViewChooseImage7Update.visibility = View.GONE
                                deleteSeventhPhotoUpdate.visibility = View.GONE
                                allPhotos[6] = ""
                            }
                        }
                        if (uiState.listing.photoReference8.contains("firebasestorage")) {
                            allPhotos.put(7, uiState.listing.photoReference8)
                            imageViewChooseImage8Update.visibility = View.VISIBLE

                            Glide.with(imageViewChooseImage2Update)
                                    .load(uiState.listing.photoReference8)
                                    .fitCenter()
                                    .error(R.drawable.plus_icon)
                                    .into(imageViewChooseImage8Update)

                            deleteEighthPhotoUpdate.setOnClickListener {
                                imageViewChooseImage8Update.setImageDrawable(null)
                                imageViewChooseImage8Update.visibility = View.GONE
                                deleteEighthPhotoUpdate.visibility = View.GONE
                                allPhotos[7] = ""
                            }
                        }
                        if (uiState.listing.photoReference9.contains("firebasestorage")) {
                            allPhotos.put(8, uiState.listing.photoReference9)
                            imageViewChooseImage9Update.visibility = View.VISIBLE

                            Glide.with(imageViewChooseImage9Update)
                                    .load(uiState.listing.photoReference9)
                                    .fitCenter()
                                    .error(R.drawable.plus_icon)
                                    .into(imageViewChooseImage9Update)

                            deleteNinthPhotoUpdate.setOnClickListener {
                                imageViewChooseImage9Update.setImageDrawable(null)
                                imageViewChooseImage9Update.visibility = View.GONE
                                deleteNinthPhotoUpdate.visibility = View.GONE
                                allPhotos[8] = ""
                            }
                        }
                        if (uiState.listing.photoReference10.contains("firebasestorage")) {
                            allPhotos.put(9, uiState.listing.photoReference10)
                            imageViewChooseImage10Update.visibility = View.VISIBLE

                            Glide.with(imageViewChooseImage10Update)
                                    .load(uiState.listing.photoReference10)
                                    .fitCenter()
                                    .error(R.drawable.plus_icon)
                                    .into(imageViewChooseImage10Update)

                            deleteTenthPhotoUpdate.setOnClickListener {
                                imageViewChooseImage10Update.setImageDrawable(null)
                                imageViewChooseImage10Update.visibility = View.GONE
                                deleteTenthPhotoUpdate.visibility = View.GONE
                                allPhotos[9] = ""
                            }
                        }
                    }
                    is SingleListingViewModel.SingleListingState.Error -> {
                        updateListingProgressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error loading listing", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun enableEditing() {
        editTextEnterImageDescriptionUpdate.isEnabled = true
        typeOfListingSpinnerUpdate.isEnabled = true
        typeOfListingSpinnerUpdate.isClickable = true
        typeOfListingSpinnerUpdate.isEnabled = true
        editTextEnterPriceUpdate.isEnabled = true
        if (Utils.doesLocaleSubscribeToEuroCurrency()) {
            editTextEnterPriceUpdate.hint = R.string.price_of_property_euro.toString()
        }
        editTextEnterSurfaceAreaUpdate.isEnabled = true
        editTextEnterNumberOfRoomsUpdate.isEnabled = true
        editTextEnterDescriptionUpdate.isEnabled = true
        editTextEnterAddressUpdate.isEnabled = true
        barCheckboxUpdate.isEnabled = true
        barCheckboxUpdate.isClickable = true
        restaurantCheckboxUpdate.isEnabled = true
        restaurantCheckboxUpdate.isClickable = true
        schoolCheckboxUpdate.isEnabled = true
        schoolCheckboxUpdate.isClickable = true
        parkCheckboxUpdate.isEnabled = true
        parkCheckboxUpdate.isClickable = true
        hospitalCheckboxUpdate.isEnabled = true
        hospitalCheckboxUpdate.isClickable = true
        statusOfPropertySpinnerUpdate.isEnabled = true
        statusOfPropertySpinnerUpdate.isClickable = true
        editTextEnterDatePutOnMarketUpdate.isEnabled = true
        editTextEnterSaleDateUpdate.isEnabled = true
        if (!isLocaleInAmerica()) {
            editTextEnterDatePutOnMarketUpdate.hint = R.string.date_put_on_market_non_american.toString()
            editTextEnterSaleDateUpdate.hint = R.string.date_sold_hint_non_american.toString()
        }
        editTextEnterNameOfAgentUpdate.isEnabled = true
    }

    private fun disableEditing() {
        editTextEnterImageDescriptionUpdate.isEnabled = false
        typeOfListingSpinnerUpdate.isEnabled = false
        typeOfListingSpinnerUpdate.isClickable = false
        editTextEnterPriceUpdate.isEnabled = false
        editTextEnterSurfaceAreaUpdate.isEnabled = false
        editTextEnterNumberOfRoomsUpdate.isEnabled = false
        editTextEnterDescriptionUpdate.isEnabled = false
        editTextEnterAddressUpdate.isEnabled = false
        barCheckboxUpdate.isEnabled = false
        barCheckboxUpdate.isClickable = false
        restaurantCheckboxUpdate.isEnabled = false
        restaurantCheckboxUpdate.isClickable = false
        schoolCheckboxUpdate.isEnabled = false
        restaurantCheckboxUpdate.isClickable = false
        parkCheckboxUpdate.isEnabled = false
        restaurantCheckboxUpdate.isClickable = false
        hospitalCheckboxUpdate.isEnabled = false
        restaurantCheckboxUpdate.isClickable = false
        statusOfPropertySpinnerUpdate.isEnabled = false
        statusOfPropertySpinnerUpdate.isClickable = false
        editTextEnterDatePutOnMarketUpdate.isEnabled = false
        editTextEnterSaleDateUpdate.isEnabled = false
        editTextEnterNameOfAgentUpdate.isEnabled = false
    }

    private fun insertListing(id: Int) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
        buttonSaveListingUpdate.setOnClickListener {
            buttonSaveListingUpdate.startAnimation(animation)
            hideDeletePhotoButtons()
            disableEditing()
            addImageIconUpdate.visibility = View.GONE
            insertListingData(id)
        }
    }

    private fun hideDeletePhotoButtons() {
        deleteFirstPhotoUpdate.visibility = View.GONE
        deleteSecondPhotoUpdate.visibility = View.GONE
        deleteThirdPhotoUpdate.visibility = View.GONE
        deleteFourthPhotoUpdate.visibility = View.GONE
        deleteFifthPhotoUpdate.visibility = View.GONE
        deleteSixthPhotoUpdate.visibility = View.GONE
        deleteSeventhPhotoUpdate.visibility = View.GONE
        deleteEighthPhotoUpdate.visibility = View.GONE
        deleteNinthPhotoUpdate.visibility = View.GONE
        deleteTenthPhotoUpdate.visibility = View.GONE
    }

    private fun insertListingData(id: Int) {
        updateListingProgressBar.visibility = View.VISIBLE

        var statusOfPropertyText = statusOfPropertySpinnerUpdate?.selectedItem.toString()
        if (statusOfPropertyText.contains("Select")) {
            statusOfPropertyText = ""
        }

        var pointsOfInterestList = ""
        if (getCheckedCategories().isNotEmpty()) {
            pointsOfInterestList = getCheckedCategories().toString()
        }

        var typeOfListingText = typeOfListingSpinnerUpdate?.selectedItem.toString()
        if (typeOfListingText.contains("Select")) {
            typeOfListingText = ""
        }

        val imageDescription = editTextEnterImageDescriptionUpdate.text.toString()
        val typeOfListing = typeOfListingText
        var priceOfListing = ""
        if (Utils.doesLocaleSubscribeToEuroCurrency()) {
            priceOfListing = Utils.convertEuroToDollar(editTextEnterPriceUpdate.text.toString().toInt()).toString()
        }
        else if (!Utils.doesLocaleSubscribeToEuroCurrency()) {
            priceOfListing = editTextEnterPriceUpdate.text.toString()
        }
        val surfaceArea = editTextEnterSurfaceAreaUpdate.text.toString()
        val numberOfRooms = editTextEnterNumberOfRoomsUpdate.text.toString()
        val descriptionOfListing = editTextEnterDescriptionUpdate.text.toString()
        val addressOfListing = editTextEnterAddressUpdate.text.toString()
        val pointsOfInterest = pointsOfInterestList
        val statusOfListing = statusOfPropertyText
        var dateOnMarket = ""
        if (isLocaleInAmerica()) {
            dateOnMarket = editTextEnterDatePutOnMarketUpdate.text.toString()
        }
        else if (!isLocaleInAmerica()) {
            dateOnMarket = Utils.convertDateFromWorldToUSA(editTextEnterDatePutOnMarketUpdate.text.toString())
        }
        var saleDateOfListing = ""
        if (isLocaleInAmerica()) {
            saleDateOfListing = editTextEnterSaleDateUpdate.text.toString()
        }
        else if (!isLocaleInAmerica()) {
            saleDateOfListing = Utils.convertDateFromWorldToUSA(editTextEnterSaleDateUpdate.text.toString())
        }
        val nameOfAgent = editTextEnterNameOfAgentUpdate.text.toString()
        val photoOne = allPhotos.getOrDefault(0, "")
        val photoTwo = allPhotos.getOrDefault(1, "")
        val photoThree = allPhotos.getOrDefault(2, "")
        val photoFour = allPhotos.getOrDefault(3, "")
        val photoFive = allPhotos.getOrDefault(4, "")
        val photoSix = allPhotos.getOrDefault(5, "")
        val photoSeven = allPhotos.getOrDefault(6, "")
        val photoEight = allPhotos.getOrDefault(7, "")
        val photoNine = allPhotos.getOrDefault(8, "")
        val photoTen = allPhotos.getOrDefault(9, "")

        val photoCount = listOf<String>(photoOne,
                photoTwo,
                photoThree,
                photoFour,
                photoFive,
                photoSix,
                photoSeven,
                photoEight,
                photoNine,
                photoTen).count { !it.isNullOrEmpty() }

        val listing = ListingEntity(
                photoCount = photoCount,
                id = id,
                photoReference = photoOne,
                photoReference2 = photoTwo,
                photoReference3 = photoThree,
                photoReference4 = photoFour,
                photoReference5 = photoFive,
                photoReference6 = photoSix,
                photoReference7 = photoSeven,
                photoReference8 = photoEight,
                photoReference9 = photoNine,
                photoReference10 = photoTen,
                photoDescription = imageDescription,
                typeOfListing = typeOfListing,
                price = priceOfListing,
                surfaceArea = surfaceArea,
                numberOfRooms = numberOfRooms,
                descriptionOfListing = descriptionOfListing,
                address = addressOfListing,
                pointsOfInterest = pointsOfInterest,
                status = statusOfListing,
                dateOnMarket = dateOnMarket,
                saleDate = saleDateOfListing,
                realEstateAgent = nameOfAgent
        )
        listingsViewModel.insertListingInfo(listing)
        lifecycleScope.launch(Main) {
            val sharedPrefs = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            editor.apply {
                putString("address", addressOfListing)
                putLong("id", id.toLong())
            }
                    .apply()
            delay(1000L)
            setAlarm()
            updateListingProgressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Listing Updated", Toast.LENGTH_LONG).show()
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun startFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Choose Picture"), 111)
    }

    private fun overrideOnBackPressed() {
        if (activity?.supportFragmentManager?.backStackEntryCount == 0) {
            activity?.onBackPressedDispatcher?.addCallback {
                activity?.supportFragmentManager?.beginTransaction()?.apply {
                    replace(R.id.fragmentContainer, AllListingsFragment())
                    commit()
                }
            }
        }
        else {
            activity?.onBackPressedDispatcher?.addCallback {
                activity?.supportFragmentManager?.popBackStack()
            }
        }

    }

    private fun enableSaveListingButton() {
        buttonSaveListingUpdate.isClickable = true
        buttonSaveListingUpdate.isEnabled = true
        buttonSaveListingUpdate.visibility = View.VISIBLE
    }

    private fun setAlarm() {
        alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent =
                PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingIntent2 =
                PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val myAlarm = AlarmManager.AlarmClockInfo(
                System.currentTimeMillis() + 2000L,
                pendingIntent2
        )
        alarmManager.setAlarmClock(myAlarm, pendingIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                    channelId, description, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLACK
            notificationChannel.enableVibration(false)

            notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == Activity.RESULT_OK && data != null) {
            filePath = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, filePath)

            if (imageViewChooseImageUpdate.drawable == null && imageViewChooseImage2Update.drawable == null) {
                imageViewChooseImageUpdate.visibility = View.VISIBLE
                imageViewChooseImageUpdate.setImageBitmap(bitmap)
                uploadImageToStorage(0)
                deleteFirstPhotoUpdate.visibility = View.VISIBLE
                deleteFirstPhotoUpdate.setOnClickListener {
                    imageViewChooseImageUpdate.setImageDrawable(null)
                    imageViewChooseImageUpdate.visibility = View.GONE
                    deleteFirstPhotoUpdate.visibility = View.GONE
                    allPhotos[0] = ""
                }
            } else if (imageViewChooseImageUpdate.drawable != null && imageViewChooseImage2Update.drawable == null) {
                imageViewChooseImage2Update.visibility = View.VISIBLE
                imageViewChooseImage2Update.setImageBitmap(bitmap)
                uploadImageToStorage(1)
                deleteSecondPhotoUpdate.visibility = View.VISIBLE
                deleteSecondPhotoUpdate.setOnClickListener {
                    imageViewChooseImage2Update.setImageDrawable(null)
                    imageViewChooseImage2Update.visibility = View.GONE
                    deleteSecondPhotoUpdate.visibility = View.GONE
                    allPhotos[1] = ""
                }
            } else if (imageViewChooseImageUpdate.drawable != null && imageViewChooseImage2Update.drawable != null
                    && imageViewChooseImage3Update.drawable == null) {
                imageViewChooseImage3Update.visibility = View.VISIBLE
                imageViewChooseImage3Update.setImageBitmap(bitmap)
                uploadImageToStorage(2)
                deleteThirdPhotoUpdate.visibility = View.VISIBLE
                deleteThirdPhotoUpdate.setOnClickListener {
                    imageViewChooseImage3Update.setImageDrawable(null)
                    imageViewChooseImage3Update.visibility = View.GONE
                    deleteThirdPhotoUpdate.visibility = View.GONE
                    allPhotos[2] = ""
                }
            } else if (imageViewChooseImageUpdate.drawable != null && imageViewChooseImage2Update.drawable != null
                    && imageViewChooseImage3Update.drawable != null && imageViewChooseImage4Update.drawable == null) {
                imageViewChooseImage4Update.visibility = View.VISIBLE
                imageViewChooseImage4Update.setImageBitmap(bitmap)
                uploadImageToStorage(3)
                deleteFourthPhotoUpdate.visibility = View.VISIBLE
                deleteFourthPhotoUpdate.setOnClickListener {
                    imageViewChooseImage4Update.setImageDrawable(null)
                    imageViewChooseImage4Update.visibility = View.GONE
                    deleteFourthPhotoUpdate.visibility = View.GONE
                    allPhotos[3] = ""
                }
            } else if (imageViewChooseImageUpdate.drawable != null && imageViewChooseImage2Update.drawable != null
                    && imageViewChooseImage3Update.drawable != null && imageViewChooseImage4Update.drawable != null
                    && imageViewChooseImage5Update.drawable == null) {
                imageViewChooseImage5Update.visibility = View.VISIBLE
                imageViewChooseImage5Update.setImageBitmap(bitmap)
                uploadImageToStorage(4)
                deleteFifthPhotoUpdate.visibility = View.VISIBLE
                deleteFifthPhotoUpdate.setOnClickListener {
                    imageViewChooseImage5Update.setImageDrawable(null)
                    imageViewChooseImage5Update.visibility = View.GONE
                    deleteFifthPhotoUpdate.visibility = View.GONE
                    allPhotos[4] = ""
                }
            } else if (imageViewChooseImageUpdate.drawable != null && imageViewChooseImage2Update.drawable != null
                    && imageViewChooseImage3Update.drawable != null && imageViewChooseImage4Update.drawable != null
                    && imageViewChooseImage5Update.drawable != null && imageViewChooseImage6Update.drawable == null) {
                imageViewChooseImage6Update.visibility = View.VISIBLE
                imageViewChooseImage6Update.setImageBitmap(bitmap)
                uploadImageToStorage(5)
                deleteSixthPhotoUpdate.visibility = View.VISIBLE
                deleteSixthPhotoUpdate.setOnClickListener {
                    imageViewChooseImage6Update.setImageDrawable(null)
                    imageViewChooseImage6Update.visibility = View.GONE
                    deleteSixthPhotoUpdate.visibility = View.GONE
                    allPhotos[5] = ""
                }
            } else if (imageViewChooseImageUpdate.drawable != null && imageViewChooseImage2Update.drawable != null
                    && imageViewChooseImage3Update.drawable != null && imageViewChooseImage4Update.drawable != null
                    && imageViewChooseImage5Update.drawable != null && imageViewChooseImage6Update.drawable != null &&
                    imageViewChooseImage7Update.drawable == null) {
                imageViewChooseImage7Update.visibility = View.VISIBLE
                imageViewChooseImage7Update.setImageBitmap(bitmap)
                uploadImageToStorage(6)
                deleteSeventhPhotoUpdate.visibility = View.VISIBLE
                deleteSeventhPhotoUpdate.setOnClickListener {
                    imageViewChooseImage7Update.setImageDrawable(null)
                    imageViewChooseImage7Update.visibility = View.GONE
                    deleteSeventhPhotoUpdate.visibility = View.GONE
                    allPhotos[6] = ""
                }
            } else if (imageViewChooseImageUpdate.drawable != null && imageViewChooseImage2Update.drawable != null
                    && imageViewChooseImage3Update.drawable != null && imageViewChooseImage4Update.drawable != null
                    && imageViewChooseImage5Update.drawable != null && imageViewChooseImage6Update.drawable != null &&
                    imageViewChooseImage7Update.drawable != null && imageViewChooseImage8Update.drawable == null) {
                imageViewChooseImage8Update.visibility = View.VISIBLE
                imageViewChooseImage8Update.setImageBitmap(bitmap)
                uploadImageToStorage(7)
                deleteEighthPhotoUpdate.visibility = View.VISIBLE
                deleteEighthPhotoUpdate.setOnClickListener {
                    imageViewChooseImage8Update.setImageDrawable(null)
                    imageViewChooseImage8Update.visibility = View.GONE
                    deleteEighthPhotoUpdate.visibility = View.GONE
                    allPhotos[7] = ""
                }
            } else if (imageViewChooseImageUpdate.drawable != null && imageViewChooseImage2Update.drawable != null
                    && imageViewChooseImage3Update.drawable != null && imageViewChooseImage4Update.drawable != null
                    && imageViewChooseImage5Update.drawable != null && imageViewChooseImage6Update.drawable != null &&
                    imageViewChooseImage7Update.drawable != null && imageViewChooseImage8Update.drawable != null &&
                    imageViewChooseImage9Update.drawable == null) {
                imageViewChooseImage9Update.visibility = View.VISIBLE
                imageViewChooseImage9Update.setImageBitmap(bitmap)
                uploadImageToStorage(8)
                deleteNinthPhotoUpdate.visibility = View.VISIBLE
                deleteNinthPhotoUpdate.setOnClickListener {
                    imageViewChooseImage9Update.setImageDrawable(null)
                    imageViewChooseImage9Update.visibility = View.GONE
                    deleteNinthPhotoUpdate.visibility = View.GONE
                    allPhotos[8] = ""
                }
            } else if (imageViewChooseImageUpdate.drawable != null && imageViewChooseImage2Update.drawable != null
                    && imageViewChooseImage3Update.drawable != null && imageViewChooseImage4Update.drawable != null
                    && imageViewChooseImage5Update.drawable != null && imageViewChooseImage6Update.drawable != null &&
                    imageViewChooseImage7Update.drawable != null && imageViewChooseImage8Update.drawable != null &&
                    imageViewChooseImage9Update.drawable != null && imageViewChooseImage10Update.drawable == null) {
                imageViewChooseImage10Update.visibility = View.VISIBLE
                imageViewChooseImage10Update.setImageBitmap(bitmap)
                uploadImageToStorage(9)
                deleteTenthPhotoUpdate.visibility = View.VISIBLE
                deleteTenthPhotoUpdate.setOnClickListener {
                    imageViewChooseImage10Update.setImageDrawable(null)
                    imageViewChooseImage10Update.visibility = View.GONE
                    deleteTenthPhotoUpdate.visibility = View.GONE
                    allPhotos[9] = ""
                }
            }
        }
    }

    private fun uploadImageToStorage(photoCount: Int) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.show()
        val uniqueId = UUID.randomUUID().toString()
        val imageRef = FirebaseStorage.getInstance().reference.child("images/$uniqueId.jpg")
        imageRef.putFile(filePath)
                .addOnSuccessListener { p0 ->
                    progressDialog.dismiss()
                    val storage = FirebaseStorage.getInstance()
                    storage.getReference("images/$uniqueId.jpg").downloadUrl.addOnSuccessListener {
                        singleListingViewModel.putPhotoReference(photoCount, it.toString())
                    }
                }
                .addOnFailureListener { p0 ->
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), p0.message, Toast.LENGTH_LONG).show()

                }
                .addOnProgressListener { p0 ->
                    val progress = (100.0 * p0.bytesTransferred) / p0.totalByteCount
                    progressDialog.setMessage("Uploaded ${progress.toInt()}%")
                }
        progressDialog.dismiss()
    }


    private fun showDeletePhotoButtons() {
        val photoOne = allPhotos.getOrDefault(0, "")
        val photoTwo = allPhotos.getOrDefault(1, "")
        val photoThree = allPhotos.getOrDefault(2, "")
        val photoFour = allPhotos.getOrDefault(3, "")
        val photoFive = allPhotos.getOrDefault(4, "")
        val photoSix = allPhotos.getOrDefault(5, "")
        val photoSeven = allPhotos.getOrDefault(6, "")
        val photoEight = allPhotos.getOrDefault(7, "")
        val photoNine = allPhotos.getOrDefault(8, "")
        val photoTen = allPhotos.getOrDefault(9, "")
        if (!photoOne.equals("")) {
            deleteFirstPhotoUpdate.visibility = View.VISIBLE
        }
        if (!photoTwo.equals("")) {
            deleteSecondPhotoUpdate.visibility = View.VISIBLE
        }
        if (!photoThree.equals("")) {
            deleteThirdPhotoUpdate.visibility = View.VISIBLE
        }
        if (!photoFour.equals("")) {
            deleteFourthPhotoUpdate.visibility = View.VISIBLE
        }
        if (!photoFive.equals("")) {
            deleteFifthPhotoUpdate.visibility = View.VISIBLE
        }
        if (!photoSix.equals("")) {
            deleteSixthPhotoUpdate.visibility = View.VISIBLE
        }
        if (!photoSeven.equals("")) {
            deleteSeventhPhotoUpdate.visibility = View.VISIBLE
        }
        if (!photoEight.equals("")) {
            deleteEighthPhotoUpdate.visibility = View.VISIBLE
        }
        if (!photoNine.equals("")) {
            deleteNinthPhotoUpdate.visibility = View.VISIBLE
        }
        if (!photoTen.equals("")) {
            deleteTenthPhotoUpdate.visibility = View.VISIBLE
        }
    }

    private fun getCheckedCategories(): List<String> = listOfNotNull(
            "Bar".takeIf { barCheckboxUpdate.isChecked },
            "School".takeIf { schoolCheckboxUpdate.isChecked },
            "Park".takeIf { parkCheckboxUpdate.isChecked },
            "Restaurant".takeIf { restaurantCheckboxUpdate.isChecked },
            "Hospital".takeIf { hospitalCheckboxUpdate.isChecked }
    )

    private fun enterDates() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val enterForSaleDate = activity?.findViewById<EditText>(R.id.editTextEnterDatePutOnMarketUpdate)
        val enterSoldDate = activity?.findViewById<EditText>(R.id.editTextEnterSaleDateUpdate)
        enterForSaleDate?.isFocusable = false
        enterSoldDate?.isFocusable = false

        enterForSaleDate?.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { view, Year, Month, Day ->
                        var myMonth = (Month + 1).toString()
                        var myDay = Day.toString()
                        if (myMonth.length < 2) {
                            myMonth = "0$myMonth"
                        }
                        if (myDay.length < 2) {
                            myDay = "0$myDay"
                        }
                        if (isLocaleInAmerica()) {
                            enterForSaleDate.setText("  $myMonth/$myDay/$Year")
                        }
                        else {
                            enterForSaleDate.setText("  $myDay/$myMonth/$Year")
                        }

                    },
                    year,
                    month,
                    day
            )
            datePickerDialog.show()
        }

        enterSoldDate?.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { view, Year, Month, Day ->
                        var myMonth = (Month + 1).toString()
                        var myDay = Day.toString()
                        if (myMonth.length < 2) {
                            myMonth = "0$myMonth"
                        }
                        if (myDay.length < 2) {
                            myDay = "0$myDay"
                        }
                        if (isLocaleInAmerica()) {
                            enterSoldDate.setText("  $myMonth/$myDay/$Year")
                        }
                        else {
                            enterSoldDate.setText("  $myDay/$myMonth/$Year")
                        }
                    },
                    year,
                    month,
                    day
            )
            datePickerDialog.show()
        }
    }

    private fun setListingUpdateButtonVisibility() {
        buttonEditListingUpdate.visibility = View.GONE
        buttonSaveListingUpdate.visibility = View.GONE
    }

    private fun handleGuestUser() {
        if (firebaseAuth.currentUser?.email.isNullOrEmpty()) {
            setListingUpdateButtonVisibility()
        }
    }
}
