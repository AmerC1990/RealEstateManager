package com.openclassrooms.realestatemanager

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by Philippe on 21/02/2018.
 */
object Utils {
    /**
     * Conversion d'un prix d'un bien immobilier (Dollars vers Euros)
     * NOTE : NE PAS SUPPRIMER, A MONTRER DURANT LA SOUTENANCE
     * @param dollars
     * @return
     */
    fun convertDollarToEuro(dollars: Int): Int {
        return (dollars * 0.812).roundToInt().toInt()
    }

    fun convertEuroToDollar(euros: Int): Int {
        return (euros * 1.4389).roundToInt().toInt()
    }

    /**
     * Conversion de la date d'aujourd'hui en un format plus approprié
     * NOTE : NE PAS SUPPRIMER, A MONTRER DURANT LA SOUTENANCE
     * @return
     */
//    val todayDate: String
//        get() {
//            val dateFormat: DateFormat = SimpleDateFormat("MM/dd/yyyy")
//            return dateFormat.format(Date())
//        }

    fun convertDateFromWorldToUSA(date: String): String {
        val currentDate = LocalDate.parse(date)
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        return currentDate.format(formatter)
    }

    fun convertDateFromUSAToWorld(date: String): String {
        val currentDate = LocalDate.parse(date)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return currentDate.format(formatter)
    }


    /**
     * Vérification de la connexion réseau
     * NOTE : NE PAS SUPPRIMER, A MONTRER DURANT LA SOUTENANCE
     * @param context
     * @return
     */
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    fun isLocaleInAmerica():Boolean {
        val defaultLocale = Resources.getSystem().configuration.locales
        return defaultLocale.toString().contains("en_US")
    }

    fun doesLocaleSubscribeToEuroCurrency(): Boolean {
        val defaultLocale = Resources.getSystem().configuration.locales
        return (defaultLocale.toString().contains("de_DE")
                || defaultLocale.toString().contains("ca_ES")
                || defaultLocale.toString().contains("en_IE")
                || defaultLocale.toString().contains("fi_FI")
                || defaultLocale.toString().contains("fr_BE")
                || defaultLocale.toString().contains("fr_FR")
                || defaultLocale.toString().contains("de_AT")
                || defaultLocale.toString().contains("de_DE")
                || defaultLocale.toString().contains("de_LI")
                || defaultLocale.toString().contains("el_GR")
                || defaultLocale.toString().contains("it_IT")
                || defaultLocale.toString().contains("lv_LV")
                || defaultLocale.toString().contains("lt_LT")
                || defaultLocale.toString().contains("pt_PT")
                || defaultLocale.toString().contains("sk_SK")
                || defaultLocale.toString().contains("sl_SI")
                || defaultLocale.toString().contains("es_ES")
                || defaultLocale.toString().contains("sv_SE")
                || defaultLocale.toString().contains("tr_TR"))
    }

     fun euroOrDollar(): String {
        return if (Utils.doesLocaleSubscribeToEuroCurrency()) {
            "\u20ac"
        }
        else {
            "$"
        }
    }
}