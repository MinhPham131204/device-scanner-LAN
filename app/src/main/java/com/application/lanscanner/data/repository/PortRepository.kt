package com.application.lanscanner.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.application.lanscanner.data.dataSource.database.iana_ports.IanaPortDao
import com.application.lanscanner.data.dataSource.database.iana_ports.IanaPortDb
import com.application.lanscanner.data.dataSource.database.iana_ports.IanaPortEntity

@Database(entities = [IanaPortEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ianaPortDao(): IanaPortDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lan_scanner_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class PortRepository(private val ianaPortDao: IanaPortDao) {

    suspend fun getIanaPorts(): List<IanaPortEntity> {
        val count = ianaPortDao.getPortsCount()

        if (count == 0) {
            val networkPorts = IanaPortDb.fetchAndParse()

            val entitiesToInsert = networkPorts.map {
                IanaPortEntity(it.portNumber, it.serviceName, it.description)
            }

            // store in DB (SQLite) via Room
            ianaPortDao.insertAll(entitiesToInsert)

            return entitiesToInsert
        } else {
            // // read from Local DB after first run
            return ianaPortDao.getAllPorts()
        }
    }
}