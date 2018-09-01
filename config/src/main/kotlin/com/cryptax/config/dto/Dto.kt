package com.cryptax.config.dto

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import java.lang.management.ManagementFactory

data class PropertiesDto(val server: ServerDto, val jwt: JwtDto, val email: EmailDto, val http: HttpDto, val db: DbDto)
data class ServerDto(val domain: String, val port: Int, val allowOrigin: String)
data class JwtDto(
    val keyStorePath: String,
    private var password: String,
    val algorithm: String,
    val issuer: String,
    val expiresInMinutes: Int,
    val refreshExpiresInDays: Int) {

    fun password(profile: String? = null): String {
        if (profile == "it") return password
        return decrypt(password)
    }
}

data class EmailDto(val enabled: Boolean, val url: String?, val function: String?, private val key: String?, val from: String?) {
    fun key(): String {
        return decrypt(key!!)
    }
}

data class HttpDto(val maxIdleConnections: Int, val keepAliveDuration: Long)

data class DbDto(val mode: String, val projectId: String?, private val credentials: String?) {
    fun credentials(): String {
        return decrypt(credentials!!)
    }
}

fun decrypt(password: String): String {
    val stringEncryptor = StandardPBEStringEncryptor()
    val runtimeMxBean = ManagementFactory.getRuntimeMXBean()
    val argument = runtimeMxBean.inputArguments.find { s -> s.contains("jasypt.encryptor.password") } ?: throw RuntimeException("jasypt.encryptor.password not found")
    val jasyptPassword = argument.substring(argument.indexOf("=") + 1)
    stringEncryptor.setPassword(jasyptPassword)
    return stringEncryptor.decrypt(password)
}
