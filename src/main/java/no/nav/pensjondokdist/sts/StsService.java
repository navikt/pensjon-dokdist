package no.nav.pensjondokdist.sts;

import org.springframework.stereotype.Service;

@Service
public class StsService {
    private StsClient stsClient;

    public StsService(StsClient stsClient) {
        this.stsClient = stsClient;
    }

    public String getSystemBrukerToken() {
        return stsClient.get();
    }
}
