package no.nav.pensjondokdist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import no.nav.pensjondokdist.util.SecretUtil;

@SpringBootApplication
public class PensjonDokdistApplication {
	private static Logger logger = LoggerFactory.getLogger(PensjonDokdistApplication.class);

	public static void main(String[] args) throws IOException {
		setupTruststore();
		System.setProperty("clientSecret", SecretUtil.readSecret("oidc/client_secret"));
		System.setProperty("serviceuser.username", SecretUtil.readSecret("serviceuser/username"));
		System.setProperty("serviceuser.password", SecretUtil.readSecret("serviceuser/password"));
		SpringApplication.run(PensjonDokdistApplication.class, args);
	}

	private static void setupTruststore() throws IOException {
		Path truststorePath = Paths.get("secrets", "truststore", "truststore.jts");
		if (Files.exists(truststorePath)) {
			// assume that the trust store path is not managed - use the one in the "secrets folder"
			String absolutePath = truststorePath.toFile().getAbsolutePath();
			logger.info("Using trust store " + absolutePath);
			System.setProperty("javax.net.ssl.trustStore", absolutePath);
			System.setProperty("javax.net.ssl.trustStorePassword", SecretUtil.readSecret("truststore/password"));
		} else {
			logger.info("Using system trust store");
		}
	}
}
