package service.books;

import database.DbConnection;
import model.Book;
import service.exceptions.BookAlreadyExistsException;
import service.exceptions.InexistentBookException;
import service.exceptions.InsufficientStockException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class DbInventoryService implements InventoryService {

    private Connection connection;

    public DbInventoryService(String url, String user, String password) {
        try {
            this.connection = DbConnection.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Error aquiring connection!");
            System.exit(0);
        }
    }

    private static Book extract(ResultSet resultSet) throws SQLException {
        String isbn = resultSet.getString("isbn");
        String title = resultSet.getString("title");
        String authors = resultSet.getString("authors");
        double price = resultSet.getDouble("price");
        int stock = resultSet.getInt("stock");

        return new Book(isbn, title, Arrays.asList(authors.split(",")), price, stock);
    }

    @Override
    public void add(String isbn, String title, String authors, double price, int stock) throws BookAlreadyExistsException {

        try {
            if (exists(title)) {
                throw new BookAlreadyExistsException(isbn);
            }

            PreparedStatement statement = connection.prepareStatement("INSERT INTO books(isbn,title,authors,price,stock) VALUES (?,?,?,?,?)");
            statement.setString(1, isbn);
            statement.setString(2, title);
            statement.setString(3, authors);
            statement.setDouble(4, price);
            statement.setInt(5, stock);

            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.out.println("DB ERROR: " + e.getMessage());
        }
    }

    @Override
    public void add(Book book) throws BookAlreadyExistsException {

    }

    @Override
    public void remove(String title) throws InexistentBookException {

        try {
            if (!exists(title)) {
                throw new InexistentBookException("Book does not exist.");
            }

            PreparedStatement statement = connection.prepareStatement("DELETE FROM books WHERE title=?");
            statement.setString(1, title);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.out.println("DB ERROR: " + e.getMessage());
        }
    }

    @Override
    public Book searchByTitle(String title) throws InexistentBookException {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM books WHERE title=?");
            statement.setString(1, title);
            ResultSet resultSet = statement.executeQuery();

            //title is unique, we have only one result
            if (!resultSet.next()) {
                throw new InexistentBookException("No book!");
            }
            Book book = extract(resultSet);
            statement.close();
            return book;

        } catch (SQLException e) {
            System.out.println("DB ERROR: " + e.getMessage());
            return null;
        }

    }

    @Override
    public Book searchByIsbn(String isbn) throws InexistentBookException {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM books WHERE isbn=?");
            statement.setString(1, isbn);
            ResultSet resultSet = statement.executeQuery();

            //title is unique, we have only one result
            if (!resultSet.next()) {
                throw new InexistentBookException("No book!");
            }

            Book book = extract(resultSet);
            statement.close();
            return book;
        } catch (SQLException e) {
            System.out.println("DB ERROR: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void updatePrice(String title, double price) throws InexistentBookException {
        try {
            if (!exists(title)) {
                throw new InexistentBookException("No book");
            }

            PreparedStatement statement = connection.prepareStatement("UPDATE books SET price=? WHERE title=?");
            statement.setDouble(1, price);
            statement.setString(2, title);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.out.println("DB ERROR: " + e.getMessage());
        }
    }

    @Override
    public void updateStock(String title, int quantity) throws InexistentBookException, InsufficientStockException {
        try {
            Book book = searchByTitle(title);
            book.checkStock(quantity);

            PreparedStatement statement = connection.prepareStatement("UPDATE books SET stock=? WHERE title=?");
            statement.setDouble(1, quantity);
            statement.setString(2, title);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.out.println("DB ERROR: " + e.getMessage());
        }
    }

    @Override
    public void displayAll() {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM books ORDER BY title");
            ResultSet resultSet = statement.executeQuery();

            boolean empty = true;
            while (resultSet.next()) {
                empty = false;
                System.out.println(extract(resultSet));
            }
            statement.close();
            if (empty) {
                System.out.println("No books in db!");
            }

        } catch (SQLException e) {
            System.out.println("DB ERROR: " + e.getMessage());
        }
    }

    //true if exists, false otherwise
    private boolean exists(String title) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM books WHERE title=?");
        statement.setString(1, title);
        ResultSet resultSet = statement.executeQuery();

        boolean exists = resultSet.next();
        statement.close();
        return exists;

    }

}
