package Model;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import java.sql.*;
import java.util.ArrayList;

@WebServlet(name = "UtenteDAO", value = "/UtenteDAO")
public class UtenteDAO extends HttpServlet {

    public static Utente doLogin(String email, String password) {
        Utente utente = new Utente();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT email, passwordEmail, nome, cognome, dataDiNascita, numeroTelefono, "
                             + "codiceFiscale, via, citta, cap, provincia, nazione, amministratore "
                             + "FROM Cliente WHERE email = ? AND passwordEmail = SHA1(?)")) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    utente.setEmail(rs.getString(1));
                    utente.setPassword(rs.getString(2));
                    utente.setNome(rs.getString(3));
                    utente.setCognome(rs.getString(4));
                    utente.setDataDiNascita(rs.getString(5));
                    utente.setNumeroTelefono(rs.getString(6));
                    utente.setCodiceFiscale(rs.getString(7));
                    utente.setVia(rs.getString(8));
                    utente.setCitta(rs.getString(9));
                    utente.setCap(rs.getString(10));
                    utente.setProvincia(rs.getString(11));
                    utente.setNazione(rs.getString(12));
                    utente.setAmministratore(rs.getBoolean(13));
                    return utente;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doLogin", e);
        }
    }

    public static void doRegistrazione(Utente utente) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Cliente (email,passwordEmail,nome,cognome,dataDiNascita,numeroTelefono,codiceFiscale,via,citta,cap,provincia,nazione,amministratore) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, utente.getEmail());
            ps.setString(2, utente.getPassword());
            ps.setString(3, utente.getNome());
            ps.setString(4, utente.getCognome());
            ps.setString(5, utente.getDataDiNascita());
            ps.setString(6, utente.getNumeroTelefono());
            ps.setString(7, utente.getCodiceFiscale());
            ps.setString(8, utente.getVia());
            ps.setString(9, utente.getCitta());
            ps.setString(10, utente.getCap());
            ps.setString(11, utente.getProvincia());
            ps.setString(12, utente.getNazione());
            ps.setBoolean(13, false);

            if (ps.executeUpdate() != 1) {
                throw new DataAccessException("Errore nel definire l'utente");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRegistrazione", e);
        }
    }

    public static boolean controlloEmail(String email) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT email FROM Cliente WHERE email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore in controlloEmail", e);
        }
        return false;
    }

    public static ArrayList<Utente> doRetriveUtente() {
        ArrayList<Utente> u = new ArrayList<Utente>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT email, passwordEmail, nome, cognome, dataDiNascita, numeroTelefono, "
                             + "codiceFiscale, via, citta, cap, provincia, nazione, amministratore "
                             + "FROM Cliente");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Utente utente = new Utente();
                utente.setEmail(rs.getString(1));
                utente.setPassword(rs.getString(2));
                utente.setNome(rs.getString(3));
                utente.setCognome(rs.getString(4));
                utente.setDataDiNascita(rs.getString(5));
                utente.setNumeroTelefono(rs.getString(6));
                utente.setCodiceFiscale(rs.getString(7));
                utente.setVia(rs.getString(8));
                utente.setCitta(rs.getString(9));
                utente.setCap(rs.getString(10));
                utente.setProvincia(rs.getString(11));
                utente.setNazione(rs.getString(12));
                utente.setAmministratore(rs.getBoolean(13));
                u.add(utente);
            }
            return u;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveUtente", e);
        }
    }

    public static void rendiAmministratore(String email) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Cliente SET amministratore=? WHERE email = ? ",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setBoolean(1, true);
            ps.setString(2, email);
            if (ps.executeUpdate() != 1) {
                throw new DataAccessException("UPDATE error.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore in rendiAmministratore", e);
        }
    }

    public static void rimuoviAmministratore(String email) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Cliente SET amministratore=? WHERE email = ? ",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setBoolean(1, false);
            ps.setString(2, email);
            if (ps.executeUpdate() != 1) {
                throw new DataAccessException("UPDATE error.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore in rimuoviAmministratore", e);
        }
    }
}