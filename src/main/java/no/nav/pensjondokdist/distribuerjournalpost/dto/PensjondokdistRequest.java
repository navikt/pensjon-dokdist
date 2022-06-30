package no.nav.pensjondokdist.distribuerjournalpost.dto;

import javax.validation.constraints.NotNull;

public class PensjondokdistRequest {
    private Adresse adresse;
    @NotNull
    private String status;
    private Distribusjonstype distribusjonstype;

    public PensjondokdistRequest() {

    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    public Distribusjonstype getDistribusjonstype() {
        return distribusjonstype;
    }

    public void setDistribusjonstype(Distribusjonstype distribusjonstype) {
        this.distribusjonstype = distribusjonstype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
