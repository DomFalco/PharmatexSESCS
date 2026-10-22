package security.functional.authorization;

import controller.RegistrazioneServlet;
import model.ConPool;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Functional - RegistrazioneServlet (Authorization)")
class RegistrazioneServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(RegistrazioneServlet servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

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

    @Test
    @DisplayName("Email nuova: INSERT nel DB + forward a Login.jsp")
    void testRegistrazioneEmailNuova() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        setParametriRegistrazione("mario@test.com", randomPassword);
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        assertThat(countClienti("mario@test.com")).isEqualTo(1);
        verify(dispatcher).forward(request, response);
        verify(request, never()).setAttribute(eq("controllo"), any());
    }

    @Test
    @DisplayName("Email duplicata: NO INSERT, forward a RegisterUser.jsp con 'Email già presente'")
    void testRegistrazioneEmailDuplicata() throws Exception {
        executeSql(insertCliente("mario@test.com"));
        String randomPassword = UUID.randomUUID().toString();
        setParametriRegistrazione("mario@test.com", randomPassword);
        when(request.getRequestDispatcher("/WEB-INF/results/RegisterUser.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        assertThat(countClienti("mario@test.com")).isEqualTo(1);
        verify(request).setAttribute("controllo", "Email già presente");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Password hashata BCrypt nel DB (grazie al setter del bean)")
    void testPasswordHashata() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        setParametriRegistrazione("mario@test.com", randomPassword);
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        String passwordInDb = queryString(
                "SELECT passwordEmail FROM Cliente WHERE email = 'mario@test.com'");

        assertThat(passwordInDb)
                .isNotEqualTo(randomPassword)
                .hasSize(60)
                .startsWith("$2a$");
    }

    @Test
    @DisplayName("Amministratore forzato a false (privilege escalation prevention)")
    void testAmministratoreForzatoFalse() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        setParametriRegistrazione("mario@test.com", randomPassword);
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        String isAdmin = queryString(
                "SELECT amministratore FROM Cliente WHERE email = 'mario@test.com'");
        assertThat(isAdmin).isEqualToIgnoringCase("false");
    }

    @Test
    @DisplayName("Tutti i campi della registrazione sono salvati correttamente")
    void testTuttiCampiSalvati() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        setParametriRegistrazione("mario@test.com", randomPassword);
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

    @Test
    @DisplayName("End-to-end: registrazione + login funzionano in sequenza")
    void testRegistrazionePoiLogin() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        setParametriRegistrazione("mario@test.com", randomPassword);
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        invokeService(new RegistrazioneServlet());

        model.Utente utente = model.UtenteDAO.doLogin("mario@test.com", randomPassword);
        assertThat(utente).isNotNull();
        assertThat(utente.getEmail()).isEqualTo("mario@test.com");
    }

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