package no.nav.pensjondokdist.saf.graphql;

import no.nav.pensjondokdist.saf.model.Journalpost;

public class GrapQlData {
    private Journalpost journalpost;

    public GrapQlData() {

    }

    public Journalpost getJournalpost() {
        return journalpost;
    }

    public void setJournalpost(Journalpost journalpost) {
        this.journalpost = journalpost;
    }
}
