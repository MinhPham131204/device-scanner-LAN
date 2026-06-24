package com.application.lanscanner.data.dataSource.database.iana_ports

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "iana_ports")
data class IanaPortEntity(
    @PrimaryKey
    val portNumber: String,
    val serviceName: String,
    val description: String
)