package p16.exception;

public class TypeException extends Exception {

    public TypeException(String message){
        super(message);
    }

    public TypeException(String message, Throwable cause) {
        super(message, cause);
    }
}

