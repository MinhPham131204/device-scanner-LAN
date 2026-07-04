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
        // Kiểm tra xem DB đã có dữ liệu chưa
        val count = ianaPortDao.getPortsCount()

        if (count == 0) {
            // DB trống (Người dùng mở app lần đầu tiên)
            // Gọi hàm fetchAndParse() từ mạng mà bạn đã viết
            val networkPorts = IanaPortDb.fetchAndParse()

            // Chuyển đổi Data class thường sang Entity để lưu DB
            val entitiesToInsert = networkPorts.map {
                IanaPortEntity(it.portNumber, it.serviceName, it.description)
            }

            // Lưu xuống SQLite thông qua Room
            ianaPortDao.insertAll(entitiesToInsert)

            return entitiesToInsert
        } else {
            // Từ lần mở app thứ 2 trở đi, đọc thẳng từ Local DB vô cùng nhanh chóng
            return ianaPortDao.getAllPorts()
        }
    }
}