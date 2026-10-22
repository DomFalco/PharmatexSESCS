package security.functional.daointegration;

import model.Utente;
import model.UtenteDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import security.functional.BaseH2Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UtenteDAOH2Test extends BaseH2Test {

    @Test
    @DisplayName("doLogin con credenziali corrette restituisce l'utente")
    void testDoLoginCorretto() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("mario@test.com", randomPassword, "Mario", "Rossi", false));

        Utente result = UtenteDAO.doLogin("mario@test.com", randomPassword);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("mario@test.com");
        assertThat(result.getNome()).isEqualTo("Mario");
        assertThat(result.getCognome()).isEqualTo("Rossi");
        assertThat(result.isAmministratore()).isFalse();
    }

    @Test
    @DisplayName("doLogin con password errata restituisce null")
    void testDoLoginPasswordErrata() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("mario@test.com", randomPassword, "Mario", "Rossi", false));

        Utente result = UtenteDAO.doLogin("mario@test.com", "wrongpassword");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("doLogin con email inesistente restituisce null")
    void testDoLoginEmailInesistente() {
        Utente result = UtenteDAO.doLogin("nessuno@test.com", "qualsiasi");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("doLogin con utente admin restituisce amministratore=true")
    void testDoLoginAdmin() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("admin@test.com", randomPassword, "Admin", "Root", true));

        Utente result = UtenteDAO.doLogin("admin@test.com", randomPassword);

        assertThat(result).isNotNull();
        assertThat(result.isAmministratore()).isTrue();
    }

    @Test
    @DisplayName("controlloEmail restituisce true per email esistente")
    void testControlloEmailEsistente() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("mario@test.com", randomPassword, "Mario", "Rossi", false));

        boolean result = UtenteDAO.controlloEmail("mario@test.com");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("controlloEmail restituisce false per email inesistente")
    void testControlloEmailInesistente() {
        boolean result = UtenteDAO.controlloEmail("nessuno@test.com");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("doRetriveUtente restituisce tutti gli utenti presenti")
    void testDoRetriveUtente() throws Exception {
        executeSql(insertClienteConBcrypt("mario@test.com", UUID.randomUUID().toString(), "Mario", "Rossi", false));
        executeSql(insertClienteConBcrypt("luigi@test.com", UUID.randomUUID().toString(), "Luigi", "Verdi", true));

        List<Utente> result = UtenteDAO.doRetriveUtente();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Utente::getEmail)
                .containsExactlyInAnyOrder("mario@test.com", "luigi@test.com");
    }

    @Test
    @DisplayName("doRetriveUtente su DB vuoto restituisce lista vuota")
    void testDoRetriveUtenteVuoto() {
        List<Utente> result = UtenteDAO.doRetriveUtente();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("rendiAmministratore promuove l'utente ad admin")
    void testRendiAmministratore() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("mario@test.com", randomPassword, "Mario", "Rossi", false));

        UtenteDAO.rendiAmministratore("mario@test.com");

        Utente result = UtenteDAO.doLogin("mario@test.com", randomPassword);
        assertThat(result).isNotNull();
        assertThat(result.isAmministratore()).isTrue();
    }

    @Test
    @DisplayName("rimuoviAmministratore degrada l'admin a utente normale")
    void testRimuoviAmministratore() throws Exception {
        String randomPassword = UUID.randomUUID().toString();
        executeSql(insertClienteConBcrypt("admin@test.com", randomPassword, "Admin", "Root", true));

        UtenteDAO.rimuoviAmministratore("admin@test.com");

        Utente result = UtenteDAO.doLogin("admin@test.com", randomPassword);
        assertThat(result).isNotNull();
        assertThat(result.isAmministratore()).isFalse();
    }

    @Test
    @DisplayName("doRegistrazione inserisce un nuovo utente nel DB")
    void testDoRegistrazione() {
        String randomPassword = UUID.randomUUID().toString();
        Utente u = new Utente();
        u.setEmail("nuovo@test.com");
        u.setPassword(randomPassword);
        u.setNome("Nuovo");
        u.setCognome("Utente");
        u.setDataDiNascita("1990-01-01");
        u.setNumeroTelefono("1234567890");
        u.setCodiceFiscale("ABCDE25F67G160H");
        u.setVia("Via Test 1");
        u.setCitta("Napoli");
        u.setCap("80100");
        u.setProvincia("NA");
        u.setNazione("Italia");

        UtenteDAO.doRegistrazione(u);

        assertThat(UtenteDAO.controlloEmail("nuovo@test.com")).isTrue();
        List<Utente> all = UtenteDAO.doRetriveUtente();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getEmail()).isEqualTo("nuovo@test.com");
        assertThat(all.get(0).isAmministratore()).isFalse();
    }

    @Test
    @DisplayName("Registrazione + Login end-to-end funzionano correttamente")
    void testRegistrazionePoiLogin_EndToEnd() {
        String randomPassword = UUID.randomUUID().toString();
        Utente u = new Utente();
        u.setEmail("e2e@test.com");
        u.setPassword(randomPassword);
        u.setNome("E2E");
        u.setCognome("Test");
        u.setDataDiNascita("1990-01-01");
        u.setNumeroTelefono("1234567890");
        u.setCodiceFiscale("ABCDE25F67G160H");
        u.setVia("Via Test 1");
        u.setCitta("Napoli");
        u.setCap("80100");
        u.setProvincia("NA");
        u.setNazione("Italia");

        UtenteDAO.doRegistrazione(u);

        Utente loginResult = UtenteDAO.doLogin("e2e@test.com", randomPassword);

        assertThat(loginResult).isNotNull();
        assertThat(loginResult.getEmail()).isEqualTo("e2e@test.com");
        assertThat(loginResult.getNome()).isEqualTo("E2E");
    }

    @Test
    @DisplayName("SQL Injection su doLogin viene neutralizzata dal PreparedStatement")
    void testSqlInjectionLogin() throws Exception {
        executeSql(insertClienteConBcrypt("admin@test.com", UUID.randomUUID().toString(), "Admin", "Root", true));

        Utente result = UtenteDAO.doLogin("admin@test.com' OR '1'='1", "qualsiasi");

        assertThat(result).isNull();
    }

    private String insertClienteConBcrypt(String email, String passwordInChiaro,
                                          String nome, String cognome, boolean admin) {
        String hashed = BCrypt.hashpw(passwordInChiaro, BCrypt.gensalt(12));
        return "INSERT INTO Cliente (email, passwordEmail, nome, cognome, "
                + "dataDiNascita, numeroTelefono, codiceFiscale, via, citta, cap, "
                + "provincia, nazione, amministratore) VALUES ("
                + "'" + email + "', "
                + "'" + hashed + "', "
                + "'" + nome + "', "
                + "'" + cognome + "', "
                + "'1990-01-01', '1234567890', 'ABCDE25F67G160H', "
                + "'Via Test 1', 'Napoli', '80100', 'NA', 'Italia', "
                + admin
                + ")";
    }
}