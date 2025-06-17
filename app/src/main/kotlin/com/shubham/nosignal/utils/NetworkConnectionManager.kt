package com.shubham.nosignal.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.telephony.SubscriptionManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Manages network connection state and provides real-time updates
 */
class NetworkConnectionManager(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    
    /**
     * Flow that emits network type changes in real-time
     */
    fun getNetworkTypeFlow(): Flow<String> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getCurrentNetworkType())
            }
            
            override fun onLost(network: Network) {
                trySend(getCurrentNetworkType())
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(getCurrentNetworkType())
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Emit initial value
        trySend(getCurrentNetworkType())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()
    
    /**
     * Get current network type as a descriptive string
     */
    fun getCurrentNetworkType(): String {
        val activeNetwork = connectivityManager.activeNetwork ?: return "Offline"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "Offline"
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                getWifiNetworkName()
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                getCellularNetworkName()
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                "Ethernet"
            }
            else -> "Unknown"
        }
    }
    
    /**
     * Get Wi-Fi network name (SSID)
     */
    private fun getWifiNetworkName(): String {
        return try {
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo?.ssid?.replace("\"", "") // Remove quotes
            if (ssid.isNullOrEmpty() || ssid == "<unknown ssid>") {
                "Wi-Fi"
            } else {
                "Wi-Fi: $ssid"
            }
        } catch (e: SecurityException) {
            // Location permission might be required for SSID
            "Wi-Fi"
        }
    }
    
    /**
     * Get cellular network name with SIM slot detection
     */
    private fun getCellularNetworkName(): String {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList
                
                when {
                    activeSubscriptions == null || activeSubscriptions.isEmpty() -> {
                        // Fallback to basic telephony manager
                        val operatorName = telephonyManager.networkOperatorName
                        if (operatorName.isNullOrEmpty()) "Cellular" else operatorName
                    }
                    activeSubscriptions.size == 1 -> {
                        // Single SIM
                        val subscription = activeSubscriptions[0]
                        val operatorName = subscription.carrierName?.toString() ?: subscription.displayName?.toString()
                        operatorName ?: "SIM"
                    }
                    else -> {
                        // Dual SIM - try to determine which one is active
                        val defaultDataSubscription = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            SubscriptionManager.getDefaultDataSubscriptionId()
                        } else {
                            -1 // Invalid subscription ID for older versions
                        }
                        
                        val activeSubscription = activeSubscriptions.find { it.subscriptionId == defaultDataSubscription }
                            ?: activeSubscriptions[0] // Fallback to first if default not found
                        
                        val simSlot = if (activeSubscription.simSlotIndex == 0) "SIM1" else "SIM2"
                        val operatorName = activeSubscription.carrierName?.toString() ?: activeSubscription.displayName?.toString()
                        
                        if (operatorName.isNullOrEmpty()) {
                            simSlot
                        } else {
                            "$simSlot: $operatorName"
                        }
                    }
                }
            } else {
                // Pre-API 22 fallback
                val operatorName = telephonyManager.networkOperatorName
                if (operatorName.isNullOrEmpty()) "Cellular" else operatorName
            }
        } catch (e: SecurityException) {
            // Phone permission might be required
            "Cellular"
        } catch (e: Exception) {
            "Cellular"
        }
    }
    
    /**
     * Check if currently connected to any network
     */
    fun isConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    /**
     * Check if currently connected to Wi-Fi
     */
    fun isWifiConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * Check if currently connected to cellular
     */
    fun isCellularConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
} 