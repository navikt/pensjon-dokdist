package no.nav.pensjondokdist.brevmetadata;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.pensjondokdist.PensjonDokdistController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
public class BrevMetadataClient {
    private final String url;
    private final RestTemplate restTemplate;
    private static Logger logger = LoggerFactory.getLogger(BrevMetadataClient.class);

    public BrevMetadataClient(@Value("${brevmetadata.url}") String url) {
        this.url = url;
        this.restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(
                Collections.singletonList(new MappingJackson2HttpMessageConverter(
                        new ObjectMapper()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                ))
        );
    }

    public Brevdata fetchBrevmetadata(String brevkode) {
        String absoluteUrl = url + "/api/brevdata/brevForBrevkode/" + brevkode;
        return restTemplate.getForObject(absoluteUrl, Brevdata.class);
    }
}
