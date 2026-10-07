package security.daofunctional;

import Model.AcquistoProdotti;
import Model.AcquistoProdottiDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali dello AcquistoProdottiDAO su H2 in-memory.
 * Il DAO gestisce la tabella "Acquistare" (relazione N:N tra Cliente e Prodotto)
 * e fornisce query di join per recuperare gli acquisti con i dati del cliente.
 */
class AcquistoProdottiDAOH2Test extends BaseH2Test {

    // ==================================================================
    // acquistaProdotto (INSERT)
    // ==================================================================

    @Test
    @DisplayName("acquistaProdotto inserisce un nuovo acquisto nel DB")
    void testAcquistaProdotto() throws Exception {
        executeSql(insertCliente("mario@test.com"));
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

        AcquistoProdottiDAO.acquistaProdotto("mario@test.com", "P0001", 2);

        int count = countAcquisti();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("acquistaProdotto con email inesistente viola la FK e lancia RuntimeException")
    void testAcquistaProdottoEmailInesistente() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

        // Il cliente non esiste -> la FK emailCliente fallisce -> RuntimeException
        try {
            AcquistoProdottiDAO.acquistaProdotto("nessuno@test.com", "P0001", 1);
            org.junit.jupiter.api.Assertions.fail("Attesa RuntimeException per FK violata");
        } catch (RuntimeException expected) {
            // Comportamento atteso: eccezione propagata
        }
    }

    // ==================================================================
    // doRetriveAcquistoUtente (SELECT con WHERE)
    // ==================================================================

    @Test
    @DisplayName("doRetriveAcquistoUtente restituisce solo gli acquisti dell'utente specificato")
    void testDoRetriveAcquistoUtente() throws Exception {
        // Due clienti, due prodotti, acquisti diversi
        executeSql(insertCliente("mario@test.com"));
        executeSql(insertCliente("luigi@test.com"));
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Letto",     "Dublino", 300.0, 3));

        AcquistoProdottiDAO.acquistaProdotto("mario@test.com", "P0001", 1);
        AcquistoProdottiDAO.acquistaProdotto("mario@test.com", "P0002", 2);
        AcquistoProdottiDAO.acquistaProdotto("luigi@test.com", "P0001", 5);

        ArrayList<AcquistoProdotti> result = AcquistoProdottiDAO.doRetriveAcquistoUtente("mario@test.com");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(AcquistoProdotti::getIdProdotto)
                .containsExactlyInAnyOrder("P0001", "P0002");
    }

    @Test
    @DisplayName("doRetriveAcquistoUtente con utente senza acquisti restituisce lista vuota")
    void testDoRetriveAcquistoUtenteVuoto() throws Exception {
        executeSql(insertCliente("mario@test.com"));

        ArrayList<AcquistoProdotti> result = AcquistoProdottiDAO.doRetriveAcquistoUtente("mario@test.com");

        assertThat(result).isEmpty();
    }

    // ==================================================================
    // doRetriveAcquisto (SELECT senza WHERE)
    // ==================================================================

    @Test
    @DisplayName("doRetriveAcquisto restituisce tutti gli acquisti (di tutti gli utenti)")
    void testDoRetriveAcquisto() throws Exception {
        executeSql(insertCliente("mario@test.com"));
        executeSql(insertCliente("luigi@test.com"));
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Letto",     "Dublino", 300.0, 3));

        AcquistoProdottiDAO.acquistaProdotto("mario@test.com", "P0001", 1);
        AcquistoProdottiDAO.acquistaProdotto("luigi@test.com", "P0002", 2);

        ArrayList<AcquistoProdotti> result = AcquistoProdottiDAO.doRetriveAcquisto();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(AcquistoProdotti::getIdProdotto)
                .containsExactlyInAnyOrder("P0001", "P0002");
    }

    @Test
    @DisplayName("doRetriveAcquisto su DB vuoto restituisce lista vuota")
    void testDoRetriveAcquistoVuoto() {
        ArrayList<AcquistoProdotti> result = AcquistoProdottiDAO.doRetriveAcquisto();
        assertThat(result).isEmpty();
    }

    // ==================================================================
    // Test di integrità dei dati restituiti
    // ==================================================================

    @Test
    @DisplayName("doRetriveAcquistoUtente restituisce i dati del cliente corretti (join)")
    void testJoinDatiCliente() throws Exception {
        executeSql(insertCliente("mario@test.com"));
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));

        AcquistoProdottiDAO.acquistaProdotto("mario@test.com", "P0001", 3);

        ArrayList<AcquistoProdotti> result = AcquistoProdottiDAO.doRetriveAcquistoUtente("mario@test.com");

        assertThat(result).hasSize(1);
        AcquistoProdotti ap = result.get(0);
        assertThat(ap.getIdProdotto()).isEqualTo("P0001");
        assertThat(ap.getNomeProd()).isEqualTo("Nuvola");
        assertThat(ap.getPrezzo()).isEqualTo(400.0);
        assertThat(ap.getQuantitaAcquistata()).isEqualTo(3);
        assertThat(ap.getNome()).isEqualTo("Mario");
        assertThat(ap.getCognome()).isEqualTo("Rossi");
        assertThat(ap.getCitta()).isEqualTo("Napoli");
    }

    // ==================================================================
    // Helper
    // ==================================================================

    private int countAcquisti() throws Exception {
        try (Connection conn = Model.ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM Acquistare");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private String insertCliente(String email) {
        return "INSERT INTO Cliente (email, passwordEmail, nome, cognome, "
                + "dataDiNascita, numeroTelefono, codiceFiscale, via, citta, cap, "
                + "provincia, nazione, amministratore) VALUES ("
                + "'" + email + "', 'hash', 'Mario', 'Rossi', "
                + "'1990-01-01', '1234567890', 'ABCDE25F67G160H', "
                + "'Via Test 1', 'Napoli', '80100', 'NA', 'Italia', false)";
    }

    private String insertProdotto(String id, String categoria, String nome,
                                  double prezzo, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita) VALUES ('"
                + id + "', '" + categoria + "', '" + nome + "', "
                + "'Descrizione di test', " + prezzo + ", " + quantita + ")";
    }
}