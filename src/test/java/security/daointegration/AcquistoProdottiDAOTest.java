package security.daointegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dell'area OWASP: DAO Integration.
 * Verifica le proprieta' di sicurezza di AcquistoProdottiDAO tramite analisi
 * statica del codice sorgente (non e' possibile testare il DAO senza un DB).
 */
@DisplayName("DAO Integration - AcquistoProdottiDAO")
class AcquistoProdottiDAOTest {

    private static final String SOURCE_PATH = "src/main/java/Model/AcquistoProdottiDAO.java";
    private static final Pattern CONCATENAZIONE_QUERY =
            Pattern.compile("prepareStatement\\([^+)]*\\+[^+)]*\\)");

    private String sourceCode;

    @BeforeEach
    void setUp() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(SOURCE_PATH));
        sourceCode = new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Il DAO usa PreparedStatement in tutti i 3 metodi")
    void testUsaPreparedStatement() {
        int numPrepareStatement = countOccurrences(sourceCode, "con.prepareStatement(");
        assertTrue(numPrepareStatement >= 3,
                "Il DAO dovrebbe usare prepareStatement in tutti i metodi (trovati: " +
                        numPrepareStatement + ")");
    }

    @Test
    @DisplayName("Il DAO non concatena valori nelle query (protezione SQL Injection)")
    void testNessunaConcatenazioneNelleQuery() {
        assertFalse(CONCATENAZIONE_QUERY.matcher(sourceCode).find(),
                "Non deve esserci concatenazione di stringhe dentro prepareStatement()");
    }

    @Test
    @DisplayName("Il DAO usa placeholder '?' nei valori delle query")
    void testPlaceholderNelleQuery() {
        int numSetString = countOccurrences(sourceCode, "ps.setString(");
        int numSetInt = countOccurrences(sourceCode, "ps.setInt(");

        assertTrue(numSetString >= 2, "Dovrebbero esserci almeno 2 ps.setString()");
        assertTrue(numSetInt >= 1, "Dovrebbe esserci almeno 1 ps.setInt()");
    }

    @Test
    @DisplayName("doRetriveAcquistoUtente usa WHERE C.email=? (filtro per utente)")
    void testFiltroPerUtente() {
        assertTrue(sourceCode.contains("WHERE C.email=?"),
                "doRetriveAcquistoUtente deve filtrare per email dell'utente " +
                        "(protezione IDOR: un utente non deve vedere gli acquisti di altri)");
    }

    @Test
    @DisplayName("acquistaProdotto esegue una INSERT con 3 parametri")
    void testAcquistaProdottoInsert() {
        assertTrue(sourceCode.contains("INSERT INTO Acquistare"),
                "acquistaProdotto deve eseguire una INSERT");
        assertTrue(sourceCode.contains("(emailCliente,idProdotto,quantitaAcquistata)"),
                "La INSERT deve specificare le 3 colonne: email, idProdotto, quantita");
    }

    @Test
    @DisplayName("doRetriveAcquisto legge da Prodotto, Acquistare e Cliente (JOIN)")
    void testJoinDelleTreTabelle() {
        assertTrue(sourceCode.contains("Prodotto as P"),
                "La query deve usare la tabella Prodotto");
        assertTrue(sourceCode.contains("Acquistare as A"),
                "La query deve usare la tabella Acquistare");
        assertTrue(sourceCode.contains("Cliente as C"),
                "La query deve usare la tabella Cliente");
    }

    @Test
    @DisplayName("Il DAO importa ProtectionDomain ma non lo usa (code smell)")
    void testImportInutile() {
        assertTrue(sourceCode.contains("import java.security.ProtectionDomain"),
                "Il DAO importa ProtectionDomain: import inutile (code smell). " +
                        "Fix suggerito: rimuovere l'import.");
    }

    @Test
    @DisplayName("doRetriveAcquisto non filtra per utente (documentazione)")
    void testDoRetriveAcquistoSenzaFiltro() {
        assertFalse(sourceCode.contains("doRetriveAcquisto.*WHERE"),
                "doRetriveAcquisto non filtra: dovrebbe essere invocato solo da admin");
    }

    @Test
    @DisplayName("doRetriveAcquistoUtente non valida 'email' null (documentazione)")
    void testDoRetriveAcquistoUtenteSenzaNullCheck() {
        assertFalse(sourceCode.contains("if (email == null)"),
                "doRetriveAcquistoUtente non valida email null: " +
                        "se il chiamante passa null, il metodo restituisce lista vuota silenziosamente");
    }

    @Test
    @DisplayName("Il DAO ha almeno 3 metodi statici")
    void testNumeroMetodiStatici() {
        Method[] methods = Model.AcquistoProdottiDAO.class.getDeclaredMethods();
        long staticMethods = Arrays.stream(methods)
                .filter(m -> java.lang.reflect.Modifier.isStatic(m.getModifiers()))
                .count();

        assertTrue(staticMethods >= 3,
                "Il DAO dovrebbe avere almeno 3 metodi statici (trovati: " + staticMethods + ")");
    }

    @Test
    @DisplayName("Il DAO NON estende HttpServlet (buona pratica)")
    @SuppressWarnings("ConstantValue")
    void testNonEstendeHttpServlet() {
        assertFalse(jakarta.servlet.http.HttpServlet.class.isAssignableFrom(
                        Model.AcquistoProdottiDAO.class),
                "AcquistoProdottiDAO non estende HttpServlet: buona pratica " +
                        "(a differenza di ProdottoDAO e UtenteDAO)");
    }

    @Test
    @DisplayName("Le eccezioni SQLException sono wrappate in RuntimeException (documentazione)")
    void testEccezioniWrappate() {
        int occurrences = countOccurrences(sourceCode, "throw new RuntimeException(e)");
        assertTrue(occurrences >= 3,
                "Il DAO wrappa SQLException in RuntimeException: " +
                        "perde il tipo specifico dell'eccezione");
    }

    @Test
    @DisplayName("Il DAO usa try-with-resources per la Connection")
    void testTryWithResources() {
        assertTrue(sourceCode.contains("try (Connection con = ConPool.getConnection())"),
                "Il DAO dovrebbe usare try-with-resources per gestire la chiusura della connessione");
    }

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