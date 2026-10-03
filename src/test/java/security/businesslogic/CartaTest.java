package security.businesslogic;

import Model.Carta;
import Model.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dell'area OWASP: Business Logic / Data Protection.
 * Verifica il comportamento della classe Carta: gestione dati carta di credito.
 * NOTA: i test DOCUMENTANO la mancanza di validazione, il salvataggio in chiaro
 * di CVV e numero carta (violazione PCI DSS), e la mutazione condivisa con Utente.
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
    @DisplayName("setNomeIntestario / getNomeIntestario funzionano")
    void testNomeIntestario() {
        carta.setNomeIntestario("Mario Rossi");
        assertEquals("Mario Rossi", carta.getNomeIntestario());
    }

    @Test
    @DisplayName("setDataScadenza / getDataScadenza funzionano")
    void testDataScadenza() {
        carta.setDataScadenza("12/28");
        assertEquals("12/28", carta.getDataScadenza());
    }

    @Test
    @DisplayName("setCVV / getCVV funzionano")
    void testCVV() {
        carta.setCVV("123");
        assertEquals("123", carta.getCVV());
    }

    // =================================================================
    // 3. DOCUMENTAZIONE: CVV salvato in chiaro (violazione PCI DSS)
    // =================================================================

    @Test
    @DisplayName("CVV viene salvato in chiaro (violazione PCI DSS)")
    void testCvvInChiaro() {
        String cvv = "123";
        carta.setCVV(cvv);

        // Il CVV viene memorizzato così com'è, senza cifratura né hashing.
        // PCI DSS vieta di memorizzare il CVV dopo l'autorizzazione.
        assertEquals(cvv, carta.getCVV(),
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

        // Il numero carta è memorizzato in chiaro.
        // PCI DSS richiede cifratura o tokenizzazione.
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
        // La classe non ha un metodo per mostrare il numero carta mascherato
        // (es. "****1234"). Se usato nei log, il numero completo è esposto.
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

        // Il setter accetta qualsiasi stringa: nessuna validazione del formato
        assertEquals("abc", carta.getNumeroCarta(),
                "Il setter non valida il formato del numero carta (Luhn, cifre)");
    }

    @Test
    @DisplayName("Numero carta di lunghezza errata viene accettato")
    void testNumeroCartaLunghezzaErrata() {
        carta.setNumeroCarta("123");  // troppo corto

        assertEquals("123", carta.getNumeroCarta(),
                "Il setter non verifica la lunghezza (16 cifre per Visa/MC)");
    }

    @Test
    @DisplayName("CVV con formato errato viene accettato (vulnerabilità documentata)")
    void testCvvFormatoErrato() {
        carta.setCVV("abcdef");  // il CVV deve essere di 3-4 cifre

        assertEquals("abcdef", carta.getCVV(),
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
        carta.setDataScadenza("01/20");  // scaduta

        // La classe non verifica che la carta sia effettivamente valida
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

        // Il setter modifica direttamente l'oggetto Utente originale:
        // side effect pericoloso se l'Utente è condiviso tra più oggetti
        assertEquals("nuova@email.com", utente.getEmail(),
                "Il setter modifica l'Utente originale: mutazione condivisa. " +
                        "Fix: clonare l'Utente nel costruttore o salvare l'email in un campo proprio");
    }

    // =================================================================
    // 8. DOCUMENTAZIONE: incapsulamento debole (campi package-private)
    // =================================================================

    @Test
    @DisplayName("I campi non sono 'private' (incapsulamento debole documentato)")
    void testCampiNonPrivati() throws Exception {
        // I campi della classe sono dichiarati senza 'private':
        // sono accessibili da qualsiasi classe nello stesso package
        Field numeroCartaField = Carta.class.getDeclaredField("numeroCarta");
        Field cvvField = Carta.class.getDeclaredField("CVV");

        assertFalse(Modifier.isPrivate(numeroCartaField.getModifiers()),
                "Il campo 'numeroCarta' non è private: incapsulamento debole");
        assertFalse(Modifier.isPrivate(cvvField.getModifiers()),
                "Il campo 'CVV' non è private: incapsulamento debole. " +
                        "Un campo così sensibile dovrebbe essere private.");
    }

    // =================================================================
    // 9. Documentazione: typo nel nome del campo
    // =================================================================

    @Test
    @DisplayName("Il campo si chiama 'nomeIntestario' (typo documentato)")
    void testTypoNomeIntestario() {
        // Il campo dovrebbe chiamarsi "nomeIntestatario" (con la 't')
        // ma è "nomeIntestario". Non è un problema funzionale,
        // ma è un'incoerenza di naming (code smell).
        boolean campoConTypoEsiste = false;
        try {
            Carta.class.getDeclaredField("nomeIntestario");
            campoConTypoEsiste = true;
        } catch (NoSuchFieldException e) {
            // Campo non trovato
        }

        assertTrue(campoConTypoEsiste,
                "Il campo si chiama 'nomeIntestario' (typo): dovrebbe essere 'nomeIntestatario'");
    }
}