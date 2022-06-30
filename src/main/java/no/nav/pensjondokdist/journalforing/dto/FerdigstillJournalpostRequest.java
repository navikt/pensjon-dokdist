package no.nav.pensjondokdist.journalforing.dto;

import no.nav.pensjondokdist.distribuerjournalpost.dto.Distribusjonstidspunkt;
import no.nav.pensjondokdist.distribuerjournalpost.dto.Distribusjonstype;

public class FerdigstillJournalpostRequest {
    private String journalfoerendeEnhet;
    private Distribusjonstype distribusjonstype;
    private Distribusjonstidspunkt distribusjonstidspunkt;

    public FerdigstillJournalpostRequest() {

    }

    public FerdigstillJournalpostRequest(String journalfoerendeEnhet) {
        this.journalfoerendeEnhet = journalfoerendeEnhet;
    }

    public String getJournalfoerendeEnhet() {
        return this.journalfoerendeEnhet;
    }

    public void setDistribusjonstidspunkt(Distribusjonstidspunkt distribusjonstidspunkt) {
        this.distribusjonstidspunkt = distribusjonstidspunkt;
    }

    public void setDistribusjonstype(Distribusjonstype distribusjonstype) {
        this.distribusjonstype = distribusjonstype;
    }
}

