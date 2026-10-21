package security.functional.inputvalidation;

import Controller.FiltraggioServletPrezzo;
import Model.Prodotto;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di FiltraggioServletPrezzo con H2 in-memory.
 */
@DisplayName("Functional - FiltraggioServletPrezzo (Input Validation)")
class FiltraggioServletPrezzoH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
        when(request.getMethod()).thenReturn("GET");
    }

    private void invokeService(FiltraggioServletPrezzo servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    // ==================================================================
    // Entrambi vuoti -> RicercaErrata.jsp
    // ==================================================================

    @Test
    @DisplayName("prezzomin e prezzomax entrambi vuoti -> forward a RicercaErrata.jsp")
    void testEntrambiVuotiRicercaErrata() throws Exception {
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        invokeService(new FiltraggioServletPrezzo());

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("FIX SEC-IV-03: singolo forward quando entrambi vuoti")
    void testDoppioForward_BugDocumentato() throws Exception {
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        invokeService(new FiltraggioServletPrezzo());

        // FIX: Ora la Servlet esegue un solo forward grazie al return
        verify(dispatcher, times(1)).forward(request, response);
    }

    // ==================================================================
    // Solo prezzomin
    // ==================================================================

    @Test
    @DisplayName("Solo prezzomin=100 -> filtro con min=100, max=5000")
    void testSoloPrezzomin() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Materasso", "Roma", 80.0, 5));
        executeSql(insertProdotto("P0003", "Materasso", "Giglio", 350.0, 5));

        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("100");
        when(request.getParameter("prezzomax")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp")).thenReturn(dispatcher);

        invokeService(new FiltraggioServletPrezzo());

        verify(request).setAttribute(eq("filtra"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 2;
        }));
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Solo prezzomax
    // ==================================================================

    @Test
    @DisplayName("Solo prezzomax=200 -> filtro con min=0, max=200")
    void testSoloPrezzomax() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Materasso", "Roma", 80.0, 5));
        executeSql(insertProdotto("P0003", "Materasso", "Giglio", 150.0, 5));

        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("200");
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp")).thenReturn(dispatcher);

        invokeService(new FiltraggioServletPrezzo());

        verify(request).setAttribute(eq("filtra"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 2;
        }));
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Entrambi valorizzati
    // ==================================================================

    @Test
    @DisplayName("prezzomin=100, prezzomax=400 -> filtro range completo")
    void testRangeCompleto() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Materasso", "Roma", 80.0, 5));
        executeSql(insertProdotto("P0003", "Materasso", "Giglio", 250.0, 5));
        executeSql(insertProdotto("P0004", "Materasso", "Test", 500.0, 5));

        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("100");
        when(request.getParameter("prezzomax")).thenReturn("400");
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp")).thenReturn(dispatcher);

        invokeService(new FiltraggioServletPrezzo());

        verify(request).setAttribute(eq("filtra"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 2;
        }));
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Attributi "filtra" e "filtraggio"
    // ==================================================================

    @Test
    @DisplayName("Attributi 'filtra' (lista) e 'filtraggio' (categoria) settati")
    void testAttributiSettati() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("100");
        when(request.getParameter("prezzomax")).thenReturn("500");
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp")).thenReturn(dispatcher);

        invokeService(new FiltraggioServletPrezzo());

        verify(request).setAttribute(eq("filtra"), any(ArrayList.class));
        verify(request).setAttribute("filtraggio", "Materasso");
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // FINDING 1: prezzomin null (ORA GESTITO)
    // ==================================================================

    @Test
    @DisplayName("prezzomin null e prezzomax vuoto -> forward a RicercaErrata.jsp")
    void testPrezzominNull_BugDocumentato() throws Exception {
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn(null);
        when(request.getParameter("prezzomax")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);
        FiltraggioServletPrezzo servlet = new FiltraggioServletPrezzo();

        invokeService(servlet);

        // FIX: La servlet gestisce il null e reindirizza
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // FINDING 2: prezzomin non numerico (ORA GESTITO)
    // ==================================================================

    @Test
    @DisplayName("prezzomin non numerico -> Bad Request 400")
    void testPrezzominNonNumerico_BugDocumentato() throws Exception {
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("abc");
        when(request.getParameter("prezzomax")).thenReturn("500");
        FiltraggioServletPrezzo servlet = new FiltraggioServletPrezzo();

        invokeService(servlet);

        // FIX: La servlet cattura l'eccezione e risponde 400
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("prezzomax non numerico -> Bad Request 400")
    void testPrezzomaxNonNumerico_BugDocumentato() throws Exception {
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("quattrocento");
        FiltraggioServletPrezzo servlet = new FiltraggioServletPrezzo();

        invokeService(servlet);

        // FIX: La servlet cattura l'eccezione e risponde 400
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ==================================================================
    // FINDING 3: categoria null in sessione (NON RISOLTO)
    // ==================================================================

    @Test
    @DisplayName("FINDING CWE-476: categoria (sessione 'filtri') null -> NPE nel DAO")
    void testCategoriaNull_BugDocumentato() {
        when(session.getAttribute("filtri")).thenReturn(null);
        when(request.getParameter("prezzomin")).thenReturn("100");
        when(request.getParameter("prezzomax")).thenReturn("500");
        FiltraggioServletPrezzo servlet = new FiltraggioServletPrezzo();

        assertThatThrownBy(() -> invokeService(servlet))
                .isInstanceOf(NullPointerException.class);
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