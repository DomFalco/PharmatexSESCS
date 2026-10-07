package Model;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

public class ConPool {

    private static DataSource datasource;

    // ===== Supporto test mode (H2 in-memory) =====
    private static boolean testMode = false;
    private static String testUrl = null;

    /**
     * Attiva la modalita' test: da questo momento getConnection() usa H2 in-memory
     * invece di MySQL. Deve essere chiamato SOLO da classi di test.
     */
    public static void enableTestMode(String url) {
        testMode = true;
        testUrl = url;
        datasource = null;
    }

    /**
     * Disattiva la modalita' test: getConnection() torna a usare MySQL.
     */
    public static void disableTestMode() {
        testMode = false;
        testUrl = null;
        datasource = null;
    }

    public static Connection getConnection() throws SQLException {
        if (datasource == null) {
            PoolProperties p = new PoolProperties();
            if (testMode) {
                configureForTest(p);
            } else {
                configureForProduction(p);
            }
            applyCommonSettings(p);
            datasource = new DataSource();
            datasource.setPoolProperties(p);
        }
        return datasource.getConnection();
    }

    // ==================================================================
    // Configurazione del pool
    // ==================================================================

    private static void configureForTest(PoolProperties p) {
        p.setUrl(testUrl);
        p.setDriverClassName("org.h2.Driver");
        p.setUsername("sa");
        p.setPassword("");
    }

    private static void configureForProduction(PoolProperties p) {
        String host = getEnvOrDefault("MYSQL_HOST", "localhost");
        String port = getEnvOrDefault("MYSQL_PORT", "3306");
        String database = getEnvOrDefault("MYSQL_DATABASE", "ecommerce");
        String user = getEnvOrDefault("MYSQL_USER", "root");
        String password = System.getenv("MYSQL_PASSWORD");

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

    private static void applyCommonSettings(PoolProperties p) {
        p.setMaxActive(100);
        p.setInitialSize(10);
        p.setMinIdle(10);
        p.setRemoveAbandonedTimeout(60);
        p.setRemoveAbandoned(true);
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null ? value : defaultValue;
    }
}