package com.wolfeleo2.myuon.server.domain

interface StudentRepository {
    suspend fun create(profile: StudentProfile): StudentProfile
    suspend fun findByUserId(userId: String): StudentProfile?
    suspend fun findByRegNo(regNo: String): StudentProfile?
}
