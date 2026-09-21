package no.khalidadan.funn.exceptions;

/**
 * Thrown when funn.txt cannot be parsed: a missing section, a line that does not
 * match the expected format, or a value that cannot be converted to the expected type.
 *
 * This is a checked exception on purpose. Import is a one-shot batch operation
 * (Part 1 of the program), so a parse failure is exactly the kind of condition
 * the caller must decide how to handle (abort the import, skip the record, log
 * and continue) rather than something that should silently crash the JVM.
 */
public class FunnFileParseException extends Exception {

    public FunnFileParseException(String message) {
        super(message);
    }

    public FunnFileParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
