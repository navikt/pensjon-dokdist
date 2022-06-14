package no.nav.pensjondokdist.saf.model;

import java.util.List;

public class Journalpost {
    private String journalpostId;
    private String journalforendeEnhet;
    private List<Dokument> dokumenter;

    public Journalpost() {
    }

    public List<Dokument> getDokumenter() {
        return dokumenter;
    }

    public void setDokumenter(List<Dokument> dokumenter) {
        this.dokumenter = dokumenter;
    }

    public String getJournalforendeEnhet() {
        return journalforendeEnhet;
    }

    public void setJournalforendeEnhet(String journalforendeEnhet) {
        this.journalforendeEnhet = journalforendeEnhet;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

}
