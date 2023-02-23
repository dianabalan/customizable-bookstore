package service.exceptions;

public class BookAlreadyExistsException extends Exception {

    private String isbn;

    public BookAlreadyExistsException(String isbn) {
        super(String.format("Book with isbn %s already exists.",isbn));
        this.isbn = isbn;
    }

    public String getIsbn(){
        return isbn;
    }
}
