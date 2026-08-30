package com.wolfeleo2.myuon.server.domain

object Validation {
    private val REG_NO_REGEX = Regex("^[A-Za-z0-9/_-]{3,32}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PHONE_REGEX = Regex("^(\\+?254|0)[17]\\d{8}$")
    private val UNIT_CODE_REGEX = Regex("^[A-Za-z0-9 _-]{3,15}$")

    val CAMPUSES = listOf(
        "Chiromo Campus",
        "Main Campus",
        "Lower Kabete Campus",
        "Upper Kabete Campus",
        "Kikuyu Campus",
        "Parklands Campus",
        "Kenya Science Campus",
        "KNH Campus (Health Sciences)",
        "Mombasa Campus",
        "Kisumu Campus"
    )

    fun validateRegNo(regNo: String) {
        require(regNo.isNotBlank()) { "Registration number is required" }
        require(regNo.length in 3..32) { "Registration number must be between 3 and 32 characters" }
        require(regNo.matches(REG_NO_REGEX)) { "Invalid registration number format" }
    }

    fun validateEmail(email: String) {
        require(email.isNotBlank()) { "Email is required" }
        require(email.length <= 128) { "Email must be 128 characters or less" }
        require(email.matches(EMAIL_REGEX)) { "Invalid email address format" }
    }

    fun validatePhone(phone: String) {
        require(phone.isNotBlank()) { "Phone number is required" }
        require(phone.replace(" ", "").matches(PHONE_REGEX)) { "Invalid Kenyan phone number format (e.g. 0712345678 or +254712345678)" }
    }

    fun validateUnitCode(unitCode: String) {
        require(unitCode.isNotBlank()) { "Unit code is required" }
        require(unitCode.matches(UNIT_CODE_REGEX)) { "Invalid unit code format" }
    }

    fun validateAmount(amount: Double) {
        require(amount > 0.0) { "Amount must be greater than zero" }
        require(amount <= 1_000_000.0) { "Amount exceeds maximum allowable transaction limit" }
    }

    fun validateYearAndSemester(yearOfStudy: Int, semester: Int) {
        require(yearOfStudy in 1..6) { "Year of study must be between 1 and 6" }
        require(semester in 1..3) { "Semester must be 1, 2, or 3" }
    }

    fun validateCampus(campus: String) {
        require(campus.isNotBlank()) { "Campus is required" }
        val matches = CAMPUSES.any { it.contains(campus.trim(), ignoreCase = true) || campus.trim().contains(it.replace(" Campus", ""), ignoreCase = true) }
        require(matches) { "Invalid campus. Must be an official University of Nairobi campus" }
    }
}
