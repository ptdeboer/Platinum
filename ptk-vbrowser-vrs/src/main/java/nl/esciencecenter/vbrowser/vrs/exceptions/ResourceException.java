package nl.esciencecenter.vbrowser.vrs.exceptions;

/**
 * Super class of all ResourceException. Typically the involve a Resource Path.
 */
public class ResourceException extends VrsException {
    private static final long serialVersionUID = 197118582694653944L;

    public static final String NOT_FOUND = "Resource Not Found.";
    public static final String ACCES_DENIED = "Access Denied";
    public static final String READ_EOF = "EOF during read";
    public static final String READ_IO = "IOException during read";
    public static final String WRITE_IO = "IOException during write";
    public static final String ALREADY_EXISTS = "Resource already exists";
    public static final String NOT_EMPTY = "Resource Not Empty.";
    public static final String RESOURCE_TYPE_MISMATCH = "Resource Type Mismatch.";

    public ResourceException(String message, Throwable cause, String name) {
        super(message, cause, name);
    };

}
