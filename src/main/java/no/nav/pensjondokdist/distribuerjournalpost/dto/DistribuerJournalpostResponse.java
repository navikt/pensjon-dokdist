package no.nav.pensjondokdist.distribuerjournalpost.dto;

public final class DistribuerJournalpostResponse {

    private String bestillingsId;

    public DistribuerJournalpostResponse() {
    }

    public DistribuerJournalpostResponse(String bestillingsId) {
        this.bestillingsId = bestillingsId;
    }

    public String getBestillingsId() {
        return bestillingsId;
    }
}
