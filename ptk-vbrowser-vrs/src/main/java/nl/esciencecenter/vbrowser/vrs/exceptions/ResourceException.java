package nl.esciencecenter.vbrowser.vrs.exceptions;

/**
 * Super class of all ResourceException. Typically the involve a Resource Path.
 */
public class ResourceException extends VrsException {

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }

}
