package no.nav.pensjondokdist.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SecretUtil {
    public static String readSecret(String path) throws IOException {
        String basedir = System.getenv().getOrDefault("SECRET_BASEDIR", System.getProperty("secret.basedir"));

        Path basepath;
        if (basedir != null && basedir.length() > 0) {
            basepath = Paths.get(basedir);
        } else {
            basepath = Paths.get(System.getProperty("user.dir"));
        }

        Path secretPath = basepath.resolve("secrets").resolve(path);
        return String.join("\n", Files.readAllLines(secretPath));
    }
}
