package security.functional.inputvalidation;

import Controller.RicercaServlet;
import Model.Prodotto;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di RicercaServlet con H2 in-memory.
 * RicercaServlet implementa SOLO doGet(), quindi i test forzano il metodo
 * HTTP a "GET" per far dispatchare correttamente service().
 */
@DisplayName("Functional - RicercaServlet (Input Validation)")
class RicercaServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
        // RicercaServlet implementa SOLO doGet() → forziamo GET
        when(request.getMethod()).thenReturn("GET");
    }

    private void invokeService(RicercaServlet servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    // ==================================================================
    // Ricerca prodotto esistente
    // ==================================================================

    @Test
    @DisplayName("Ricerca prodotto esistente → forward a Prodotto.jsp con attributo 'prodotto'")
    void testRicercaProdottoEsistente() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        when(request.getParameter("search")).thenReturn("Nuvola");
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotto.jsp")).thenReturn(dispatcher);

        invokeService(new RicercaServlet());

        verify(request).setAttribute(eq("prodotto"), argThat(o -> {
            Prodotto p = (Prodotto) o;
            return p.getIdProdotto() != null && p.getIdProdotto().equals("P0001");
        }));
        verify(session).setAttribute(eq("prod"), any(Prodotto.class));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Ricerca con input lowercase → trovato (Servlet uppercasa prima di chiamare il DAO)")
    void testRicercaCaseInsensitive() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        when(request.getParameter("search")).thenReturn("nuvola");
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotto.jsp")).thenReturn(dispatcher);

        invokeService(new RicercaServlet());

        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Ricerca prodotto inesistente
    // ==================================================================

    @Test
    @DisplayName("Ricerca prodotto inesistente → forward a RicercaErrata.jsp")
    void testRicercaProdottoInesistente() throws Exception {
        when(request.getParameter("search")).thenReturn("NonEsiste");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        invokeService(new RicercaServlet());

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Ricerca con input vuoto → forward a RicercaErrata.jsp")
    void testRicercaInputVuoto() throws Exception {
        when(request.getParameter("search")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        invokeService(new RicercaServlet());

        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // FINDING 1: NPE su input null
    // ==================================================================

    @Test
    @DisplayName("FINDING CWE-476: input null → NullPointerException")
    void testInputNull_BugDocumentato() {
        when(request.getParameter("search")).thenReturn(null);

        assertThatThrownBy(() -> invokeService(new RicercaServlet()))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================================================================
    // FINDING 2: doppio upper ridondante
    // ==================================================================

    @Test
    @DisplayName("FINDING: input già in uppercase funziona (doppio upper documentato)")
    void testInputUppercaseFunziona() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        when(request.getParameter("search")).thenReturn("NUVOLA");
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotto.jsp")).thenReturn(dispatcher);

        invokeService(new RicercaServlet());

        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Verifica: prodotto salvato in sessione E in request
    // ==================================================================

    @Test
    @DisplayName("Il prodotto trovato è salvato sia in sessione ('prod') sia in request ('prodotto')")
    void testProdottoSalvatoInSessioneERequest() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        when(request.getParameter("search")).thenReturn("Nuvola");
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotto.jsp")).thenReturn(dispatcher);

        invokeService(new RicercaServlet());

        verify(session).setAttribute(eq("prod"), any(Prodotto.class));
        verify(request).setAttribute(eq("prodotto"), any(Prodotto.class));
    }

    // ==================================================================
    // Helper
    // ==================================================================

    private String insertProdotto(String id, String categoria, String nome,
                                  double prezzo, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita) VALUES ('"
                + id + "', '" + categoria + "', '" + nome + "', "
                + "'Descrizione di test', " + prezzo + ", " + quantita + ")";
    }
}