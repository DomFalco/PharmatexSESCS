package security.functional.inputvalidation;

import Controller.FiltraggioServletPrezzo;
import Model.Prodotto;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di FiltraggioServletPrezzo con H2 in-memory.
 * FiltraggioServletPrezzo legge la categoria dalla SESSIONE ("filtri") e
 * filtra i prodotti per range di prezzo.
 * Verifica:
 * - Entrambi vuoti -> forward a RicercaErrata.jsp (con doppio forward documentato)
 * - Solo prezzomin -> filtro con max=5000
 * - Solo prezzomax -> filtro con min=0
 * - Entrambi valorizzati -> filtro completo
 * - FINDING SEC-IV-03: doppio forward se entrambi vuoti (manca il return)
 * - FINDING CWE-476: prezzomin/prezzomax null -> NPE
 * - FINDING: prezzomin/prezzomax non numerici -> NumberFormatException
 * - FINDING CWE-476: categoria (sessione "filtri") null -> NPE nel DAO
 */
@DisplayName("Functional - FiltraggioServletPrezzo (Input Validation)")
class FiltraggioServletPrezzoH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
        // FiltraggioServletPrezzo implementa SOLO doGet() -> forziamo GET
        when(request.getMethod()).thenReturn("GET");
    }

    private void invokeService(FiltraggioServletPrezzo servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    // ==================================================================
    // Entrambi vuoti -> RicercaErrata.jsp
    // ==================================================================

    @Test
    @DisplayName("prezzomin e prezzomax entrambi vuoti -> la Servlet esegue il forward (con doppio forward documentato)")
    void testEntrambiVuotiRicercaErrata() throws Exception {
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp")).thenReturn(dispatcher);

        invokeService(new FiltraggioServletPrezzo());

        verify(dispatcher, atLeastOnce()).forward(request, response);
    }

    /**
     * FINDING (documentato): doppio forward quando entrambi i prezzi sono vuoti.
     * La Servlet, nel caso "prezzomin == '' AND prezzomax == ''", esegue
     * il forward a RicercaErrata.jsp ma NON ha un `return` dopo. Quindi
     * prosegue nell'esecuzione e chiama un SECONDO forward a Prodotti.jsp
     * con `filterList = null` (dato che il blocco if/else non ha assegnato
     * nulla in questo ramo).
     * In un container reale (Tomcat) questo causa IllegalStateException:
     * "Cannot forward after response has been committed". Con i mock,
     * semplicemente produce due forward.
     * La stessa assenza di `return` era presente in MaterialeServlet (fix
     * SEC-IV-02) -> suggeriamo lo stesso fix qui: aggiungere `return;`
     * dopo il primo forward.
     */
    @Test
    @DisplayName("FINDING SEC-IV-03: doppio forward quando entrambi vuoti (manca il return)")
    void testDoppioForward_BugDocumentato() throws Exception {
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp")).thenReturn(dispatcher);

        invokeService(new FiltraggioServletPrezzo());

        verify(dispatcher, times(2)).forward(request, response);
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
    // FINDING 1: prezzomin null
    // ==================================================================

    /**
     * FINDING (documentato): CWE-476 - NPE su prezzomin null.
     * La Servlet chiama request.getParameter("prezzomin").equals("") senza
     * verificare che il parametro non sia null. Se il client non invia
     * il parametro, getParameter ritorna null -> NPE.
     */
    @Test
    @DisplayName("FINDING CWE-476: prezzomin null -> NullPointerException")
    void testPrezzominNull_BugDocumentato() {
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn(null);
        when(request.getParameter("prezzomax")).thenReturn("500");
        FiltraggioServletPrezzo servlet = new FiltraggioServletPrezzo();

        assertThatThrownBy(() -> invokeService(servlet))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================================================================
    // FINDING 2: prezzomin non numerico
    // ==================================================================

    /**
     * FINDING (documentato): NumberFormatException su input non numerico.
     *
     * Double.parseDouble("abc") lancia NumberFormatException -> 500.
     */
    @Test
    @DisplayName("FINDING: prezzomin non numerico -> NumberFormatException")
    void testPrezzominNonNumerico_BugDocumentato() {
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("abc");
        when(request.getParameter("prezzomax")).thenReturn("500");
        FiltraggioServletPrezzo servlet = new FiltraggioServletPrezzo();

        assertThatThrownBy(() -> invokeService(servlet))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("FINDING: prezzomax non numerico -> NumberFormatException")
    void testPrezzomaxNonNumerico_BugDocumentato() {
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("quattrocento");
        FiltraggioServletPrezzo servlet = new FiltraggioServletPrezzo();

        assertThatThrownBy(() -> invokeService(servlet))
                .isInstanceOf(NumberFormatException.class);
    }

    // ==================================================================
    // FINDING 3: categoria null in sessione
    // ==================================================================

    /**
     * FINDING (documentato): CWE-476 - NPE se la sessione non ha "filtri".
     *
     * ProdottoDAO.doRetriveByFilter chiama `categoria.substring(0, categoria.length() - 1)`
     * senza null check. Se "filtri" non e' in sessione, la Servlet passa null
     * al DAO -> NPE.
     */
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