package security.functional.businesslogic;

import Controller.DatiPagamentoServlet;
import Model.ConPool;
import Model.Prodotto;
import Model.Utente;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;
import security.functional.TestFunctions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di DatiPagamentoServlet con H2 in-memory.
 *
 * Verifica:
 * - Autorizzazione: sessione mancante / utente non autenticato → 403
 * - Validazione: carrello null/vuoto → 400
 * - Acquisto riuscito: INSERT in Acquistare + UPDATE quantita Prodotto
 * - Inserimento carta di credito
 * - Svuotamento carrello dopo il pagamento
 * - FINDING: qList.size() < cart_list.size() → IndexOutOfBoundsException
 */
@DisplayName("Functional - DatiPagamentoServlet (Business Logic)")
class DatiPagamentoServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(DatiPagamentoServlet servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    private Utente utente() {
        Utente u = new Utente();
        u.setEmail("mario@test.com");
        u.setAmministratore(false);
        return u;
    }

    private Prodotto prodotto(String id, int quantita) {
        Prodotto p = new Prodotto();
        p.setIdProdotto(id);
        p.setQuantita(quantita);
        return p;
    }

    private ArrayList<Prodotto> carrello(Prodotto... prodotti) {
        ArrayList<Prodotto> list = new ArrayList<>();
        Collections.addAll(list, prodotti);
        return list;
    }

    private ArrayList<Integer> quantita(Integer... q) {
        ArrayList<Integer> list = new ArrayList<>();
        Collections.addAll(list, q);
        return list;
    }

    private void setDatiCarta() {
        when(request.getParameter("NCarta")).thenReturn("1234567890123456");
        when(request.getParameter("credenziali")).thenReturn("Mario Rossi");
        when(request.getParameter("dataScadenza")).thenReturn("12/2027");
        when(request.getParameter("cvv")).thenReturn("123");
    }

    // ==================================================================
    // Autorizzazione (403)
    // ==================================================================

    @Test
    @DisplayName("Sessione mancante → 403")
    void testSessioneNullRiceve403() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        invokeService(new DatiPagamentoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente null → 403")
    void testUtenteNullRiceve403() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(null);

        invokeService(new DatiPagamentoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN),
                eq("Accesso negato: utente non autenticato."));
    }

    // ==================================================================
    // Validazione carrello (400)
    // ==================================================================

    @Test
    @DisplayName("carrello null → 400 'Carrello vuoto'")
    void testCarrelloNullRiceve400() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(utente());
        when(session.getAttribute("cart-list")).thenReturn(null);
        when(session.getAttribute("quantitaArticoli")).thenReturn(quantita(1));

        invokeService(new DatiPagamentoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), eq("Carrello vuoto."));
    }

    @Test
    @DisplayName("quantitaArticoli null → 400 'Carrello vuoto'")
    void testQuantitaNullRiceve400() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(utente());
        when(session.getAttribute("cart-list")).thenReturn(carrello(prodotto("P0001", 5)));
        when(session.getAttribute("quantitaArticoli")).thenReturn(null);

        invokeService(new DatiPagamentoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), eq("Carrello vuoto."));
    }

    @Test
    @DisplayName("carrello vuoto → 400 'Carrello vuoto'")
    void testCarrelloVuotoRiceve400() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(utente());
        when(session.getAttribute("cart-list")).thenReturn(new ArrayList<Prodotto>());
        when(session.getAttribute("quantitaArticoli")).thenReturn(new ArrayList<Integer>());

        invokeService(new DatiPagamentoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), eq("Carrello vuoto."));
    }

    // ==================================================================
    // Acquisto riuscito
    // ==================================================================

    @Test
    @DisplayName("Acquisto riuscito: INSERT in Acquistare + UPDATE quantita Prodotto + forward HomePage")
    void testAcquistoRiuscito() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 10));
        executeSql(insertCliente("mario@test.com"));
        setDatiCarta();

        when(session.getAttribute("Utente")).thenReturn(utente());
        when(session.getAttribute("cart-list")).thenReturn(carrello(prodotto("P0001", 10)));
        when(session.getAttribute("quantitaArticoli")).thenReturn(quantita(3));
        when(request.getRequestDispatcher("HomePage")).thenReturn(dispatcher);

        invokeService(new DatiPagamentoServlet());

        // Verifica INSERT in Acquistare
        assertThat(countAcquisti("mario@test.com", "P0001")).isEqualTo(1);
        // Verifica UPDATE quantita (10 - 3 = 7)
        assertThat(getQuantitaProdotto("P0001")).isEqualTo(7);
        // Verifica carta inserita
        assertThat(countCarte("1234567890123456")).isEqualTo(1);
        // Verifica forward
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Acquisto con 2 prodotti: entrambi registrati + quantita aggiornate")
    void testAcquistoDueProdotti() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 10));
        executeSql(insertProdotto("P0002", "Dublino", 5));
        executeSql(insertCliente("mario@test.com"));
        setDatiCarta();

        when(session.getAttribute("Utente")).thenReturn(utente());
        when(session.getAttribute("cart-list")).thenReturn(
                carrello(prodotto("P0001", 10), prodotto("P0002", 5)));
        when(session.getAttribute("quantitaArticoli")).thenReturn(quantita(2, 1));
        when(request.getRequestDispatcher("HomePage")).thenReturn(dispatcher);

        invokeService(new DatiPagamentoServlet());

        assertThat(countAcquisti("mario@test.com", "P0001")).isEqualTo(1);
        assertThat(countAcquisti("mario@test.com", "P0002")).isEqualTo(1);
        assertThat(getQuantitaProdotto("P0001")).isEqualTo(8);
        assertThat(getQuantitaProdotto("P0002")).isEqualTo(4);
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Svuotamento carrello
    // ==================================================================

    @Test
    @DisplayName("Dopo l'acquisto il carrello è svuotato in sessione")
    void testCarrelloSvuotatoDopoPagamento() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 10));
        executeSql(insertCliente("mario@test.com"));
        setDatiCarta();

        ArrayList<Prodotto> cartList = carrello(prodotto("P0001", 10));
        ArrayList<Integer> qList = quantita(3);

        when(session.getAttribute("Utente")).thenReturn(utente());
        when(session.getAttribute("cart-list")).thenReturn(cartList);
        when(session.getAttribute("quantitaArticoli")).thenReturn(qList);
        when(request.getRequestDispatcher("HomePage")).thenReturn(dispatcher);

        invokeService(new DatiPagamentoServlet());

        // Il carrello è stato svuotato (le stesse liste sono state clear()-ate)
        assertThat(cartList).isEmpty();
        assertThat(qList).isEmpty();
        // E rimesse in sessione (sempre le stesse reference)
        verify(session, atLeastOnce()).setAttribute(eq("cart-list"), any());
        verify(session, atLeastOnce()).setAttribute(eq("quantitaArticoli"), any());
    }

    // ==================================================================
    // Carta inserita
    // ==================================================================

    @Test
    @DisplayName("Carta di credito inserita con dati corretti")
    void testCartaInseritaCorrettamente() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 10));
        executeSql(insertCliente("mario@test.com"));
        setDatiCarta();

        when(session.getAttribute("Utente")).thenReturn(utente());
        when(session.getAttribute("cart-list")).thenReturn(carrello(prodotto("P0001", 10)));
        when(session.getAttribute("quantitaArticoli")).thenReturn(quantita(1));
        when(request.getRequestDispatcher("HomePage")).thenReturn(dispatcher);

        invokeService(new DatiPagamentoServlet());

        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT numeroCarta, nomeIntestatario, CVV, emailProprietario "
                             + "FROM CartaDiCredito WHERE numeroCarta = ?")) {
            ps.setString(1, "1234567890123456");
            try (ResultSet rs = ps.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("nomeIntestatario")).isEqualTo("Mario Rossi");
                assertThat(rs.getString("CVV")).isEqualTo("123");
                assertThat(rs.getString("emailProprietario")).isEqualTo("mario@test.com");
            }
        }
    }

    // ==================================================================
    // FINDING: qList più corta di cart_list
    // ==================================================================

    /**
     * FINDING (documentato): qList.size() < cart_list.size() → IndexOutOfBoundsException.
     *
     * Il loop scorre cart_list con `for (int i = 0; i < cart_list.size(); i++)`
     * e accede a `qList.get(i)`. Se qList ha meno elementi di cart_list,
     * si ottiene una IndexOutOfBoundsException non gestita.
     *
     * Conseguenza: la Servlet risponde con 500 (Internal Server Error) invece
     * di un 400 con messaggio "Dati carrello incoerenti".
     *
     * Fix suggerito: validare `cart_list.size() == qList.size()` prima del loop.
     */
    @Test
    @DisplayName("FINDING: qList più corta di cart_list → IndexOutOfBoundsException (500)")
    void testListeIncoerenti_BugDocumentato() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 10));
        executeSql(insertProdotto("P0002", "Dublino", 5));
        executeSql(insertCliente("mario@test.com"));
        setDatiCarta();

        when(session.getAttribute("Utente")).thenReturn(utente());
        // 2 prodotti ma solo 1 quantita
        when(session.getAttribute("cart-list")).thenReturn(
                carrello(prodotto("P0001", 10), prodotto("P0002", 5)));
        when(session.getAttribute("quantitaArticoli")).thenReturn(quantita(2));
        when(request.getRequestDispatcher("HomePage")).thenReturn(dispatcher);

        assertThatThrownBy(() -> invokeService(new DatiPagamentoServlet()))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    // ==================================================================
    // Helper
    // ==================================================================

    private int countAcquisti(String email, String idProdotto) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM Acquistare WHERE emailCliente = ? AND idProdotto = ?")) {
            ps.setString(1, email);
            ps.setString(2, idProdotto);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int countCarte(String numero) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM CartaDiCredito WHERE numeroCarta = ?")) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int getQuantitaProdotto(String id) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT quantita FROM Prodotto WHERE idProdotto = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private String insertProdotto(String id, String nome, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita) VALUES ('"
                + id + "', 'Test', '" + nome + "', 'Desc', 100.0, " + quantita + ")";
    }

    private String insertCliente(String email) {
        String hashed = TestFunctions.sha1("password");
        return "INSERT INTO Cliente (email, passwordEmail, nome, cognome, "
                + "dataDiNascita, numeroTelefono, codiceFiscale, via, citta, cap, "
                + "provincia, nazione, amministratore) VALUES ("
                + "'" + email + "', '" + hashed + "', 'Mario', 'Rossi', "
                + "'1990-01-01', '1234567890', 'ABCDE25F67G160H', "
                + "'Via Test 1', 'Napoli', '80100', 'NA', 'Italia', false)";
    }
}