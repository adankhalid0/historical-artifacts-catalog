package no.khalidadan.funn.db;

import no.khalidadan.funn.exceptions.DatabaseOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the JDBC connection string and schema setup. Everything in this
 * package talks to the database exclusively through java.sql; nothing
 * outside the db package imports java.sql at all, so the rest of the
 * program (the model classes, the file parser, the menu) has no idea the
 * data happens to live in SQLite specifically.
 */
public class DatabaseManager {

    private final String jdbcUrl;

    public DatabaseManager(Path databaseFile) {
        this.jdbcUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();
    }

    public Connection nyTilkobling() throws DatabaseOperationException {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Kunne ikke koble til databasen: " + jdbcUrl, e);
        }
    }

    /** (Re)creates every table from funn.sql on the classpath. Safe to call repeatedly: the schema drops and recreates each table. */
    public void initialiserSkjema() throws DatabaseOperationException {
        String skjemaSql = lesRessursfil("/funn.sql");
        try (Connection connection = nyTilkobling();
             Statement statement = connection.createStatement()) {
            for (String enkeltSetning : skjemaSql.split(";")) {
                String trimmet = enkeltSetning.strip();
                if (!trimmet.isEmpty()) {
                    statement.execute(trimmet);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Kunne ikke initialisere databaseskjema", e);
        }
    }

    private String lesRessursfil(String klassestiRessurs) throws DatabaseOperationException {
        try (InputStream input = getClass().getResourceAsStream(klassestiRessurs)) {
            if (input == null) {
                throw new DatabaseOperationException("Fant ikke ressursfilen " + klassestiRessurs + " på classpath", null);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DatabaseOperationException("Kunne ikke lese ressursfilen " + klassestiRessurs, e);
        }
    }
}
