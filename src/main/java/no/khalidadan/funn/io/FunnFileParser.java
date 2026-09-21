package no.khalidadan.funn.io;

import no.khalidadan.funn.exceptions.FunnFileParseException;
import no.khalidadan.funn.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads funn.txt (see README for the file format) and turns it into
 * in-memory model objects. Everything that can go wrong while reading - a
 * missing section header, a line that will not parse as a number or a date,
 * the file ending mid-record - is caught here and re-thrown as a single
 * FunnFileParseException that says which line failed and why, rather than
 * leaking a raw NumberFormatException, DateTimeParseException or
 * IndexOutOfBoundsException up to whoever called us.
 */
public class FunnFileParser {

    public ParseResultat parse(Path filsti) throws FunnFileParseException {
        List<String> linjer;
        try {
            linjer = Files.readAllLines(filsti, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FunnFileParseException("Kunne ikke lese filen: " + filsti, e);
        }
        // Blank lines aren't part of the format; ignoring them makes the parser
        // robust to trailing newlines or a stray blank line without changing
        // what any of the position-based reads below mean.
        linjer.removeIf(String::isBlank);

        Linjeleser leser = new Linjeleser(linjer);
        try {
            List<Person> personer = lesPersoner(leser);
            List<Museum> museer = lesMuseer(leser);
            List<Funngjenstand> funn = lesFunn(leser);
            return new ParseResultat(personer, museer, funn);
        } catch (RuntimeException e) {
            // NumberFormatException, DateTimeParseException, etc. from the reads
            // below all mean the same thing to a caller: the file didn't match
            // the expected format at the line we were on.
            throw new FunnFileParseException(
                    "Feil ved linje " + leser.gjeldendeLinjenummer() + ": " + e.getMessage(), e);
        }
    }

    private List<Person> lesPersoner(Linjeleser leser) throws FunnFileParseException {
        leser.forventOverskrift("Personer:");
        int antall = Integer.parseInt(leser.nesteLinje());
        List<Person> personer = new ArrayList<>(antall);
        for (int i = 0; i < antall; i++) {
            int id = Integer.parseInt(leser.nesteLinje());
            String navn = leser.nesteLinje();
            String telefon = leser.nesteLinje();
            String epost = leser.nesteLinje();
            personer.add(new Person(id, navn, telefon, epost));
        }
        return personer;
    }

    private List<Museum> lesMuseer(Linjeleser leser) throws FunnFileParseException {
        leser.forventOverskrift("Museer:");
        int antall = Integer.parseInt(leser.nesteLinje());
        List<Museum> museer = new ArrayList<>(antall);
        for (int i = 0; i < antall; i++) {
            int id = Integer.parseInt(leser.nesteLinje());
            String navn = leser.nesteLinje();
            String sted = leser.nesteLinje();
            museer.add(new Museum(id, navn, sted));
        }
        return museer;
    }

    private List<Funngjenstand> lesFunn(Linjeleser leser) throws FunnFileParseException {
        leser.forventOverskrift("Funn:");
        List<Funngjenstand> funn = new ArrayList<>();
        while (leser.harMerLinjer()) {
            funn.add(lesEttFunn(leser));
        }
        return funn;
    }

    private Funngjenstand lesEttFunn(Linjeleser leser) throws FunnFileParseException {
        int id = Integer.parseInt(leser.nesteLinje());

        String[] koordinater = leser.nesteLinje().split(",");
        double breddegrad = Double.parseDouble(koordinater[0].trim());
        double lengdegrad = Double.parseDouble(koordinater[1].trim());

        int personId = Integer.parseInt(leser.nesteLinje());
        LocalDate datoFunnet;
        try {
            datoFunnet = LocalDate.parse(leser.nesteLinje());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("ugyldig dato på funn #" + id, e);
        }
        int antattAar = Integer.parseInt(leser.nesteLinje());

        // The museum-id line is only present when the find actually sits in a
        // museum (see vedlegg 1 in the exam text: "Her er det ingen id til
        // museum" - the line is skipped entirely, not left blank). We tell
        // the two cases apart by peeking: a museum id is numeric, a type name
        // ("Mynt"/"Våpen"/"Smykke") is not.
        Integer museumId = null;
        if (erHeltall(leser.pekNesteLinje())) {
            museumId = Integer.parseInt(leser.nesteLinje());
        }

        String type = leser.nesteLinje();
        Funngjenstand resultat = switch (type) {
            case "Mynt" -> {
                double diameter = Double.parseDouble(leser.nesteLinje());
                String metall = leser.nesteLinje();
                yield new Mynt(id, breddegrad, lengdegrad, personId, datoFunnet, antattAar, museumId, diameter, metall);
            }
            case "Våpen" -> {
                String vapenType = leser.nesteLinje();
                String materiale = leser.nesteLinje();
                double vekt = Double.parseDouble(leser.nesteLinje());
                yield new Vapen(id, breddegrad, lengdegrad, personId, datoFunnet, antattAar, museumId, vapenType, materiale, vekt);
            }
            case "Smykke" -> {
                String smykkeType = leser.nesteLinje();
                double verdi = Double.parseDouble(leser.nesteLinje());
                String bildefil = leser.nesteLinje();
                yield new Smykke(id, breddegrad, lengdegrad, personId, datoFunnet, antattAar, museumId, smykkeType, verdi, bildefil);
            }
            default -> throw new IllegalArgumentException("ukjent funntype \"" + type + "\" på funn #" + id);
        };

        leser.forventOverskrift("-------");
        return resultat;
    }

    private static boolean erHeltall(String tekst) {
        if (tekst == null || tekst.isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(tekst.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Small position-tracking cursor over the file's lines. Kept private to
     * this class: nothing outside FunnFileParser needs to know the file is
     * read line-by-line rather than, say, streamed.
     */
    private static final class Linjeleser {
        private final List<String> linjer;
        private int indeks = 0;

        Linjeleser(List<String> linjer) {
            this.linjer = linjer;
        }

        boolean harMerLinjer() {
            return indeks < linjer.size();
        }

        String nesteLinje() {
            return linjer.get(indeks++).trim();
        }

        String pekNesteLinje() {
            return harMerLinjer() ? linjer.get(indeks) : null;
        }

        void forventOverskrift(String forventet) throws FunnFileParseException {
            String funnet = nesteLinje();
            if (!funnet.equals(forventet)) {
                throw new FunnFileParseException(
                        "Forventet \"" + forventet + "\" på linje " + gjeldendeLinjenummer() + ", fant \"" + funnet + "\"");
            }
        }

        int gjeldendeLinjenummer() {
            return indeks;
        }
    }
}
