package nl.esciencecenter.ptk.exceptions;

public class InternalRuntimeException extends RuntimeException {

    public InternalRuntimeException(String message) {
        super(message);
    }

    public InternalRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
