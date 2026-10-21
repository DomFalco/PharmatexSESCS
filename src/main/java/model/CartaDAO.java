package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartaDAO {

    // Costruttore privato per nascondere quello pubblico implicito
    private CartaDAO() {
    }

    public static void aggiuntaCredenzialiPagamento(Carta p, String email) {
        List<String> cartaCredito = new ArrayList<>();

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
        for (String s : cartaCredito) {
            if (s.equals(p.getNumeroCarta())) {
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
                ps1.setString(2, p.getNomeIntestatario());
                ps1.setString(3, p.getDataScadenza());
                ps1.setString(4, p.getCvv());
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