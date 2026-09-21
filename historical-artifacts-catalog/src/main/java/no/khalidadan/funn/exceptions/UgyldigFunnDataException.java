package no.khalidadan.funn.exceptions;

/**
 * Unchecked on purpose, unlike the other two exceptions in this package.
 * This one is thrown from inside model constructors when encapsulation's
 * invariants are violated (a negative weight, an estimated year in the
 * future, a blank name). That is a programming/data error, not something a
 * caller is expected to catch and recover from mid-flow - it should fail
 * fast at the point the bad object would otherwise have been created.
 */
public class UgyldigFunnDataException extends IllegalArgumentException {

    public UgyldigFunnDataException(String message) {
        super(message);
    }
}
