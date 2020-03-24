package no.nav.pensjondokdist.saf.graphql;


public class Variables {
    private String journalpostId;

    public Variables() {

    }

    public Variables(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }
}
