package service.books;

import com.opencsv.bean.CsvToBeanBuilder;
import model.Book;
import service.exceptions.BookAlreadyExistsException;
import service.exceptions.InexistentBookException;
import service.exceptions.InsufficientStockException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MyInventoryService implements InventoryService {

    private TreeMap<String, Book> books = new TreeMap<>();

    public MyInventoryService() {

        try {
            //two ways to read a resource file

            Reader fr = new FileReader("src/main/resources/books.csv");

            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("books.csv");
            Reader reader = new InputStreamReader(inputStream);

            List<Book> csvBooks =
                    new CsvToBeanBuilder(reader)
                            .withSeparator(';')
                            .withType(Book.class)
                            .build()
                            .parse();

            for (Book book : csvBooks) {
                this.books.put(book.getTitle(), book);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    @Deprecated
    public void add(Book book) throws BookAlreadyExistsException {
        if (books.containsKey(book.getTitle())) {
            throw new BookAlreadyExistsException(book.getIsbn());
        }
        books.put(book.getTitle(), book);
    }

    public void add(String isbn, String title, String authors, double price, int stock) throws BookAlreadyExistsException {
        add(new Book(isbn, title, Arrays.asList(authors.split(",")), price, stock));
    }

    public void remove(String title) throws InexistentBookException {
        if (!books.containsKey(title)) {
            throw new InexistentBookException(String.format("No book with title %s exists in the inventory.", title));
        }
        books.remove(title);
    }

    public Book searchByTitle(String title) throws InexistentBookException {
        Book book = books.get(title);
        if (book == null) {
            throw new InexistentBookException(String.format("No book with title %s exists in the inventory.", title));
        }
        return book;
    }

    public Book searchByIsbn(String isbn) throws InexistentBookException {

        for (Book book : this.books.values()) {
            if (book.getIsbn().equals(isbn)) {
                return book;
            }
        }

        throw new InexistentBookException(String.format("No book with isbn %s exists in the inventory.", isbn));

    }

    public void updatePrice(String title, double price) throws InexistentBookException {
        Book book = books.get(title);
        if (book == null) {
            throw new InexistentBookException(String.format("No book with title %s exists in the inventory.", title));
        }

        book.setPrice(price);
    }

    public void updateStock(String title, int quantity) throws InexistentBookException, InsufficientStockException {
        Book book = books.get(title);
        if (book == null) {
            throw new InexistentBookException(String.format("No book with title %s exists in the inventory.", title));
        }

        book.updateStock(quantity);
    }

    public void displayAll() {
        for (Map.Entry<String, Book> entry : this.books.entrySet()) {
            System.out.println(entry.getValue());
        }
    }

}
