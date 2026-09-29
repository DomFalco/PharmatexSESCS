package Model;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

public class ConPool {
    private static DataSource datasource;

    public static Connection getConnection() throws SQLException {
        if (datasource == null) {
            PoolProperties p = new PoolProperties();

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