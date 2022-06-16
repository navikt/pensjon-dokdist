package no.nav.pensjondokdist.distribuerjournalpost;

import static no.nav.pensjondokdist.util.JsonUtil.toJsonString;

import no.nav.pensjondokdist.distribuerjournalpost.dto.Distribusjonstidspunkt;
import no.nav.pensjondokdist.distribuerjournalpost.dto.Distribusjonstype;
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

    public DistribuerJournalpostResponse distribuerJournalpost(String journalId, PensjondokdistRequest request, Distribusjonstype distribusjonstype) {
        LOG.debug("journalId: " + journalId + " DistribuerJournalpostRequest: " + toJsonString(request));

        return distribuerJournalpostClient.post(
                DistribuerJournalpostRequest.builder()
                        .journalpostId(journalId)
                        .distribusjonstype(distribusjonstype)
                        .distribusjonstidspunkt(Distribusjonstidspunkt.KJERNETID)
                        .bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
                        .dokumentProdApp(BESTILLENDE_FAGSYSTEM)
                        .adresse(request.getAdresse())
                        .build());
    }

    public Distribusjonstype dokzumentkategoriToDistribusjonstype(DokumentkategoriCode dokumentkategoriCode) {
        switch (dokumentkategoriCode){
            case IB:
                return Distribusjonstype.ANNET;
            case B:
                return Distribusjonstype.VIKTIG;
            case VB:
                return Distribusjonstype.VEDTAK;
            default:
                throw new PensjonDokdistException("Ugyldig dokumentkategorikode for distribusjon");
        }
    }
}

