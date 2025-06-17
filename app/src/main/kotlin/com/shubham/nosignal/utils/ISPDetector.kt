package com.shubham.nosignal.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import android.util.Log
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
                            isp.isNotEmpty() -> isp
                            org.isNotEmpty() -> org.removePrefix("AS\\d+\\s+".toRegex().toString())
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
     * Get cellular ISP information
     */
    private fun getCellularISP(): String {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            
            val networkOperatorName = telephonyManager.networkOperatorName
            val simOperatorName = telephonyManager.simOperatorName
            
            return when {
                !networkOperatorName.isNullOrEmpty() -> networkOperatorName
                !simOperatorName.isNullOrEmpty() -> simOperatorName
                else -> "Cellular Network"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cellular ISP", e)
            return "Cellular Network"
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