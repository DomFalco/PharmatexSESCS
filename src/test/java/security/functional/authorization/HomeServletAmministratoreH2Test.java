package security.functional.authorization;

import Controller.HomeServletAmministratore;
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

import java.util.ArrayList;

import static org.mockito.Mockito.*;

/**
 * Test funzionali di HomeServletAmministratore con H2 in-memory.
 * Verifica:
 * - Autorizzazione: sessione mancante / admin mancante / non-admin → 403
 * - Routing per parametro "valore":
 *   null     → tutti i prodotti
 *   quantita → prodotti esauriti
 *   ordine   → riepilogo acquisti
 *   clienti  → tutti gli utenti
 *   aggiungi → form aggiunta prodotto
 * - FINDING: valore sconosciuto → la Servlet non fa nulla (silent drop)
 */
@DisplayName("Functional - HomeServletAmministratore (Authorization)")
class HomeServletAmministratoreH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(HomeServletAmministratore servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    private Utente admin() {
        Utente u = new Utente();
        u.setEmail("admin@test.com");
        u.setAmministratore(true);
        return u;
    }

    private Utente utenteNormale() {
        Utente u = new Utente();
        u.setEmail("mario@test.com");
        u.setAmministratore(false);
        return u;
    }

    // ==================================================================
    // Autorizzazione (403)
    // ==================================================================

    @Test
    @DisplayName("Sessione mancante → 403")
    void testSessioneNullRiceve403() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        invokeService(new HomeServletAmministratore());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Admin null in sessione → 403")
    void testAdminNullRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(null);

        invokeService(new HomeServletAmministratore());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente non amministratore → 403")
    void testUtenteNonAdminRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(utenteNormale());

        invokeService(new HomeServletAmministratore());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    // ==================================================================
    // Routing: valore = null → tutti i prodotti
    // ==================================================================

    @Test
    @DisplayName("valore=null → carica tutti i prodotti e forwarda a VediTuttiIProdotti.jsp")
    void testValoreNullCaricaTuttiProdotti() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Letto", "Dublino", 300.0, 3));
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("valore")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp"))
                .thenReturn(dispatcher);

        invokeService(new HomeServletAmministratore());

        verify(request).setAttribute(eq("tuttiProdotti"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 2;
        }));
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Routing: valore = "quantita" → prodotti esauriti
    // ==================================================================

    @Test
    @DisplayName("valore=quantita → carica prodotti esauriti e forwarda a QuantitaEsaurita.jsp")
    void testValoreQuantitaCaricaEsauriti() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Letto", "Dublino", 300.0, 0));  // esaurito
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("valore")).thenReturn("quantita");
        when(request.getRequestDispatcher("/WEB-INF/amministratore/QuantitaEsaurita.jsp"))
                .thenReturn(dispatcher);

        invokeService(new HomeServletAmministratore());

        verify(request).setAttribute(eq("prodottiEsauriti"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 1 && list.get(0).getIdProdotto().equals("P0002");
        }));
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Routing: valore = "ordine" → riepilogo acquisti
    // ==================================================================

    @Test
    @DisplayName("valore=ordine → carica riepilogo acquisti e forwarda a RiepilogoOrdini.jsp")
    void testValoreOrdineCaricaAcquisti() throws Exception {
        executeSql(insertCliente("mario@test.com"));
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertAcquisto("mario@test.com", "P0001", 2));

        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("valore")).thenReturn("ordine");
        when(request.getRequestDispatcher("/WEB-INF/amministratore/RiepilogoOrdini.jsp"))
                .thenReturn(dispatcher);

        invokeService(new HomeServletAmministratore());

        verify(request).setAttribute(eq("riepilogoProdotti"), any(ArrayList.class));
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Routing: valore = "clienti" → tutti gli utenti
    // ==================================================================

    @Test
    @DisplayName("valore=clienti → carica tutti gli utenti e forwarda a VisualizzaUtenti.jsp")
    void testValoreClientiCaricaUtenti() throws Exception {
        executeSql(insertClienteCompleto("mario@test.com"));
        executeSql(insertClienteCompleto("luigi@test.com"));
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("valore")).thenReturn("clienti");
        when(request.getRequestDispatcher("/WEB-INF/amministratore/VisualizzaUtenti.jsp"))
                .thenReturn(dispatcher);

        invokeService(new HomeServletAmministratore());

        verify(request).setAttribute(eq("riepilogoUtente"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Utente> list = (ArrayList<Utente>) o;
            return list.size() == 2;
        }));
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Routing: valore = "aggiungi" → form aggiunta prodotto
    // ==================================================================

    @Test
    @DisplayName("valore=aggiungi → forward a AggiungiNuovoProdotto.jsp (nessun dato caricato)")
    void testValoreAggiungiCaricaForm() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("valore")).thenReturn("aggiungi");
        when(request.getRequestDispatcher("/WEB-INF/amministratore/AggiungiNuovoProdotto.jsp"))
                .thenReturn(dispatcher);

        invokeService(new HomeServletAmministratore());

        verify(dispatcher).forward(request, response);
        // Nessun attributo caricato per il form
        verify(request, never()).setAttribute(eq("tuttiProdotti"), any());
        verify(request, never()).setAttribute(eq("prodottiEsauriti"), any());
    }

    // ==================================================================
    // FINDING: valore sconosciuto → silent drop
    // ==================================================================

    /**
     * FINDING (documentato): valore sconosciuto causa silent drop.
     * Se il parametro "valore" ha un valore non previsto (es. "xyz"), la
     * Servlet non fa NULLA: nessun forward, nessun errore, nessun attributo.
     * La richiesta termina senza produrre output.
     * Conseguenza: un admin che raggiunge una URL malformata (?valore=xyz)
     * riceve una risposta vuota senza spiegazione.
     * Fix suggerito: aggiungere un else finale che restituisce 400 o
     * reindirizza alla home admin.
     */
    @Test
    @DisplayName("FINDING: valore sconosciuto → la Servlet non fa nulla (silent drop)")
    void testValoreSconosciuto_SilentDrop() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("valore")).thenReturn("xyz");

        invokeService(new HomeServletAmministratore());

        // Nessun forward, nessun errore
        verify(response, never()).sendError(anyInt(), anyString());
        verify(dispatcher, never()).forward(any(), any());
    }

    // ==================================================================
    // Helper: INSERT
    // ==================================================================

    private String insertProdotto(String id, String categoria, String nome,
                                  double prezzo, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita) VALUES ('"
                + id + "', '" + categoria + "', '" + nome + "', "
                + "'Descrizione di test', " + prezzo + ", " + quantita + ")";
    }

    private String insertCliente(String email) {
        return "INSERT INTO Cliente (email, passwordEmail, nome, cognome, "
                + "dataDiNascita, numeroTelefono, codiceFiscale, via, citta, cap, "
                + "provincia, nazione, amministratore) VALUES ("
                + "'" + email + "', 'hash', 'Mario', 'Rossi', "
                + "'1990-01-01', '1234567890', 'ABCDE25F67G160H', "
                + "'Via Test 1', 'Napoli', '80100', 'NA', 'Italia', false)";
    }

    private String insertClienteCompleto(String email) {
        String hashed = TestFunctions.sha1("password");
        return "INSERT INTO Cliente (email, passwordEmail, nome, cognome, "
                + "dataDiNascita, numeroTelefono, codiceFiscale, via, citta, cap, "
                + "provincia, nazione, amministratore) VALUES ("
                + "'" + email + "', '" + hashed + "', 'Mario', 'Rossi', "
                + "'1990-01-01', '1234567890', 'ABCDE25F67G160H', "
                + "'Via Test 1', 'Napoli', '80100', 'NA', 'Italia', false)";
    }

    private String insertAcquisto(String email, String idProdotto, int quantita) {
        return "INSERT INTO Acquistare (emailCliente, idProdotto, quantitaAcquistata) "
                + "VALUES ('" + email + "', '" + idProdotto + "', " + quantita + ")";
    }
}