package security.unit.inputvalidation;

import Controller.JspHelper;
import Model.Prodotto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica il comportamento di JspHelper: estrazione sicura dei dati
 * dalla request per il rendering nei JSP.
 */
@DisplayName("Input Validation - JspHelper")
class JspHelperTest {

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = org.mockito.Mockito.mock(HttpServletRequest.class);
    }

    // =================================================================
    // estraiProdotti
    // =================================================================

    @Test
    @DisplayName("estraiProdotti usa l'attributo 'action' quando presente")
    void testEstraiProdottiConAction() {
        ArrayList<Prodotto> lista = new ArrayList<>();
        lista.add(new Prodotto());
        org.mockito.Mockito.when(request.getParameter("action")).thenReturn("Elettronica");
        org.mockito.Mockito.when(request.getAttribute("Elettronica")).thenReturn(lista);

        List<Prodotto> risultato = JspHelper.estraiProdotti(request);

        assertNotNull(risultato);
        assertEquals(1, risultato.size());
    }

    @Test
    @DisplayName("estraiProdotti usa l'attributo 'filtra' quando action è null")
    void testEstraiProdottiSenzaAction() {
        ArrayList<Prodotto> lista = new ArrayList<>();
        lista.add(new Prodotto());
        org.mockito.Mockito.when(request.getParameter("action")).thenReturn(null);
        org.mockito.Mockito.when(request.getAttribute("filtra")).thenReturn(lista);

        List<Prodotto> risultato = JspHelper.estraiProdotti(request);

        assertNotNull(risultato);
        assertEquals(1, risultato.size());
    }

    @Test
    @DisplayName("estraiProdotti restituisce lista vuota se attributo è null")
    void testEstraiProdottiAttributoNull() {
        org.mockito.Mockito.when(request.getParameter("action")).thenReturn(null);
        org.mockito.Mockito.when(request.getAttribute("filtra")).thenReturn(null);

        List<Prodotto> risultato = JspHelper.estraiProdotti(request);

        assertNotNull(risultato);
        assertTrue(risultato.isEmpty());
    }

    @Test
    @DisplayName("estraiProdotti restituisce lista vuota se attributo è null con action")
    void testEstraiProdottiAttributoNullConAction() {
        org.mockito.Mockito.when(request.getParameter("action")).thenReturn("Categoria");
        org.mockito.Mockito.when(request.getAttribute("Categoria")).thenReturn(null);

        List<Prodotto> risultato = JspHelper.estraiProdotti(request);

        assertNotNull(risultato);
        assertTrue(risultato.isEmpty());
    }

    @Test
    @DisplayName("estraiProdotti restituisce la stessa lista se presente")
    void testEstraiProdottiListaPresente() {
        ArrayList<Prodotto> lista = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            lista.add(new Prodotto());
        }
        org.mockito.Mockito.when(request.getParameter("action")).thenReturn("Test");
        org.mockito.Mockito.when(request.getAttribute("Test")).thenReturn(lista);

        List<Prodotto> risultato = JspHelper.estraiProdotti(request);

        assertEquals(5, risultato.size());
        assertSame(lista, risultato);
    }

    // =================================================================
    // estraiTitolo
    // =================================================================

    @Test
    @DisplayName("estraiTitolo restituisce 'action' quando presente")
    void testEstraiTitoloConAction() {
        org.mockito.Mockito.when(request.getParameter("action")).thenReturn("Categoria");

        String risultato = JspHelper.estraiTitolo(request);

        assertEquals("Categoria", risultato);
    }

    @Test
    @DisplayName("estraiTitolo restituisce 'filtraggio' quando action è null")
    void testEstraiTitoloSenzaAction() {
        org.mockito.Mockito.when(request.getParameter("action")).thenReturn(null);
        org.mockito.Mockito.when(request.getAttribute("filtraggio")).thenReturn("Materassi");

        String risultato = JspHelper.estraiTitolo(request);

        assertEquals("Materassi", risultato);
    }

    @Test
    @DisplayName("estraiTitolo restituisce stringa vuota se filtraggio è null")
    void testEstraiTitoloFiltraggioNull() {
        org.mockito.Mockito.when(request.getParameter("action")).thenReturn(null);
        org.mockito.Mockito.when(request.getAttribute("filtraggio")).thenReturn(null);

        String risultato = JspHelper.estraiTitolo(request);

        assertEquals("", risultato);
    }

    @Test
    @DisplayName("estraiTitolo gestisce attributi non-String con toString()")
    void testEstraiTitoloConOggettoNonString() {
        org.mockito.Mockito.when(request.getParameter("action")).thenReturn(null);
        org.mockito.Mockito.when(request.getAttribute("filtraggio")).thenReturn(12345);

        String risultato = JspHelper.estraiTitolo(request);

        assertEquals("12345", risultato);
    }
}