package com.openclassrooms.realestatemanager.receiver

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.NotificationTarget
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.data.repository.ListingRepository
import com.openclassrooms.realestatemanager.ui.activities.LoginActivity
import com.openclassrooms.realestatemanager.ui.activities.MainActivity
import com.openclassrooms.realestatemanager.viewmodels.SingleListingViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val listingId = sharedPreferences.getLong("id", 0L)
        val listingAddress = sharedPreferences.getString("address", null)
        val address: String = if (listingAddress.isNullOrEmpty()) {
            "(Missing Address)"
        }
        else {
            listingAddress
        }
        if (listingId != 0L) {
            fireNotification(id = listingId, context = context, listingAddress = address, firebaseAuth = firebaseAuth)
        }
    }

    private fun fireNotification(
            firebaseAuth: FirebaseAuth,
            id: Long,
            context: Context,
            listingAddress: String) {
        val notificationIntent: Intent = if (firebaseAuth.currentUser?.email.isNullOrEmpty()) {
            Intent(context, LoginActivity::class.java)
        } else {
            Intent(context, MainActivity::class.java)
        }
        notificationIntent.putExtra("id_from_notification", id.toInt())
        val contentIntent = PendingIntent.getActivity(
                context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, "listing")
                .setSmallIcon(R.drawable.defaulthomeimage)
                .setContentTitle("Your Listing is now live!")
                .setStyle(NotificationCompat.BigTextStyle().bigText("Your listing at $listingAddress is now live!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(Color.parseColor("#3F51B5"))

        builder.setContentIntent(contentIntent)
        builder.setDefaults(Notification.DEFAULT_SOUND)
        builder.setAutoCancel(true)
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(200, builder.build())
    }
}

