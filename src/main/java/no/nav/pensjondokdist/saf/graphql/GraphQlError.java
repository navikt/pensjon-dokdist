package no.nav.pensjondokdist.saf.graphql;

import java.util.List;

public class GraphQlError {

    private String message;

    private List<ErrorLocation> locations;

    private List<String> path;

    private String exceptionType;

    private String exception;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ErrorLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<ErrorLocation> locations) {
        this.locations = locations;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

}
