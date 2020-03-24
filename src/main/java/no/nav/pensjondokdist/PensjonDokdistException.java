package no.nav.pensjondokdist;

public class PensjonDokdistException extends RuntimeException{
    public PensjonDokdistException(String message) {
        super(message);
    }

    public PensjonDokdistException(String message, Exception cause) {
        super(message, cause);
    }
}
