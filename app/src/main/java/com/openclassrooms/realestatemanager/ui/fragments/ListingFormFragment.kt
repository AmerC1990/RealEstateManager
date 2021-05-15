package com.openclassrooms.realestatemanager.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.android.synthetic.main.fragment_listing_form.*
import java.util.*


class ListingFormFragment : Fragment() {
    lateinit var  filePath: Uri
    private var viewModel = ListingsViewModel()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_listing_form, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overrideOnBackPressed()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        insertListing()
    }

    private fun insertListing() {
        buttonChooseImage.setOnClickListener {
            startFileChooser()
        }
//        buttonUploadImage.setOnClickListener {
//             uploadImage()
//        }
        buttonSaveListing.setOnClickListener {
            viewModel.insertListingInfo(insertListingData(uploadImage()))
        }



    }

    private fun insertListingData(uniqueId: String): ListingEntity {
        val imageDescription = editTextEnterImageDescription.text.toString()
        val typeOfListing = editTextEnterType.text.toString()
        val priceOfListing = editTextEnterPrice.text.toString()
        val surfaceArea = editTextEnterSurfaceArea.text.toString()
        val numberOfRooms = editTextEnterNumberOfRooms.text.toString()
        val descriptionOfListing = editTextEnterDescription.text.toString()
        val addressOfListing = editTextEnterAddress.text.toString()
        val pointsOfInterest = editTextEnterPointsOfInterest.text.toString()
        val statusOfListing = editTextEnterStatus.text.toString()
        val dateOnMarket = editTextEnterDatePutOnMarket.text.toString()
        val saleDateOfListing = editTextEnterSaleDate.text.toString()
        val nameOfAgent = editTextEnterNameOfAgent.text.toString()

        return ListingEntity(photoReference = uniqueId,
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
    }

    private fun uploadImage(): String {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Uploading")
        progressDialog.show()
        val uniqueId = UUID.randomUUID().toString()

        val imageRef = FirebaseStorage.getInstance().reference.child("images/$uniqueId.jpg")
//        val imageRef = FirebaseStorage.getInstance().reference.child("images/amer.jpg")
        imageRef.putFile(filePath)
                .addOnSuccessListener {p0 ->
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "File Uploaded", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {p0 ->
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), p0.message, Toast.LENGTH_LONG).show()

                }
                .addOnProgressListener {p0 ->
                    val progress = (100.0 * p0.bytesTransferred) / p0.totalByteCount
                    progressDialog.setMessage("Uploaded ${progress.toInt()}%")
                }
        return uniqueId
    }

    private fun overrideOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback {
            activity?.supportFragmentManager?.popBackStack()
        }
    }

        private fun startFileChooser() {
            val i = Intent()
            i.setType("image/*")
            i.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(i,"Choose Picture"), 111)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == Activity.RESULT_OK && data != null) {
            filePath = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, filePath)
            imageView.setImageBitmap(bitmap)
        }
    }
    }


