package security.functional.inputvalidation;

import Controller.AggiuntaProdottoServlet;
import Model.ConPool;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
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

    @Test
    @DisplayName("FINDING CWE-862: nessun controllo autorizzazione (utente anonimo può aggiungere)")
    void testNessunControlloAutorizzazione_BugDocumentato() throws Exception {
        setParametriValidi();
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new AggiuntaProdottoServlet());

        assertThat(countProdotti("P0099")).isEqualTo(1);
        verify(response, never()).sendError(eq(403), anyString());
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // FINDING 2 e 3: NumberFormatException (ORA CORRETTA IN 400 BAD REQUEST)
    // ==================================================================

    @ParameterizedTest
    @CsvSource({
            "larghezza, abc",
            "prezzo,    quaranta",
            "quantita,  dieci"
    })
    @DisplayName("Parametro non numerico → Bad Request 400")
    void testParametroNonNumerico(String parametro, String valoreNonNumerico) throws Exception {
        setParametriValidi();
        when(request.getParameter(parametro)).thenReturn(valoreNonNumerico);
        AggiuntaProdottoServlet servlet = new AggiuntaProdottoServlet();

        invokeService(servlet);

        // FIX: La servlet ora cattura l'eccezione e risponde 400
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ==================================================================
    // FINDING 4: idProdotto null accettato
    // ==================================================================

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