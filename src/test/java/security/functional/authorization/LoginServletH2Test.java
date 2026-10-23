package security.functional.authorization;

import controller.LoginServlet;
import model.Utente;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import security.functional.BaseServletH2Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Functional - LoginServlet (Authorization)")
class LoginServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(LoginServlet servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    @Test
    @DisplayName("Login utente normale: imposta 'Utente' in sessione e forwarda a HomePage")
    void testLoginUtenteNormale() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("mario@test.com", randomPassword, false));
        when(request.getParameter("Email")).thenReturn("mario@test.com");
        when(request.getParameter("Password")).thenReturn(randomPassword);
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("HomePage")).thenReturn(dispatcher);

        invokeService(new LoginServlet());

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(session).setAttribute(eq("Utente"), captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("mario@test.com");
        assertThat(captor.getValue().isAmministratore()).isFalse();
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Login admin: imposta 'Amministratore' in sessione e forwarda a HomeServletAmministratore")
    void testLoginAdmin() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("admin@test.com", randomPassword, true));
        when(request.getParameter("Email")).thenReturn("admin@test.com");
        when(request.getParameter("Password")).thenReturn(randomPassword);
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new LoginServlet());

        ArgumentCaptor<Utente> captor = ArgumentCaptor.forClass(Utente.class);
        verify(session).setAttribute(eq("Amministratore"), captor.capture());
        assertThat(captor.getValue().isAmministratore()).isTrue();
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Password errata: imposta 'parametri' con messaggio di errore e forwarda a Login.jsp")
    void testLoginPasswordErrata() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("mario@test.com", randomPassword, false));
        when(request.getParameter("Email")).thenReturn("mario@test.com");
        when(request.getParameter("Password")).thenReturn("wrongpassword");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new LoginServlet());

        verify(request).setAttribute("parametri", "Email o password errati!");
        // FIX: Ora la Servlet usa forward (più sicuro) invece di include
        verify(dispatcher).forward(request, response);
        verify(session, never()).setAttribute(eq("Utente"), any());
        verify(session, never()).setAttribute(eq("Amministratore"), any());
    }

    @Test
    @DisplayName("Email inesistente: imposta 'parametri' con messaggio di errore e forwarda a Login.jsp")
    void testLoginEmailInesistente() throws Exception {
        when(request.getParameter("Email")).thenReturn("nessuno@test.com");
        when(request.getParameter("Password")).thenReturn("qualsiasi");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new LoginServlet());

        verify(request).setAttribute("parametri", "Email o password errati!");
        // FIX: Ora la Servlet usa forward (più sicuro) invece di include
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Logout: invalida la sessione e forwarda a HomePage")
    void testLogout() throws Exception {
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getRequestDispatcher("HomePage")).thenReturn(dispatcher);

        invokeService(new LoginServlet());

        verify(session).invalidate();
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("SQL Injection su email: il login fallisce, non imposta sessioni")
    void testSqlInjectionEmail() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("admin@test.com", randomPassword, true));
        when(request.getParameter("Email")).thenReturn("admin@test.com' OR '1'='1");
        when(request.getParameter("Password")).thenReturn("qualsiasi");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new LoginServlet());

        verify(session, never()).setAttribute(eq("Utente"), any());
        verify(session, never()).setAttribute(eq("Amministratore"), any());
        verify(request).setAttribute("parametri", "Email o password errati!");
    }

    @Test
    @DisplayName("SQL Injection su password: il login fallisce, non imposta sessioni")
    void testSqlInjectionPassword() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("admin@test.com", randomPassword, true));
        when(request.getParameter("Email")).thenReturn("admin@test.com");
        when(request.getParameter("Password")).thenReturn("' OR '1'='1");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new LoginServlet());

        verify(session, never()).setAttribute(eq("Amministratore"), any());
        verify(request).setAttribute("parametri", "Email o password errati!");
    }

    private String insertClienteConBcrypt(String email, String passwordInChiaro, boolean admin) {
        String hashed = BCrypt.hashpw(passwordInChiaro, BCrypt.gensalt(12));

        return "INSERT INTO Cliente (email, passwordEmail, nome, cognome, "
                + "dataDiNascita, numeroTelefono, codiceFiscale, via, citta, cap, "
                + "provincia, nazione, amministratore) VALUES ("
                + "'" + email + "', '" + hashed + "', 'Mario', 'Rossi', "
                + "'1990-01-01', '1234567890', 'ABCDE25F67G160H', "
                + "'Via Test 1', 'Napoli', '80100', 'NA', 'Italia', "
                + Boolean.toString(admin) + ")";
    }
}