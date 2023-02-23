package model;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import service.exceptions.InsufficientStockException;

import java.util.List;

public class Book {

    @CsvBindByName
    private String isbn;
    //title is unique
    @CsvBindByName
    private String title;

    @CsvBindAndSplitByName(elementType = String.class, splitOn = ",")
    private List<String> authors;
    @CsvBindByName
    private double price;
    @CsvBindByName
    private int stock;

    public Book(){

    }

    public Book(String isbn, String title, List<String> authors, double price, int stock) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.price = price;
        this.stock = stock;
    }


    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void updateStock(int quantity) throws InsufficientStockException {
        checkStock(quantity);

        //new version - don't add, override
        this.stock = quantity;
    }

    public void checkStock(int quantity) throws InsufficientStockException {
        if (this.stock + quantity < 0) {
            throw new InsufficientStockException("Insufficient stock.");
        }
    }

    @Override
    public String toString() {
        return "Book{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", authors=" + authors +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}
