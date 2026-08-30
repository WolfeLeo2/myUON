package com.wolfeleo2.myuon.server.db

import kotlin.test.Test
import kotlin.test.assertEquals

class PostgresUrlParserTest {

    @Test
    fun `parses standard Neon URI correctly`() {
        val raw = "postgresql://neondb_owner:secret_pass123@ep-fancy-sky-b2t7xxiw-pooler.c-6.eu-central-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
        val parsed = PostgresUrlParser.parse(raw)

        assertEquals("neondb_owner", parsed.user)
        assertEquals("secret_pass123", parsed.password)
        assertEquals("jdbc:postgresql://ep-fancy-sky-b2t7xxiw-pooler.c-6.eu-central-1.aws.neon.tech/neondb?sslmode=require", parsed.jdbcUrl)
    }

    @Test
    fun `preserves H2 test URL`() {
        val raw = "jdbc:h2:mem:test;MODE=PostgreSQL"
        val parsed = PostgresUrlParser.parse(raw)

        assertEquals(raw, parsed.jdbcUrl)
    }
}
