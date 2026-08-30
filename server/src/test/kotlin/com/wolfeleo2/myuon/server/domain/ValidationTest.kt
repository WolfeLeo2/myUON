package com.wolfeleo2.myuon.server.domain

import kotlin.test.Test
import kotlin.test.assertFailsWith

class ValidationTest {

    @Test
    fun `valid regNo passes`() {
        Validation.validateRegNo("P15/12345/2022")
        Validation.validateRegNo("I08/4567/2021")
        Validation.validateRegNo("B02/9999/2024")
    }

    @Test
    fun `invalid regNo fails`() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateRegNo("P15/12345'; DROP TABLE students;--")
        }
        assertFailsWith<IllegalArgumentException> {
            Validation.validateRegNo("")
        }
    }

    @Test
    fun `valid email passes and invalid email fails`() {
        Validation.validateEmail("jane.student@students.uonbi.ac.ke")
        assertFailsWith<IllegalArgumentException> {
            Validation.validateEmail("invalid-email-string")
        }
    }

    @Test
    fun `valid amount passes and non-positive amount fails`() {
        Validation.validateAmount(100.0)
        assertFailsWith<IllegalArgumentException> {
            Validation.validateAmount(-50.0)
        }
        assertFailsWith<IllegalArgumentException> {
            Validation.validateAmount(0.0)
        }
    }
}
