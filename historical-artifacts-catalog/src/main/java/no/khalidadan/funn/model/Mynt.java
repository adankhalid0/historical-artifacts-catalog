package no.khalidadan.funn.model;

import no.khalidadan.funn.exceptions.UgyldigFunnDataException;

import java.time.LocalDate;
import java.util.Locale;

public final class Mynt extends Funngjenstand {

    private final double diameterMm;
    private final String metall;

    public Mynt(int id, double breddegrad, double lengdegrad, int personId, LocalDate datoFunnet,
                int antattAar, Integer museumId, double diameterMm, String metall) {
        super(id, breddegrad, lengdegrad, personId, datoFunnet, antattAar, museumId);
        if (diameterMm <= 0) {
            throw new UgyldigFunnDataException("Diameter på mynt #" + id + " må være positiv, var: " + diameterMm);
        }
        if (metall == null || metall.isBlank()) {
            throw new UgyldigFunnDataException("Metall på mynt #" + id + " kan ikke være tomt");
        }
        this.diameterMm = diameterMm;
        this.metall = metall;
    }

    @Override
    public String getTypeNavn() {
        return "Mynt";
    }

    @Override
    protected String detaljer() {
        return String.format(Locale.US, "%.0fmm, %s", diameterMm, metall);
    }

    public double getDiameterMm() {
        return diameterMm;
    }

    public String getMetall() {
        return metall;
    }
}
