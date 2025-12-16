package no.nav.pensjon.dokdist

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.util.StreamUtils
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import java.io.File
import java.nio.charset.StandardCharsets

@SpringBootApplication
class DokDistApplication

fun main(args: Array<String>) {
    val activeProfiles = activeSpringProfiles()
    if (activeProfiles.isEmpty()) {
        logger.error("No Spring profiles active: Should probably be 'nais' or 'local'.")
    }

    ifRunningLocallySetupSecrets(activeProfiles)
    runApplication<DokDistApplication>(*args)
}

private fun activeSpringProfiles() =
    listOfNotNull(System.getenv("SPRING_PROFILES_ACTIVE"), System.getProperty("spring.profiles.active"))
        .firstOrNull()
        ?.split(',')
        ?: emptyList()

private val logger = LoggerFactory.getLogger(DokDistApplication::class.java)

private fun ifRunningLocallySetupSecrets(activeProfiles: List<String>) {
    if (activeProfiles.contains("local")) {
        logger.info("Running with 'local' profile active, will attempt load secrets.")
        val azureAdJson = with(File("secrets/azuread.json")) {
            if (exists()) {
                inputStream().use { StreamUtils.copyToString(it, StandardCharsets.UTF_8) }
            } else {
                logger.error("Required AzureAD-secret file doesn't exist: $absolutePath")
                logger.error("Run fetch-secrets.sh in project root to download from kubernetes.")
                throw IllegalStateException("Required AzureAD-secret file doesn't exist: $absolutePath")
            }
        }

        JsonMapper().readValue<Map<String, String>>(azureAdJson).also {
            setSystemProperty("spring.security.oauth2.client.registration.azure.clientId", it["AZURE_APP_CLIENT_ID"])
            setSystemProperty("spring.security.oauth2.client.registration.azure.client-secret", it["AZURE_APP_CLIENT_SECRET"])
            setSystemProperty("spring.security.oauth2.client.provider.azure.token-uri", it["AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"])
            setSystemProperty("spring.security.oauth2.client.provider.azure.issuer-uri", it["AZURE_OPENID_CONFIG_ISSUER"])
            setSystemProperty("spring.security.oauth2.client.provider.azure.jwk-set-uri", it["AZURE_OPENID_CONFIG_JWKS_URI"])
        }
    }
}

private fun setSystemProperty(name: String, value: String?) {
    if (value.isNullOrBlank()) {
        logger.warn("Won't set $name: it isn't defined in secret")
    } else {
        System.setProperty(name, value)
    }
}
