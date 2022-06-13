package no.nav.pensjondokdist.distribuerjournalpost.dto;

import no.nav.pensjondokdist.journalforing.dto.DistribusjonsTidspunkt;
import no.nav.pensjondokdist.journalforing.dto.Distribusjonstype;

public final class DistribuerJournalpostRequest {

    private String journalpostId;
    private String batchId;
    private String bestillendeFagsystem;
    private String dokumentProdApp;
    private Adresse adresse;
    private Distribusjonstype distribusjonstype;
    private DistribusjonsTidspunkt distribusjonsTidspunkt;

    public DistribuerJournalpostRequest(String journalpostId, String batchId, String bestillendeFagsystem, String dokumentProdApp, Adresse adresse, Distribusjonstype distribusjonstype, DistribusjonsTidspunkt distribusjonsTidspunkt) {
        this.journalpostId = journalpostId;
        this.batchId = batchId;
        this.bestillendeFagsystem = bestillendeFagsystem;
        this.dokumentProdApp = dokumentProdApp;
        this.adresse = adresse;
        this.distribusjonstype = distribusjonstype;
        this.distribusjonsTidspunkt = distribusjonsTidspunkt;
    }

    public DistribuerJournalpostRequest() {
    }

    public static DistribuerJournalpostRequestBuilder builder() {
        return new DistribuerJournalpostRequestBuilder();
    }

    public DistribusjonsTidspunkt getDistribusjonsTidspunkt() {
        return distribusjonsTidspunkt;
    }

    public Distribusjonstype getDistribusjonstype() {
        return distribusjonstype;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getBestillendeFagsystem() {
        return bestillendeFagsystem;
    }

    public String getDokumentProdApp() {
        return dokumentProdApp;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public static class DistribuerJournalpostRequestBuilder {
        private String journalpostId;
        private String batchId;
        private String bestillendeFagsystem;
        private String dokumentProdApp;
        private Adresse adresse;
        private Distribusjonstype distribusjonstype;
        private DistribusjonsTidspunkt distribusjonsTidspunkt;

        DistribuerJournalpostRequestBuilder() {
        }

        public DistribuerJournalpostRequest.DistribuerJournalpostRequestBuilder journalpostId(String journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public DistribuerJournalpostRequest.DistribuerJournalpostRequestBuilder distribusjonstype(Distribusjonstype distribusjonstype) {
            this.distribusjonstype = distribusjonstype;
            return this;
        }

        public DistribuerJournalpostRequest.DistribuerJournalpostRequestBuilder distribusjonsTidspunkt(DistribusjonsTidspunkt distribusjonsTidspunkt) {
            this.distribusjonsTidspunkt = distribusjonsTidspunkt;
            return this;
        }

        public DistribuerJournalpostRequest.DistribuerJournalpostRequestBuilder batchId(String batchId) {
            this.batchId = batchId;
            return this;
        }

        public DistribuerJournalpostRequest.DistribuerJournalpostRequestBuilder bestillendeFagsystem(String bestillendeFagsystem) {
            this.bestillendeFagsystem = bestillendeFagsystem;
            return this;
        }

        public DistribuerJournalpostRequest.DistribuerJournalpostRequestBuilder dokumentProdApp(String dokumentProdApp) {
            this.dokumentProdApp = dokumentProdApp;
            return this;
        }

        public DistribuerJournalpostRequest.DistribuerJournalpostRequestBuilder adresse(Adresse adresse) {
            this.adresse = adresse;
            return this;
        }

        public DistribuerJournalpostRequest build() {
            return new DistribuerJournalpostRequest(journalpostId, batchId, bestillendeFagsystem, dokumentProdApp, adresse, distribusjonstype, distribusjonsTidspunkt);
        }
    }
}
