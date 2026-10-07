package security.daofunctional;

import Model.Carta;
import Model.CartaDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali del CartaDAO su H2 in-memory.
 * Il CartaDAO ha un solo metodo pubblico: aggiuntaCredenzialiPagamento(Carta, email).
 * La logica interna e':
 *   1. SELECT numeroCarta FROM CartaDiCredito (carica TUTTE le carte in memoria)
 *   2. Se il numero carta non e' presente, esegue INSERT
 *   3. Altrimenti non fa nulla (evita duplicati)
 */
class CartaDAOH2Test extends BaseH2Test {

    // ==================================================================
    // aggiuntaCredenzialiPagamento - casi funzionali
    // ==================================================================

    @Test
    @DisplayName("aggiuntaCredenzialiPagamento inserisce una nuova carta nel DB")
    void testAggiuntaNuovaCarta() throws Exception {
        executeSql(insertCliente("mario@test.com"));

        Carta carta = new Carta(null);
        carta.setNumeroCarta("1234567890123456");
        carta.setNomeIntestario("Mario Rossi");
        carta.setDataScadenza("12/2027");
        carta.setCVV("123");

        CartaDAO.aggiuntaCredenzialiPagamento(carta, "mario@test.com");

        int count = countCarte();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("aggiuntaCredenzialiPagamento NON duplica una carta gia' esistente")
    void testAggiuntaCartaDuplicata() throws Exception {
        executeSql(insertCliente("mario@test.com"));

        Carta carta = new Carta(null);
        carta.setNumeroCarta("1234567890123456");
        carta.setNomeIntestario("Mario Rossi");
        carta.setDataScadenza("12/2027");
        carta.setCVV("123");

        // Chiamiamo due volte lo stesso metodo con la stessa carta
        CartaDAO.aggiuntaCredenzialiPagamento(carta, "mario@test.com");
        CartaDAO.aggiuntaCredenzialiPagamento(carta, "mario@test.com");

        // Deve esserci solo una riga nel DB (la seconda chiamata salta l'INSERT)
        int count = countCarte();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("aggiuntaCredenzialiPagamento con due carte diverse inserisce entrambe")
    void testAggiuntaDueCarteDiverse() throws Exception {
        executeSql(insertCliente("mario@test.com"));

        Carta carta1 = new Carta(null);
        carta1.setNumeroCarta("1111111111111111");
        carta1.setNomeIntestario("Mario Rossi");
        carta1.setDataScadenza("12/2027");
        carta1.setCVV("111");

        Carta carta2 = new Carta(null);
        carta2.setNumeroCarta("2222222222222222");
        carta2.setNomeIntestario("Mario Rossi");
        carta2.setDataScadenza("06/2028");
        carta2.setCVV("222");

        CartaDAO.aggiuntaCredenzialiPagamento(carta1, "mario@test.com");
        CartaDAO.aggiuntaCredenzialiPagamento(carta2, "mario@test.com");

        assertThat(countCarte()).isEqualTo(2);
    }

    @Test
    @DisplayName("aggiuntaCredenzialiPagamento salva il numero carta in chiaro (FINDING PCI-DSS)")
    void testNumeroCartaInChiaro() throws Exception {
        executeSql(insertCliente("mario@test.com"));

        Carta carta = new Carta(null);
        carta.setNumeroCarta("4111111111111111");
        carta.setNomeIntestario("Mario Rossi");
        carta.setDataScadenza("12/2027");
        carta.setCVV("123");

        CartaDAO.aggiuntaCredenzialiPagamento(carta, "mario@test.com");

        // FINDING (documentato): il numero carta e il CVV sono salvati in chiaro.
        // Il CVV, in particolare, non dovrebbe MAI essere persistito (PCI-DSS 3.2).
        String numeroSalvato = queryString(
                "SELECT numeroCarta FROM CartaDiCredito WHERE numeroCarta = '4111111111111111'"
        );
        assertThat(numeroSalvato).isEqualTo("4111111111111111");

        String cvvSalvato = queryString(
                "SELECT CVV FROM CartaDiCredito WHERE numeroCarta = '4111111111111111'"
        );
        assertThat(cvvSalvato).isEqualTo("123");
    }

    // ==================================================================
    // Helper: contatori e query di supporto
    // ==================================================================

    private int countCarte() throws Exception {
        try (java.sql.Connection conn = Model.ConPool.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM CartaDiCredito");
             java.sql.ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private String queryString(String sql) throws Exception {
        try (java.sql.Connection conn = Model.ConPool.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
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
}