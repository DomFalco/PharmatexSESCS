package security.functional.inputvalidation;

import Controller.FiltraggioServletMateriale;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di FiltraggioServletMateriale con H2 in-memory.
 * La Servlet genera HTML via response.getWriter() e salva i parametri
 * (sanitizzati) in sessione.
 * Verifica:
 * - mat="Materasso" → scrive 4 option, salva in sessione
 * - mat="Rete" → scrive 2 option
 * - mat="Cuscino" → scrive 3 option
 * - mat sconosciuto → nessuna option, ma sessione aggiornata
 * - Sanitizzazione: caratteri speciali rimossi
 * - Null check: parametri null → diventano stringa vuota
 */
@DisplayName("Functional - FiltraggioServletMateriale (Input Validation)")
class FiltraggioServletMaterialeH2Test extends BaseServletH2Test {

    private StringWriter responseBody;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        setUpServletMocks();
        // FiltraggioServletMateriale implementa SOLO doGet() → forziamo GET
        when(request.getMethod()).thenReturn("GET");
        // Mock del writer per catturare l'output HTML
        responseBody = new StringWriter();
        writer = new PrintWriter(responseBody);
        when(response.getWriter()).thenReturn(writer);
    }

    private void invokeService(FiltraggioServletMateriale servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
        writer.flush();
    }

    // ==================================================================
    // Ramo "Materasso"
    // ==================================================================

    @Test
    @DisplayName("mat=Materasso → scrive option Memory/Molla/Lana/Lattice")
    void testMaterassoScriveOption() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Materasso");
        when(request.getParameter("materiale")).thenReturn("Memory");

        invokeService(new FiltraggioServletMateriale());

        assertThat(responseBody.toString())
                .contains(
                        "<option>Memory</option>",
                        "<option>Molla</option>",
                        "<option>Lana</option>",
                        "<option>Lattice</option>");
    }

    @Test
    @DisplayName("mat=materasso (lowercase) → case-insensitive, scrive le option")
    void testMaterassoCaseInsensitive() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("materasso");
        when(request.getParameter("materiale")).thenReturn("Memory");

        invokeService(new FiltraggioServletMateriale());

        assertThat(responseBody.toString()).contains("<option>Memory</option>");
    }

    // ==================================================================
    // Ramo "Rete"
    // ==================================================================

    @Test
    @DisplayName("mat=Rete → scrive option Faggio/Ferro")
    void testReteScriveOption() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Rete");
        when(request.getParameter("materiale")).thenReturn("Faggio");

        invokeService(new FiltraggioServletMateriale());

        assertThat(responseBody.toString())
                .contains(
                        "<option>Faggio</option>",
                        "<option>Ferro</option>");
    }

    // ==================================================================
    // Ramo "Cuscino"
    // ==================================================================

    @Test
    @DisplayName("mat=Cuscino → scrive option Memory/Basic/Fibre sintetiche")
    void testCuscinoScriveOption() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Cuscino");
        when(request.getParameter("materiale")).thenReturn("Memory");

        invokeService(new FiltraggioServletMateriale());

        assertThat(responseBody.toString())
                .contains(
                        "<option>Memory</option>",
                        "<option>Basic</option>",
                        "<option>Fibre sintetiche</option>");
    }

    // ==================================================================
    // Ramo "sconosciuto"
    // ==================================================================

    @Test
    @DisplayName("mat sconosciuto → nessuna option scritta, ma sessione aggiornata")
    void testMatSconosciutoNessunaOption() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Sconosciuto");
        when(request.getParameter("materiale")).thenReturn("X");

        invokeService(new FiltraggioServletMateriale());

        assertThat(responseBody.toString()).isEmpty();
        verify(session).setAttribute("mat", "Sconosciuto");
        verify(session).setAttribute("materiale", "X");
    }

    // ==================================================================
    // Sanitizzazione
    // ==================================================================

    @Test
    @DisplayName("Input con caratteri speciali → sanitizzato (rimossi dal confronto)")
    void testSanitizzazioneRimuoveCaratteriSpeciali() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Materasso<script>");
        when(request.getParameter("materiale")).thenReturn("Memory'; DROP TABLE--");

        invokeService(new FiltraggioServletMateriale());

        // Il confronto avviene su "Materassoscript" (rimossi < >)
        // Non matcha "Materasso" esatto → nessuna option scritta
        assertThat(responseBody.toString()).isEmpty();
        // In sessione è salvato il valore sanitizzato
        verify(session).setAttribute("mat", "Materassoscript");
        verify(session).setAttribute("materiale", "Memory DROP TABLE");
    }

    @Test
    @DisplayName("Sanitizzazione rimuove XSS payload (angle brackets)")
    void testSanitizzazioneXSS() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Materasso");
        when(request.getParameter("materiale")).thenReturn("<script>alert(1)</script>");

        invokeService(new FiltraggioServletMateriale());

        // Il materiale non è usato per il match, ma va salvato sanitizzato
        verify(session).setAttribute("materiale", "scriptalert1script");
    }

    // ==================================================================
    // Null check
    // ==================================================================

    @Test
    @DisplayName("Parametri null → sanitizzati a stringa vuota (no NPE)")
    void testParametriNull() throws Exception {
        when(request.getParameter("prodotto")).thenReturn(null);
        when(request.getParameter("materiale")).thenReturn(null);

        invokeService(new FiltraggioServletMateriale());

        // Nessun NPE, nessuna option
        assertThat(responseBody.toString()).isEmpty();
        verify(session).setAttribute("mat", "");
        verify(session).setAttribute("materiale", "");
    }

    // ==================================================================
    // Salvataggio in sessione
    // ==================================================================

    @Test
    @DisplayName("Parametri validi → salvati in sessione sanitizzati")
    void testParametriSalvatiInSessione() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Materasso");
        when(request.getParameter("materiale")).thenReturn("Memory");

        invokeService(new FiltraggioServletMateriale());

        verify(session).setAttribute("mat", "Materasso");
        verify(session).setAttribute("materiale", "Memory");
    }

    // ==================================================================
    // Session creata se non esiste
    // ==================================================================

    @Test
    @DisplayName("Session null → viene creata (request.getSession() senza false)")
    void testSessionCreataSeNonEsiste() throws Exception {
        when(request.getParameter("prodotto")).thenReturn("Materasso");
        when(request.getParameter("materiale")).thenReturn("Memory");
        // getSession() senza false ritorna sempre una session (anche nuova)
        when(request.getSession()).thenReturn(session);

        invokeService(new FiltraggioServletMateriale());

        verify(request).getSession();
        verify(session, atLeastOnce()).setAttribute(anyString(), anyString());
    }
}