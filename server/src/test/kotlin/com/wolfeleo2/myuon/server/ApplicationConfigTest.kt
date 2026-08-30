package com.wolfeleo2.myuon.server

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ApplicationConfigTest {
    @Test
    fun `requireEnv throws a message naming the missing variable`() {
        val exception = assertFailsWith<IllegalStateException> {
            requireEnv("MYUON_DEFINITELY_UNSET_VAR")
        }
        assertTrue(exception.message!!.contains("MYUON_DEFINITELY_UNSET_VAR"))
    }
}
