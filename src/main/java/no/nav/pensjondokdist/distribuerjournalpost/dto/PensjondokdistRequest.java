package no.nav.pensjondokdist.distribuerjournalpost.dto;

public class PensjondokdistRequest {
    private Adresse adresse;
    private String status;

    public PensjondokdistRequest() {

    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
