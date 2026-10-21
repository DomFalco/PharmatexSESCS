package security.functional.inputvalidation;

import Controller.ModificaProdottiServletAmministratore;
import Model.ConPool;
import Model.Prodotto;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Functional - ModificaProdottiServletAmministratore (Input Validation)")
class ModificaProdottiServletAmministratoreH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(ModificaProdottiServletAmministratore servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    private Prodotto prodottoInSessione(String id, int quantita) {
        Prodotto p = new Prodotto();
        p.setIdProdotto(id);
        p.setQuantita(quantita);
        return p;
    }

    @Test
    @DisplayName("nuovoPrezzo + quantitaTotale -> aggiorna entrambi + forward")
    void testModificaPrezzoEQuantita() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("350.0");
        when(request.getParameter("quantitaTotale")).thenReturn("3");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        assertThat(getPrezzo("P0001")).isEqualTo(350.0);
        assertThat(getQuantita("P0001")).isEqualTo(8);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Solo nuovoPrezzo -> aggiorna solo il prezzo")
    void testSoloPrezzo() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("299.99");
        when(request.getParameter("quantitaTotale")).thenReturn("");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        assertThat(getPrezzo("P0001")).isEqualTo(299.99);
        assertThat(getQuantita("P0001")).isEqualTo(5);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Solo quantitaTotale -> aggiorna solo la quantita (incrementale)")
    void testSoloQuantita() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("10");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        assertThat(getPrezzo("P0001")).isEqualTo(400.0);
        assertThat(getQuantita("P0001")).isEqualTo(15);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("FINDING CWE-862: nessun controllo autorizzazione (utente anonimo puo' modificare)")
    void testNessunControlloAutorizzazione_BugDocumentato() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(session.getAttribute("Amministratore")).thenReturn(null);
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("999.99");
        when(request.getParameter("quantitaTotale")).thenReturn("");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        assertThat(getPrezzo("P0001")).isEqualTo(999.99);
        verify(response, never()).sendError(eq(403), anyString());
    }

    @ParameterizedTest(name = "Parametri non validi: id={0}, prezzo={1}, quantita={2} -> 400 Bad Request")
    @CsvSource({
            "null, 350.0, 3",     // prodotto null
            "P0001, null, 3",     // prezzo null
            "P0001, abc, 3"       // prezzo non numerico
    })
    @DisplayName("Parametri non validi rispondono con 400 Bad Request")
    void testParametriInvalidi(String idProdotto, String prezzo, String quantita) throws Exception {
        if ("null".equals(idProdotto)) {
            when(session.getAttribute("idModificaPrezzo")).thenReturn(null);
        } else {
            when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione(idProdotto, 5));
        }

        when(request.getParameter("nuovoPrezzo")).thenReturn("null".equals(prezzo) ? null : prezzo);
        when(request.getParameter("quantitaTotale")).thenReturn(quantita);

        ModificaProdottiServletAmministratore servlet = new ModificaProdottiServletAmministratore();

        invokeService(servlet);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("FIX: nuovoPrezzo='' e quantitaTotale=null -> Bad Request 400 (invece di silent drop)")
    void testSilentDrop() throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn(null);

        invokeService(new ModificaProdottiServletAmministratore());

        verify(dispatcher, never()).forward(any(), any());
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("doGet inoltra a doPost (comportamento trasparente)")
    void testDoGetInoltraADoPost() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(request.getMethod()).thenReturn("GET");
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("300.0");
        when(request.getParameter("quantitaTotale")).thenReturn("");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        assertThat(getPrezzo("P0001")).isEqualTo(300.0);
        verify(dispatcher).forward(request, response);
    }

    private double getPrezzo(String id) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT prezzo FROM Prodotto WHERE idProdotto = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    private int getQuantita(String id) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT quantita FROM Prodotto WHERE idProdotto = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private String insertProdotto(String id, String nome, double prezzo, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, prezzo, quantita) VALUES ('"
                + id + "', 'Test', '" + nome + "', 'Desc', " + prezzo + ", " + quantita + ")";
    }
}