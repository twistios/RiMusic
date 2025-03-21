package it.fast4x.rimusic.extensions.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class InternetConnectivityObserver(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _internetNetworkStatus = Channel<Boolean>(Channel.CONFLATED)
    val internetNetworkStatus = _internetNetworkStatus.receiveAsFlow()

    private val internetNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _internetNetworkStatus.trySend(true)
        }

        override fun onLost(network: Network) {
            _internetNetworkStatus.trySend(false)
        }
    }

    init {
        val request = NetworkRequest.Builder()
            // add Internet capability to request
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, internetNetworkCallback)
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(internetNetworkCallback)
    }
}