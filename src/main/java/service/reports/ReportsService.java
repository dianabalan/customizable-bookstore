package service.reports;

import com.opencsv.CSVWriter;
import database.DbConnection;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ReportsService {

    private Connection connection;

    public ReportsService(String url, String user, String pass) {
        try {
            this.connection = DbConnection.getConnection(url, user, pass);
        } catch (SQLException e) {
            System.out.println("error getting connection");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        ReportsService service = new ReportsService("jdbc:mysql://localhost:3306/bookstore", "root", "1234");
        service.generateAlphabeticalBooksReport();
    }

    public void generateAlphabeticalBooksReport() {
        try {
            Statement statement = this.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM books ORDER BY title ASC");

            Writer writer = new FileWriter("src/main/resources/booksAlphabetical.csv");
            CSVWriter csvWriter = new CSVWriter(writer, ';', CSVWriter.NO_QUOTE_CHARACTER, '"', "\n");

            csvWriter.writeAll(resultSet, true);
            writer.close();
        } catch (SQLException e) {

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
