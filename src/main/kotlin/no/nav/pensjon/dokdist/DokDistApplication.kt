package no.nav.pensjon.dokdist

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.*

@SpringBootApplication
class DokDistApplication

fun main(args: Array<String>){
    setupTruststore()
    runApplication<DokDistApplication>(*args)
}

private val logger = LoggerFactory.getLogger(DokDistApplication::class.java)

private fun setupTruststore() {
    val truststorePath = Paths.get("secrets", "truststore", "truststore.jts")
    if (Files.exists(truststorePath)) {
        // assume that the trust store path is not managed - use the one in the "secrets folder"
        val absolutePath = truststorePath.toFile().absolutePath
        logger.info("Using trust store $absolutePath")
        System.setProperty("javax.net.ssl.trustStore", absolutePath)
        System.setProperty("javax.net.ssl.trustStorePassword", readSecret("truststore/password"))
    } else {
        logger.info("Using system trust store")
    }
}

private fun readSecret(path: String): String {
    val baseDir = listOfNotNull(System.getenv("SECRET_BASEDIR"), System.getProperty("secret.basedir"), System.getProperty("user.dir"))
        .firstOrNull { it.isNotBlank() }
        ?: throw RuntimeException("Could not read secret '$path': no secret basedir found (SECRET_BASEDIR, secret.basedir, user.dir")

    return Files.readString(Paths.get(baseDir).resolve("secrets").resolve(path))
}
