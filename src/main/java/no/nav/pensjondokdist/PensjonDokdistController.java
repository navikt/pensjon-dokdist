package no.nav.pensjondokdist;

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

    public PensjonDokdistController(DistribuerJournalpostService distribuerJournalpostService,
            JournalforingService journalforingService) {
        this.distribuerJournalpostService = distribuerJournalpostService;
        this.journalforingService = journalforingService;
    }

    @RequestMapping(value = "/api/journalpost/{id}/send", method = RequestMethod.POST)
    public ResponseEntity sendJournalbrev(@PathVariable("id") String journalpostId, @RequestBody PensjondokdistRequest request) {
        if (changeStatusJournalbrev(journalpostId, request.getStatus())) {
            DistribuerJournalpostResponse response = distribuerJournalpostService.distribuerJournalpost(journalpostId, request);
            if (!response.getBestillingsId().isEmpty()) {
                logger.info("Journalpost: " + journalpostId + " bestillingsId: " + response.getBestillingsId());
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    private Boolean changeStatusJournalbrev(String journalpostId, String journalpostStatus) {
        return journalforingService.ferdigstillJournalpost(
                journalpostStatus,
                journalpostId);
    }
}
