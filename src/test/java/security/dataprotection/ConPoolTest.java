package security.dataprotection;

import Model.ConPool;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test dell'area OWASP: Data Protection.
 * Verifica che ConPool.java rispetti le best practice di sicurezza:
 * - Credenziali lette da variabili d'ambiente (fix GitGuardian)
 * - Nessuna password hardcoded (regression test)
 * - Fail-fast se MYSQL_PASSWORD non è impostata
 * NOTA: usa API compatibili con Java 8 (no Files.readString, no Path.of).
 */
@DisplayName("Data Protection - ConPool credenziali DB")
class ConPoolTest {

    private static final String SOURCE_PATH = "src/main/java/Model/ConPool.java";

    @BeforeEach
    @AfterEach
    void resetDatasource() throws Exception {
        Field field = ConPool.class.getDeclaredField("datasource");
        field.setAccessible(true);
        field.set(null, null);
    }

    /**
     * Helper compatibile con Java 8: legge un file come stringa.
     */
    private String readSourceFile() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(SOURCE_PATH));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("ConPool legge MYSQL_PASSWORD da variabile d'ambiente")
    void testLeggeMysqlPasswordDaEnv() throws Exception {
        String source = readSourceFile();
        assertTrue(source.contains("System.getenv(\"MYSQL_PASSWORD\")"),
                "ConPool deve leggere MYSQL_PASSWORD tramite System.getenv()");
    }

    @Test
    @DisplayName("ConPool legge tutte le credenziali da variabili d'ambiente")
    void testLeggeTutteLeCredenzialiDaEnv() throws Exception {
        String source = readSourceFile();
        assertTrue(source.contains("System.getenv(\"MYSQL_HOST\")"), "deve leggere MYSQL_HOST");
        assertTrue(source.contains("System.getenv(\"MYSQL_PORT\")"), "deve leggere MYSQL_PORT");
        assertTrue(source.contains("System.getenv(\"MYSQL_DATABASE\")"), "deve leggere MYSQL_DATABASE");
        assertTrue(source.contains("System.getenv(\"MYSQL_USER\")"), "deve leggere MYSQL_USER");
        assertTrue(source.contains("System.getenv(\"MYSQL_PASSWORD\")"), "deve leggere MYSQL_PASSWORD");
    }

    @Test
    @DisplayName("ConPool non contiene password hardcoded (regression test GitGuardian)")
    void testNessunaPasswordHardcoded() throws Exception {
        String source = readSourceFile();
        assertFalse(source.matches("(?s).*password\\s*=\\s*\"[^\"]+\".*"),
                "ConPool non deve contenere password hardcoded");
        assertFalse(source.contains("password=root"), "Non deve contenere password comuni hardcoded");
        assertFalse(source.contains("password=admin"), "Non deve contenere password comuni hardcoded");
    }

    @Test
    @DisplayName("ConPool applica il fail-fast se MYSQL_PASSWORD non è impostata")
    void testFailFastSenzaPassword() {
        Assumptions.assumeTrue(System.getenv("MYSQL_PASSWORD") == null,
                "Test saltato: MYSQL_PASSWORD è impostata nell'ambiente corrente");

        assertThrows(IllegalStateException.class, ConPool::getConnection,
                "Senza MYSQL_PASSWORD, getConnection deve lanciare IllegalStateException");
    }

    @Test
    @DisplayName("ConPool usa il driver MySQL corretto")
    void testUsaDriverMySQLCorretto() throws Exception {
        String source = readSourceFile();
        assertTrue(source.contains("com.mysql.cj.jdbc.Driver"),
                "ConPool deve usare il driver MySQL 'com.mysql.cj.jdbc.Driver'");
        assertFalse(source.contains("com.mysql.jdbc.Driver"),
                "ConPool non deve usare il vecchio driver deprecato");
    }

    @Test
    @DisplayName("ConPool costruisce correttamente l'URL JDBC")
    void testUrlJdbcCorretto() throws Exception {
        String source = readSourceFile();
        assertTrue(source.contains("jdbc:mysql://"),
                "L'URL JDBC deve iniziare con 'jdbc:mysql://'");
        assertTrue(source.contains("serverTimezone"),
                "L'URL JDBC deve specificare il serverTimezone");
    }

    @Test
    @DisplayName("ConPool configura il pool con parametri di sicurezza")
    void testParametriPoolConfigurati() throws Exception {
        String source = readSourceFile();
        assertTrue(source.contains("setRemoveAbandoned(true)"),
                "ConPool deve abilitare la rimozione delle connessioni abbandonate");
        assertTrue(source.contains("setRemoveAbandonedTimeout"),
                "ConPool deve impostare un timeout per le connessioni abbandonate");
    }
}