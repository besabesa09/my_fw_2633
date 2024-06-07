package p16.exception;

public class NoPackageException extends Exception {

    public NoPackageException(String message) {
        super(message);
    }

    public NoPackageException(String message, Throwable cause) {
        super(message, cause);
    }

}

