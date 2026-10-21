package security.unit.businesslogic;

import model.Prodotto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dell'area OWASP: Business Logic.
 * Verifica il comportamento della classe Prodotto: getter, setter, toString
 * e documenta la mancanza di validazione sugli invarianti di business.
 */
@DisplayName("Business Logic - Prodotto")
class ProdottoTest {

    private Prodotto prodotto;

    @BeforeEach
    void setUp() {
        prodotto = new Prodotto();
    }

    // =================================================================
    // 1. Test GETTER e SETTER per i campi String
    // =================================================================

    @Test
    @DisplayName("setIdProdotto / getIdProdotto funzionano correttamente")
    void testIdProdotto() {
        prodotto.setIdProdotto("MAT001");
        assertEquals("MAT001", prodotto.getIdProdotto());
    }

    @Test
    @DisplayName("setNomeProd / getNomeProd funzionano correttamente")
    void testNomeProd() {
        prodotto.setNomeProd("Materasso Memory");
        assertEquals("Materasso Memory", prodotto.getNomeProd());
    }

    @Test
    @DisplayName("setDescrizione / getDescrizione funzionano correttamente")
    void testDescrizione() {
        prodotto.setDescrizione("Materasso in memory foam");
        assertEquals("Materasso in memory foam", prodotto.getDescrizione());
    }

    @Test
    @DisplayName("setNomeCategoria / getNomeCategoria funzionano correttamente")
    void testNomeCategoria() {
        prodotto.setNomeCategoria("Materasso");
        assertEquals("Materasso", prodotto.getNomeCategoria());
    }

    @Test
    @DisplayName("setTipoMaterialeMaterasso / getTipologiaMaterasso funzionano")
    void testTipoMaterialeMaterasso() {
        prodotto.setTipoMaterialeMaterasso("Memory");
        assertEquals("Memory", prodotto.getTipologiaMaterasso());
    }

    // =================================================================
    // 2. Test GETTER e SETTER per i campi numerici
    // =================================================================

    @Test
    @DisplayName("setPrezzo / getPrezzo funzionano correttamente")
    void testPrezzo() {
        prodotto.setPrezzo(499.99);
        assertEquals(499.99, prodotto.getPrezzo(), 0.001);
    }

    @Test
    @DisplayName("setQuantita / getQuantita funzionano correttamente")
    void testQuantita() {
        prodotto.setQuantita(10);
        assertEquals(10, prodotto.getQuantita());
    }

    @Test
    @DisplayName("setLarghezza / getLarghezza funzionano correttamente")
    void testLarghezza() {
        prodotto.setLarghezza(90.5);
        assertEquals(90.5, prodotto.getLarghezza(), 0.001);
    }

    @Test
    @DisplayName("setLunghezza / getLunghezza funzionano correttamente")
    void testLunghezza() {
        prodotto.setLunghezza(200.0);
        assertEquals(200.0, prodotto.getLunghezza(), 0.001);
    }

    // =================================================================
    // 3. Documentazione: mancanza di validazione sugli invarianti
    // =================================================================

    @Test
    @DisplayName("Prezzo negativo viene accettato (vulnerabilità documentata)")
    void testPrezzoNegativoAccettato() {
        prodotto.setPrezzo(-100.0);

        assertEquals(-100.0, prodotto.getPrezzo(), 0.001,
                "Il setter non valida il prezzo: valori negativi sono accettati. " +
                        "Fix suggerito: lanciare IllegalArgumentException se prezzo < 0");
    }

    @Test
    @DisplayName("Quantità negativa viene accettata (vulnerabilità documentata)")
    void testQuantitaNegativaAccettata() {
        prodotto.setQuantita(-5);

        assertEquals(-5, prodotto.getQuantita(),
                "Il setter non valida la quantità: valori negativi sono accettati. " +
                        "Fix suggerito: lanciare IllegalArgumentException se quantita < 0");
    }

    @Test
    @DisplayName("Larghezza negativa viene accettata (vulnerabilità documentata)")
    void testLarghezzaNegativaAccettata() {
        prodotto.setLarghezza(-10.0);

        assertEquals(-10.0, prodotto.getLarghezza(), 0.001,
                "Il setter non valida la larghezza: valori negativi sono accettati");
    }

    @Test
    @DisplayName("Lunghezza negativa viene accettata (vulnerabilità documentata)")
    void testLunghezzaNegativaAccettata() {
        prodotto.setLunghezza(-50.0);

        assertEquals(-50.0, prodotto.getLunghezza(), 0.001,
                "Il setter non valida la lunghezza: valori negativi sono accettati");
    }

    @Test
    @DisplayName("Prezzo zero viene accettato (documentazione)")
    void testPrezzoZeroAccettato() {
        prodotto.setPrezzo(0.0);

        assertEquals(0.0, prodotto.getPrezzo(), 0.001,
                "Il setter accetta prezzo zero (gratis): potrebbe essere un problema di business");
    }

    // =================================================================
    // 4. FIX: campo tipoLetto rimosso
    // =================================================================

    @Test
    @DisplayName("Il campo inutilizzato 'tipoLetto' è stato rimosso (fix applicato)")
    void testCampoTipoLettoRimosso() {
        // Il campo 'tipoLetto' era dichiarato ma non aveva getter/setter (inutilizzato).
        // È stato rimosso per pulizia del codice e per risolvere una segnalazione di SonarQube.
        // Verifichiamo tramite reflection che il campo NON esista più.

        boolean campoEsiste = false;
        try {
            Prodotto.class.getDeclaredField("tipoLetto");
            campoEsiste = true;
        } catch (NoSuchFieldException e) {
            // Campo non trovato -> corretto!
        }

        assertFalse(campoEsiste,
                "Il campo inutilizzato 'tipoLetto' dovrebbe essere stato rimosso.");
    }

    // =================================================================
    // 5. Test del metodo toString()
    // =================================================================

    @Test
    @DisplayName("toString() contiene le informazioni del prodotto")
    void testToString() {
        prodotto.setIdProdotto("MAT001");
        prodotto.setNomeProd("Materasso Memory");
        prodotto.setPrezzo(499.99);
        prodotto.setQuantita(10);

        String result = prodotto.toString();

        assertNotNull(result);
        assertTrue(result.contains("MAT001"));
        assertTrue(result.contains("Materasso Memory"));
        assertTrue(result.contains("499.99"));
        assertTrue(result.contains("10"));
    }

    @Test
    @DisplayName("toString() espone tutti i campi (documentazione)")
    void testToStringEsponeTuttiICampi() {
        prodotto.setIdProdotto("MAT001");
        prodotto.setNomeProd("Materasso");
        prodotto.setDescrizione("Descrizione interna");

        String result = prodotto.toString();

        // toString() espone tutti i campi, anche quelli potenzialmente sensibili
        // (es. descrizione interna). Se usato nei log, potrebbe essere un data leak.
        assertTrue(result.contains("Descrizione interna"),
                "toString() espone tutti i campi: attenzione all'uso nei log");
    }

    // =================================================================
    // 6. Test dei valori di default
    // =================================================================

    @Test
    @DisplayName("I valori di default di un nuovo Prodotto sono null / 0")
    void testValoriDefault() {
        Prodotto p = new Prodotto();

        assertNull(p.getIdProdotto());
        assertNull(p.getNomeProd());
        assertNull(p.getDescrizione());
        assertEquals(0.0, p.getPrezzo(), 0.001);
        assertEquals(0, p.getQuantita());
        assertEquals(0.0, p.getLarghezza(), 0.001);
        assertEquals(0.0, p.getLunghezza(), 0.001);
    }
}