package no.nav.pensjondokdist.saf;

import org.springframework.stereotype.Service;

@Service
public class SafService {
    private SafClient safClient;

    public SafService(SafClient safClient) {
        this.safClient = safClient;
    }

    public String hentJournalforendeEnhet(String journalpostId) {
        return safClient.get(journalpostId);
    }
}
