package com.cryptax.config

import com.cryptax.config.dto.PropertiesDto
import com.cryptax.config.gcp.GcpConfig
import com.cryptax.config.kodein.KodeinConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.cloud.datastore.DatastoreOptions
import io.vertx.core.Vertx
import io.vertx.ext.mail.StartTLSOptions
import io.vertx.kotlin.ext.auth.KeyStoreOptions
import io.vertx.kotlin.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.ext.auth.jwt.JWTOptions
import io.vertx.kotlin.ext.mail.MailConfig
import org.kodein.di.Kodein

abstract class AppConfig(private val overrideProfile: String?, vertx: Vertx? = null, externalKodeinModule: Kodein.Module? = null) {

    val profile: String by lazy { profile() }

    val properties: PropertiesDto = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule())
        .readValue(AppConfig::class.java.classLoader.getResourceAsStream("config-$profile.yml"), PropertiesDto::class.java)

    var datastoreOptions: DatastoreOptions? = null

    init {
        datastoreOptions = if (properties.db.mode == "cloud-datastore")
            GcpConfig(properties.db).datastoreOptions()
        else
            null
    }

    private val mailConfig = MailConfig(
        hostname = properties.email.host,
        port = properties.email.port,
        starttls = StartTLSOptions.REQUIRED,
        username = properties.email.username,
        password = properties.email.password(profile),
        trustAll = true,
        ssl = true)

    val kodeinDefaultModule = KodeinConfig(properties, mailConfig, properties.db, vertx, datastoreOptions, externalKodeinModule).kodeinModule

    val jwtAuthOptions = JWTAuthOptions(keyStore = KeyStoreOptions(path = properties.jwt.keyStorePath, password = properties.jwt.password(profile)))
    val jwtOptions = JWTOptions(algorithm = properties.jwt.algorithm, issuer = properties.jwt.issuer, expiresInMinutes = properties.jwt.expiresInMinutes)
    val jwtRefreshOptions = JWTOptions(algorithm = properties.jwt.algorithm, issuer = properties.jwt.issuer, expiresInMinutes = properties.jwt.refreshExpiresInDays * 1440)

    private fun profile(): String {
        if (overrideProfile != null) return overrideProfile
        return AppConfig.profile()
    }

    companion object {
        fun profile(): String {
            val profileEnv = System.getenv("PROFILE")
            return profileEnv ?: return "local"
        }
    }
}

class DefaultAppConfig(vertx: Vertx) : AppConfig(null, vertx, null)
