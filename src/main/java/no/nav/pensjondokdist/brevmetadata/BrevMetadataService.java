package no.nav.pensjondokdist.brevmetadata;

import org.springframework.stereotype.Service;

@Service
public class BrevMetadataService {
    private final BrevMetadataClient client;

    public BrevMetadataService(BrevMetadataClient client) {
        this.client = client;
    }

    public Brevdata fetchBrevdata(String brevkode){
        return client.fetchBrevmetadata(brevkode);
    }
}
