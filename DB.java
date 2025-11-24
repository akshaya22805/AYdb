import java.sql.Connection;
import java.sql.DriverManager;

public class DB {
    public static Connection getConnection() throws Exception {

        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String database = System.getenv("DB_NAME");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        Class.forName("org.postgresql.Driver");

        return DriverManager.getConnection(url, user, pass);
    }
}

