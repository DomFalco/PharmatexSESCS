package security.functional.inputvalidation;

import Controller.MaterialeServlet;
import Model.Prodotto;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di MaterialeServlet con H2 in-memory.
 * MaterialeServlet legge "mat" e "materiale" dalla SESSIONE (non dalla request)
 * e filtra i prodotti in base al tipo di materiale richiesto.
 * Verifica:
 * - Sessione null → forward a RicercaErrata.jsp
 * - mat null → forward a RicercaErrata.jsp
 * - materiale null → forward a RicercaErrata.jsp
 * - mat = "materasso" → filtra per tipo materiale materasso
 * - mat = "rete" → filtra per materiale rete
 * - mat = "cuscino" → filtra per materiale cuscino
 * - mat sconosciuto → lista vuota → forward RicercaErrata.jsp
 * - nessun risultato → forward RicercaErrata.jsp
 * - risultati trovati → forward ProdottiMateriale.jsp con attributo
 */
@DisplayName("Functional - MaterialeServlet (Input Validation)")
class MaterialeServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(MaterialeServlet servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    // ==================================================================
    // Sessione mancante / attributi null → RicercaErrata.jsp
    // ==================================================================

    @Test
    @DisplayName("Sessione null → forward a RicercaErrata.jsp")
    void testSessioneNullRicercaErrata() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        invokeService(new MaterialeServlet());

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("mat null → forward a RicercaErrata.jsp")
    void testMatNullRicercaErrata() throws Exception {
        when(session.getAttribute("mat")).thenReturn(null);
        when(session.getAttribute("materiale")).thenReturn("Memory");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        invokeService(new MaterialeServlet());

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("materiale null → forward a RicercaErrata.jsp")
    void testMaterialeNullRicercaErrata() throws Exception {
        when(session.getAttribute("mat")).thenReturn("materasso");
        when(session.getAttribute("materiale")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        invokeService(new MaterialeServlet());

        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Filtri per materiale (mat = materasso / rete / cuscino)
    // ==================================================================

    @Test
    @DisplayName("mat=materasso → filtra prodotti con tipoMaterialeMaterasso e forward ProdottiMateriale.jsp")
    void testMaterassoSuccesso() throws Exception {
        executeSql(insertProdottoCompleto("P0001", "Nuvola", "Memory", null, null));
        executeSql(insertProdottoCompleto("P0002", "Roma", "Lattice", null, null));
        executeSql(insertProdottoCompleto("P0003", "Giglio", "Memory", null, null));

        when(session.getAttribute("mat")).thenReturn("materasso");
        when(session.getAttribute("materiale")).thenReturn("Memory");
        when(request.getRequestDispatcher("/WEB-INF/results/ProdottiMateriale.jsp")).thenReturn(dispatcher);

        invokeService(new MaterialeServlet());

        verify(request).setAttribute(eq("prodottiMateriale"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 2;
        }));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("mat=rete → filtra prodotti con materialeRete e forward ProdottiMateriale.jsp")
    void testReteSuccesso() throws Exception {
        executeSql(insertProdottoCompleto("P0001", "Faggio", null, "Faggio", null));
        executeSql(insertProdottoCompleto("P0002", "Tecna", null, "Ferro", null));

        when(session.getAttribute("mat")).thenReturn("rete");
        when(session.getAttribute("materiale")).thenReturn("Faggio");
        when(request.getRequestDispatcher("/WEB-INF/results/ProdottiMateriale.jsp")).thenReturn(dispatcher);

        invokeService(new MaterialeServlet());

        verify(request).setAttribute(eq("prodottiMateriale"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 1;
        }));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("mat=cuscino → filtra prodotti con materialeCuscino e forward ProdottiMateriale.jsp")
    void testCuscinoSuccesso() throws Exception {
        executeSql(insertProdottoCompleto("P0001", "Greta", null, null, "Memory"));
        executeSql(insertProdottoCompleto("P0002", "Guanciale", null, null, "Poliestere"));

        when(session.getAttribute("mat")).thenReturn("cuscino");
        when(session.getAttribute("materiale")).thenReturn("Memory");
        when(request.getRequestDispatcher("/WEB-INF/results/ProdottiMateriale.jsp")).thenReturn(dispatcher);

        invokeService(new MaterialeServlet());

        verify(request).setAttribute(eq("prodottiMateriale"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 1;
        }));
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Casi di "nessun risultato" → RicercaErrata.jsp
    // ==================================================================

    @Test
    @DisplayName("mat sconosciuto (non materasso/rete/cuscino) → lista vuota → RicercaErrata.jsp")
    void testMatSconosciutoRicercaErrata() throws Exception {
        when(session.getAttribute("mat")).thenReturn("sconosciuto");
        when(session.getAttribute("materiale")).thenReturn("qualsiasi");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        invokeService(new MaterialeServlet());

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Nessun prodotto per il materiale cercato → RicercaErrata.jsp")
    void testNessunProdottoRicercaErrata() throws Exception {
        // DB vuoto: nessun prodotto con quel materiale
        when(session.getAttribute("mat")).thenReturn("materasso");
        when(session.getAttribute("materiale")).thenReturn("Memory");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        invokeService(new MaterialeServlet());

        verify(dispatcher).forward(request, response);
        // Nessun forward a ProdottiMateriale.jsp
        verify(request, never()).getRequestDispatcher("/WEB-INF/results/ProdottiMateriale.jsp");
    }

    // ==================================================================
    // doPost → doGet (delega)
    // ==================================================================

    @Test
    @DisplayName("doPost inoltra a doGet (comportamento trasparente)")
    void testDoPostInoltraADoGet() throws Exception {
        executeSql(insertProdottoCompleto("P0001", "Nuvola", "Memory", null, null));
        when(request.getMethod()).thenReturn("POST");
        when(session.getAttribute("mat")).thenReturn("materasso");
        when(session.getAttribute("materiale")).thenReturn("Memory");
        when(request.getRequestDispatcher("/WEB-INF/results/ProdottiMateriale.jsp")).thenReturn(dispatcher);

        invokeService(new MaterialeServlet());

        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Helper: INSERT prodotto con campi materiale
    // ==================================================================

    private String insertProdottoCompleto(String id, String nome,
                                          String materialeMaterasso,
                                          String materialeRete,
                                          String materialeCuscino) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita, tipoMaterialeMaterasso, materialeRete, materialeCuscino) VALUES ("
                + "'" + id + "', 'Test', '" + nome + "', 'Desc', 100.0, 5, "
                + (materialeMaterasso == null ? "NULL" : "'" + materialeMaterasso + "'") + ", "
                + (materialeRete == null ? "NULL" : "'" + materialeRete + "'") + ", "
                + (materialeCuscino == null ? "NULL" : "'" + materialeCuscino + "'")
                + ")";
    }
}