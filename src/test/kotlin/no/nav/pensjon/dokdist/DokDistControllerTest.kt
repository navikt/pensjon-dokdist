package no.nav.pensjon.dokdist

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import no.nav.pensjon.dokdist.brevmetadata.*
import no.nav.pensjon.dokdist.dokarkiv.DokarkivService
import no.nav.pensjon.dokdist.dokdistfordeling.*
import no.nav.pensjon.dokdist.dokdistfordeling.DistribuerJournalpostRequest.Distribusjonstype
import no.nav.pensjon.dokdist.saf.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
class DokDistControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockkBean
    private lateinit var saf: SafService

    @MockkBean
    private lateinit var brevmetadata: BrevmetadataService

    @MockkBean
    private lateinit var dokarkiv: DokarkivService

    @MockkBean
    private lateinit var distribuerJournalpost: DistribuerJournalpostService

    private val brevkode = "1234"
    private val journalpost = Journalpost("1234", "en-kul-enhet", listOf(Journalpost.Dokument("1234")))

    @Test
    fun `getJournalpost responds for non fritekst-brev`() {
        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost
        mockMvc.perform(get("/api/journalpost/${journalpost.journalpostId}").accept(APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("journalpostId").value(journalpost.journalpostId))
            .andExpect(jsonPath("fritekst").value(false))
    }

    @Test
    fun `getJournalpost responds for fritekst-brev`() {
        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost.copy(dokumenter = listOf(Journalpost.Dokument(FRITEKST_BREV_KODE)))
        mockMvc.perform(get("/api/journalpost/${journalpost.journalpostId}").accept(APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("journalpostId").value(journalpost.journalpostId))
            .andExpect(jsonPath("fritekst").value(true))
    }

    @Test
    fun `getJournalpost gives badrequest for no documents`() {
        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost.copy(dokumenter = emptyList())
        mockMvc.perform(get("/api/journalpost/${journalpost.journalpostId}").accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getJournalpost gives badrequest for no documents with brevkode`() {
        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost.copy(dokumenter = listOf(Journalpost.Dokument(null)))
        mockMvc.perform(get("/api/journalpost/${journalpost.journalpostId}").accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getJournalpost gives notfound for missing journalpost`() {
        every { saf.fetchJournal(journalpost.journalpostId) } returns null
        mockMvc.perform(get("/api/journalpost/${journalpost.journalpostId}").accept(APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `distribuerJournalpost FERDIG_OG_SENTRALPRINT is successful`() {
        val distribuerRequestJson = ObjectMapper().writeValueAsString(DistribuerRequest(FERDIG_OG_KLAR_SENTRAL_PRINT_STATUS, null))
        val brevdata = Brevdata(DokumentkategoriCode.B)

        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost
        every { brevmetadata.fetchBrevmetadata(brevkode) } returns brevdata
        every { distribuerJournalpost.distribuer(journalpost.journalpostId, brevdata.dokumentkategori.toDistribusjonstype()) }
            .returns(DistribueringInternalResponse(DistribuerJournalpostResponse("9911"), Status.OK))

        mockMvc.perform(
            post("/api/journalpost/${journalpost.journalpostId}/send")
                .content(distribuerRequestJson)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        ).andExpect(status().isOk)
    }

    @Test
    fun `distribuerJournalpost for FRITEKST_BREV requested distribusjonstype overrides brevdata`() {
        val distribuerRequestJson = ObjectMapper().writeValueAsString(DistribuerRequest(FERDIG_OG_KLAR_SENTRAL_PRINT_STATUS, Distribusjonstype.ANNET))
        val brevdata = Brevdata(DokumentkategoriCode.B)

        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost.copy(dokumenter = listOf(Journalpost.Dokument(FRITEKST_BREV_KODE)))
        every { brevmetadata.fetchBrevmetadata(brevkode) } returns brevdata
        every { distribuerJournalpost.distribuer(journalpost.journalpostId, Distribusjonstype.ANNET) }
            .returns(DistribueringInternalResponse(DistribuerJournalpostResponse("9911"), Status.OK))

        mockMvc.perform(
            post("/api/journalpost/${journalpost.journalpostId}/send")
                .content(distribuerRequestJson)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        ).andExpect(status().isOk)

        verify(atLeast = 1) { distribuerJournalpost.distribuer(journalpost.journalpostId, Distribusjonstype.ANNET) }
    }

    @Test
    fun `distribuerJournalpost for FRITEKST_BREV requires distribusjonstype`() {
        val distribuerRequestJson = ObjectMapper().writeValueAsString(DistribuerRequest(FERDIG_OG_KLAR_SENTRAL_PRINT_STATUS, null))
        val brevdata = Brevdata(DokumentkategoriCode.B)

        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost.copy(dokumenter = listOf(Journalpost.Dokument(FRITEKST_BREV_KODE)))
        every { brevmetadata.fetchBrevmetadata(brevkode) } returns brevdata
        every { distribuerJournalpost.distribuer(journalpost.journalpostId, Distribusjonstype.ANNET) }
            .returns(DistribueringInternalResponse(DistribuerJournalpostResponse("9911"), Status.OK))

        mockMvc.perform(
            post("/api/journalpost/${journalpost.journalpostId}/send")
                .content(distribuerRequestJson)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `distribuerJournalpost FERDIG_OG_LOKALPRINT is successful`() {
        val distribuerRequestJson = ObjectMapper().writeValueAsString(DistribuerRequest(FERDIG_OG_KLAR_LOKAL_PRINT_STATUS, null))
        val brevdata = Brevdata(DokumentkategoriCode.B)

        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost
        every { brevmetadata.fetchBrevmetadata(brevkode) } returns brevdata
        every { distribuerJournalpost.distribuer(journalpost.journalpostId, brevdata.dokumentkategori.toDistribusjonstype()) }
            .returns(DistribueringInternalResponse(DistribuerJournalpostResponse("9911"), Status.OK))
        justRun { dokarkiv.ferdigstillJournalpost(journalpost.journalpostId, journalpost.journalfoerendeEnhet!!) }

        mockMvc.perform(
            post("/api/journalpost/${journalpost.journalpostId}/send")
                .content(distribuerRequestJson)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        ).andExpect(status().isOk)

        verify(atLeast = 1) { dokarkiv.ferdigstillJournalpost(journalpost.journalpostId, journalpost.journalfoerendeEnhet!!) }
    }

    @Test
    fun `distribuerJournalpost annen status responds bad request`() {
        val distribuerRequestJson = ObjectMapper().writeValueAsString(DistribuerRequest("hei", null))
        val brevdata = Brevdata(DokumentkategoriCode.B)

        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost
        every { brevmetadata.fetchBrevmetadata(brevkode) } returns brevdata
        every { distribuerJournalpost.distribuer(journalpost.journalpostId, brevdata.dokumentkategori.toDistribusjonstype()) }
            .returns(DistribueringInternalResponse(DistribuerJournalpostResponse("9911"), Status.OK))

        mockMvc.perform(
            post("/api/journalpost/${journalpost.journalpostId}/send")
                .content(distribuerRequestJson)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        ).andExpect(status().isBadRequest)

        verify(exactly = 0) { distribuerJournalpost.distribuer(any(), any()) }
    }

    @Test
    fun `distribuerJournalpost missing journalfoerendeEnhet responds bad request`() {
        val distribuerRequestJson = ObjectMapper().writeValueAsString(DistribuerRequest("FS", null))
        val brevdata = Brevdata(DokumentkategoriCode.B)

        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost.copy(journalfoerendeEnhet = null)
        every { brevmetadata.fetchBrevmetadata(brevkode) } returns brevdata

        mockMvc.perform(
            post("/api/journalpost/${journalpost.journalpostId}/send")
                .content(distribuerRequestJson)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `distribuerJournalpost with missing brevdata responds bad request`() {
        val distribuerRequestJson = ObjectMapper().writeValueAsString(DistribuerRequest("FS", null))

        every { saf.fetchJournal(journalpost.journalpostId) } returns journalpost
        every { brevmetadata.fetchBrevmetadata(brevkode) } returns null

        mockMvc.perform(
            post("/api/journalpost/${journalpost.journalpostId}/send")
                .content(distribuerRequestJson)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        ).andExpect(status().isBadRequest)
    }
}
