package security.functional.authorization;

import Controller.RegistrazioneServlet;
import Model.ConPool;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di RegistrazioneServlet con H2 in-memory.
 * Verifica il comportamento runtime della Servlet contro un DB reale:
 * - Registrazione con email nuova: INSERT + forward Login.jsp
 * - Registrazione con email duplicata: forward RegisterUser.jsp
 * - Hashing automatico della password nel bean
 * - Privilege escalation prevention (amministratore sempre false)
 * - Tutti i campi salvati correttamente
 */
@DisplayName("Functional - RegistrazioneServlet (Authorization)")
class RegistrazioneServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    // ==================================================================
    // Helper: invoca service() (metodo pubblico di HttpServlet)
    // ==================================================================

    private void invokeService(RegistrazioneServlet servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    // ==================================================================
    // Helper: configura tutti i parametri della richiesta
    // ==================================================================

    private void setParametriRegistrazione(String email, String password) {
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("passwordEmail")).thenReturn(password);
        when(request.getParameter("nome")).thenReturn("Mario");
        when(request.getParameter("cognome")).thenReturn("Rossi");
        when(request.getParameter("datadiNascita")).thenReturn("1990-01-01");
        when(request.getParameter("numeroTelefono")).thenReturn("1234567890");
        when(request.getParameter("codiceFiscale")).thenReturn("ABCDE25F67G160H");
        when(request.getParameter("via")).thenReturn("Via Test 1");
        when(request.getParameter("citta")).thenReturn("Napoli");
        when(request.getParameter("cap")).thenReturn("80100");
        when(request.getParameter("provincia")).thenReturn("NA");
        when(request.getParameter("nazione")).thenReturn("Italia");
    }

    // ==================================================================
    // Registrazione con email nuova
    // ==================================================================

    @Test
    @DisplayName("Email nuova: INSERT nel DB + forward a Login.jsp")
    void testRegistrazioneEmailNuova() throws Exception {
        setParametriRegistrazione("mario@test.com", "password123");
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        // Verifica INSERT
        assertThat(countClienti("mario@test.com")).isEqualTo(1);
        // Verifica forward
        verify(dispatcher).forward(request, response);
        // Verifica che NON sia stato impostato l'attributo di errore
        verify(request, never()).setAttribute(eq("controllo"), any());
    }

    // ==================================================================
    // Registrazione con email duplicata
    // ==================================================================

    @Test
    @DisplayName("Email duplicata: NO INSERT, forward a RegisterUser.jsp con 'Email già presente'")
    void testRegistrazioneEmailDuplicata() throws Exception {
        // Pre-inserisci l'utente
        executeSql(insertCliente("mario@test.com"));

        setParametriRegistrazione("mario@test.com", "password123");
        when(request.getRequestDispatcher("/WEB-INF/results/RegisterUser.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        // Verifica che NON sia stato inserito un secondo utente
        assertThat(countClienti("mario@test.com")).isEqualTo(1);
        // Verifica attributo errore
        verify(request).setAttribute("controllo", "Email già presente");
        // Verifica forward
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Hashing automatico della password
    // ==================================================================

    @Test
    @DisplayName("Password hashata SHA-1 nel DB (grazie al setter del bean)")
    void testPasswordHashata() throws Exception {
        setParametriRegistrazione("mario@test.com", "password123");
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        // La password salvata NON deve essere "password123" in chiaro
        String passwordInDb = queryString(
                "SELECT passwordEmail FROM Cliente WHERE email = 'mario@test.com'");
        assertThat(passwordInDb).isNotEqualTo("password123");
        // Deve essere un hash SHA-1 (40 caratteri esadecimali)
        assertThat(passwordInDb).hasSize(40).matches("[0-9a-f]{40}");
    }

    // ==================================================================
    // Privilege escalation prevention
    // ==================================================================

    @Test
    @DisplayName("Amministratore forzato a false (privilege escalation prevention)")
    void testAmministratoreForzatoFalse() throws Exception {
        setParametriRegistrazione("mario@test.com", "password123");
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        // Anche se un utente tentasse di inviare "amministratore=true", la Servlet lo ignora
        String isAdmin = queryString(
                "SELECT amministratore FROM Cliente WHERE email = 'mario@test.com'");
        assertThat(isAdmin).isEqualToIgnoringCase("false");
    }

    // ==================================================================
    // Integrità dei dati salvati
    // ==================================================================

    @Test
    @DisplayName("Tutti i campi della registrazione sono salvati correttamente")
    void testTuttiCampiSalvati() throws Exception {
        setParametriRegistrazione("mario@test.com", "password123");
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nome, cognome, dataDiNascita, numeroTelefono, codiceFiscale, "
                             + "via, citta, cap, provincia, nazione FROM Cliente WHERE email = ?")) {
            ps.setString(1, "mario@test.com");
            try (ResultSet rs = ps.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("nome")).isEqualTo("Mario");
                assertThat(rs.getString("cognome")).isEqualTo("Rossi");
                assertThat(rs.getString("dataDiNascita")).isEqualTo("1990-01-01");
                assertThat(rs.getString("numeroTelefono")).isEqualTo("1234567890");
                assertThat(rs.getString("codiceFiscale")).isEqualTo("ABCDE25F67G160H");
                assertThat(rs.getString("via")).isEqualTo("Via Test 1");
                assertThat(rs.getString("citta")).isEqualTo("Napoli");
                assertThat(rs.getString("cap")).isEqualTo("80100");
                assertThat(rs.getString("provincia")).isEqualTo("NA");
                assertThat(rs.getString("nazione")).isEqualTo("Italia");
            }
        }
    }

    // ==================================================================
    // End-to-end: registrazione + login
    // ==================================================================

    @Test
    @DisplayName("End-to-end: registrazione + login funzionano in sequenza")
    void testRegistrazionePoiLogin() throws Exception {
        setParametriRegistrazione("mario@test.com", "password123");
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        // Ora verifica che il login funzioni con le stesse credenziali
        // (usando direttamente il DAO, perché il flusso è stato già testato)
        Model.Utente utente = Model.UtenteDAO.doLogin("mario@test.com", "password123");
        assertThat(utente).isNotNull();
        assertThat(utente.getEmail()).isEqualTo("mario@test.com");
    }

    // ==================================================================
    // Helper
    // ==================================================================

    private int countClienti(String email) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM Cliente WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private String queryString(String sql) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private String insertCliente(String email) {
        return "INSERT INTO Cliente (email, passwordEmail, nome, cognome, "
                + "dataDiNascita, numeroTelefono, codiceFiscale, via, citta, cap, "
                + "provincia, nazione, amministratore) VALUES ("
                + "'" + email + "', 'hash', 'Mario', 'Rossi', "
                + "'1990-01-01', '1234567890', 'ABCDE25F67G160H', "
                + "'Via Test 1', 'Napoli', '80100', 'NA', 'Italia', false)";
    }
}