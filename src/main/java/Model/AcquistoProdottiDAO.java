package Model;

import java.sql.*;
import java.util.ArrayList;

public class AcquistoProdottiDAO {

    public static void acquistaProdotto(String email, String idProdotto, int quantita) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Acquistare (emailCliente,idProdotto,quantitaAcquistata) VALUES (?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, email);
            ps.setString(2, idProdotto);
            ps.setInt(3, quantita);

            if (ps.executeUpdate() != 1) {
                throw new DataAccessException("Errore nel definire i prodotti");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore in acquistaProdotto", e);
        }
    }

    public static ArrayList<AcquistoProdotti> doRetriveAcquisto() {
        ArrayList<AcquistoProdotti> prodottiOrdinati = new ArrayList<AcquistoProdotti>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT P.idProdotto,P.nomeProd,P.prezzo,A.quantitaAcquistata,C.Nome,C.Cognome,C.via,C.citta,C.cap,C.provincia FROM (Prodotto as P join Acquistare as A on P.idProdotto=A.idProdotto)join Cliente as C on C.email=A.emailCliente ");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AcquistoProdotti ap = new AcquistoProdotti(new Utente(), new Prodotto());
                ap.setIdProdotto(rs.getString(1));
                ap.setNomeProd(rs.getString(2));
                ap.setPrezzo(rs.getDouble(3));
                ap.setQuantitaAcquistata(rs.getInt(4));
                ap.setNome(rs.getString(5));
                ap.setCognome(rs.getString(6));
                ap.setVia(rs.getString(7));
                ap.setCitta(rs.getString(8));
                ap.setCap(rs.getString(9));
                ap.setProvincia(rs.getString(10));
                prodottiOrdinati.add(ap);
            }
            return prodottiOrdinati;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveAcquisto", e);
        }
    }

    public static ArrayList<AcquistoProdotti> doRetriveAcquistoUtente(String email) {
        ArrayList<AcquistoProdotti> prodottiOrdinati = new ArrayList<AcquistoProdotti>();
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT P.idProdotto,P.nomeProd,P.prezzo,A.quantitaAcquistata,C.Nome,C.Cognome,C.via,C.citta,C.cap,C.provincia FROM (Prodotto as P join Acquistare as A on P.idProdotto=A.idProdotto)join Cliente as C on C.email=A.emailCliente WHERE C.email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AcquistoProdotti ap = new AcquistoProdotti(new Utente(), new Prodotto());
                    ap.setIdProdotto(rs.getString(1));
                    ap.setNomeProd(rs.getString(2));
                    ap.setPrezzo(rs.getDouble(3));
                    ap.setQuantitaAcquistata(rs.getInt(4));
                    ap.setNome(rs.getString(5));
                    ap.setCognome(rs.getString(6));
                    ap.setVia(rs.getString(7));
                    ap.setCitta(rs.getString(8));
                    ap.setCap(rs.getString(9));
                    ap.setProvincia(rs.getString(10));
                    prodottiOrdinati.add(ap);
                }
            }
            return prodottiOrdinati;
        } catch (SQLException e) {
            throw new DataAccessException("Errore in doRetriveAcquistoUtente", e);
        }
    }
}