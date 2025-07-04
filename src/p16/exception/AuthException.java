package p16.exception;

public class AuthException extends Exception{
    public AuthException(String message) {
        super("Vous n'êtes pas autorisé. " + message);
    }
}
