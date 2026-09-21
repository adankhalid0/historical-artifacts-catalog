package no.khalidadan.funn.model;

import no.khalidadan.funn.exceptions.UgyldigFunnDataException;

/**
 * A museum a find can be housed in. Not every find has one - see
 * {@link Funngjenstand#getMuseumId()}.
 */
public final class Museum {

    private final int id;
    private final String navn;
    private final String sted;

    public Museum(int id, String navn, String sted) {
        if (id <= 0) {
            throw new UgyldigFunnDataException("Museum-id må være positiv, var: " + id);
        }
        if (navn == null || navn.isBlank()) {
            throw new UgyldigFunnDataException("Museumsnavn kan ikke være tomt (museum-id " + id + ")");
        }
        this.id = id;
        this.navn = navn;
        this.sted = sted;
    }

    public int getId() {
        return id;
    }

    public String getNavn() {
        return navn;
    }

    public String getSted() {
        return sted;
    }

    @Override
    public String toString() {
        return navn + ", " + sted + " (id=" + id + ")";
    }
}
