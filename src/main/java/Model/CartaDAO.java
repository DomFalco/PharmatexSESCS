package Model;

import java.sql.*;
import java.util.ArrayList;

public class CartaDAO {

    public static void aggiuntaCredenzialiPagamento(Carta p, String email) {
        ArrayList<String> cartaCredito = new ArrayList<String>();

        // Fase 1: SELECT di tutti i numeri carta già presenti
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT numeroCarta FROM CartaDiCredito");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Carta ap = new Carta(new Utente());
                ap.setNumeroCarta(rs.getString(1));
                cartaCredito.add(ap.getNumeroCarta());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore in aggiuntaCredenzialiPagamento (SELECT)", e);
        }

        // Fase 2: verifica duplicati
        boolean esiste = false;
        for (int i = 0; i < cartaCredito.size(); i++) {
            if (cartaCredito.get(i).equals(p.getNumeroCarta())) {
                esiste = true;
                break;
            }
        }

        // Fase 3: INSERT se non duplicato
        if (!esiste) {
            try (Connection con = ConPool.getConnection();
                 PreparedStatement ps1 = con.prepareStatement(
                         "INSERT INTO CartaDiCredito (numeroCarta,nomeIntestatario,dataScadenza,CVV,emailProprietario) VALUES (?,?,?,?,?)",
                         Statement.RETURN_GENERATED_KEYS)) {
                ps1.setString(1, p.getNumeroCarta());
                ps1.setString(2, p.getNomeIntestario());
                ps1.setString(3, p.getDataScadenza());
                ps1.setString(4, p.getCVV());
                ps1.setString(5, email);
                if (ps1.executeUpdate() != 1) {
                    throw new DataAccessException("Errore nel definire il metodo di pagamento");
                }
            } catch (SQLException e) {
                throw new DataAccessException("Errore in aggiuntaCredenzialiPagamento (INSERT)", e);
            }
        }
    }
}