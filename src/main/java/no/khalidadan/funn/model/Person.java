package no.khalidadan.funn.model;

import no.khalidadan.funn.exceptions.UgyldigFunnDataException;

/**
 * A hobby archaeologist who reported one or more finds.
 * Fields are private and final: once a Person is built there is no way for
 * calling code to reach in and put it into an inconsistent state, which is
 * the whole point of encapsulation, not just marking fields "private" and
 * handing out a setter for every one of them.
 */
public final class Person {

    private final int id;
    private final String navn;
    private final String telefon;
    private final String epost;

    public Person(int id, String navn, String telefon, String epost) {
        if (id <= 0) {
            throw new UgyldigFunnDataException("Person-id må være positiv, var: " + id);
        }
        if (navn == null || navn.isBlank()) {
            throw new UgyldigFunnDataException("Navn kan ikke være tomt (person-id " + id + ")");
        }
        this.id = id;
        this.navn = navn;
        this.telefon = telefon;
        this.epost = epost;
    }

    public int getId() {
        return id;
    }

    public String getNavn() {
        return navn;
    }

    public String getTelefon() {
        return telefon;
    }

    public String getEpost() {
        return epost;
    }

    @Override
    public String toString() {
        return navn + " (id=" + id + ")";
    }
}
