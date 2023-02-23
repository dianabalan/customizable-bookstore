package service.books;

import model.Book;
import service.exceptions.BookAlreadyExistsException;
import service.exceptions.InexistentBookException;
import service.exceptions.InsufficientStockException;

public interface InventoryService {

    void add(String isbn, String title, String authors, double price, int stock) throws BookAlreadyExistsException;

    @Deprecated
    void add(Book book) throws BookAlreadyExistsException;

    void updateStock(String title, int quantity) throws InexistentBookException, InsufficientStockException;

    void remove(String title) throws InexistentBookException;

    Book searchByTitle(String title) throws InexistentBookException;

    Book searchByIsbn(String isbn) throws InexistentBookException;

    void updatePrice(String title, double price) throws InexistentBookException;

    void displayAll();
}


