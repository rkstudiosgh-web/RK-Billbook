package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Booking
import com.example.data.model.CameramanRecord
import com.example.data.model.VcrBill
import com.example.data.model.VideoEditingOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun getBookingById(id: Long): Flow<Booking?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)

    // Cameraman records
    @Query("SELECT * FROM cameraman_records ORDER BY eventDate DESC, createdAt DESC")
    fun getAllCameramanRecords(): Flow<List<CameramanRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCameramanRecord(record: CameramanRecord): Long

    @Update
    suspend fun updateCameramanRecord(record: CameramanRecord)

    @Delete
    suspend fun deleteCameramanRecord(record: CameramanRecord)

    // VCR Cassette Bills
    @Query("SELECT * FROM vcr_bills ORDER BY createdAt DESC")
    fun getAllVcrBills(): Flow<List<VcrBill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVcrBill(bill: VcrBill): Long

    @Update
    suspend fun updateVcrBill(bill: VcrBill)

    @Delete
    suspend fun deleteVcrBill(bill: VcrBill)

    // Video Editing Orders
    @Query("SELECT * FROM video_editing_orders ORDER BY createdAt DESC")
    fun getAllEditingOrders(): Flow<List<VideoEditingOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditingOrder(order: VideoEditingOrder): Long

    @Update
    suspend fun updateEditingOrder(order: VideoEditingOrder)

    @Delete
    suspend fun deleteEditingOrder(order: VideoEditingOrder)
}
