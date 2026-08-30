package com.wolfeleo2.myuon.server.domain

interface HostelRepository {
    suspend fun getHostels(): List<HostelHall>
    suspend fun getHostelBooking(regNo: String): HostelRoomBooking?
    suspend fun createBooking(booking: HostelRoomBooking): HostelRoomBooking
    suspend fun markBookingPaid(bookingId: String): Boolean
}
