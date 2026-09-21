package no.khalidadan.funn.db;

import no.khalidadan.funn.exceptions.DatabaseOperationException;
import no.khalidadan.funn.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The one class in the project that speaks JDBC to store and retrieve
 * finds, persons and museums. Every public method here either takes model
 * objects (Person, Museum, Funngjenstand) or hands them back out - callers
 * never see a ResultSet or a SQLException, only DatabaseOperationException,
 * so a caller such as the Part 2 menu can report a database failure to the
 * user without importing java.sql itself.
 *
 * Every method uses try-with-resources for its Connection/PreparedStatement/
 * ResultSet, so a failure partway through still closes everything that was
 * opened - nothing is left to be cleaned up by a finally block by hand.
 */
public class FunnRepository {

    private final DatabaseManager databaseManager;

    public FunnRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    // ---------- lagring (Part 1: import) ----------

    public void lagrePerson(Person person) throws DatabaseOperationException {
        String sql = "INSERT INTO person (id, navn, telefon, epost) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseManager.nyTilkobling();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, person.getId());
            statement.setString(2, person.getNavn());
            statement.setString(3, person.getTelefon());
            statement.setString(4, person.getEpost());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Kunne ikke lagre person " + person.getId(), e);
        }
    }

    public void lagreMuseum(Museum museum) throws DatabaseOperationException {
        String sql = "INSERT INTO museum (id, navn, sted) VALUES (?, ?, ?)";
        try (Connection connection = databaseManager.nyTilkobling();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, museum.getId());
            statement.setString(2, museum.getNavn());
            statement.setString(3, museum.getSted());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Kunne ikke lagre museum " + museum.getId(), e);
        }
    }

    public void lagreFunngjenstand(Funngjenstand funn) throws DatabaseOperationException {
        String sql = "INSERT INTO funngjenstand "
                + "(id, breddegrad, lengdegrad, person_id, dato_funnet, antatt_aar, museum_id, type) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseManager.nyTilkobling()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, funn.getId());
                statement.setDouble(2, funn.getBreddegrad());
                statement.setDouble(3, funn.getLengdegrad());
                statement.setInt(4, funn.getPersonId());
                statement.setString(5, funn.getDatoFunnet().toString());
                statement.setInt(6, funn.getAntattAar());
                if (funn.harMuseum()) {
                    statement.setInt(7, funn.getMuseumId());
                } else {
                    statement.setNull(7, Types.INTEGER);
                }
                statement.setString(8, funn.getTypeNavn());
                statement.executeUpdate();
            }
            lagreTypespesifikkeFelter(connection, funn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Kunne ikke lagre funngjenstand " + funn.getId(), e);
        }
    }

    private void lagreTypespesifikkeFelter(Connection connection, Funngjenstand funn) throws SQLException {
        if (funn instanceof Mynt mynt) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO mynt (funn_id, diameter_mm, metall) VALUES (?, ?, ?)")) {
                statement.setInt(1, mynt.getId());
                statement.setDouble(2, mynt.getDiameterMm());
                statement.setString(3, mynt.getMetall());
                statement.executeUpdate();
            }
        } else if (funn instanceof Vapen vapen) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO vapen (funn_id, vapen_type, materiale, vekt_gram) VALUES (?, ?, ?, ?)")) {
                statement.setInt(1, vapen.getId());
                statement.setString(2, vapen.getVapenType());
                statement.setString(3, vapen.getMateriale());
                statement.setDouble(4, vapen.getVektGram());
                statement.executeUpdate();
            }
        } else if (funn instanceof Smykke smykke) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO smykke (funn_id, smykke_type, verdiestimat_nok, bildefil) VALUES (?, ?, ?, ?)")) {
                statement.setInt(1, smykke.getId());
                statement.setString(2, smykke.getSmykkeType());
                statement.setDouble(3, smykke.getVerdiestimatNok());
                statement.setString(4, smykke.getBildefil());
                statement.executeUpdate();
            }
        } else {
            // Guards against silently losing data if a fourth subclass of
            // Funngjenstand is ever added without teaching this method about it.
            throw new IllegalStateException("Ukjent Funngjenstand-underklasse: " + funn.getClass());
        }
    }

    // ---------- uthenting (Part 2: menu) ----------

    /** All finds, of every type, sorted by id. */
    public List<Funngjenstand> hentAlleFunn() throws DatabaseOperationException {
        return hentFunnMedFilter(null);
    }

    /** Finds whose antatt_aar is strictly before the given year. */
    public List<Funngjenstand> hentFunnEldreEnn(int aar) throws DatabaseOperationException {
        return hentFunnMedFilter(aar);
    }

    public int tellFunn() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM funngjenstand";
        try (Connection connection = databaseManager.nyTilkobling();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Kunne ikke telle funngjenstander", e);
        }
    }

    private List<Funngjenstand> hentFunnMedFilter(Integer eldreEnnAar) throws DatabaseOperationException {
        try (Connection connection = databaseManager.nyTilkobling()) {
            List<Funngjenstand> alle = new ArrayList<>();
            alle.addAll(hentMynter(connection, eldreEnnAar));
            alle.addAll(hentVapen(connection, eldreEnnAar));
            alle.addAll(hentSmykker(connection, eldreEnnAar));
            alle.sort(Comparator.comparingInt(Funngjenstand::getId));
            return alle;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Kunne ikke hente funngjenstander", e);
        }
    }

    private List<Mynt> hentMynter(Connection connection, Integer eldreEnnAar) throws SQLException {
        String sql = "SELECT f.id, f.breddegrad, f.lengdegrad, f.person_id, f.dato_funnet, f.antatt_aar, f.museum_id, "
                + "m.diameter_mm, m.metall "
                + "FROM funngjenstand f JOIN mynt m ON m.funn_id = f.id"
                + (eldreEnnAar != null ? " WHERE f.antatt_aar < ?" : "");
        List<Mynt> resultat = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (eldreEnnAar != null) {
                statement.setInt(1, eldreEnnAar);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    resultat.add(new Mynt(
                            rs.getInt("id"), rs.getDouble("breddegrad"), rs.getDouble("lengdegrad"),
                            rs.getInt("person_id"), LocalDate.parse(rs.getString("dato_funnet")),
                            rs.getInt("antatt_aar"), museumIdEllerNull(rs),
                            rs.getDouble("diameter_mm"), rs.getString("metall")));
                }
            }
        }
        return resultat;
    }

    private List<Vapen> hentVapen(Connection connection, Integer eldreEnnAar) throws SQLException {
        String sql = "SELECT f.id, f.breddegrad, f.lengdegrad, f.person_id, f.dato_funnet, f.antatt_aar, f.museum_id, "
                + "v.vapen_type, v.materiale, v.vekt_gram "
                + "FROM funngjenstand f JOIN vapen v ON v.funn_id = f.id"
                + (eldreEnnAar != null ? " WHERE f.antatt_aar < ?" : "");
        List<Vapen> resultat = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (eldreEnnAar != null) {
                statement.setInt(1, eldreEnnAar);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    resultat.add(new Vapen(
                            rs.getInt("id"), rs.getDouble("breddegrad"), rs.getDouble("lengdegrad"),
                            rs.getInt("person_id"), LocalDate.parse(rs.getString("dato_funnet")),
                            rs.getInt("antatt_aar"), museumIdEllerNull(rs),
                            rs.getString("vapen_type"), rs.getString("materiale"), rs.getDouble("vekt_gram")));
                }
            }
        }
        return resultat;
    }

    private List<Smykke> hentSmykker(Connection connection, Integer eldreEnnAar) throws SQLException {
        String sql = "SELECT f.id, f.breddegrad, f.lengdegrad, f.person_id, f.dato_funnet, f.antatt_aar, f.museum_id, "
                + "s.smykke_type, s.verdiestimat_nok, s.bildefil "
                + "FROM funngjenstand f JOIN smykke s ON s.funn_id = f.id"
                + (eldreEnnAar != null ? " WHERE f.antatt_aar < ?" : "");
        List<Smykke> resultat = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (eldreEnnAar != null) {
                statement.setInt(1, eldreEnnAar);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    resultat.add(new Smykke(
                            rs.getInt("id"), rs.getDouble("breddegrad"), rs.getDouble("lengdegrad"),
                            rs.getInt("person_id"), LocalDate.parse(rs.getString("dato_funnet")),
                            rs.getInt("antatt_aar"), museumIdEllerNull(rs),
                            rs.getString("smykke_type"), rs.getDouble("verdiestimat_nok"), rs.getString("bildefil")));
                }
            }
        }
        return resultat;
    }

    private static Integer museumIdEllerNull(ResultSet rs) throws SQLException {
        int verdi = rs.getInt("museum_id");
        return rs.wasNull() ? null : verdi;
    }
}
