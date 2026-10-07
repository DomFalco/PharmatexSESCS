package security.functional.inputvalidation;

import Controller.InizioServlet;
import Model.Prodotto;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di InizioServlet con H2 in-memory.
 * InizioServlet ha 3 comportamenti:
 * 1. action == null + valore != null -> carica prodotti per categoria (DB)
 *    e forward a /WEB-INF/amministratore/CategorieProdotti.jsp
 * 2. action == "login" / "contatti" -> forward a JSP statiche (no DB)
 * 3. action == altro -> salva "filtri" in sessione + carica prodotti (DB)
 *    e forward a /WEB-INF/results/Prodotti.jsp
 * Verifica anche le validazioni (regex su action e valore).
 */
@DisplayName("Functional - InizioServlet (Input Validation)")
class InizioServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
        // InizioServlet implementa doGet + doPost (delega) -> POST default ok
    }

    private void invokeService(InizioServlet servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    // ==================================================================
    // Ramo 1: action == null, valore valorizzato -> CategorieProdotti.jsp
    // ==================================================================

    @Test
    @DisplayName("action=null, valore='Materasso' -> carica prodotti + forward CategorieProdotti.jsp")
    void testValoreCategoria() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Materasso", "Roma", 239.0, 5));
        executeSql(insertProdotto("P0003", "Letto", "Dublino", 300.0, 3));

        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("valore")).thenReturn("Materasso");
        when(request.getRequestDispatcher("/WEB-INF/amministratore/CategorieProdotti.jsp"))
                .thenReturn(dispatcher);

        invokeService(new InizioServlet());

        // Solo 2 prodotti hanno categoria "Materasso"
        verify(request).setAttribute(eq("CategorieProdotti"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 2;
        }));
        verify(request).setAttribute("Categoria", "Materasso");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("action=null, valore=null -> 400 'Parametri mancanti'")
    void testActionNullValoreNullRiceve400() throws Exception {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("valore")).thenReturn(null);

        invokeService(new InizioServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST),
                eq("Parametri mancanti."));
    }

    @Test
    @DisplayName("action=null, valore con caratteri invalidi -> 400")
    void testValoreInvalidoRiceve400() throws Exception {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("valore")).thenReturn("Materasso<script>");

        invokeService(new InizioServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST),
                eq("Parametro 'valore' non valido."));
    }

    // ==================================================================
    // Ramo 2: action == "login" / "contatti" -> JSP statiche (no DB)
    // ==================================================================

    @Test
    @DisplayName("action='login' -> forward a Login.jsp (no DB)")
    void testActionLogin() throws Exception {
        when(request.getParameter("action")).thenReturn("login");
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new InizioServlet());

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("action='contatti' -> forward a Contatti.jsp (no DB)")
    void testActionContatti() throws Exception {
        when(request.getParameter("action")).thenReturn("contatti");
        when(request.getRequestDispatcher("/WEB-INF/results/Contatti.jsp")).thenReturn(dispatcher);

        invokeService(new InizioServlet());

        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Ramo 3: action == categoria -> Prodotti.jsp + salva in sessione
    // ==================================================================

    @Test
    @DisplayName("action='Materasso' -> salva 'filtri' in sessione + carica prodotti + forward Prodotti.jsp")
    void testActionCategoria() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Materasso", "Roma", 239.0, 5));

        when(request.getParameter("action")).thenReturn("Materasso");
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp")).thenReturn(dispatcher);

        invokeService(new InizioServlet());

        // Salva categoria in sessione
        verify(session).setAttribute("filtri", "Materasso");
        // Attributo dinamico: nome = categoria, valore = lista prodotti
        verify(request).setAttribute(eq("Materasso"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 2;
        }));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("action='Letto' con DB vuoto -> lista vuota + forward Prodotti.jsp")
    void testActionCategoriaDbVuoto() throws Exception {
        when(request.getParameter("action")).thenReturn("Letto");
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp")).thenReturn(dispatcher);

        invokeService(new InizioServlet());

        verify(request).setAttribute(eq("Letto"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.isEmpty();
        }));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("action con caratteri invalidi -> 400 'Parametro action non valido'")
    void testActionInvalidoRiceve400() throws Exception {
        when(request.getParameter("action")).thenReturn("Materasso<script>");

        invokeService(new InizioServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST),
                eq("Parametro 'action' non valido."));
    }

    // ==================================================================
    // doPost -> doGet
    // ==================================================================

    @Test
    @DisplayName("doPost inoltra a doGet (comportamento trasparente)")
    void testDoPostInoltraADoGet() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("login");
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new InizioServlet());

        verify(dispatcher).forward(request, response);
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