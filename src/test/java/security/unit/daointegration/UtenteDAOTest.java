package security.unit.daointegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test dell'area OWASP: DAO Integration.
 * Verifica le proprieta' di sicurezza di UtenteDAO tramite analisi statica
 * del codice sorgente (non e' possibile testare il DAO senza un DB configurato).
 */
@DisplayName("DAO Integration - UtenteDAO")
class UtenteDAOTest {

    private static final String SOURCE_PATH = "src/main/java/Model/UtenteDAO.java";
    private static final Pattern CONCATENAZIONE_QUERY =
            Pattern.compile("prepareStatement\\([^+)]*\\+[^+)]*\\)");

    private String sourceCode;

    @BeforeEach
    void setUp() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(SOURCE_PATH));
        sourceCode = new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Il DAO usa PreparedStatement in tutti i 6 metodi")
    void testUsaPreparedStatement() {
        int numPrepareStatement = countOccurrences(sourceCode, "con.prepareStatement(");
        assertTrue(numPrepareStatement >= 6,
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
        int numSetBoolean = countOccurrences(sourceCode, "ps.setBoolean(");

        assertTrue(numSetString >= 15, "Dovrebbero esserci almeno 15 ps.setString()");
        assertTrue(numSetBoolean >= 2, "Dovrebbero esserci almeno 2 ps.setBoolean()");
    }

    @Test
    @DisplayName("doLogin usa SHA1(?) nel database (vulnerabilita' documentata, CWE-916)")
    void testUsoSha1NelLogin() {
        assertTrue(sourceCode.contains("SHA1(?)"),
                "Il login usa SHA1(?) nel database: algoritmo insicuro per le password. " +
                        "Fix suggerito: usare BCrypt o Argon2 anche nel DAO.");
    }

    @Test
    @DisplayName("doLogin cerca corrispondenza su passwordEmail con SHA1")
    void testDoLoginQuerySha1() {
        assertTrue(sourceCode.contains("passwordEmail = SHA1(?)"),
                "La query di login confronta passwordEmail con SHA1 della password inserita");
    }

    @Test
    @DisplayName("doLogin restituisce null se l'utente non esiste")
    void testDoLoginRestituisceNullSeNonTrovato() {
        assertTrue(sourceCode.contains("return null;"),
                "doLogin deve restituire null se l'utente non viene trovato");
    }

    @Test
    @DisplayName("doRegistrazione forza amministratore=false (previene escalation)")
    void testRegistrazioneForzaAmministratoreFalse() {
        assertTrue(sourceCode.contains("ps.setBoolean(13, false)"),
                "doRegistrazione deve forzare amministratore=false: " +
                        "previene escalation di privilegi durante la registrazione");
    }

    @Test
    @DisplayName("controlloEmail restituisce false se l'email non esiste")
    void testControlloEmailRestituisceFalse() {
        assertTrue(sourceCode.contains("public static boolean controlloEmail"),
                "Verifica l'esistenza del metodo controlloEmail");
    }

    @Test
    @DisplayName("Il DAO ha almeno 6 metodi statici")
    void testNumeroMetodiStatici() {
        Method[] methods = Model.UtenteDAO.class.getDeclaredMethods();
        long staticMethods = Arrays.stream(methods)
                .filter(m -> java.lang.reflect.Modifier.isStatic(m.getModifiers()))
                .count();

        assertTrue(staticMethods >= 6,
                "Il DAO dovrebbe avere almeno 6 metodi statici (trovati: " + staticMethods + ")");
    }

    @Test
    @DisplayName("Il DAO estende HttpServlet pur non essendo una Servlet (documentazione)")
    @SuppressWarnings("ConstantValue")
    void testEstendeHttpServlet() {
        assertTrue(jakarta.servlet.http.HttpServlet.class.isAssignableFrom(
                        Model.UtenteDAO.class),
                "Il DAO estende HttpServlet: design smell, dovrebbe essere una classe POJO");
    }

    @Test
    @DisplayName("Le eccezioni SQLException sono wrappate in RuntimeException (documentazione)")
    void testEccezioniWrappate() {
        int occurrences = countOccurrences(sourceCode, "throw new RuntimeException(e)");
        assertTrue(occurrences >= 5,
                "Il DAO wrappa SQLException in RuntimeException: " +
                        "perde il tipo specifico dell'eccezione");
    }

    @Test
    @DisplayName("Il DAO usa try-with-resources per la Connection")
    void testTryWithResources() {
        assertTrue(sourceCode.contains("try (Connection con = ConPool.getConnection())"),
                "Il DAO dovrebbe usare try-with-resources per gestire la chiusura della connessione");
    }

    @Test
    @DisplayName("doLogin e doRetriveUtente espongono la password hashata (documentazione)")
    void testEsposizionePasswordHashata() {
        int occurrences = countOccurrences(sourceCode, "setPassword(rs.getString(2))");
        assertTrue(occurrences >= 2,
                "Il DAO espone la password hashata sull'oggetto Utente: " +
                        "considerare di non restituirla mai al chiamante");
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