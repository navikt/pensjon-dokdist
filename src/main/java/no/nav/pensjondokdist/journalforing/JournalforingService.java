package no.nav.pensjondokdist.journalforing;

import no.nav.pensjondokdist.saf.model.Journalpost;
import org.springframework.stereotype.Service;

import no.nav.pensjondokdist.journalforing.dto.FerdigstillJournalpostRequest;
import no.nav.pensjondokdist.saf.SafService;
import no.nav.pensjondokdist.sts.StsService;

@Service
public class JournalforingService {
    private JournalforingClient client;
    private SafService safService;
    private StsService stsService;

    public JournalforingService(JournalforingClient client, SafService safService, StsService stsService) {
        this.client = client;
        this.safService = safService;
        this.stsService = stsService;
    }

    public Boolean ferdigstillJournalpost(String journalpostStatus, String journalpostId, String journalfoerendeEnhet) {
        if (journalpostStatus.equals("FS")) {
            return true;
        } else if (journalpostStatus.equals("FL")) {
            FerdigstillJournalpostRequest request = new FerdigstillJournalpostRequest(journalfoerendeEnhet);
            return client.execute(journalpostId, request, getServiceBrukerToken());
        } else {
            return false;
        }
    }

    private String getServiceBrukerToken() {
        return stsService.getSystemBrukerToken();
    }
}
