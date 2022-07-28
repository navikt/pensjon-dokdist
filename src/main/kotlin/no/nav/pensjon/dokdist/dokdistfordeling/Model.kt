package no.nav.pensjon.dokdist.dokdistfordeling

data class DistribuerJournalpostResponse (val bestillingsId: String)

data class DistribuerJournalpostRequest (
    val journalpostId: String,
    val bestillendeFagsystem: String,
    val dokumentProdApp: String,
    val distribusjonstype: Distribusjonstype,
    val distribusjonstidspunkt: Distribusjonstidspunkt,
) {
    enum class Distribusjonstype { VEDTAK, VIKTIG, ANNET, }
    enum class Distribusjonstidspunkt { UMIDDELBART, KJERNETID, }
}
