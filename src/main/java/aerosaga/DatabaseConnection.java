
package aerosaga;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input =
                 DatabaseConnection.class.getClassLoader()
                     .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                    "application.properties not found in src/main/resources"
                );
            }

            PROPERTIES.load(input);

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            PROPERTIES.getProperty("spring.datasource.url"),
            PROPERTIES.getProperty("spring.datasource.username"),
            PROPERTIES.getProperty("spring.datasource.password")
        );
    }
}
