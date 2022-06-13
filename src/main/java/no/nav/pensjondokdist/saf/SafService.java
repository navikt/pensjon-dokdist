package no.nav.pensjondokdist.saf;

import no.nav.pensjondokdist.saf.model.Journalpost;
import org.springframework.stereotype.Service;

@Service
public class SafService {
    private SafClient safClient;

    public SafService(SafClient safClient) {
        this.safClient = safClient;
    }

    public Journalpost hentJournalPost(String journalpostId) {
        return safClient.fetchJournal(journalpostId);
    }
}
