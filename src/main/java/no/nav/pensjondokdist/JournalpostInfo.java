package no.nav.pensjondokdist;

public class JournalpostInfo {

    private String journalpostId;
    private Boolean isFritekst;
    public JournalpostInfo() {

    }

    public JournalpostInfo(String journalpostId, Boolean isFritekst) {

        this.journalpostId = journalpostId;
        this.isFritekst = isFritekst;
    }

    public Boolean getFritekst() {
        return isFritekst;
    }


    public String getJournalpostId() {
        return journalpostId;
    }

    public void setFritekst(Boolean fritekst) {
        isFritekst = fritekst;
    }
}
