package com.shubham.nosignal.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Utility class for detecting ISP information
 */
class ISPDetector(private val context: Context) {
    
    companion object {
        private const val TAG = "ISPDetector"
        private const val IP_INFO_URL = "https://ipinfo.io/json"
        private const val TIMEOUT_SECONDS = 10L
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    /**
     * Get ISP name using multiple detection methods
     */
    suspend fun getISPName(): String = withContext(Dispatchers.IO) {
        try {
            // First try online IP info service
            val onlineISP = getISPFromOnlineService()
            if (onlineISP != "Unknown ISP") {
                return@withContext onlineISP
            }
            
            // Fallback to local network info
            return@withContext getISPFromLocalNetwork()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting ISP", e)
            return@withContext "Unknown ISP"
        }
    }
    
    /**
     * Get ISP name from online IP info service
     */
    private suspend fun getISPFromOnlineService(): String = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(IP_INFO_URL)
                .header("User-Agent", "NoSignal-Android-SpeedTest/1.0")
                .build()
            
            val response = client.newCall(request).execute()
            response.use { resp ->
                if (resp.isSuccessful) {
                    val responseBody = resp.body?.string()
                    if (!responseBody.isNullOrEmpty()) {
                        val json = JSONObject(responseBody)
                        val org = json.optString("org", "")
                        val isp = json.optString("isp", "")
                        
                        return@withContext when {
                            isp.isNotEmpty() -> cleanISPName(isp)
                            org.isNotEmpty() -> cleanISPName(org)
                            else -> "Unknown ISP"
                        }
                    }
                }
            }
            return@withContext "Unknown ISP"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get ISP from online service", e)
            return@withContext "Unknown ISP"
        }
    }
    
    /**
     * Clean ISP name from response
     */
    private fun cleanISPName(rawName: String): String {
        return rawName
            .replace(Regex("^AS\\d+\\s+"), "") // Remove AS number prefix
            .replace("Limited", "Ltd")
            .replace("Private Limited", "Pvt Ltd")
            .trim()
    }
    
    /**
     * Get ISP name from local network information
     */
    private fun getISPFromLocalNetwork(): String {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            
            return when {
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                    getWiFiISP()
                }
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                    getCellularISP()
                }
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> {
                    "Ethernet Connection"
                }
                else -> "Unknown Network"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local network info", e)
            return "Unknown ISP"
        }
    }
    
    /**
     * Get WiFi ISP information
     */
    private fun getWiFiISP(): String {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val linkProperties = connectivityManager.getLinkProperties(network)
            
            // Try to get domain name from DHCP info
            val domainName = linkProperties?.domains
            if (!domainName.isNullOrEmpty()) {
                return "WiFi - $domainName"
            }
            
            return "WiFi Network"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi ISP", e)
            return "WiFi Network"
        }
    }
    
    /**
     * Get cellular ISP information with better carrier detection
     */
    private fun getCellularISP(): String {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            
            // Check if we have the required permissions
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Try without permissions first
                val networkOperatorName = telephonyManager.networkOperatorName
                if (!networkOperatorName.isNullOrEmpty()) {
                    return mapIndianCarrier(networkOperatorName)
                }
                return "Cellular Network"
            }
            
            // Get both network and SIM operator names
            val networkOperatorName = telephonyManager.networkOperatorName
            val simOperatorName = telephonyManager.simOperatorName
            val networkOperator = telephonyManager.networkOperator
            val simOperator = telephonyManager.simOperator
            
            Log.d(TAG, "Network operator: $networkOperatorName ($networkOperator)")
            Log.d(TAG, "SIM operator: $simOperatorName ($simOperator)")
            
            return when {
                !networkOperatorName.isNullOrEmpty() -> mapIndianCarrier(networkOperatorName)
                !simOperatorName.isNullOrEmpty() -> mapIndianCarrier(simOperatorName)
                !networkOperator.isNullOrEmpty() -> mapIndianCarrierByMCC(networkOperator)
                !simOperator.isNullOrEmpty() -> mapIndianCarrierByMCC(simOperator)
                else -> "Cellular Network"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cellular ISP", e)
            return "Cellular Network"
        }
    }
    
    /**
     * Map Indian carrier names to standard names
     */
    private fun mapIndianCarrier(operatorName: String): String {
        val name = operatorName.uppercase()
        return when {
            name.contains("JIO") || name.contains("RELIANCE") -> "Jio"
            name.contains("AIRTEL") -> "Airtel"
            name.contains("VI") || name.contains("VODAFONE") || name.contains("IDEA") -> "Vi (Vodafone Idea)"
            name.contains("BSNL") -> "BSNL"
            name.contains("MTNL") -> "MTNL"
            name.contains("TATA") -> "Tata Teleservices"
            name.contains("TELENOR") -> "Telenor"
            name.contains("UNINOR") -> "Uninor"
            name.contains("LOOP") -> "Loop Mobile"
            name.contains("MTS") -> "MTS"
            name.contains("RELIANCE CDMA") -> "Reliance CDMA"
            else -> operatorName
        }
    }
    
    /**
     * Map Indian carriers by MCC-MNC codes
     */
    private fun mapIndianCarrierByMCC(mccMnc: String): String {
        if (mccMnc.length < 5) return "Cellular Network"
        
        val mcc = mccMnc.substring(0, 3)
        val mnc = mccMnc.substring(3)
        
        // India MCC is 404, 405
        if (mcc != "404" && mcc != "405") return "Cellular Network"
        
        return when (mnc) {
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", 
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" -> "Airtel"
            "21", "22", "23", "24", "25", "26", "27", "28", "29", "30" -> "Airtel"
            "31" -> "Airtel"
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49" -> "Airtel"
            "50", "51", "52", "53", "54", "55", "56" -> "Jio"
            "57", "58", "59", "60", "61", "62", "63", "64", "65", "66" -> "Jio"
            "67", "68", "69", "70", "71", "72", "73", "74", "75", "76" -> "Jio"
            "77", "78", "79", "80", "81", "82", "83", "84", "85", "86" -> "Jio"
            "87", "88", "89", "90", "91", "92", "93", "94", "95", "96" -> "Jio"
            "84", "86", "89", "93" -> "Vi (Vodafone Idea)"
            "01", "04", "07", "10", "12", "13", "14", "15", "16", "17", 
            "18", "19", "20", "27", "30", "34", "37", "38", "39", "41", 
            "42", "43", "44", "45", "46", "60", "84", "86", "89", "93" -> "Vi (Vodafone Idea)"
            else -> "Cellular Network"
        }
    }
    
    /**
     * Get network type for display
     */
    fun getNetworkType(): String {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            
            return when {
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                    "Wi-Fi"
                }
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                    getCellularNetworkType()
                }
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> {
                    "Ethernet"
                }
                else -> "Unknown"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network type", e)
            return "Unknown"
        }
    }
    
    /**
     * Get detailed cellular network type
     */
    private fun getCellularNetworkType(): String {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            
            @Suppress("DEPRECATION")
            return when (telephonyManager.networkType) {
                TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSPAP -> "3G HSPA"
                TelephonyManager.NETWORK_TYPE_UMTS -> "3G UMTS"
                TelephonyManager.NETWORK_TYPE_EDGE -> "2G EDGE"
                TelephonyManager.NETWORK_TYPE_GPRS -> "2G GPRS"
                TelephonyManager.NETWORK_TYPE_GSM -> "2G GSM"
                else -> "Cellular"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cellular network type", e)
            return "Cellular"
        }
    }
} 