package no.nav.pensjondokdist.distribuerjournalpost;

import static no.nav.pensjondokdist.util.JsonUtil.toJsonString;

import no.nav.pensjondokdist.brevmetadata.DokumentkategoriCode;
import no.nav.pensjondokdist.journalforing.dto.DistribusjonsTidspunkt;
import no.nav.pensjondokdist.journalforing.dto.Distribusjonstype;
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

    public DistribuerJournalpostResponse distribuerJournalpost(String journalId, PensjondokdistRequest request, DokumentkategoriCode dokumentKategori) {
        LOG.debug("journalId: " + journalId + " DistribuerJournalpostRequet: " + toJsonString(request));

        return distribuerJournalpostClient.post(
                DistribuerJournalpostRequest.builder()
                        .journalpostId(journalId)
                        .distribusjonstype(dokumentkategoriToDistribusjonstype(dokumentKategori))
                        .distribusjonsTidspunkt(DistribusjonsTidspunkt.KJERNETID)
                        .bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
                        .dokumentProdApp(BESTILLENDE_FAGSYSTEM)
                        .adresse(request.getAdresse())
                        .build());
    }

    public Distribusjonstype dokumentkategoriToDistribusjonstype(DokumentkategoriCode dokumentkategoriCode) {
        switch (dokumentkategoriCode){
            case IB:
            case EP:
            case ES:
            case E_BLANKETT:
            case F:
            case IS:
            case KD:
            case KM:
            case KS:
            case SED:
            case TS:
            case B:
                return Distribusjonstype.ANNET;
            case VB:
                return Distribusjonstype.VEDTAK;
        }
        return Distribusjonstype.ANNET;
    }
}

