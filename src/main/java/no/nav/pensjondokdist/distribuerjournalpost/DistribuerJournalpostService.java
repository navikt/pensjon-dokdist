package no.nav.pensjondokdist.distribuerjournalpost;

import static no.nav.pensjondokdist.util.JsonUtil.toJsonString;

import no.nav.pensjondokdist.PensjonDokdistException;
import no.nav.pensjondokdist.brevmetadata.DokumentkategoriCode;
import no.nav.pensjondokdist.journalforing.dto.Distribusjonstidspunkt;
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
                        .distribusjonstidspunkt(Distribusjonstidspunkt.KJERNETID)
                        .bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
                        .dokumentProdApp(BESTILLENDE_FAGSYSTEM)
                        .adresse(request.getAdresse())
                        .build());
    }

    public Distribusjonstype dokumentkategoriToDistribusjonstype(DokumentkategoriCode dokumentkategoriCode) {
        switch (dokumentkategoriCode){
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
                throw new PensjonDokdistException("Ugyldig dokumentkategorikode for distribusjon");
            case IB:
                return Distribusjonstype.ANNET;
            case B:
                return Distribusjonstype.VIKTIG;
            case VB:
                return Distribusjonstype.VEDTAK;
        }
        return Distribusjonstype.ANNET;
    }
}

