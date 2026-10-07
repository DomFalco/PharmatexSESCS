package security.functional.inputvalidation;

import Controller.AggiuntaProdottoServlet;
import Model.ConPool;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import security.functional.BaseServletH2Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di AggiuntaProdottoServlet con H2 in-memory.
 *
 * Verifica:
 * - Aggiunta prodotto con parametri validi → INSERT + forward
 * - Tutti i campi salvati correttamente
 * - FINDING 1: nessun controllo autorizzazione (chiunque può aggiungere)
 * - FINDING 2: NumberFormatException su larghezza/lunghezza/prezzo non numerici
 * - FINDING 3: NumberFormatException su quantita non numerica
 * - FINDING 4: idProdotto null accettato (viola PRIMARY KEY)
 */
@DisplayName("Functional - AggiuntaProdottoServlet (Input Validation)")
class AggiuntaProdottoServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(AggiuntaProdottoServlet servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    /**
     * Configura tutti i 16 parametri richiesti dalla Servlet con valori validi.
     */
    private void setParametriValidi() {
        when(request.getParameter("idProdotto")).thenReturn("P0099");
        when(request.getParameter("nomeCategoria")).thenReturn("Cuscino");
        when(request.getParameter("nomeProdotto")).thenReturn("TestCuscino");
        when(request.getParameter("descrizione")).thenReturn("Descrizione di test");
        when(request.getParameter("larghezza")).thenReturn("70.0");
        when(request.getParameter("lunghezza")).thenReturn("45.0");
        when(request.getParameter("prezzo")).thenReturn("40.0");
        when(request.getParameter("quantita")).thenReturn("10");
        when(request.getParameter("tipoMaterialeMaterasso")).thenReturn(null);
        when(request.getParameter("coloreLetto")).thenReturn(null);
        when(request.getParameter("materialeRete")).thenReturn(null);
        when(request.getParameter("rivestimentoDivano")).thenReturn(null);
        when(request.getParameter("coloreDivano")).thenReturn(null);
        when(request.getParameter("tipoStoffaCuscino")).thenReturn("Poliestere");
        when(request.getParameter("materialeCuscino")).thenReturn("Memory");
        when(request.getParameter("formaCuscino")).thenReturn("Rettangolare");
    }

    // ==================================================================
    // Aggiunta prodotto valida
    // ==================================================================

    @Test
    @DisplayName("Aggiunta valida → INSERT + forward a HomeServletAmministratore")
    void testAggiuntaValida() throws Exception {
        setParametriValidi();
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new AggiuntaProdottoServlet());

        assertThat(countProdotti("P0099")).isEqualTo(1);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Aggiunta valida → tutti i 16 campi salvati correttamente")
    void testTuttiCampiSalvati() throws Exception {
        setParametriValidi();
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new AggiuntaProdottoServlet());

        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nomeCategoria, nomeProd, descrizione, larghezza, lunghezza, "
                             + "prezzo, quantita, tipoStoffaCuscino, materialeCuscino, formaCuscino "
                             + "FROM Prodotto WHERE idProdotto = ?")) {
            ps.setString(1, "P0099");
            try (ResultSet rs = ps.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("nomeCategoria")).isEqualTo("Cuscino");
                assertThat(rs.getString("nomeProd")).isEqualTo("TestCuscino");
                assertThat(rs.getString("descrizione")).isEqualTo("Descrizione di test");
                assertThat(rs.getDouble("larghezza")).isEqualTo(70.0);
                assertThat(rs.getDouble("lunghezza")).isEqualTo(45.0);
                assertThat(rs.getDouble("prezzo")).isEqualTo(40.0);
                assertThat(rs.getInt("quantita")).isEqualTo(10);
                assertThat(rs.getString("tipoStoffaCuscino")).isEqualTo("Poliestere");
                assertThat(rs.getString("materialeCuscino")).isEqualTo("Memory");
                assertThat(rs.getString("formaCuscino")).isEqualTo("Rettangolare");
            }
        }
    }

    // ==================================================================
    // FINDING 1: nessun controllo autorizzazione
    // ==================================================================

    /**
     * FINDING (documentato): CWE-862 — Missing Authorization.
     *
     * AggiuntaProdottoServlet NON controlla la sessione né il ruolo admin.
     * Chiunque può chiamare /AggiuntaProdottoServlet e inserire un prodotto
     * nel database, anche un utente anonimo o un utente normale.
     *
     * Questo è un grave bug di sicurezza: le funzionalità amministrative
     * devono richiedere autenticazione e ruolo admin.
     *
     * Fix suggerito: aggiungere il controllo sessione + ruolo admin come
     * nelle altre Servlet (es. CercaProdottoPerModificaServlet).
     */
    @Test
    @DisplayName("FINDING CWE-862: nessun controllo autorizzazione (utente anonimo può aggiungere)")
    void testNessunControlloAutorizzazione_BugDocumentato() throws Exception {
        // Nessuna sessione, nessun admin — la Servlet non se ne cura
        setParametriValidi();
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new AggiuntaProdottoServlet());

        // Il prodotto è stato inserito NONOSTANTE l'assenza di autenticazione
        assertThat(countProdotti("P0099")).isEqualTo(1);
        // Nessun 403 viene restituito
        verify(response, never()).sendError(eq(403), anyString());
        // Forward eseguito normalmente
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // FINDING 2 e 3: NumberFormatException su input non numerici
    // ==================================================================

    /**
     * FINDING (documentato): NumberFormatException non gestita.
     *
     * La Servlet usa Double.parseDouble() / Integer.parseInt() senza
     * try/catch. Se l'utente invia un valore non numerico per larghezza,
     * lunghezza, prezzo o quantita, la Servlet lancia NumberFormatException
     * → 500 Internal Server Error.
     *
     * Fix suggerito: validare i parametri prima del parsing, oppure gestire
     * l'eccezione e restituire 400 con messaggio "Formato numerico non valido".
     */
    @ParameterizedTest
    @CsvSource({
            "larghezza, abc",
            "prezzo,    quaranta",
            "quantita,  dieci"
    })
    @DisplayName("FINDING: parametro non numerico → NumberFormatException")
    void testParametroNonNumerico_BugDocumentato(String parametro, String valoreNonNumerico) {
        setParametriValidi();
        when(request.getParameter(parametro)).thenReturn(valoreNonNumerico);
        AggiuntaProdottoServlet servlet = new AggiuntaProdottoServlet();

        assertThatThrownBy(() -> invokeService(servlet))
                .isInstanceOf(NumberFormatException.class);
    }

    // ==================================================================
    // FINDING 4: idProdotto null accettato
    // ==================================================================

    /**
     * FINDING (documentato): idProdotto null non validato.
     *
     * Se l'utente non fornisce "idProdotto", il bean riceve null e il DAO
     * esegue INSERT con NULL nella PRIMARY KEY. Su H2/MySQL questo causa
     * un errore SQL (violazione NOT NULL), che il DAO rilancia come
     * DataAccessException → 500.
     *
     * Fix suggerito: validare che idProdotto non sia null/vuoto prima
     * dell'INSERT.
     */
    @Test
    @DisplayName("FINDING: idProdotto null causa errore SQL (500)")
    void testIdProdottoNull_BugDocumentato() {
        setParametriValidi();
        when(request.getParameter("idProdotto")).thenReturn(null);
        AggiuntaProdottoServlet servlet = new AggiuntaProdottoServlet();

        assertThatThrownBy(() -> invokeService(servlet))
                .isInstanceOf(RuntimeException.class);
    }

    // ==================================================================
    // doGet → doPost
    // ==================================================================

    @Test
    @DisplayName("doGet inoltra a doPost (comportamento trasparente)")
    void testDoGetInoltraADoPost() throws Exception {
        setParametriValidi();
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new AggiuntaProdottoServlet());

        assertThat(countProdotti("P0099")).isEqualTo(1);
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Helper
    // ==================================================================

    private int countProdotti(String id) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM Prodotto WHERE idProdotto = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}