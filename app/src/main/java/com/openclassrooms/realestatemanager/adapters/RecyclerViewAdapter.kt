package com.openclassrooms.realestatemanager.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import kotlinx.android.synthetic.main.listing_row.view.*

class RecyclerViewAdapter: RecyclerView.Adapter<RecyclerViewAdapter.Viewholder>() {

    var items = ArrayList<ListingEntity>()

    fun setListData(data: ArrayList<ListingEntity>) {
        this.items = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.Viewholder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.listing_row, parent, false)
        return Viewholder(inflater)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.Viewholder, position: Int) {
        holder.bind(items[position])
    }

    class Viewholder(view: View): RecyclerView.ViewHolder(view) {
        val statusTextview = view.statusTextview
        val addressTextview = view.addressTextview
        val numberOfRoomsTextview = view.numberOfRoomsTextview
        val surfaceAreaTextview = view.surfaceAreaTextview
        val listingPhotoImageview = view.listingPhoto

        fun bind(data: ListingEntity) {
            statusTextview.text = data.status
            addressTextview.text = data.address
            numberOfRoomsTextview.text = data.numberOfRooms
            surfaceAreaTextview.text = data.surfaceArea
            val storage = FirebaseStorage.getInstance()
//            val gsReference = storage.getReferenceFromUrl("images/0f1b3e7d-ea0e-4dc79c93-69d3abb1554e.jpg")
//            val storage = FirebaseStorage.getInstance().reference.child("images/0f1b3e7d-ea0e-4dc79c93-69d3abb1554e.jpg")
//            val imageref = FirebaseStorage.getInstance().reference.child("images/$data.photoReference.jpg")
//            val storageReference = FirebaseStorage.getInstance().reference.child("images/${data.photoReference}.jpg")
            storage.getReference("images/").listAll().addOnSuccessListener {
                for (file in it.items) {
                    file.downloadUrl.addOnSuccessListener {uri->
                        Glide.with(listingPhotoImageview)
                                .load(uri)
                                .into(listingPhotoImageview)
                    }

                }
            }
//
//            storage.reference.child("images/${data.photoReference}.jpg").downloadUrl.addOnSuccessListener {uri->
//                Glide.with(listingPhotoImageview)
//                        .load(uri)
//                        .into(listingPhotoImageview)
//            }.addOnFailureListener {
//                Log.d("whyerror", it.toString())
//            }


//            val irr = storage.reference.child("images/0f1b3e7d-ea0e-4dc79c93-69d3abb1554e.jpg").downloadUrl.addOnSuccessListener {
//                    Log.d("erroruri", ir.toString())




//            }






        }
    }
}