package security.unit.dataprotection;

import Model.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dell'area OWASP: Data Protection.
 * Verifica il comportamento del metodo {setPassword}
 */
@DisplayName("Data Protection - Hashing password (CWE-916)")
class PasswordHashingTest {

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
    @DisplayName("L'hash SHA-1 ha 40 caratteri esadecimali")
    void testHashHa40CaratteriEsadecimali() {
        utente.setPassword("password123");
        String hash = utente.getPassword();

        assertEquals(40, hash.length(),
                "L'hash SHA-1 deve avere esattamente 40 caratteri");
        assertTrue(hash.matches("[0-9a-f]{40}"),
                "L'hash SHA-1 deve contenere solo caratteri esadecimali [0-9a-f]");
    }

    @Test
    @DisplayName("La stessa password produce sempre lo stesso hash (mancanza di salt)")
    void testStessaPasswordStessoHash() {
        Utente u1 = new Utente();
        Utente u2 = new Utente();
        u1.setPassword("password123");
        u2.setPassword("password123");

        assertEquals(u1.getPassword(), u2.getPassword(),
                "SHA-1 senza salt produce lo stesso hash per password identiche " +
                        "(documentazione della vulnerabilità CWE-916)");
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
    @DisplayName("Password vuota produce l'hash SHA-1 noto di stringa vuota")
    void testPasswordVuotaHashNoto() {
        utente.setPassword("");
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709",
                utente.getPassword(),
                "L'hash SHA-1 della stringa vuota è un valore noto e " +
                        "dimostra che l'algoritmo è prevedibile");
    }

    @Test
    @DisplayName("SHA-1 è insicuro per le password (documentazione vulnerabilità)")
    void testDocumentazioneVulnerabilitaSha1() {
        utente.setPassword("password123");
        String hash = utente.getPassword();
        assertEquals(40, hash.length(),
                "Il progetto usa SHA-1 (vulnerabilità accettata come rischio residuo)");
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
        assertDoesNotThrow(() -> utente.setPassword("pàsswòrd€123"),
                "setPassword deve gestire correttamente caratteri UTF-8");
        assertEquals(40, utente.getPassword().length(),
                "Anche con caratteri Unicode l'hash SHA-1 ha 40 caratteri");
    }
}
