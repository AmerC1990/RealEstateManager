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
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils.convertDateFromWorldToUSA
import com.openclassrooms.realestatemanager.Utils.convertEuroToDollar
import com.openclassrooms.realestatemanager.Utils.doesLocaleSubscribeToEuroCurrency
import com.openclassrooms.realestatemanager.Utils.isLocaleInAmerica
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.receiver.AlarmReceiver
import com.openclassrooms.realestatemanager.ui.activities.MainActivity
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import com.openclassrooms.realestatemanager.viewmodels.SingleListingViewModel
import kotlinx.android.synthetic.main.fragment_listing_form.*
import kotlinx.android.synthetic.main.fragment_listing_form.barCheckbox
import kotlinx.android.synthetic.main.fragment_listing_form.hospitalCheckbox
import kotlinx.android.synthetic.main.fragment_listing_form.parkCheckbox
import kotlinx.android.synthetic.main.fragment_listing_form.restaurantCheckbox
import kotlinx.android.synthetic.main.fragment_listing_form.schoolCheckbox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*


class ListingFormFragment : Fragment() {
    private lateinit var alarmManager: AlarmManager
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    private val channelId = "listing"
    private val description = "notification"
    lateinit var filePath: Uri
    private val viewModel: ListingsViewModel by inject()
    private val singleListingViewModel: SingleListingViewModel by inject()
    private val allPhotos = mutableMapOf<Int, String>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        return if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet") {
            return inflater.inflate(R.layout.listing_form_fagment_tablet, container, false)
        }
        else {
            return inflater.inflate(R.layout.fragment_listing_form, container, false)
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
        if (doesLocaleSubscribeToEuroCurrency()) {
            editTextEnterPrice.hint = getString(R.string.price_of_property_euro)
        }
        if (!isLocaleInAmerica()) {
            editTextEnterDatePutOnMarket.hint =  getString(R.string.date_put_on_market_non_american)
            editTextEnterSaleDate.hint = getString(R.string.date_sold_hint_non_american)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpDatePickers()

        val types = resources.getStringArray(R.array.type_of_listing_spinner)
        val statuses = resources.getStringArray(R.array.status_of_property_spinner)

        val typeOfListingSpinner = activity?.findViewById<Spinner>(R.id.typeOfListingSpinner)
        val statusOfPropertySpinner = activity?.findViewById<Spinner>(R.id.statusOfPropertySpinner)
        if (typeOfListingSpinner != null) {
            val adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, types)
            typeOfListingSpinner.adapter = adapter

        }
        if (statusOfPropertySpinner != null) {
            val adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, statuses)
            statusOfPropertySpinner.adapter = adapter

        }
        attachObservers()
        insertListing()
    }

    private fun setUpDatePickers() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val enterForSaleDate = activity?.findViewById<EditText>(R.id.editTextEnterDatePutOnMarket)
        val enterSoldDate = activity?.findViewById<EditText>(R.id.editTextEnterSaleDate)
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

    private fun insertListing() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
        addImageIcon.setOnClickListener {
            startFileChooser()
        }
        buttonSaveListing.setOnClickListener {
            buttonSaveListing.startAnimation(animation)
            insertListingData()
        }
    }

    private fun insertListingData() {
        newListingProgressBar.visibility = View.VISIBLE
        val typeOfListingSpinner = activity?.findViewById<Spinner>(R.id.typeOfListingSpinner)
        val statusOfPropertySpinner = activity?.findViewById<Spinner>(R.id.statusOfPropertySpinner)
        var typeOfListingText = typeOfListingSpinner?.selectedItem.toString()
        var statusOfPropertyText = statusOfPropertySpinner?.selectedItem.toString()
        if (typeOfListingText.contains("Select")) {
            typeOfListingText = ""
        }
        if (statusOfPropertyText.contains("Select")) {
            statusOfPropertyText = ""
        }
        var pointsOfInterestList = ""
        if (getCheckedCategories().isNotEmpty()) {
            pointsOfInterestList = getCheckedCategories().toString()
        }

        val imageDescription = editTextEnterImageDescription.text.toString()
        val typeOfListing = typeOfListingText
        var priceOfListing = ""
        if (doesLocaleSubscribeToEuroCurrency()) {
            priceOfListing = convertEuroToDollar(editTextEnterPrice.text.toString().toInt()).toString()
        }
        else if (!doesLocaleSubscribeToEuroCurrency()) {
            priceOfListing = editTextEnterPrice.text.toString()
        }

        val surfaceArea = editTextEnterSurfaceArea.text.toString()
        val numberOfRooms = editTextEnterNumberOfRooms.text.toString()
        val descriptionOfListing = editTextEnterDescription.text.toString()
        val addressOfListing = editTextEnterAddress.text.toString()
        val pointsOfInterest = pointsOfInterestList
        val statusOfListing = statusOfPropertyText
        var dateOnMarket = ""
        if (isLocaleInAmerica()) {
            dateOnMarket = editTextEnterDatePutOnMarket.text.toString()
        }
        else if (!isLocaleInAmerica()) {
            dateOnMarket = convertDateFromWorldToUSA(editTextEnterDatePutOnMarket.text.toString())
        }
        var saleDateOfListing = ""
        if (isLocaleInAmerica()) {
            saleDateOfListing = editTextEnterSaleDate.text.toString()
        }
        else if (!isLocaleInAmerica()) {
            saleDateOfListing = convertDateFromWorldToUSA(editTextEnterSaleDate.text.toString())
        }
        val nameOfAgent = editTextEnterNameOfAgent.text.toString()
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
        newListingProgressBar.visibility = View.GONE
        val sharedPrefs = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.apply {
            putString("address", addressOfListing)
        }
                .apply()
        viewModel.insertListingInfo(listing)
    }

    private fun overrideOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragmentContainer, AllListingsFragment())
                commit()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, AllListingsFragment())
            commit()
        }
        return true
    }

    private fun startFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Choose Picture"), 111)
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

    private fun attachObservers() {
        lifecycleScope.launchWhenCreated {
            singleListingViewModel.uiState.collect { uiState ->
                when (uiState) {
                    is SingleListingViewModel.SingleListingState.SuccessPhoto -> {
                        allPhotos.put(uiState.photoCount, uiState.photoReference)
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is ListingsViewModel.ListingState.SuccessSingle -> {
                        newListingProgressBar.visibility = View.GONE
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Listing Saved", Toast.LENGTH_LONG).show()
                            val sharedPrefs = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPrefs.edit()
                            editor.apply {
                                putLong("id", uiState.listingId)
                            }
                                    .apply()
                            setAlarm()
                            delay(1500)
                            if (activity?.applicationContext?.let { getDeviceInfo(it, Device.DEVICE_TYPE) } == "Tablet" &&
                                    resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                activity?.supportFragmentManager?.beginTransaction()?.apply {
                                    replace(R.id.containerForAllListings, AllListingsFragment())
                                    commit()
                                }
                            }
                            else {
                                activity?.supportFragmentManager?.beginTransaction()?.apply {
                                    replace(R.id.fragmentContainer, AllListingsFragment())
                                    commit()
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == Activity.RESULT_OK && data != null) {
            filePath = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, filePath)

            if (imageViewChooseImage.drawable == null && imageViewChooseImage2.drawable == null) {
                imageViewChooseImage.visibility = View.VISIBLE
                imageViewChooseImage.setImageBitmap(bitmap)
                uploadImageToStorage(0)
                deleteFirstPhoto.visibility = View.VISIBLE
                deleteFirstPhoto.setOnClickListener {
                    imageViewChooseImage.setImageDrawable(null)
                    imageViewChooseImage.visibility = View.GONE
                    deleteFirstPhoto.visibility = View.GONE
                    allPhotos.put(0, "")
                }
            } else if (imageViewChooseImage.drawable != null && imageViewChooseImage2.drawable == null) {
                imageViewChooseImage2.visibility = View.VISIBLE
                imageViewChooseImage2.setImageBitmap(bitmap)
                uploadImageToStorage(1)
                deleteSecondPhoto.visibility = View.VISIBLE
                deleteSecondPhoto.setOnClickListener {
                    imageViewChooseImage2.setImageDrawable(null)
                    imageViewChooseImage2.visibility = View.GONE
                    deleteSecondPhoto.visibility = View.GONE
                    allPhotos.put(1, "")
                }
            } else if (imageViewChooseImage.drawable != null && imageViewChooseImage2.drawable != null
                    && imageViewChooseImage3.drawable == null) {
                imageViewChooseImage3.visibility = View.VISIBLE
                imageViewChooseImage3.setImageBitmap(bitmap)
                uploadImageToStorage(2)
                deleteThirdPhoto.visibility = View.VISIBLE
                deleteThirdPhoto.setOnClickListener {
                    imageViewChooseImage3.setImageDrawable(null)
                    imageViewChooseImage3.visibility = View.GONE
                    deleteThirdPhoto.visibility = View.GONE
                    allPhotos.put(2, "")
                }
            } else if (imageViewChooseImage.drawable != null && imageViewChooseImage2.drawable != null
                    && imageViewChooseImage3.drawable != null && imageViewChooseImage4.drawable == null) {
                imageViewChooseImage4.visibility = View.VISIBLE
                imageViewChooseImage4.setImageBitmap(bitmap)
                uploadImageToStorage(3)
                deleteFourthPhoto.visibility = View.VISIBLE
                deleteFourthPhoto.setOnClickListener {
                    imageViewChooseImage4.setImageDrawable(null)
                    imageViewChooseImage4.visibility = View.GONE
                    deleteFourthPhoto.visibility = View.GONE
                    allPhotos.put(3, "")
                }
            } else if (imageViewChooseImage.drawable != null && imageViewChooseImage2.drawable != null
                    && imageViewChooseImage3.drawable != null && imageViewChooseImage4.drawable != null
                    && imageViewChooseImage5.drawable == null) {
                imageViewChooseImage5.visibility = View.VISIBLE
                imageViewChooseImage5.setImageBitmap(bitmap)
                uploadImageToStorage(4)
                deleteFifthPhoto.visibility = View.VISIBLE
                deleteFifthPhoto.setOnClickListener {
                    imageViewChooseImage5.setImageDrawable(null)
                    imageViewChooseImage5.visibility = View.GONE
                    deleteFifthPhoto.visibility = View.GONE
                    allPhotos.put(4, "")
                }
            } else if (imageViewChooseImage.drawable != null && imageViewChooseImage2.drawable != null
                    && imageViewChooseImage3.drawable != null && imageViewChooseImage4.drawable != null
                    && imageViewChooseImage5.drawable != null && imageViewChooseImage6.drawable == null) {
                imageViewChooseImage6.visibility = View.VISIBLE
                imageViewChooseImage6.setImageBitmap(bitmap)
                uploadImageToStorage(5)
                deleteSixthPhoto.visibility = View.VISIBLE
                deleteSixthPhoto.setOnClickListener {
                    imageViewChooseImage6.setImageDrawable(null)
                    imageViewChooseImage6.visibility = View.GONE
                    deleteSixthPhoto.visibility = View.GONE
                    allPhotos.put(5, "")
                }
            } else if (imageViewChooseImage.drawable != null && imageViewChooseImage2.drawable != null
                    && imageViewChooseImage3.drawable != null && imageViewChooseImage4.drawable != null
                    && imageViewChooseImage5.drawable != null && imageViewChooseImage6.drawable != null &&
                    imageViewChooseImage7.drawable == null) {
                imageViewChooseImage7.visibility = View.VISIBLE
                imageViewChooseImage7.setImageBitmap(bitmap)
                uploadImageToStorage(6)
                deleteSeventhPhoto.visibility = View.VISIBLE
                deleteSeventhPhoto.setOnClickListener {
                    imageViewChooseImage7.setImageDrawable(null)
                    imageViewChooseImage7.visibility = View.GONE
                    deleteSeventhPhoto.visibility = View.GONE
                    allPhotos.put(6, "")
                }
            } else if (imageViewChooseImage.drawable != null && imageViewChooseImage2.drawable != null
                    && imageViewChooseImage3.drawable != null && imageViewChooseImage4.drawable != null
                    && imageViewChooseImage5.drawable != null && imageViewChooseImage6.drawable != null &&
                    imageViewChooseImage7.drawable != null && imageViewChooseImage8.drawable == null) {
                imageViewChooseImage8.visibility = View.VISIBLE
                imageViewChooseImage8.setImageBitmap(bitmap)
                uploadImageToStorage(7)
                deleteEighthPhoto.visibility = View.VISIBLE
                deleteEighthPhoto.setOnClickListener {
                    imageViewChooseImage8.setImageDrawable(null)
                    imageViewChooseImage8.visibility = View.GONE
                    deleteEighthPhoto.visibility = View.GONE
                    allPhotos.put(7, "")
                }
            } else if (imageViewChooseImage.drawable != null && imageViewChooseImage2.drawable != null
                    && imageViewChooseImage3.drawable != null && imageViewChooseImage4.drawable != null
                    && imageViewChooseImage5.drawable != null && imageViewChooseImage6.drawable != null &&
                    imageViewChooseImage7.drawable != null && imageViewChooseImage8.drawable != null &&
                    imageViewChooseImage9.drawable == null) {
                imageViewChooseImage9.visibility = View.VISIBLE
                imageViewChooseImage9.setImageBitmap(bitmap)
                uploadImageToStorage(8)
                deleteNinthPhoto.visibility = View.VISIBLE
                deleteNinthPhoto.setOnClickListener {
                    imageViewChooseImage9.setImageDrawable(null)
                    imageViewChooseImage9.visibility = View.GONE
                    deleteNinthPhoto.visibility = View.GONE
                    allPhotos[8] = ""
                }
            } else if (imageViewChooseImage.drawable != null && imageViewChooseImage2.drawable != null
                    && imageViewChooseImage3.drawable != null && imageViewChooseImage4.drawable != null
                    && imageViewChooseImage5.drawable != null && imageViewChooseImage6.drawable != null &&
                    imageViewChooseImage7.drawable != null && imageViewChooseImage8.drawable != null &&
                    imageViewChooseImage9.drawable != null && imageViewChooseImage10.drawable == null) {
                imageViewChooseImage10.visibility = View.VISIBLE
                imageViewChooseImage10.setImageBitmap(bitmap)
                uploadImageToStorage(9)
                deleteTenthPhoto.visibility = View.VISIBLE
                deleteTenthPhoto.setOnClickListener {
                    imageViewChooseImage10.setImageDrawable(null)
                    imageViewChooseImage10.visibility = View.GONE
                    deleteTenthPhoto.visibility = View.GONE
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

    private fun getCheckedCategories(): List<String> = listOfNotNull(
            "Bar".takeIf { barCheckbox.isChecked },
            "School".takeIf { schoolCheckbox.isChecked },
            "Park".takeIf { parkCheckbox.isChecked },
            "Restaurant".takeIf { restaurantCheckbox.isChecked },
            "Hospital".takeIf { hospitalCheckbox.isChecked }
    )
}


