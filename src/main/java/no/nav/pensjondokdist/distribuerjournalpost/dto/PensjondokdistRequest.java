package no.nav.pensjondokdist.distribuerjournalpost.dto;

import no.nav.pensjondokdist.journalforing.dto.Distribusjonstype;

public class PensjondokdistRequest {
    private Adresse adresse;
    private String status;
    private Distribusjonstype type;

    public PensjondokdistRequest() {

    }

    public Adresse getAdresse() {
        return adresse;
    }

    public Distribusjonstype getType() {
        return type;
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

    public void setType(Distribusjonstype type) {
        this.type = type;
    }
}
