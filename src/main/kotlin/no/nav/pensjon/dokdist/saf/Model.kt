package no.nav.pensjon.dokdist.saf

data class Journalpost(
    val journalpostId: String,
    val journalfoerendeEnhet: String?,
    val dokumenter: List<Dokument?>?,
) {
    data class Dokument(val brevkode: String?)
}
