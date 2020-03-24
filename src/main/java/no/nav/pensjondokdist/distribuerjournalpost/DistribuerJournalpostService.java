package no.nav.pensjondokdist.distribuerjournalpost;

import static no.nav.pensjondokdist.util.JsonUtil.toJsonString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import no.nav.pensjondokdist.distribuerjournalpost.dto.DistribuerJournalpostRequest;
import no.nav.pensjondokdist.distribuerjournalpost.dto.DistribuerJournalpostResponse;
import no.nav.pensjondokdist.distribuerjournalpost.dto.PensjondokdistRequest;

@Service
public class DistribuerJournalpostService {
    private DistribuerJournalpostClient distribuerJournalpostClient;
    private static final Logger LOG = LoggerFactory.getLogger(DistribuerJournalpostService.class);
    private static String BESTILLENDE_FAGSYSTEM = "AT05";

    public DistribuerJournalpostService(DistribuerJournalpostClient client) {
        this.distribuerJournalpostClient = client;
    }

    public DistribuerJournalpostResponse distribuerJournalpost(String journalId, PensjondokdistRequest request) {
        LOG.debug("journalId: " + journalId + " DistribuerJournalpostRequet: " + toJsonString(request));

        return distribuerJournalpostClient.post(
                DistribuerJournalpostRequest.builder()
                        .journalpostId(journalId)
                        .bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
                        .dokumentProdApp(BESTILLENDE_FAGSYSTEM)
                        .adresse(request.getAdresse())
                        .build());
    }
}

