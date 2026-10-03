package security.daointegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test dell'area OWASP: DAO Integration.
 * Verifica le proprieta' di sicurezza di CartaDAO tramite analisi statica
 * del codice sorgente (non e' possibile testare il DAO senza un DB).
 * Obiettivi:
 * - Verificare l'uso di PreparedStatement (protezione da SQL Injection)
 * - Documentare l'assenza di WHERE (caricamento di tutti i numeri di carta)
 * - Documentare il salvataggio di CVV in chiaro (violazione PCI DSS)
 */
@DisplayName("DAO Integration - CartaDAO")
class CartaDAOTest {

    private static final String SOURCE_PATH = "src/main/java/Model/CartaDAO.java";
    private String sourceCode;

    @BeforeEach
    void setUp() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(SOURCE_PATH));
        sourceCode = new String(bytes, StandardCharsets.UTF_8);
    }

    // =================================================================
    // TEST DI SICUREZZA: uso di PreparedStatement
    // =================================================================

    @Test
    @DisplayName("Il DAO usa PreparedStatement per SELECT e INSERT")
    void testUsaPreparedStatement() {
        int numPrepareStatement = countOccurrences(sourceCode, "con.prepareStatement(");
        assertTrue(numPrepareStatement >= 2,
                "Il DAO dovrebbe usare prepareStatement in tutti i metodi (trovati: " +
                        numPrepareStatement + ")");
    }

    @Test
    @DisplayName("Il DAO non concatena valori nelle query (protezione SQL Injection)")
    void testNessunaConcatenazioneNelleQuery() {
        assertFalse(sourceCode.matches("(?s).*prepareStatement\\([^)]*\\+[^)]*\\).*"),
                "Non deve esserci concatenazione di stringhe dentro prepareStatement()");
    }

    @Test
    @DisplayName("Il DAO usa placeholder '?' nei valori della INSERT")
    void testPlaceholderNellaInsert() {
        assertTrue(sourceCode.contains("(?,?,?,?,?)"),
                "La INSERT deve usare 5 placeholder per i parametri della carta");
    }

    // =================================================================
    // DOCUMENTAZIONE: problemi gravi di privacy e sicurezza
    // =================================================================

    @Test
    @DisplayName("La SELECT non ha WHERE (vulnerabilita' documentata: carica tutte le carte)")
    void testSelectSenzaWhere() {
        assertTrue(sourceCode.contains("SELECT numeroCarta FROM CartaDiCredito"),
                "La SELECT deve essere 'SELECT numeroCarta FROM CartaDiCredito'");
        assertFalse(sourceCode.contains("SELECT numeroCarta FROM CartaDiCredito WHERE"),
                "La SELECT non ha WHERE: carica TUTTI i numeri di carta in memoria. " +
                        "Fix suggerito: SELECT numeroCarta FROM CartaDiCredito WHERE numeroCarta=?");
    }

    @Test
    @DisplayName("Il CVV viene salvato in chiaro (violazione PCI DSS documentata)")
    void testCvvInChiaro() {
        assertTrue(sourceCode.contains("CVV"),
                "La INSERT include il CVV: violazione PCI DSS (il CVV non deve essere persistito)");
        assertTrue(sourceCode.contains("ps1.setString(4, p.getCVV())"),
                "Il CVV viene salvato tramite setString: memorizzato in chiaro. " +
                        "Violazione PCI DSS: il CVV non deve MAI essere persistito.");
    }

    @Test
    @DisplayName("Il numero carta viene salvato in chiaro (violazione PCI DSS documentata)")
    void testNumeroCartaInChiaro() {
        assertTrue(sourceCode.contains("ps1.setString(1, p.getNumeroCarta())"),
                "Il numero carta viene salvato tramite setString: memorizzato in chiaro. " +
                        "Violazione PCI DSS: richiederebbe cifratura o tokenizzazione.");
    }

    // =================================================================
    // DOCUMENTAZIONE: problemi di efficienza e design
    // =================================================================

    @Test
    @DisplayName("Il check di esistenza carica tutti i numeri carta in una ArrayList (documentazione)")
    void testCheckEsistenzaInMemoria() {
        assertTrue(sourceCode.contains("ArrayList<String> cartaCredito"),
                "Il DAO carica tutti i numeri di carta in memoria: " +
                        "inefficiente e aumenta la superficie di esposizione dati. " +
                        "Fix suggerito: usare SELECT 1 ... WHERE numeroCarta=? LIMIT 1");
    }

    @Test
    @DisplayName("Nessun else se la carta esiste gia' (silent skip documentato)")
    void testNessunElseSeCartaEsiste() {
        assertTrue(sourceCode.contains("if(f==0)"),
                "Il DAO usa 'if(f==0)' per decidere se inserire: se la carta esiste, " +
                        "non fa nulla silenziosamente (il chiamante non riceve feedback)");
        assertFalse(sourceCode.contains("else"),
                "Manca un ramo else: se la carta esiste, il DAO non informa il chiamante");
    }

    @Test
    @DisplayName("Il DAO usa try-with-resources per la Connection")
    void testTryWithResources() {
        assertTrue(sourceCode.contains("try (Connection con = ConPool.getConnection())"),
                "Il DAO dovrebbe usare try-with-resources per gestire la chiusura della connessione");
    }

    // =================================================================
    // TEST: gestione delle eccezioni
    // =================================================================

    @Test
    @DisplayName("Le eccezioni SQLException sono wrappate in RuntimeException (documentazione)")
    void testEccezioniWrappate() {
        int occurrences = countOccurrences(sourceCode, "throw new RuntimeException(e)");
        assertTrue(occurrences >= 1,
                "Il DAO wrappa SQLException in RuntimeException: " +
                        "perde il tipo specifico dell'eccezione");
    }

    // =================================================================
    // TEST DI DESIGN: metodi statici e no HttpServlet
    // =================================================================

    @Test
    @DisplayName("Il DAO ha almeno 1 metodo statico")
    void testNumeroMetodiStatici() {
        Method[] methods = Model.CartaDAO.class.getDeclaredMethods();
        long staticMethods = Arrays.stream(methods)
                .filter(m -> java.lang.reflect.Modifier.isStatic(m.getModifiers()))
                .count();

        assertTrue(staticMethods >= 1,
                "Il DAO dovrebbe avere almeno 1 metodo statico (trovati: " + staticMethods + ")");
    }

    @Test
    @DisplayName("Il DAO NON estende HttpServlet (buona pratica)")
    @SuppressWarnings("ConstantValue")
    void testNonEstendeHttpServlet() {
        assertFalse(jakarta.servlet.http.HttpServlet.class.isAssignableFrom(
                        Model.CartaDAO.class),
                "CartaDAO non estende HttpServlet: buona pratica");
    }

    // =================================================================
    // Helper
    // =================================================================

    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}