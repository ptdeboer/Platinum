package nl.esciencecenter.vbrowser.vrs.exceptions;

public class XMLDataException extends VrsException {

    public XMLDataException(String message) {
        super(message, null);
    }

    public XMLDataException(String message, Throwable t) {
        super(message, t);
    }

}
