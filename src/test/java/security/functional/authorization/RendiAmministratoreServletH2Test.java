package security.functional.authorization;

import Controller.RendiAmministratoreServlet;
import Model.ConPool;
import Model.Utente;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di RendiAmministratoreServlet con H2 in-memory.
 *
 * Verifica:
 * - Autorizzazione: sessione mancante / non-admin → 403
 * - Validazione: action null / vuota / non riconosciuta → 400
 * - Promozione (action="amministratore...") → UPDATE DB + forward
 * - Rimozione (action="rimuovipermessi...") → UPDATE DB + forward
 * - FINDING: email inesistente causa RuntimeException (bug)
 */
@DisplayName("Functional - RendiAmministratoreServlet (Authorization)")
class RendiAmministratoreServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(RendiAmministratoreServlet servlet) throws Exception {
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

        invokeService(new RendiAmministratoreServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Admin null → 403")
    void testAdminNullRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(null);

        invokeService(new RendiAmministratoreServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente non-admin → 403")
    void testUtenteNonAdminRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(utenteNormale());

        invokeService(new RendiAmministratoreServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    // ==================================================================
    // Validazione input (400)
    // ==================================================================

    @Test
    @DisplayName("Action null → 400")
    void testActionNullRiceve400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("action")).thenReturn(null);

        invokeService(new RendiAmministratoreServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Action='amministratore' senza email → 400")
    void testActionPromozioneSenzaEmailRiceve400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("action")).thenReturn("amministratore");

        invokeService(new RendiAmministratoreServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST),
                eq("Email utente mancante."));
    }

    @Test
    @DisplayName("Action='rimuovipermessi' senza email → 400")
    void testActionRimozioneSenzaEmailRiceve400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("action")).thenReturn("rimuovipermessi");

        invokeService(new RendiAmministratoreServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST),
                eq("Email utente mancante."));
    }

    @Test
    @DisplayName("Action non riconosciuta → 400")
    void testActionNonRiconosciutaRiceve400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("action")).thenReturn("xyz");

        invokeService(new RendiAmministratoreServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST),
                eq("Azione non riconosciuta."));
    }

    // ==================================================================
    // Promozione admin (successo)
    // ==================================================================

    @Test
    @DisplayName("Action='amministratoremario@test.com' → promuove + forward VisualizzaUtenti.jsp")
    void testPromozioneAdminSuccesso() throws Exception {
        executeSql(insertCliente("mario@test.com", false));
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("action")).thenReturn("amministratoremario@test.com");
        when(request.getRequestDispatcher("/WEB-INF/amministratore/VisualizzaUtenti.jsp"))
                .thenReturn(dispatcher);

        invokeService(new RendiAmministratoreServlet());

        // Verifica DB: mario è ora admin
        assertThat(isAdmin("mario@test.com")).isTrue();
        // Verifica forward + attributo
        verify(request).setAttribute(eq("riepilogoUtente"), any());
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Rimozione admin (successo)
    // ==================================================================

    @Test
    @DisplayName("Action='rimuovipermessimario@test.com' → degrada + forward VisualizzaUtenti.jsp")
    void testRimozioneAdminSuccesso() throws Exception {
        executeSql(insertCliente("mario@test.com", true));  // già admin
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("action")).thenReturn("rimuovipermessimario@test.com");
        when(request.getRequestDispatcher("/WEB-INF/amministratore/VisualizzaUtenti.jsp"))
                .thenReturn(dispatcher);

        invokeService(new RendiAmministratoreServlet());

        // Verifica DB: mario non è più admin
        assertThat(isAdmin("mario@test.com")).isFalse();
        verify(request).setAttribute(eq("riepilogoUtente"), any());
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // FINDING: email inesistente
    // ==================================================================

    /**
     * FINDING (documentato): promozione di email inesistente causa RuntimeException.
     *
     * Se l'email passata non esiste nel DB, UtenteDAO.rendiAmministratore
     * esegue un UPDATE che non matcha nessuna riga. Poiché executeUpdate()
     * ritorna 0 (non 1), lancia RuntimeException("UPDATE error.").
     *
     * La Servlet non gestisce questa eccezione, quindi risponde con 500
     * (Internal Server Error) invece di un 404 più appropriato.
     *
     * Fix suggerito: gestire l'eccezione e restituire SC_NOT_FOUND con un
     * messaggio "Utente non trovato".
     */
    @Test
    @DisplayName("FINDING: promozione email inesistente → RuntimeException (500)")
    void testPromozioneEmailInesistente_BugDocumentato() {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("action")).thenReturn("amministratorenessuno@test.com");

        assertThatThrownBy(() -> invokeService(new RendiAmministratoreServlet()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("UPDATE error");
    }

    // ==================================================================
    // Helper
    // ==================================================================

    private boolean isAdmin(String email) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT amministratore FROM Cliente WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private String insertCliente(String email, boolean admin) {
        return "INSERT INTO Cliente (email, passwordEmail, nome, cognome, "
                + "dataDiNascita, numeroTelefono, codiceFiscale, via, citta, cap, "
                + "provincia, nazione, amministratore) VALUES ("
                + "'" + email + "', 'hash', 'Mario', 'Rossi', "
                + "'1990-01-01', '1234567890', 'ABCDE25F67G160H', "
                + "'Via Test 1', 'Napoli', '80100', 'NA', 'Italia', "
                + Boolean.toString(admin) + ")";
    }
}