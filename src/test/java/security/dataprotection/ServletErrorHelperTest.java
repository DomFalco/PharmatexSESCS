package security.dataprotection;

import Controller.ServletErrorHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Data Protection.
 * Verifica che ServletErrorHelper gestisca correttamente l'invio di errori HTTP,
 * inclusa la gestione dell'IOException (client disconnesso), evitando
 * l'esposizione di stack trace e garantendo robustezza.
 */
@DisplayName("Data Protection - ServletErrorHelper")
class ServletErrorHelperTest {

    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        response = mock(HttpServletResponse.class);
    }

    @Test
    @DisplayName("sendError invoca response.sendError con i parametri corretti")
    void testSendErrorInvocaResponse() throws Exception {
        ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato");

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato");
    }

    @Test
    @DisplayName("sendError gestisce IOException senza propagarla (client disconnesso)")
    void testSendErrorGestisceIOException() throws Exception {
        doThrow(new IOException("Client disconnesso"))
                .when(response).sendError(anyInt(), anyString());

        // Non deve lanciare eccezioni
        ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Test");

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Test");
    }

    @Test
    @DisplayName("sendError funziona con 403 Forbidden")
    void testSendErrorForbidden() throws Exception {
        ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "msg");

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "msg");
    }

    @Test
    @DisplayName("sendError funziona con 400 Bad Request")
    void testSendErrorBadRequest() throws Exception {
        ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "msg");

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "msg");
    }

    @Test
    @DisplayName("sendError funziona con 404 Not Found")
    void testSendErrorNotFound() throws Exception {
        ServletErrorHelper.sendError(response, HttpServletResponse.SC_NOT_FOUND, "msg");

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "msg");
    }
}