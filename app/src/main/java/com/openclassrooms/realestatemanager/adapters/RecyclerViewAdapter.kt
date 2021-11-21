package com.openclassrooms.realestatemanager.adapters

import android.graphics.Color
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.ui.fragments.ListingFormFragment
import com.openclassrooms.realestatemanager.ui.fragments.ViewAndUpdateListingFragment
import kotlinx.android.synthetic.main.fragment_listing_form.*
import kotlinx.android.synthetic.main.listing_row.view.*

class RecyclerViewAdapter: RecyclerView.Adapter<RecyclerViewAdapter.Viewholder>() {

    var items = ArrayList<ListingEntity>()

    fun setListData(data: ArrayList<ListingEntity>) {
        this.items = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.listing_row, parent, false)
        return Viewholder(inflater)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.bind(items[position])
    }

    class Viewholder(view: View): RecyclerView.ViewHolder(view) {
        private val statusTextview: TextView = view.statusTextview
        private val addressTextview: TextView = view.addressTextview
        private val numberOfRoomsTextview: TextView = view.numberOfRoomsTextview
        private val surfaceAreaTextview: TextView = view.surfaceAreaTextview
        private val listingPhotoImageview: ImageView = view.listingPhoto
        private val mapImageView: ImageView = view.mapImageView

        fun bind(data: ListingEntity) {
            Log.d("debugmobile", "regular adapter bind")
            statusTextview.text = data.status
            if (statusTextview.text.toString().contains("For Sale")) {
                statusTextview.setTextColor(Color.parseColor("#20AA32"))
            } else if (statusTextview.text.toString().contains("Sold")) {
                statusTextview.setTextColor(Color.parseColor("#EF0A0A"))
            } else if(!statusTextview.text.toString().contains("For Sale",ignoreCase = true) &&
                    !statusTextview.text.toString().contains("Sold",ignoreCase = true)) {
                statusTextview.text = ""
            }
            addressTextview.text = String.format("\n" + addressTextview.context.getString(R.string.address) + ":  " + "\n" + "\n" + data.address)
            numberOfRoomsTextview.text = String.format("\n" + numberOfRoomsTextview.context.getString(R.string.number_of_rooms) + ": " + "\n" + "\n" + data.numberOfRooms)
            surfaceAreaTextview.text = String.format("\n" + surfaceAreaTextview.context.getString(R.string.surface) + ": " + "\n" + "\n" + data.surfaceArea)

            val allPhotoUrls = listOf(data.photoReference,
                    data.photoReference2,
                    data.photoReference3,
                    data.photoReference4,
                    data.photoReference5,
                    data.photoReference6,
                    data.photoReference7,
                    data.photoReference8,
                    data.photoReference9,
                    data.photoReference10)


            val nullablePhotoUrl = allPhotoUrls.find { it != "" }
            val photoUrl = nullablePhotoUrl ?: ""

            Glide.with(listingPhotoImageview)
                    .load(photoUrl)
                    .centerCrop()
                    .error(R.drawable.noimageavailable)
                    .into(listingPhotoImageview)

            val address = data.address
            val url = "https://maps.googleapis.com/maps/api/staticmap?center=" +
                    "${address}&zoom=15&size=200x200&maptype=satellite&scale=2&markers=size:mid%7Ccolor:red%7C${address}&key=AIzaSyCjjNtiV2d0NKjAmJk-G6Sge8LLQe58f4A"

            Glide.with(mapImageView)
                    .asBitmap()
                    .load(url)
                    .centerCrop()
                    .error(R.drawable.mapimage)
                    .into(mapImageView)

            itemView.setOnClickListener {
                val id = data.id.toString()
                val bundle = Bundle()
                bundle.putString("listing_id", id)
                val activity = itemView.context as AppCompatActivity

                val transaction = activity.supportFragmentManager.beginTransaction()
                if (activity.findViewById<FrameLayout>(R.id.fragmentContainer) != null) {
                    val fragment = ViewAndUpdateListingFragment()
                    fragment.arguments = bundle
                    transaction.replace(R.id.fragmentContainer, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
                if (activity.findViewById<FrameLayout>(R.id.containerForListingDetails) != null) {
//                    itemView.setBackgroundColor(Color.parseColor("#B3D8E8"))
                    val fragment = ViewAndUpdateListingFragment()
                    fragment.arguments = bundle
                    transaction.replace(R.id.containerForListingDetails, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
        }
    }
}