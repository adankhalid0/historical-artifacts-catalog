package no.khalidadan.funn.model;

import no.khalidadan.funn.exceptions.UgyldigFunnDataException;

import java.time.LocalDate;
import java.util.Locale;

public final class Vapen extends Funngjenstand {

    private final String vapenType;
    private final String materiale;
    private final double vektGram;

    public Vapen(int id, double breddegrad, double lengdegrad, int personId, LocalDate datoFunnet,
                 int antattAar, Integer museumId, String vapenType, String materiale, double vektGram) {
        super(id, breddegrad, lengdegrad, personId, datoFunnet, antattAar, museumId);
        if (vapenType == null || vapenType.isBlank()) {
            throw new UgyldigFunnDataException("Våpentype på våpen #" + id + " kan ikke være tom");
        }
        if (materiale == null || materiale.isBlank()) {
            throw new UgyldigFunnDataException("Materiale på våpen #" + id + " kan ikke være tomt");
        }
        if (vektGram <= 0) {
            throw new UgyldigFunnDataException("Vekt på våpen #" + id + " må være positiv, var: " + vektGram);
        }
        this.vapenType = vapenType;
        this.materiale = materiale;
        this.vektGram = vektGram;
    }

    @Override
    public String getTypeNavn() {
        return "Våpen";
    }

    @Override
    protected String detaljer() {
        return String.format(Locale.US, "%s (%s), %.0fg", vapenType, materiale, vektGram);
    }

    public String getVapenType() {
        return vapenType;
    }

    public String getMateriale() {
        return materiale;
    }

    public double getVektGram() {
        return vektGram;
    }
}
