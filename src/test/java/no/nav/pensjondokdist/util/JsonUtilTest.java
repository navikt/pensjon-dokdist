package no.nav.pensjondokdist.util;

import static org.assertj.core.api.Assertions.assertThat;

import static no.nav.pensjondokdist.util.JsonUtil.toJsonString;
import static no.nav.pensjondokdist.util.JsonUtil.toObjectFromJsonString;

import com.fasterxml.jackson.core.type.TypeReference;

import no.nav.pensjondokdist.journalforing.dto.Distribusjonstidspunkt;
import no.nav.pensjondokdist.journalforing.dto.Distribusjonstype;
import org.junit.Test;

import no.nav.pensjondokdist.distribuerjournalpost.dto.Adresse;
import no.nav.pensjondokdist.distribuerjournalpost.dto.DistribuerJournalpostRequest;
import no.nav.pensjondokdist.journalforing.dto.FerdigstillJournalpostRequest;
import no.nav.pensjondokdist.saf.graphql.GrapQlData;
import no.nav.pensjondokdist.saf.graphql.GraphQlRequest;
import no.nav.pensjondokdist.saf.graphql.GraphQlResponse;
import no.nav.pensjondokdist.saf.graphql.Variables;
import no.nav.pensjondokdist.saf.model.Journalpost;

public class JsonUtilTest {

    @Test
    public void testConvertDistribuerJournalpostRequestToJsonString() {
        Adresse adresse = new Adresse();
        adresse.setAdresseType("normal");
        adresse.setLand("NO");

        DistribuerJournalpostRequest distribuerJournalpostRequest
                = new DistribuerJournalpostRequest("12345", "cd123", "AT05", "test",
                adresse, Distribusjonstype.ANNET, Distribusjonstidspunkt.KJERNETID);
        String jsonString = toJsonString(distribuerJournalpostRequest);
        DistribuerJournalpostRequest requestFromJsonString = toObjectFromJsonString(jsonString, new TypeReference<DistribuerJournalpostRequest>(){});

        assertThat(requestFromJsonString).usingRecursiveComparison().isEqualTo(distribuerJournalpostRequest);
    }

    @Test
    public void testFerdigstillJournalpostRequestToJsonString() {
        FerdigstillJournalpostRequest request = new FerdigstillJournalpostRequest("4080");
        String jsonString = toJsonString(request);
        FerdigstillJournalpostRequest requestFromJsonString = toObjectFromJsonString(jsonString, new TypeReference<FerdigstillJournalpostRequest>(){});

        assertThat(requestFromJsonString).isEqualToComparingFieldByField(request);
    }

    @Test
    public void testGraphQlRequestToJsonString() {
        GraphQlRequest request = new GraphQlRequest("Query", new Variables("12345"));
        String jsonString = toJsonString(request);
        GraphQlRequest requsetFromJsonString = toObjectFromJsonString(jsonString, new TypeReference<GraphQlRequest>(){});

        assertThat(requsetFromJsonString).usingRecursiveComparison().isEqualTo(request);
    }

    @Test
    public void testGraphQlResponseFromJsonString() {
        GraphQlResponse graphQlResponse = new GraphQlResponse();
        GrapQlData data = new GrapQlData();
        Journalpost journalpost = new Journalpost();
        journalpost.setJournalforendeEnhet("4850");
        journalpost.setJournalpostId("12345");
        data.setJournalpost(journalpost);
        graphQlResponse.setData(data);

        String jsonString = toJsonString(graphQlResponse);
        GraphQlResponse responseFromJsonString = toObjectFromJsonString(jsonString, new TypeReference<GraphQlResponse>(){});

        assertThat(responseFromJsonString).usingRecursiveComparison().isEqualTo(graphQlResponse);
    }
}
