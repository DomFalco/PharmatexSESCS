package security.businesslogic;

import Model.AcquistoProdotti;
import Model.Prodotto;
import Model.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dell'area OWASP: Business Logic.
 * Verifica il comportamento della classe AcquistoProdotti: wrapper di
 * Utente + Prodotto per rappresentare un acquisto.
 * NOTA: i test DOCUMENTANO la mancanza di validazione e il problema di
 * mutazione condivisa (i setter modificano gli oggetti originali).
 */
@DisplayName("Business Logic - AcquistoProdotti")
class AcquistoProdottiTest {

    private Utente utente;
    private Prodotto prodotto;
    private AcquistoProdotti acquisto;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario.rossi@example.com");

        prodotto = new Prodotto();
        prodotto.setIdProdotto("MAT001");
        prodotto.setNomeProd("Materasso Memory");
        prodotto.setPrezzo(499.99);

        acquisto = new AcquistoProdotti(utente, prodotto);
    }

    // =================================================================
    // 1. Costruttore
    // =================================================================

    @Test
    @DisplayName("Il costruttore accetta utente e prodotto validi")
    void testCostruttoreAccettaParametriValidi() {
        assertDoesNotThrow(() -> new AcquistoProdotti(utente, prodotto));
    }

    @Test
    @DisplayName("Costruttore con Utente null causa NPE (vulnerabilità documentata)")
    void testCostruttoreUtenteNullCausaNPE() {
        AcquistoProdotti a = new AcquistoProdotti(null, prodotto);

        // Il costruttore accetta utente null senza validazione:
        // l'errore si manifesta solo al primo accesso
        assertThrows(NullPointerException.class, a::getNome,
                "Il costruttore non valida utente null: NPE al primo accesso");
    }

    @Test
    @DisplayName("Costruttore con Prodotto null causa NPE (vulnerabilità documentata)")
    void testCostruttoreProdottoNullCausaNPE() {
        AcquistoProdotti a = new AcquistoProdotti(utente, null);

        assertThrows(NullPointerException.class, a::getIdProdotto,
                "Il costruttore non valida prodotto null: NPE al primo accesso");
    }

    // =================================================================
    // 2. Getter che delegano al Prodotto
    // =================================================================

    @Test
    @DisplayName("getIdProdotto delega al Prodotto")
    void testGetIdProdotto() {
        assertEquals("MAT001", acquisto.getIdProdotto());
    }

    @Test
    @DisplayName("getNomeProd delega al Prodotto")
    void testGetNomeProd() {
        assertEquals("Materasso Memory", acquisto.getNomeProd());
    }

    @Test
    @DisplayName("getPrezzo delega al Prodotto")
    void testGetPrezzo() {
        assertEquals(499.99, acquisto.getPrezzo(), 0.001);
    }

    // =================================================================
    // 3. Getter che delegano all'Utente
    // =================================================================

    @Test
    @DisplayName("getNome delega all'Utente")
    void testGetNome() {
        assertEquals("Mario", acquisto.getNome());
    }

    @Test
    @DisplayName("getCognome delega all'Utente")
    void testGetCognome() {
        assertEquals("Rossi", acquisto.getCognome());
    }

    // =================================================================
    // 4. Quantità acquistata
    // =================================================================

    @Test
    @DisplayName("quantitaAcquistata di default è 0 (non impostata dal costruttore)")
    void testQuantitaDefault() {
        assertEquals(0, acquisto.getQuantitaAcquistata(),
                "Il costruttore non inizializza quantitaAcquistata: default 0");
    }

    @Test
    @DisplayName("setQuantitaAcquistata / getQuantitaAcquistata funzionano")
    void testSetQuantitaAcquistata() {
        acquisto.setQuantitaAcquistata(3);
        assertEquals(3, acquisto.getQuantitaAcquistata());
    }

    // =================================================================
    // 5. Documentazione: MUTAZIONE CONDIVISA (problema critico di design)
    // =================================================================

    @Test
    @DisplayName("setIdProdotto modifica il Prodotto originale (mutazione condivisa)")
    void testSetIdProdottoModificaProdottoOriginale() {
        acquisto.setIdProdotto("MAT999");

        // Il setter modifica direttamente il Prodotto originale,
        // non una copia: side effect pericoloso
        assertEquals("MAT999", prodotto.getIdProdotto(),
                "Il setter modifica il Prodotto originale: mutazione condivisa " +
                        "(bug di design). Fix: clonare gli oggetti nel costruttore");
    }

    @Test
    @DisplayName("setNomeProd modifica il Prodotto originale (mutazione condivisa)")
    void testSetNomeProdModificaProdottoOriginale() {
        acquisto.setNomeProd("Nuovo Nome");

        assertEquals("Nuovo Nome", prodotto.getNomeProd(),
                "Il setter modifica il Prodotto originale: mutazione condivisa");
    }

    @Test
    @DisplayName("setPrezzo modifica il Prodotto originale (mutazione condivisa)")
    void testSetPrezzoModificaProdottoOriginale() {
        acquisto.setPrezzo(1.99);

        assertEquals(1.99, prodotto.getPrezzo(), 0.001,
                "Il setter modifica il prezzo del Prodotto originale: " +
                        "rischio di manipolazione dati (attacco di prezzo)");
    }

    @Test
    @DisplayName("setNome modifica l'Utente originale (mutazione condivisa)")
    void testSetNomeModificaUtenteOriginale() {
        acquisto.setNome("Luigi");

        assertEquals("Luigi", utente.getNome(),
                "Il setter modifica l'Utente originale: mutazione condivisa");
    }

    @Test
    @DisplayName("setCognome modifica l'Utente originale (mutazione condivisa)")
    void testSetCognomeModificaUtenteOriginale() {
        acquisto.setCognome("Verdi");

        assertEquals("Verdi", utente.getCognome(),
                "Il setter modifica l'Utente originale: mutazione condivisa");
    }

    @Test
    @DisplayName("setVia / setCitta modificano l'Utente originale")
    void testSetIndirizzoModificaUtenteOriginale() {
        acquisto.setVia("Via Nuova 1");
        acquisto.setCitta("Napoli");

        assertEquals("Via Nuova 1", utente.getVia());
        assertEquals("Napoli", utente.getCitta());
    }

    // =================================================================
    // 6. Documentazione: mancanza di validazione quantità
    // =================================================================

    @Test
    @DisplayName("Quantità negativa viene accettata (vulnerabilità documentata)")
    void testQuantitaNegativaAccettata() {
        acquisto.setQuantitaAcquistata(-10);

        assertEquals(-10, acquisto.getQuantitaAcquistata(),
                "Il setter non valida la quantità: valori negativi accettati. " +
                        "Fix suggerito: lanciare IllegalArgumentException se < 0");
    }

    // =================================================================
    // 7. Documentazione: mancanza del calcolo totale
    // =================================================================

    @Test
    @DisplayName("Manca il metodo getTotale() (funzionalità di business assente)")
    @SuppressWarnings("JavaReflectionMemberAccess") // <-- Aggiunto per sopprimere il warning
    void testMancanzaCalcoloTotale() {
        acquisto.setQuantitaAcquistata(3);

        // La classe NON ha un metodo per calcolare il totale (prezzo × quantità)
        // Questo è un problema di Business Logic: il totale è una funzione
        // essenziale di un oggetto "acquisto"
        boolean metodoTotaleEsiste = false;
        try {
            //noinspection JavaReflectionMemberAccess
            AcquistoProdotti.class.getMethod("getTotale");
            metodoTotaleEsiste = true;
        } catch (NoSuchMethodException e) {
            // Metodo non trovato
        }

        assertFalse(metodoTotaleEsiste,
                "La classe NON ha getTotale(): il calcolo del totale " +
                        "è assente dalla Business Logic (funzionalità mancante)");
    }
}