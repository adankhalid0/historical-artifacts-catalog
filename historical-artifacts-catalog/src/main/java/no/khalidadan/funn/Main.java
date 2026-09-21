package no.khalidadan.funn;

import no.khalidadan.funn.db.DatabaseManager;
import no.khalidadan.funn.db.FunnRepository;
import no.khalidadan.funn.exceptions.DatabaseOperationException;
import no.khalidadan.funn.exceptions.FunnFileParseException;
import no.khalidadan.funn.io.FunnFileParser;
import no.khalidadan.funn.io.ParseResultat;
import no.khalidadan.funn.model.Funngjenstand;
import no.khalidadan.funn.model.Museum;
import no.khalidadan.funn.model.Person;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Part 1 (import funn.txt into the database) and Part 2 (the menu) live in
 * one main method rather than two, per the assignment text's "du kan velge
 * å ha del 1 og del 2 i samme program". The schema is dropped and recreated
 * on every run (see funn.sql), so importing again on every start is
 * idempotent - there's no state left over from a previous run to conflict
 * with, and no separate "have I already imported?" check is needed.
 */
public class Main {

    public static void main(String[] args) {
        // funn.txt has Norwegian characters (æøå) in names, materials and museum
        // names. The default console encoding is platform-dependent (Windows in
        // particular often defaults to something other than UTF-8), so this is
        // set explicitly rather than trusting the JVM's default - otherwise the
        // program's own output would depend on which OS graded it.
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        Path databaseFile = Path.of("funn.db");
        Path dataFile = Path.of("data", "funn.txt");

        DatabaseManager databaseManager = new DatabaseManager(databaseFile);
        FunnRepository repository = new FunnRepository(databaseManager);

        System.out.println("=== Del 1: importerer " + dataFile + " ===");
        if (!kjorImport(dataFile, databaseManager, repository)) {
            System.out.println("Import feilet, avslutter.");
            return;
        }

        System.out.println();
        System.out.println("=== Del 2: se på funnene ===");
        kjorMeny(repository);
    }

    private static boolean kjorImport(Path dataFile, DatabaseManager databaseManager, FunnRepository repository) {
        try {
            databaseManager.initialiserSkjema();

            ParseResultat data = new FunnFileParser().parse(dataFile);

            for (Person person : data.personer()) {
                repository.lagrePerson(person);
            }
            for (Museum museum : data.museer()) {
                repository.lagreMuseum(museum);
            }
            for (Funngjenstand funn : data.funn()) {
                repository.lagreFunngjenstand(funn);
            }

            System.out.printf("Importerte %d personer, %d museer og %d funngjenstander.%n",
                    data.personer().size(), data.museer().size(), data.funn().size());
            return true;

        } catch (FunnFileParseException e) {
            System.out.println("Klarte ikke å tolke " + dataFile + ": " + e.getMessage());
            return false;
        } catch (DatabaseOperationException e) {
            System.out.println("Databasefeil under import: " + e.getMessage());
            return false;
        }
    }

    private static void kjorMeny(FunnRepository repository) {
        Scanner scanner = new Scanner(System.in);
        boolean fortsett = true;

        while (fortsett) {
            skrivMeny();
            String valg = scanner.nextLine().trim();

            try {
                switch (valg) {
                    case "1" -> skrivUtFunn(repository.hentAlleFunn());
                    case "2" -> {
                        System.out.print("Vis funn eldre enn hvilket årstall? ");
                        int aar = Integer.parseInt(scanner.nextLine().trim());
                        skrivUtFunn(repository.hentFunnEldreEnn(aar));
                    }
                    case "3" -> System.out.println("Antall registrerte funngjenstander: " + repository.tellFunn());
                    case "4" -> fortsett = false;
                    default -> System.out.println("Ugyldig valg, prøv igjen.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Det årstallet var ikke et gyldig tall.");
            } catch (DatabaseOperationException e) {
                // A database problem during the menu loop shouldn't crash the whole
                // program - print it and let the user try a different option.
                System.out.println("Databasefeil: " + e.getMessage());
            }
            System.out.println();
        }

        System.out.println("Avslutter. Ha en fin dag!");
    }

    private static void skrivMeny() {
        System.out.println("""
                1) Se informasjon om alle funngjenstander
                2) Se informasjon om alle funngjenstander eldre enn <årstall>
                3) Få informasjon om antall funngjenstander registrert
                4) Avslutt
                """);
        System.out.print("Velg (1-4): ");
    }

    private static void skrivUtFunn(List<Funngjenstand> funn) {
        if (funn.isEmpty()) {
            System.out.println("Ingen funn matcher.");
            return;
        }
        funn.forEach(f -> System.out.println(f.tilRapportLinje()));
        System.out.println("(" + funn.size() + " funn)");
    }
}
