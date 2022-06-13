package no.nav.pensjondokdist;

import no.nav.pensjondokdist.brevmetadata.BrevMetadataService;
import no.nav.pensjondokdist.brevmetadata.Brevdata;
import no.nav.pensjondokdist.brevmetadata.DokumentkategoriCode;
import no.nav.pensjondokdist.saf.SafService;
import no.nav.pensjondokdist.saf.model.Journalpost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import no.nav.pensjondokdist.distribuerjournalpost.DistribuerJournalpostService;
import no.nav.pensjondokdist.distribuerjournalpost.dto.DistribuerJournalpostResponse;
import no.nav.pensjondokdist.distribuerjournalpost.dto.PensjondokdistRequest;
import no.nav.pensjondokdist.journalforing.JournalforingService;


@Controller
public class PensjonDokdistController {
    private static Logger logger = LoggerFactory.getLogger(PensjonDokdistController.class);

    private DistribuerJournalpostService distribuerJournalpostService;
    private JournalforingService journalforingService;
    private final SafService safService;
    private final BrevMetadataService brevMetadataService;

    public PensjonDokdistController(DistribuerJournalpostService distribuerJournalpostService,
                                    JournalforingService journalforingService,
                                    SafService safService,
                                    BrevMetadataService brevMetadataService) {
        this.distribuerJournalpostService = distribuerJournalpostService;
        this.journalforingService = journalforingService;
        this.safService = safService;
        this.brevMetadataService = brevMetadataService;
    }

    @RequestMapping(value = "/api/journalpost/{id}/send", method = RequestMethod.POST)
    public ResponseEntity sendJournalbrev(@PathVariable("id") String journalpostId, @RequestBody PensjondokdistRequest request) {
        Journalpost journalpost = safService.hentJournalPost(journalpostId);
        DokumentkategoriCode dokumentKategori = brevMetadataService.fetchBrevdata(journalpost.getBrevkode()).getDokumentkategori();

        if (changeStatusJournalbrev(journalpostId, request.getStatus(), journalpost.getJournalforendeEnhet())) {

            DistribuerJournalpostResponse response = distribuerJournalpostService.distribuerJournalpost(
                    journalpostId, request, dokumentKategori);

            if (!response.getBestillingsId().isEmpty()) {
                logger.info("Journalpost: " + journalpostId + " bestillingsId: " + response.getBestillingsId());
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    private Boolean changeStatusJournalbrev(String journalpostId, String journalpostStatus, String journalfoerendeEnhet) {
        return journalforingService.ferdigstillJournalpost(
                journalpostStatus,
                journalpostId,
                journalfoerendeEnhet);
    }
}
