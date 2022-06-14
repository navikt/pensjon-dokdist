package no.nav.pensjondokdist.saf.model;

public class Dokument {

    public Dokument(){

    }

    String brevkode;

    public Dokument(String brevkode) {
        this.brevkode = brevkode;
    }

    public String getBrevkode() {
        return brevkode;
    }

    public void setBrevkode(String brevkode) {
        this.brevkode = brevkode;
    }
}
