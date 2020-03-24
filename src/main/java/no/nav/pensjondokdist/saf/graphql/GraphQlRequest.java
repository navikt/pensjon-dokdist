package no.nav.pensjondokdist.saf.graphql;

public class GraphQlRequest {

    private String query;
    private Variables variables;

    public GraphQlRequest() {

    }

    public GraphQlRequest(String query, Variables variables) {
        this.query = query;
        this.variables = variables;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Variables getVariables() {
        return variables;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }
}
