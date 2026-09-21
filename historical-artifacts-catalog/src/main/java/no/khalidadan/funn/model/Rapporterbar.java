package no.khalidadan.funn.model;

/**
 * Anything that can render itself as a single, human-readable report line.
 * Implemented by {@link Funngjenstand} (via a template method, see that
 * class) so that Part 2 of the program can print a list of finds without
 * knowing or caring whether each one is a Mynt, a Vapen or a Smykke.
 *
 * This is the "interface" half of the exam's abstract-class-or-interface
 * requirement; Funngjenstand below covers the abstract-class half.
 */
public interface Rapporterbar {
    String tilRapportLinje();
}
