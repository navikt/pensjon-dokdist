package no.nav.pensjon.dokdist.brevmetadata

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException.BadRequest
import org.springframework.web.client.getForEntity

interface BrevmetadataService {
    fun fetchBrevmetadata(brevkode: String): Brevdata?
}

data class Brevdata(val dokumentkategori: DokumentkategoriCode)

@Component
class BrevmetadataClient(
    @Value("\${brevmetadata.url}") private val url: String,
    restTemplateBuilder: RestTemplateBuilder,
) : BrevmetadataService {
    private val restTemplate = restTemplateBuilder.rootUri(url).build()

    override fun fetchBrevmetadata(brevkode: String): Brevdata? =
        try {
            restTemplate.getForEntity<Brevdata>("/api/brevdata/brevForBrevkode/$brevkode").body
        } catch (e: BadRequest) {
            null
        }

}
