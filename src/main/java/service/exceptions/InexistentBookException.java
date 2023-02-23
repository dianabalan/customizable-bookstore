package service.exceptions;

public class InexistentBookException extends Exception{
    public InexistentBookException(String message) {
        super(message);
    }
}
