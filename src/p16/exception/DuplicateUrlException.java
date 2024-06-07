package p16.exception;

public class DuplicateUrlException extends Exception {

    public DuplicateUrlException(String message) {
        super(message);
    }

    public DuplicateUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}

