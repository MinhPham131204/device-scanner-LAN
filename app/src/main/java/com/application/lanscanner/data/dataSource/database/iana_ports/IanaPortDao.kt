package com.application.lanscanner.data.dataSource.database.iana_ports

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IanaPortDao {
    // Get all port data in DB
    @Query("SELECT * FROM iana_ports")
    suspend fun getAllPorts(): List<IanaPortEntity>

    // Store new ports list in DB
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ports: List<IanaPortEntity>)

    // Count the number of records to check if the database already contains data.
    @Query("SELECT COUNT(*) FROM iana_ports")
    suspend fun getPortsCount(): Int
}