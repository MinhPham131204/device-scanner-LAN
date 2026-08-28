package com.application.lanscanner.data.repository

import android.content.Context
import com.application.lanscanner.data.dataSource.coreScanner.PingScanner
import com.application.lanscanner.data.model.LanDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class NetworkRepository(
    private val pingScanner: PingScanner
) {
    fun scanLanDevices(networkAddress: String, numOfHosts: Int, context: Context): Flow<LanDevice> {
        return pingScanner.scanSubnetRealtime(networkAddress, numOfHosts, context)
            .map { result -> // PingResult
                LanDevice(
                    ipAddress = result.ipAddress,
                    name = result.hostname
                )
            }
            .flowOn(Dispatchers.IO)
    }
}