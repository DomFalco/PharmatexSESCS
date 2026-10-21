package security.unit.businesslogic;

import model.Carta;
import model.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dell'area OWASP: Business Logic / Data Protection.
 * Verifica il comportamento della classe Carta: gestione dati carta di credito.
 */
@DisplayName("Business Logic - Carta")
class CartaTest {

    private Utente utente;
    private Carta carta;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setEmail("mario.rossi@example.com");
        carta = new Carta(utente);
    }

    // =================================================================
    // 1. Costruttore e delega all'Utente
    // =================================================================

    @Test
    @DisplayName("Il costruttore accetta un Utente valido")
    void testCostruttoreAccettaUtente() {
        assertDoesNotThrow(() -> new Carta(utente));
    }

    @Test
    @DisplayName("getEmailProprietario delega all'Utente")
    void testGetEmailProprietario() {
        assertEquals("mario.rossi@example.com", carta.getEmailProprietario());
    }

    // =================================================================
    // 2. Getter e Setter dei campi carta
    // =================================================================

    @Test
    @DisplayName("setNumeroCarta / getNumeroCarta funzionano")
    void testNumeroCarta() {
        carta.setNumeroCarta("1234567890123456");
        assertEquals("1234567890123456", carta.getNumeroCarta());
    }

    @Test
    @DisplayName("setNomeIntestatario / getNomeIntestatario funzionano")
    void testNomeIntestatario() {
        carta.setNomeIntestatario("Mario Rossi");
        assertEquals("Mario Rossi", carta.getNomeIntestatario());
    }

    @Test
    @DisplayName("setDataScadenza / getDataScadenza funzionano")
    void testDataScadenza() {
        carta.setDataScadenza("12/28");
        assertEquals("12/28", carta.getDataScadenza());
    }

    @Test
    @DisplayName("setCvv / getCvv funzionano")
    void testCvv() {
        carta.setCvv("123");
        assertEquals("123", carta.getCvv());
    }

    // =================================================================
    // 3. DOCUMENTAZIONE: CVV salvato in chiaro (violazione PCI DSS)
    // =================================================================

    @Test
    @DisplayName("CVV viene salvato in chiaro (violazione PCI DSS)")
    void testCvvInChiaro() {
        String cvv = "123";
        carta.setCvv(cvv);

        assertEquals(cvv, carta.getCvv(),
                "Il CVV è memorizzato in chiaro: VIOLAZIONE PCI DSS. " +
                        "Il CVV non deve MAI essere persistito dopo l'autorizzazione.");
    }

    // =================================================================
    // 4. DOCUMENTAZIONE: numero carta in chiaro
    // =================================================================

    @Test
    @DisplayName("Numero carta viene salvato in chiaro (violazione PCI DSS)")
    void testNumeroCartaInChiaro() {
        String numero = "1234567890123456";
        carta.setNumeroCarta(numero);

        assertEquals(numero, carta.getNumeroCarta(),
                "Il numero carta è memorizzato in chiaro: VIOLAZIONE PCI DSS. " +
                        "Richiede cifratura o tokenizzazione.");
    }

    // =================================================================
    // 5. DOCUMENTAZIONE: manca mascheramento del numero carta
    // =================================================================

    @Test
    @DisplayName("Manca un metodo di mascheramento del numero carta")
    void testMancanzaMascheramentoNumeroCarta() {
        boolean metodoMaskingEsiste = false;
        for (java.lang.reflect.Method m : Carta.class.getDeclaredMethods()) {
            if (m.getName().toLowerCase().contains("mask")) {
                metodoMaskingEsiste = true;
                break;
            }
        }

        assertFalse(metodoMaskingEsiste,
                "Manca un metodo per mascherare il numero carta: " +
                        "rischio di esporre dati nei log/schermate");
    }

    // =================================================================
    // 6. DOCUMENTAZIONE: mancanza di validazione
    // =================================================================

    @Test
    @DisplayName("Numero carta non numerico viene accettato (vulnerabilità documentata)")
    void testNumeroCartaNonNumericoAccettato() {
        carta.setNumeroCarta("abc");

        assertEquals("abc", carta.getNumeroCarta(),
                "Il setter non valida il formato del numero carta (Luhn, cifre)");
    }

    @Test
    @DisplayName("Numero carta di lunghezza errata viene accettato")
    void testNumeroCartaLunghezzaErrata() {
        carta.setNumeroCarta("123");

        assertEquals("123", carta.getNumeroCarta(),
                "Il setter non verifica la lunghezza (16 cifre per Visa/MC)");
    }

    @Test
    @DisplayName("CVV con formato errato viene accettato (vulnerabilità documentata)")
    void testCvvFormatoErrato() {
        carta.setCvv("abcdef");

        assertEquals("abcdef", carta.getCvv(),
                "Il setter non valida il formato del CVV (3-4 cifre)");
    }

    @Test
    @DisplayName("Data di scadenza con formato errato viene accettata")
    void testDataScadenzaFormatoErrato() {
        carta.setDataScadenza("not-a-date");

        assertEquals("not-a-date", carta.getDataScadenza(),
                "Il setter non valida il formato della data di scadenza");
    }

    @Test
    @DisplayName("Data di scadenza nel passato viene accettata")
    void testDataScadenzaPassata() {
        carta.setDataScadenza("01/20");

        assertEquals("01/20", carta.getDataScadenza(),
                "Il setter non verifica che la data di scadenza sia futura");
    }

    // =================================================================
    // 7. DOCUMENTAZIONE: mutazione condivisa con Utente
    // =================================================================

    @Test
    @DisplayName("setEmailProprietario modifica l'Utente originale")
    void testSetEmailModificaUtenteOriginale() {
        carta.setEmailProprietario("nuova@email.com");

        assertEquals("nuova@email.com", utente.getEmail(),
                "Il setter modifica l'Utente originale: mutazione condivisa.");
    }

    // =================================================================
    // 8. FIX: incapsulamento corretto (campi private)
    // =================================================================

    @Test
    @DisplayName("I campi sono 'private' (incapsulamento corretto)")
    void testCampiPrivati() throws Exception {
        Field numeroCartaField = Carta.class.getDeclaredField("numeroCarta");
        Field cvvField = Carta.class.getDeclaredField("cvv");

        assertTrue(Modifier.isPrivate(numeroCartaField.getModifiers()),
                "Il campo 'numeroCarta' deve essere private");
        assertTrue(Modifier.isPrivate(cvvField.getModifiers()),
                "Il campo 'cvv' deve essere private");
    }

    // =================================================================
    // 9. FIX: corretto il typo nel nome del campo
    // =================================================================

    @Test
    @DisplayName("Il campo si chiama 'nomeIntestatario' (typo corretto)")
    void testTypoNomeIntestatarioCorretto() {
        boolean campoConTypoEsiste = false;
        try {
            Carta.class.getDeclaredField("nomeIntestario");
            campoConTypoEsiste = true;
        } catch (NoSuchFieldException e) {
            // Campo non trovato (corretto)
        }

        assertFalse(campoConTypoEsiste, "Il campo non dovrebbe più chiamarsi 'nomeIntestario'");

        boolean campoCorrettoEsiste = false;
        try {
            Carta.class.getDeclaredField("nomeIntestatario");
            campoCorrettoEsiste = true;
        } catch (NoSuchFieldException e) {
            // Campo non trovato (errore)
        }

        assertTrue(campoCorrettoEsiste, "Il campo dovrebbe chiamarsi 'nomeIntestatario'");
    }
}