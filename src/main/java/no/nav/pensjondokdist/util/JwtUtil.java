package no.nav.pensjondokdist.util;

import java.time.Instant;
import java.util.List;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import no.nav.pensjondokdist.PensjonDokdistException;

public class JwtUtil {

    private static JwtConsumer unvalidatingConsumer = new JwtConsumerBuilder()
            .setSkipAllValidators()
            .setDisableRequireSignature()
            .setSkipSignatureVerification()
            .build();

    private JwtUtil() {}

    public static Instant getExpirationTime(String jwt){
        try {
            long expirationTime = unvalidatingConsumer.processToClaims(jwt).getExpirationTime().getValue();
            return Instant.ofEpochSecond(expirationTime);
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new PensjonDokdistException(e.getMessage());
        }
    }

    public static String getClientName(String jwt) {
        try {
            JwtClaims claims = unvalidatingConsumer.processToClaims(jwt);
            String authorizedParty = claims.getStringClaimValue("azp");
            if (authorizedParty != null) {
                return authorizedParty;
            }
            List<String> audience = claims.getAudience();
            if(audience.size() == 1){
                return audience.get(0);
            }
            throw new PensjonDokdistException("Kunne ikke hente client name");
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new PensjonDokdistException(e.getMessage());
        }
    }
}
