package security.daofunctional;

import Model.ConPool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe base per i test funzionali dei DAO con H2 in-memory.
 *
 * Responsabilità:
 *   1. Attiva il test mode in ConPool (H2 invece di MySQL).
 *   2. Registra l'alias SHA1 in H2 (equivalente alla funzione SHA1() di MySQL).
 *   3. Esegue lo schema schema-h2.sql all'avvio.
 *   4. Pulisce il DB prima di ogni test (TRUNCATE + RESTART IDENTITY).
 *   5. Fornisce il metodo executeSql(...) per popolare i dati di test.
 *
 * Le sottoclassi devono solo implementare i test; l'infrastruttura è qui.
 */
public abstract class BaseH2Test {

    /**
     * URL JDBC di H2 in-memory con compatibilità MySQL.
     *
     *   MODE=MySQL          → abilita la sintassi MySQL (LIMIT, concat, ecc.)
     *   DB_CLOSE_DELAY=-1   → mantiene il DB in memoria finché la JVM è attiva,
     *                          anche quando tutte le connessioni sono chiuse
     */
    private static final String H2_URL =
            "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1";

    @BeforeAll
    static void setUpDatabase() throws Exception {
        // 1. Attiva la modalità test: ConPool userà H2 da qui in avanti
        ConPool.enableTestMode(H2_URL);

        try (Connection conn = ConPool.getConnection();
             Statement stmt = conn.createStatement()) {

            // 2. Registra l'alias SHA1: ogni chiamata a SHA1(x) in H2 chiamerà
            //    TestFunctions.sha1(x). Necessario per UtenteDAO.doLogin().
            stmt.execute(
                    "CREATE ALIAS IF NOT EXISTS SHA1 FOR \"security.daofunctional.TestFunctions.sha1\""
            );

            // 3. Esegui lo schema H2
            executeSchemaFile(stmt);
        }
    }

    @AfterAll
    static void tearDownDatabase() {
        // Ripristina ConPool allo stato di produzione (MySQL)
        ConPool.disableTestMode();
    }

    /**
     * Pulisce tutte le tabelle prima di ogni test.
     * Ordine: prima le tabelle con FK, poi quelle referenziate.
     * SET REFERENTIAL_INTEGRITY FALSE disabilita temporaneamente i vincoli FK.
     */
    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = ConPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("TRUNCATE TABLE Acquistare RESTART IDENTITY");
            stmt.execute("TRUNCATE TABLE CartaDiCredito RESTART IDENTITY");
            stmt.execute("TRUNCATE TABLE Prodotto RESTART IDENTITY");
            stmt.execute("TRUNCATE TABLE Cliente RESTART IDENTITY");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    /**
     * Helper per i test: esegue un'istruzione SQL arbitraria (di solito INSERT)
     * contro il database H2.
     */
    protected void executeSql(String sql) throws SQLException {
        try (Connection conn = ConPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // ==================================================================
    // Metodi privati di supporto
    // ==================================================================

    /**
     * Legge schema-h2.sql dal classpath, rimuove i commenti (righe che
     * iniziano con --) e esegue ogni statement separato da ";".
     */
    private static void executeSchemaFile(Statement stmt) throws IOException, SQLException {
        String raw = readResource("/schema-h2.sql");
        String cleaned = stripSqlComments(raw);

        for (String statement : cleaned.split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                stmt.execute(trimmed);
            }
        }
    }

    private static String readResource(String path) throws IOException {
        try (InputStream in = BaseH2Test.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Risorsa non trovata nel classpath: " + path);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static String stripSqlComments(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (!line.trim().startsWith("--")) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}