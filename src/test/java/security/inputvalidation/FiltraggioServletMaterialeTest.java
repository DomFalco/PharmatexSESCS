package security.inputvalidation;

import Controller.FiltraggioServletMateriale;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica che FiltraggioServletMateriale gestisca correttamente
 * gli input HTTP, applicando sanitizzazione e prevenendo XSS.
 */
@DisplayName("Input Validation - FiltraggioServletMateriale")
class FiltraggioServletMaterialeTest {

    private FiltraggioServletMateriale servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringWriter responseStringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new FiltraggioServletMateriale();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        // Cattura l'output scritto dal Servlet tramite response.getWriter()
        responseStringWriter = new StringWriter();
        printWriter = new PrintWriter(responseStringWriter);

        when(response.getWriter()).thenReturn(printWriter);
        when(request.getSession()).thenReturn(session);
        // Necessari per invocare service() (che internamente chiama doGet)
        when(request.getMethod()).thenReturn("GET");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
    }

    private String getResponseOutput() {
        printWriter.flush();
        return responseStringWriter.toString();
    }

    // ====== 1. Casi validi per ogni categoria ======

    @Test
    @DisplayName("'Materasso' restituisce le opzioni corrette")
    void testMaterassoOptions() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Materasso");
        when(request.getParameter("materiale")).thenReturn("Selezionare...");

        servlet.service(request, response);
        String output = getResponseOutput();

        assertTrue(output.contains("<option>Memory</option>"));
        assertTrue(output.contains("<option>Molla</option>"));
        assertTrue(output.contains("<option>Lana</option>"));
        assertTrue(output.contains("<option>Lattice</option>"));
    }

    @Test
    @DisplayName("'Rete' restituisce le opzioni corrette")
    void testReteOptions() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Rete");
        when(request.getParameter("materiale")).thenReturn("Selezionare...");

        servlet.service(request, response);
        String output = getResponseOutput();

        assertTrue(output.contains("<option>Faggio</option>"));
        assertTrue(output.contains("<option>Ferro</option>"));
    }

    @Test
    @DisplayName("'Cuscino' restituisce le opzioni corrette")
    void testCuscinoOptions() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Cuscino");
        when(request.getParameter("materiale")).thenReturn("Selezionare...");

        servlet.service(request, response);
        String output = getResponseOutput();

        assertTrue(output.contains("<option>Basic</option>"));
        assertTrue(output.contains("<option>Fibre sintetiche</option>"));
    }

    // ====== 2. Case-insensitive ======

    @Test
    @DisplayName("La logica è case-insensitive (MATERASSO = Materasso)")
    void testCaseInsensitive() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("MATERASSO");
        when(request.getParameter("materiale")).thenReturn("Selezionare...");

        servlet.service(request, response);
        String output = getResponseOutput();

        assertTrue(output.contains("<option>Memory</option>"));
    }

    // ====== 3. Sanitizzazione dell'input ======

    @Test
    @DisplayName("Input malevolo con <script> non produce tag script nell'output")
    void testSanitizationBeforeComparison() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("<script>alert(1)</script>");
        when(request.getParameter("materiale")).thenReturn("Selezionare...");

        servlet.service(request, response);
        String output = getResponseOutput();

        // L'output HTML del Servlet non deve contenere lo script iniettato
        assertFalse(output.contains("<script>alert(1)</script>"),
                "L'output non deve contenere codice script non sanitizzato");
    }

    // ====== 4. Salvataggio sicuro in sessione ======

    @Test
    @DisplayName("La sessione contiene i valori sanitizzati (non quelli originali)")
    void testSessionContainsSanitizedValues() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Materasso");
        when(request.getParameter("materiale")).thenReturn("Memory");

        servlet.service(request, response);

        ArgumentCaptor<String> matCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> materialeCaptor = ArgumentCaptor.forClass(String.class);

        verify(session).setAttribute(eq("mat"), matCaptor.capture());
        verify(session).setAttribute(eq("materiale"), materialeCaptor.capture());

        assertEquals("Materasso", matCaptor.getValue());
        assertEquals("Memory", materialeCaptor.getValue());
    }

    @Test
    @DisplayName("La sessione non contiene caratteri malevoli (sanitizzati)")
    void testSessionSanitizesMaliciousInput() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("<script>");
        when(request.getParameter("materiale")).thenReturn("onerror=alert(1)");

        servlet.service(request, response);

        ArgumentCaptor<String> matCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> materialeCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).setAttribute(eq("mat"), matCaptor.capture());
        verify(session).setAttribute(eq("materiale"), materialeCaptor.capture());

        // Dopo sanitizzazione: "script" e "onerroralert1"
        assertFalse(matCaptor.getValue().contains("<"));
        assertFalse(matCaptor.getValue().contains(">"));
        assertFalse(materialeCaptor.getValue().contains("("));
        assertFalse(materialeCaptor.getValue().contains(")"));
        assertFalse(materialeCaptor.getValue().contains("="));
    }

    // ====== 5. Robustezza ======

    @Test
    @DisplayName("Input null non causa NullPointerException")
    void testNullInputDoesNotCrash() {
        when(request.getParameter("prodotto")).thenReturn(null);
        when(request.getParameter("materiale")).thenReturn(null);

        assertDoesNotThrow(() -> servlet.service(request, response),
                "Input null deve essere gestito dal null check");
    }

    @Test
    @DisplayName("Categoria sconosciuta non produce opzioni HTML")
    void testUnknownCategoryProducesNoOptions() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Tavolo");
        when(request.getParameter("materiale")).thenReturn("Legno");

        servlet.service(request, response);
        String output = getResponseOutput();

        assertEquals("", output, "Categoria sconosciuta non deve produrre opzioni");
    }
}