package no.khalidadan.funn.exceptions;

/**
 * Wraps java.sql.SQLException at the repository boundary so the rest of the
 * application (the menu in Part 2, for example) never needs to import java.sql
 * or know that the data happens to live in a JDBC database. If the storage
 * layer changed to something else entirely, only the db package would need
 * to change, not every caller that reports on finds.
 */
public class DatabaseOperationException extends Exception {

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
