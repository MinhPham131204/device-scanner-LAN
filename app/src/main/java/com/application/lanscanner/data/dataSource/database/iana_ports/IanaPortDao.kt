package com.application.lanscanner.data.dataSource.database.iana_ports

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IanaPortDao {
    // Lấy toàn bộ dữ liệu port trong DB
    @Query("SELECT * FROM iana_ports")
    suspend fun getAllPorts(): List<IanaPortEntity>

    // Lưu danh sách port mới vào DB, nếu trùng khóa chính thì ghi đè
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ports: List<IanaPortEntity>)

    // Đếm số lượng bản ghi để kiểm tra xem DB đã có dữ liệu chưa
    @Query("SELECT COUNT(*) FROM iana_ports")
    suspend fun getPortsCount(): Int
}