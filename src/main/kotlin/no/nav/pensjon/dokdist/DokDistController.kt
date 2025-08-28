package no.nav.pensjon.dokdist

import no.nav.pensjon.dokdist.brevmetadata.BrevmetadataService
import no.nav.pensjon.dokdist.dokarkiv.DokarkivService
import no.nav.pensjon.dokdist.dokdistfordeling.*
import no.nav.pensjon.dokdist.dokdistfordeling.DistribuerJournalpostRequest.Distribusjonstype
import no.nav.pensjon.dokdist.saf.*
import org.slf4j.*
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

const val FRITEKST_BREV_KODE = "PE_IY_05_300"
const val FERDIG_OG_KLAR_SENTRAL_PRINT_STATUS = "FS"
const val FERDIG_OG_KLAR_LOKAL_PRINT_STATUS = "FL"

class DokDistException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

data class JournalpostResponse(val journalpostId: String, val fritekst: Boolean)
data class DistribuerRequest(val status: String, val distribusjonstype: Distribusjonstype?)

@RestController
class DokDistController(
    private val saf: SafService,
    private val brevmetadata: BrevmetadataService,
    private val dokarkiv: DokarkivService,
    private val distribuerJournalpost: DistribuerJournalpostService,
) {

    @GetMapping("/api/journalpost/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getJournalpost(@PathVariable("id") journalpostId: String): JournalpostResponse =
        JournalpostResponse(journalpostId, fetchJournalpost(journalpostId).findBrevkode() == FRITEKST_BREV_KODE)

    @PostMapping("/api/journalpost/{id}/send")
    fun distribuerJournalpost(
        @PathVariable("id") journalpostId: String,
        @RequestBody request: DistribuerRequest,
    ): ResponseEntity<DistribuerJournalpostResponse> {
        val journalpost = fetchJournalpost(journalpostId)
        val journalfoerendeEnhet = journalpost.journalfoerendeEnhet
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Journalpost $journalpostId mangler journalfoerendeEnhet")
        val distribusjonstype = bestemDistribusjonstype(journalpost, request.distribusjonstype)

        if (readyToDistribute(journalpostId, journalfoerendeEnhet, request.status)) {
            val response = distribuerJournalpost.distribuer(journalpostId, distribusjonstype)

            return when (response.status) {
                Status.OK if response.originalResponse!!.bestillingsId.isNotBlank() -> {
                    logger.info("Journalpost $journalpostId er distribuert med bestillingsId ${response.originalResponse.bestillingsId}")
                    ResponseEntity.ok(response.originalResponse)
                }
                Status.CONFLICT -> ResponseEntity.noContent().build()
                else -> throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fikk tom bestillingsId fra dokdistfordeling")
            }
        }

        return ResponseEntity.badRequest().build()
    }

    private fun fetchJournalpost(journalpostId: String): Journalpost =
        saf.fetchJournal(journalpostId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Journalpost finnes ikke: $journalpostId")

    // Litt merkelig denne funksjonen her.
    // Funksjonen vil nekte distribusjon av en journalpost basert på status angitt i request.
    private fun readyToDistribute(journalpostId: String, journalfoerendeEnhet: String, status: String): Boolean =
        when (status) {
            FERDIG_OG_KLAR_SENTRAL_PRINT_STATUS -> true
            FERDIG_OG_KLAR_LOKAL_PRINT_STATUS -> {
                dokarkiv.ferdigstillJournalpost(journalpostId, journalfoerendeEnhet)
                true
            }
            else -> false
        }

    private fun bestemDistribusjonstype(journalpost: Journalpost, requestType: Distribusjonstype?): Distribusjonstype {
        val brevkode = journalpost.findBrevkode()

        return if (brevkode == FRITEKST_BREV_KODE) {
            requestType ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Mangler distribusjonstype ved distribusjon av fritekstbrev")
        } else {
            brevmetadata.fetchBrevmetadata(brevkode)?.dokumentkategori?.toDistribusjonstype()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Fant ikke brevmetadata for brevkode: $brevkode")
        }
    }

    private fun Journalpost.findBrevkode(): String {
        val dokument = dokumenter?.filterNotNull()
            ?.firstOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Journalpost mangler dokumenter: $journalpostId")

        return dokument.brevkode ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Journalpost mangler brevkode: $journalpostId")
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(DokDistController::class.java)
    }
}
