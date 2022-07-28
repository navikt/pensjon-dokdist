package no.nav.pensjon.dokdist.brevmetadata

import no.nav.pensjon.dokdist.DokDistException
import no.nav.pensjon.dokdist.dokdistfordeling.DistribuerJournalpostRequest

enum class DokumentkategoriCode {
    /**
     * Brev
     */
    B,

    /**
     * E-post
     */
    EP,

    /**
     * Elektronisk skjema
     */
    ES,

    /**
     * E-blankett
     */
    E_BLANKETT, F,

    /**
     * Informasjonsbrev
     */
    IB,

    /**
     * Ikke tolkbart skjema
     */
    IS,

    /**
     * Konvertert fra elektronisk arkiv
     */
    KD,

    /**
     * Konvertert fra papirarkiv (skannet)
     */
    KM,

    /**
     * Konverterte data fra gammelt system
     */
    KS,

    /**
     * Strukturerte elektroniske dokumenter
     */
    SED,

    /**
     * Tolkbart skjema
     */
    TS,

    /**
     * Vedtaksbrev
     */
    VB;

    fun toDistribusjonstype(): DistribuerJournalpostRequest.Distribusjonstype {
        return when (this) {
            EP, ES, E_BLANKETT, F, IS, KD, KM, KS, SED, TS -> throw DokDistException(
                "Ugyldig dokumentkategorikode for distribusjon"
            )
            IB -> DistribuerJournalpostRequest.Distribusjonstype.ANNET
            B -> DistribuerJournalpostRequest.Distribusjonstype.VIKTIG
            VB -> DistribuerJournalpostRequest.Distribusjonstype.VEDTAK
        }
    }
}
