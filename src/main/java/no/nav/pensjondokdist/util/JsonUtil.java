package no.nav.pensjondokdist.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.pensjondokdist.PensjonDokdistException;

public class JsonUtil {
    public static String toJsonString(Object object) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new PensjonDokdistException(e.getMessage(), e);
        }
    }

    public static <T> T toObjectFromJsonString(String string, TypeReference<T> typeReference) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(string, typeReference);
        } catch (JsonProcessingException e) {
            throw new PensjonDokdistException("Feil ved konvertering fra JSON", e);
        }
    }
}
