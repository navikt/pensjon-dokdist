package no.nav.pensjondokdist.saf.graphql;

import java.util.List;

public class GraphQlResponse {
    private GrapQlData data;
    private List<GraphQlError> errors;

    public GraphQlResponse() {

    }

    public void setData(GrapQlData data) {
        this.data = data;
    }

    public GrapQlData getData() {
        return data;
    }

    public List<GraphQlError> getErrors() {
        return errors;
    }

    public void setErrors(List<GraphQlError> errors) {
        this.errors = errors;
    }
}
