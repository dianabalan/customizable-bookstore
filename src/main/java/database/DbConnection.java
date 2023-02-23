package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    private static Connection connection;

    public static Connection getConnection(String url, String username, String password) throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Acquire database connection successful!");
        }
        return connection;
    }

}
