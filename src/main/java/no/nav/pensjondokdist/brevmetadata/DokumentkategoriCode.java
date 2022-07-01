package no.nav.pensjondokdist.brevmetadata;

import no.nav.pensjondokdist.PensjonDokdistException;
import no.nav.pensjondokdist.distribuerjournalpost.dto.Distribusjonstype;

public enum DokumentkategoriCode {
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
    E_BLANKETT,

    F,
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

    public Distribusjonstype toDistribusjonstype() {
        switch (this){
            case EP:
            case ES:
            case E_BLANKETT:
            case F:
            case IS:
            case KD:
            case KM:
            case KS:
            case SED:
            case TS:
                throw new PensjonDokdistException("Ugyldig dokumentkategorikode for distribusjon");
            case IB:
                return Distribusjonstype.ANNET;
            case B:
                return Distribusjonstype.VIKTIG;
            case VB:
                return Distribusjonstype.VEDTAK;
        }
        return Distribusjonstype.ANNET;
    }
}
