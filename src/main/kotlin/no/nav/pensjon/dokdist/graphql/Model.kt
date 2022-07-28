package no.nav.pensjon.dokdist.graphql

import com.fasterxml.jackson.annotation.JsonProperty

data class GraphQLRequest<T: Any>(val query: String, val variables: T)

data class GraphQLResponse<T: Any>(
    @JsonProperty val data: T?,
    @JsonProperty val errors: List<Error>?
) {
    data class Error(
        val message: String,
        val locations: List<Location>?,
        val path: List<String>?,
        val exceptionType: String?,
        val exception: String?,
    ) {
        data class Location(
            private val line: String?,
            private val column: String?,
        )
    }
}
