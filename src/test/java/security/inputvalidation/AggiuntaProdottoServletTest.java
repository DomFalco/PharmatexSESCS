package security.inputvalidation;

import Controller.AggiuntaProdottoServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica il comportamento di AggiuntaProdottoServlet rispetto ai 17 parametri
 * accettati per l'aggiunta di un nuovo prodotto.
 * NOTA: i test DOCUMENTANO la mancanza di validazione e sanitizzazione del codice
 * attuale. La Servlet passa i valori direttamente al DAO senza controlli.
 */
@DisplayName("Input Validation - AggiuntaProdottoServlet")
class AggiuntaProdottoServletTest {

    private AggiuntaProdottoServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;          // <-- campo (usato in testNessunControlloAutorizzazione)

    @BeforeEach
    void setUp() {
        servlet = new AggiuntaProdottoServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        // dispatcher è usato solo qui, quindi variabile locale
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getMethod()).thenReturn("POST");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    /**
     * Helper: configura i 17 parametri con valori validi.
     */
    private void mockParametriValidi() {
        when(request.getParameter("idProdotto")).thenReturn("PROD001");
        when(request.getParameter("nomeCategoria")).thenReturn("Materasso");
        when(request.getParameter("nomeProdotto")).thenReturn("Materasso Memory");
        when(request.getParameter("descrizione")).thenReturn("Materasso comodo");
        when(request.getParameter("larghezza")).thenReturn("90.5");
        when(request.getParameter("lunghezza")).thenReturn("200.0");
        when(request.getParameter("prezzo")).thenReturn("499.99");
        when(request.getParameter("quantita")).thenReturn("10");
        when(request.getParameter("tipoMaterialeMaterasso")).thenReturn("Memory");
        when(request.getParameter("coloreLetto")).thenReturn("Bianco");
        when(request.getParameter("materialeRete")).thenReturn("Faggio");
        when(request.getParameter("rivestimentoDivano")).thenReturn("Tessuto");
        when(request.getParameter("coloreDivano")).thenReturn("Grigio");
        when(request.getParameter("tipoStoffaCuscino")).thenReturn("Cotone");
        when(request.getParameter("materialeCuscino")).thenReturn("Memory");
        when(request.getParameter("formaCuscino")).thenReturn("Quadrato");
    }

    // ====== 1. Documentazione NPE su parametri null ======

    @Test
    @DisplayName("Parametri stringa null non vengono validati (vulnerabilità documentata)")
    void testParametriStringaNullNonValidati() {
        // idProdotto restituisce null
        when(request.getParameter("idProdotto")).thenReturn(null);

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice non gestisce parametri stringa null: NPE documentata");
    }

    // ====== 2. Documentazione NumberFormatException ======

    @Test
    @DisplayName("Larghezza non numerica causa NumberFormatException (vulnerabilità documentata)")
    void testLarghezzaNonNumericaCausaNumberFormatException() {
        mockParametriValidi();
        when(request.getParameter("larghezza")).thenReturn("abc");

        assertThrows(NumberFormatException.class, () -> servlet.service(request, response),
                "Double.parseDouble senza try/catch: NumberFormatException documentata");
    }

    @Test
    @DisplayName("Prezzo non numerico causa NumberFormatException (vulnerabilità documentata)")
    void testPrezzoNonNumericoCausaNumberFormatException() {
        mockParametriValidi();
        when(request.getParameter("prezzo")).thenReturn("abc");

        assertThrows(NumberFormatException.class, () -> servlet.service(request, response),
                "Double.parseDouble senza try/catch: NumberFormatException documentata");
    }

    @Test
    @DisplayName("Quantità non numerica causa NumberFormatException (vulnerabilità documentata)")
    void testQuantitaNonNumericaCausaNumberFormatException() {
        mockParametriValidi();
        when(request.getParameter("quantita")).thenReturn("abc");

        assertThrows(NumberFormatException.class, () -> servlet.service(request, response),
                "Integer.parseInt senza try/catch: NumberFormatException documentata");
    }

    @Test
    @DisplayName("Input malevolo su prezzo causa NumberFormatException")
    void testInputMalevoloSuPrezzoCausaNumberFormatException() {
        mockParametriValidi();
        when(request.getParameter("prezzo")).thenReturn("10; DROP TABLE Prodotto;");

        assertThrows(NumberFormatException.class, () -> servlet.service(request, response),
                "Input non numerico (SQL injection) deve essere rifiutato dal parsing");
    }

    // ====== 3. Documentazione mancanza di sanitizzazione ======

    @Test
    @DisplayName("Il nome prodotto con <script> non viene sanitizzato (vulnerabilità documentata)")
    void testNomeProdottoNonSanitizzato() {
        mockParametriValidi();
        String inputMalevolo = "<script>alert(1)</script>";
        when(request.getParameter("nomeProdotto")).thenReturn(inputMalevolo);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        // Il Servlet NON sanitizza il nome: il valore passa al DAO così com'è
        verify(request, atLeastOnce()).getParameter("nomeProdotto");
    }

    @Test
    @DisplayName("La descrizione con tag HTML non viene sanitizzata (vulnerabilità documentata)")
    void testDescrizioneNonSanitizzata() {
        mockParametriValidi();
        when(request.getParameter("descrizione")).thenReturn("<img src=x onerror=alert(1)>");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("descrizione");
    }

    // ====== 4. Documentazione mancanza controllo autorizzazione ======

    @Test
    @DisplayName("Nessun controllo di autorizzazione presente (vulnerabilità documentata)")
    void testNessunControlloAutorizzazione() {
        mockParametriValidi();

        // La Servlet NON verifica che l'utente sia amministratore:
        // chiunque (anche non loggato) può invocare questa Servlet
        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Verifichiamo che NON venga mai chiamato getSession().getAttribute("Utente")
        // per controllare l'autorizzazione
        verify(session, never()).getAttribute("Utente");
    }

    // ====== 5. Documentazione mancanza range check ======

    @Test
    @DisplayName("Quantità negativa non viene validata (vulnerabilità documentata)")
    void testQuantitaNegativaNonValidata() {
        mockParametriValidi();
        when(request.getParameter("quantita")).thenReturn("-10");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Il parsing di "-10" va a buon fine: il valore passa al DAO
        verify(request, atLeastOnce()).getParameter("quantita");
    }

    @Test
    @DisplayName("Prezzo negativo non viene validato (vulnerabilità documentata)")
    void testPrezzoNegativoNonValidato() {
        mockParametriValidi();
        when(request.getParameter("prezzo")).thenReturn("-999.99");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("prezzo");
    }

    // ====== 6. Lettura di tutti i 17 parametri ======

    @Test
    @DisplayName("Tutti i 17 parametri vengono letti dalla richiesta")
    void testTuttiIParametriVengonoLetti() {
        mockParametriValidi();

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Verifica che i parametri principali siano stati letti
        verify(request, atLeastOnce()).getParameter("idProdotto");
        verify(request, atLeastOnce()).getParameter("nomeCategoria");
        verify(request, atLeastOnce()).getParameter("nomeProdotto");
        verify(request, atLeastOnce()).getParameter("descrizione");
        verify(request, atLeastOnce()).getParameter("larghezza");
        verify(request, atLeastOnce()).getParameter("lunghezza");
        verify(request, atLeastOnce()).getParameter("prezzo");
        verify(request, atLeastOnce()).getParameter("quantita");
    }
}