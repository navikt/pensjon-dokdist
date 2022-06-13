package no.nav.pensjondokdist.brevmetadata;

public class Brevdata {
    private DokumentkategoriCode dokumentkategori;

    public DokumentkategoriCode getDokumentkategori() {
        return dokumentkategori;
    }

    public Brevdata(DokumentkategoriCode dokumentkategori) {
        this.dokumentkategori = dokumentkategori;
    }

    public Brevdata() {
    }
}

