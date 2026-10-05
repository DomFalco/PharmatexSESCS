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
 * Verifica le proprieta' di sicurezza di ProdottoDAO tramite analisi statica
 * del codice sorgente (non e' possibile testare il DAO senza un DB configurato).
 * Obiettivi:
 * - Verificare l'uso di PreparedStatement (protezione da SQL Injection)
 * - Verificare l'assenza di concatenazione di stringhe nelle query
 * - Documentare i problemi residui (substring, RuntimeException generiche)
 */
@DisplayName("DAO Integration - ProdottoDAO")
class ProdottoDAOTest {

    private static final String SOURCE_PATH = "src/main/java/Model/ProdottoDAO.java";
    private static final Pattern CONCATENAZIONE_QUERY =
            Pattern.compile("prepareStatement\\([^)]*\\+[^)]*\\)");

    private String sourceCode;

    @BeforeEach
    void setUp() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(SOURCE_PATH));
        sourceCode = new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Il DAO usa PreparedStatement in tutti i metodi")
    void testUsaPreparedStatement() {
        int numPrepareStatement = countOccurrences(sourceCode, "con.prepareStatement(");
        assertTrue(numPrepareStatement >= 12,
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
        int numSetDouble = countOccurrences(sourceCode, "ps.setDouble(");

        assertTrue(numSetString >= 4, "Dovrebbero esserci almeno 4 ps.setString()");
        assertTrue(numSetInt >= 1, "Dovrebbe esserci almeno 1 ps.setInt()");
        assertTrue(numSetDouble >= 3, "Dovrebbero esserci almeno 3 ps.setDouble()");
    }

    @Test
    @DisplayName("Il DAO usa Statement.RETURN_GENERATED_KEYS (documentazione)")
    void testReturnGeneratedKeys() {
        int occurrences = countOccurrences(sourceCode, "Statement.RETURN_GENERATED_KEYS");
        assertTrue(occurrences >= 1,
                "Il DAO usa RETURN_GENERATED_KEYS (non necessario su UPDATE/DELETE)");
    }

    @Test
    @DisplayName("doRetriveByFilter usa substring su 'categoria' (documentazione)")
    void testSubstringInFilter() {
        assertTrue(sourceCode.contains("categoria.substring(0, categoria.length() - 1)"),
                "Il DAO usa substring(0, length-1) su 'categoria': " +
                        "StringIndexOutOfBoundsException se la stringa e' vuota");
    }

    @Test
    @DisplayName("doRetriveBySearch restituisce Prodotto anche se non trova nulla (documentazione)")
    void testSearchRestituisceProdottoVuoto() {
        assertTrue(sourceCode.contains("public static Prodotto doRetriveBySearch"),
                "Verifica l'esistenza del metodo doRetriveBySearch");
    }

    @Test
    @DisplayName("Il DAO ha almeno 12 metodi statici")
    void testNumeroMetodiStatici() {
        Method[] methods = Model.ProdottoDAO.class.getDeclaredMethods();
        long staticMethods = Arrays.stream(methods)
                .filter(m -> java.lang.reflect.Modifier.isStatic(m.getModifiers()))
                .count();

        assertTrue(staticMethods >= 12,
                "Il DAO dovrebbe avere almeno 12 metodi statici (trovati: " + staticMethods + ")");
    }

    @Test
    @DisplayName("Il DAO estende HttpServlet pur non essendo una Servlet (documentazione)")
    @SuppressWarnings("ConstantValue")
    void testEstendeHttpServlet() {
        assertTrue(jakarta.servlet.http.HttpServlet.class.isAssignableFrom(
                        Model.ProdottoDAO.class),
                "Il DAO estende HttpServlet: design smell, dovrebbe essere una classe POJO");
    }

    @Test
    @DisplayName("Le eccezioni SQLException sono wrappate in RuntimeException (documentazione)")
    void testEccezioniWrappate() {
        int occurrences = countOccurrences(sourceCode, "throw new RuntimeException(e)");
        assertTrue(occurrences >= 10,
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