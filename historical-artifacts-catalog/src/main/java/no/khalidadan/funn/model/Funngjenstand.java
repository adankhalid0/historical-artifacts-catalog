package no.khalidadan.funn.model;

import no.khalidadan.funn.exceptions.UgyldigFunnDataException;

import java.time.LocalDate;

/**
 * A found historical artifact. This is the abstract-class half of the exam's
 * "abstract class or interface" requirement (the interface half is
 * {@link Rapporterbar}).
 *
 * Mynt, Vapen and Smykke share every field defined here - where it was found,
 * who found it, when, how old it is believed to be, and which museum (if
 * any) it now sits in - and differ only in a handful of type-specific
 * properties. That is exactly the "common properties, some different
 * properties" relationship the case description points at, so it is modeled
 * as inheritance rather than three unrelated classes or one giant class with
 * a pile of nullable fields for properties that only apply to one type.
 *
 * tilRapportLinje() is a template method: the shared parts of the report
 * line (id, date, finder, museum status) are assembled once here and are
 * final, so subclasses cannot accidentally produce an inconsistent report
 * format; each subclass only has to supply its own type name and detail
 * string via the two abstract methods below. That is the polymorphism this
 * class exists to demonstrate - Part 2 of the program calls
 * tilRapportLinje() on a List&lt;Funngjenstand&gt; without ever checking
 * "is this a Mynt or a Vapen".
 */
public abstract class Funngjenstand implements Rapporterbar {

    private final int id;
    private final double breddegrad;
    private final double lengdegrad;
    private final int personId;
    private final LocalDate datoFunnet;
    private final int antattAar;

    // The only field on a find that legitimately changes after the fact: a
    // find can be handed over to a museum (or moved between museums) after
    // it was first registered. Everything else about a find - who found it,
    // where, when - does not change once recorded, so those fields above are
    // plain finals with no setters. This one field gets a validated setter
    // instead of being final, which is the point being demonstrated: encapsulation
    // is about controlling *how* a field can change, not simply hiding it.
    private Integer museumId;

    protected Funngjenstand(int id, double breddegrad, double lengdegrad, int personId,
                             LocalDate datoFunnet, int antattAar, Integer museumId) {
        if (id <= 0) {
            throw new UgyldigFunnDataException("Funn-id må være positiv, var: " + id);
        }
        if (breddegrad < -90 || breddegrad > 90) {
            throw new UgyldigFunnDataException("Breddegrad utenfor gyldig område (-90..90): " + breddegrad);
        }
        if (lengdegrad < -180 || lengdegrad > 180) {
            throw new UgyldigFunnDataException("Lengdegrad utenfor gyldig område (-180..180): " + lengdegrad);
        }
        if (personId <= 0) {
            throw new UgyldigFunnDataException("Person-id på funn #" + id + " må være positiv, var: " + personId);
        }
        if (datoFunnet == null || datoFunnet.isAfter(LocalDate.now())) {
            throw new UgyldigFunnDataException("Funndato på funn #" + id + " mangler eller er i fremtiden: " + datoFunnet);
        }
        if (antattAar > LocalDate.now().getYear() || antattAar < -3000) {
            throw new UgyldigFunnDataException("Antatt årstall på funn #" + id + " er urimelig: " + antattAar);
        }

        this.id = id;
        this.breddegrad = breddegrad;
        this.lengdegrad = lengdegrad;
        this.personId = personId;
        this.datoFunnet = datoFunnet;
        this.antattAar = antattAar;
        setMuseumId(museumId);
    }

    /** Subclass-specific type label, e.g. "Mynt". Used in the report line and when writing to the database. */
    public abstract String getTypeNavn();

    /** Subclass-specific detail string, e.g. "28mm, sølv". Kept separate from tilRapportLinje() so the shared format stays in one place. */
    protected abstract String detaljer();

    @Override
    public final String tilRapportLinje() {
        String museumInfo = harMuseum()
                ? "oppbevart på museum #" + museumId
                : "ikke plassert på museum";
        return String.format("[%s #%d] funnet %s av person #%d, antatt fra år %d, %s | %s",
                getTypeNavn(), id, datoFunnet, personId, antattAar, museumInfo, detaljer());
    }

    public boolean harMuseum() {
        return museumId != null;
    }

    public void setMuseumId(Integer museumId) {
        if (museumId != null && museumId <= 0) {
            throw new UgyldigFunnDataException("Museum-id på funn #" + id + " må være positiv, var: " + museumId);
        }
        this.museumId = museumId;
    }

    public int getId() {
        return id;
    }

    public double getBreddegrad() {
        return breddegrad;
    }

    public double getLengdegrad() {
        return lengdegrad;
    }

    public int getPersonId() {
        return personId;
    }

    public LocalDate getDatoFunnet() {
        return datoFunnet;
    }

    public int getAntattAar() {
        return antattAar;
    }

    public Integer getMuseumId() {
        return museumId;
    }
}
