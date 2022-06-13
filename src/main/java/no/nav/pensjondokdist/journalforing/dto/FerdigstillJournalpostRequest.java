package no.nav.pensjondokdist.journalforing.dto;

public class FerdigstillJournalpostRequest {
    private String journalfoerendeEnhet;
    private Distribusjonstype distribusjonstype;
    private DistribusjonsTidspunkt distribusjonsTidspunkt;

    public FerdigstillJournalpostRequest() {

    }

    public FerdigstillJournalpostRequest(String journalfoerendeEnhet) {
        this.journalfoerendeEnhet = journalfoerendeEnhet;
    }

    public String getJournalfoerendeEnhet() {
        return this.journalfoerendeEnhet;
    }

    public void setDistribusjonsTidspunkt(DistribusjonsTidspunkt distribusjonsTidspunkt) {
        this.distribusjonsTidspunkt = distribusjonsTidspunkt;
    }

    public void setDistribusjonstype(Distribusjonstype distribusjonstype) {
        this.distribusjonstype = distribusjonstype;
    }
}

