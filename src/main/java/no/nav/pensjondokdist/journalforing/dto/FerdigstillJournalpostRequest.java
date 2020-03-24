package no.nav.pensjondokdist.journalforing.dto;

public class FerdigstillJournalpostRequest {
    private String journalfoerendeEnhet;

    public FerdigstillJournalpostRequest() {

    }

    public FerdigstillJournalpostRequest(String journalfoerendeEnhet) {
        this.journalfoerendeEnhet = journalfoerendeEnhet;
    }

    public String getJournalfoerendeEnhet() {
        return this.journalfoerendeEnhet;
    }
}

