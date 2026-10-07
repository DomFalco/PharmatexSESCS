package Model;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

public class ConPool {

    private static DataSource datasource;

    // ===== Supporto test mode (H2 in-memory) =====
    // Questi campi sono valorizzati SOLO dai test tramite enableTestMode().
    // In produzione restano a false/null e il comportamento è identico a prima.
    private static boolean testMode = false;
    private static String testUrl = null;

    /**
     * Attiva la modalità test: da questo momento getConnection() usa H2 in-memory
     * invece di MySQL. Deve essere chiamato SOLO da classi di test.
     *
     * @param url URL JDBC di H2 (es. "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1")
     */
    public static void enableTestMode(String url) {
        testMode = true;
        testUrl = url;
        datasource = null; // forza re-inizializzazione al prossimo getConnection()
    }

    /**
     * Disattiva la modalità test: getConnection() torna a usare MySQL.
     * Utile in @AfterAll per lasciare il sistema in stato pulito.
     */
    public static void disableTestMode() {
        testMode = false;
        testUrl = null;
        datasource = null; // forza re-inizializzazione al prossimo getConnection()
    }

    public static Connection getConnection() throws SQLException {
        if (datasource == null) {
            PoolProperties p = new PoolProperties();

            if (testMode) {
                // ===== Configurazione H2 per i test =====
                p.setUrl(testUrl);
                p.setDriverClassName("org.h2.Driver");
                p.setUsername("sa");
                p.setPassword("");
            } else {
                // ===== Configurazione MySQL per la produzione =====
                String host = System.getenv("MYSQL_HOST");
                String port = System.getenv("MYSQL_PORT");
                String database = System.getenv("MYSQL_DATABASE");
                String user = System.getenv("MYSQL_USER");
                String password = System.getenv("MYSQL_PASSWORD");

                // Fallback per sviluppo locale (opzionale)
                if (host == null) host = "localhost";
                if (port == null) port = "3306";
                if (database == null) database = "ecommerce";
                if (user == null) user = "root";
                if (password == null) {
                    throw new IllegalStateException("MYSQL_PASSWORD environment variable not set.");
                }

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                        + "?serverTimezone=" + TimeZone.getDefault().getID();

                p.setUrl(url);
                p.setDriverClassName("com.mysql.cj.jdbc.Driver");
                p.setUsername(user);
                p.setPassword(password);
            }

            p.setMaxActive(100);
            p.setInitialSize(10);
            p.setMinIdle(10);
            p.setRemoveAbandonedTimeout(60);
            p.setRemoveAbandoned(true);

            datasource = new DataSource();
            datasource.setPoolProperties(p);
        }
        return datasource.getConnection();
    }
}