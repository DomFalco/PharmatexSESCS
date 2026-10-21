package security.unit.dataprotection;

import Model.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dell'area OWASP: Data Protection.
 * Verifica il comportamento del metodo setPassword.
 * Aggiornato per verificare l'uso di BCrypt al posto di SHA-1 (Fix CWE-916).
 */
@DisplayName("Data Protection - Hashing password (CWE-916)")
class UtenteTest {

    private Utente utente;

    @BeforeEach
    void setUp() {
        utente = new Utente();
    }

    @Test
    @DisplayName("La password non viene salvata in chiaro")
    void testPasswordNonSalvataInChiaro() {
        String passwordInChiaro = "password123";
        utente.setPassword(passwordInChiaro);

        assertNotNull(utente.getPassword(), "La password hashata non deve essere null");
        assertNotEquals(passwordInChiaro, utente.getPassword(),
                "La password NON deve essere salvata in chiaro");
    }

    @Test
    @DisplayName("L'hash BCrypt ha 60 caratteri e inizia con $2a$")
    void testHashHa60CaratteriBCrypt() {
        utente.setPassword("password123");
        String hash = utente.getPassword();

        assertEquals(60, hash.length(),
                "L'hash BCrypt deve avere esattamente 60 caratteri");
        assertTrue(hash.startsWith("$2a$"),
                "L'hash BCrypt deve iniziare con il prefisso $2a$");
    }

    @Test
    @DisplayName("Password identiche producono hash diversi (presenza di salt)")
    void testStessaPasswordHashDiversi() {
        Utente u1 = new Utente();
        Utente u2 = new Utente();
        u1.setPassword("password123");
        u2.setPassword("password123");

        assertNotEquals(u1.getPassword(), u2.getPassword(),
                "BCrypt usa un salt casuale: password identiche devono produrre hash diversi!");

        assertTrue(u1.checkPassword("password123"));
        assertTrue(u2.checkPassword("password123"));
    }

    @Test
    @DisplayName("Password diverse producono hash diversi")
    void testPasswordDiverseHashDiversi() {
        Utente u1 = new Utente();
        Utente u2 = new Utente();
        u1.setPassword("password123");
        u2.setPassword("password456");

        assertNotEquals(u1.getPassword(), u2.getPassword(),
                "Password diverse devono produrre hash diversi");
    }

    @Test
    @DisplayName("Password vuota produce un hash BCrypt valido e non prevedibile")
    void testPasswordVuotaHashNonPrevedibile() {
        utente.setPassword("");

        assertEquals(60, utente.getPassword().length(),
                "Anche una stringa vuota produce un hash BCrypt di 60 caratteri");

        assertNotEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", utente.getPassword(),
                "L'hash della stringa vuota NON deve essere quello noto di SHA-1");

        assertTrue(utente.checkPassword(""), "La verifica di una password vuota deve funzionare");
    }

    @Test
    @DisplayName("Il metodo checkPassword verifica correttamente la password")
    void testCheckPassword() {
        utente.setPassword("password123");

        assertTrue(utente.checkPassword("password123"),
                "checkPassword deve restituire true per la password corretta");
        assertFalse(utente.checkPassword("passwordErrata"),
                "checkPassword deve restituire false per una password errata");
    }

    @Test
    @DisplayName("Il metodo setPassword non lancia eccezioni con input normali")
    void testSetPasswordNonLanciaEccezioni() {
        assertDoesNotThrow(() -> utente.setPassword("passwordConCaratteri@123!"),
                "setPassword non deve lanciare eccezioni con input validi");
    }

    @Test
    @DisplayName("Il metodo gestisce password con caratteri Unicode (UTF-8)")
    void testPasswordConCaratteriUnicode() {
        String pwdUnicode = "pàsswòrd€123";
        assertDoesNotThrow(() -> utente.setPassword(pwdUnicode),
                "setPassword deve gestire correttamente caratteri UTF-8");

        assertEquals(60, utente.getPassword().length(),
                "Anche con caratteri Unicode l'hash BCrypt ha 60 caratteri");
        assertTrue(utente.checkPassword(pwdUnicode),
                "checkPassword deve funzionare anche con caratteri Unicode");
    }
}