package no.nav.pensjondokdist.saf.model;

public class Journalpost {
    private String journalpostId;
    private String journalforendeEnhet;
    private String brevkode;

    public Journalpost() {
    }

    public String getBrevkode() {
        return brevkode;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setBrevkode(String brevkode) {
        this.brevkode = brevkode;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getJournalforendeEnhet() {
        return journalforendeEnhet;
    }

    public void setJournalforendeEnhet(String journalforendeEnhet) {
        this.journalforendeEnhet = journalforendeEnhet;
    }

}
