package no.nav.pensjondokdist.distribuerjournalpost.dto;

public final class Adresse {

    private String adresseType;
    private String adresselinje1;
    private String adresselinje2;
    private String adresselinje3;
    private String postnummer;
    private String poststed;
    private String land;

    public Adresse(String adresseType, String adresselinje1, String adresselinje2, String adresselinje3, String postnummer, String poststed, String land) {
        this.adresseType = adresseType;
        this.adresselinje1 = adresselinje1;
        this.adresselinje2 = adresselinje2;
        this.adresselinje3 = adresselinje3;
        this.postnummer = postnummer;
        this.poststed = poststed;
        this.land = land;
    }

    public Adresse() {
    }


    public String getAdresseType() {
        return adresseType;
    }

    public void setAdresseType(String adresseType) {
        this.adresseType = adresseType;
    }

    public String getAdresselinje1() {
        return adresselinje1;
    }

    public void setAdresselinje1(String adresselinje1) {
        this.adresselinje1 = adresselinje1;
    }

    public String getAdresselinje2() {
        return adresselinje2;
    }

    public void setAdresselinje2(String adresselinje2) {
        this.adresselinje2 = adresselinje2;
    }

    public String getAdresselinje3() {
        return adresselinje3;
    }

    public void setAdresselinje3(String adresselinje3) {
        this.adresselinje3 = adresselinje3;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public String getPoststed() {
        return poststed;
    }

    public void setPoststed(String poststed) {
        this.poststed = poststed;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }


}
