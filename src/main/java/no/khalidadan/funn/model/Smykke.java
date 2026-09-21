package no.khalidadan.funn.model;

import no.khalidadan.funn.exceptions.UgyldigFunnDataException;

import java.time.LocalDate;
import java.util.Locale;

public final class Smykke extends Funngjenstand {

    private final String smykkeType;
    private final double verdiestimatNok;
    private final String bildefil;

    public Smykke(int id, double breddegrad, double lengdegrad, int personId, LocalDate datoFunnet,
                  int antattAar, Integer museumId, String smykkeType, double verdiestimatNok, String bildefil) {
        super(id, breddegrad, lengdegrad, personId, datoFunnet, antattAar, museumId);
        if (smykkeType == null || smykkeType.isBlank()) {
            throw new UgyldigFunnDataException("Smykketype på smykke #" + id + " kan ikke være tom");
        }
        if (verdiestimatNok < 0) {
            throw new UgyldigFunnDataException("Verdiestimat på smykke #" + id + " kan ikke være negativt: " + verdiestimatNok);
        }
        this.smykkeType = smykkeType;
        this.verdiestimatNok = verdiestimatNok;
        this.bildefil = bildefil;
    }

    @Override
    public String getTypeNavn() {
        return "Smykke";
    }

    @Override
    protected String detaljer() {
        return String.format(Locale.US, "%s, verdiestimat %.0f NOK", smykkeType, verdiestimatNok);
    }

    public String getSmykkeType() {
        return smykkeType;
    }

    public double getVerdiestimatNok() {
        return verdiestimatNok;
    }

    public String getBildefil() {
        return bildefil;
    }
}
