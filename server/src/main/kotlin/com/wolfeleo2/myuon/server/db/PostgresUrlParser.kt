package com.wolfeleo2.myuon.server.db

import java.net.URI

data class ParsedDbConnection(
    val jdbcUrl: String,
    val user: String,
    val password: String
)

object PostgresUrlParser {
    fun parse(rawUrl: String, defaultUser: String = "", defaultPassword: String = ""): ParsedDbConnection {
        if (rawUrl.startsWith("jdbc:h2:")) {
            return ParsedDbConnection(rawUrl, defaultUser, defaultPassword)
        }

        val normalized = when {
            rawUrl.startsWith("jdbc:postgresql://") -> rawUrl.removePrefix("jdbc:")
            rawUrl.startsWith("postgres://") -> rawUrl.replaceFirst("postgres://", "postgresql://")
            else -> rawUrl
        }

        return try {
            val uri = URI(normalized)
            val userInfo = uri.userInfo ?: ""
            val user = when {
                userInfo.contains(":") -> userInfo.substringBefore(":")
                userInfo.isNotBlank() -> userInfo
                else -> defaultUser
            }
            val password = when {
                userInfo.contains(":") -> userInfo.substringAfter(":")
                else -> defaultPassword
            }

            val host = uri.host ?: "localhost"
            val port = if (uri.port != -1) ":${uri.port}" else ""
            val path = uri.path ?: "/neondb"
            val rawQuery = uri.query

            val filteredQuery = if (!rawQuery.isNullOrBlank()) {
                val params = rawQuery.split("&")
                    .filterNot { it.startsWith("channel_binding=") }
                if (params.isNotEmpty()) "?" + params.joinToString("&") else ""
            } else {
                "?sslmode=require"
            }

            val finalJdbcUrl = "jdbc:postgresql://$host$port$path$filteredQuery"
            ParsedDbConnection(finalJdbcUrl, user, password)
        } catch (_: Exception) {
            val fallbackJdbc = if (rawUrl.startsWith("jdbc:")) rawUrl else "jdbc:postgresql://$rawUrl"
            ParsedDbConnection(fallbackJdbc, defaultUser, defaultPassword)
        }
    }
}
