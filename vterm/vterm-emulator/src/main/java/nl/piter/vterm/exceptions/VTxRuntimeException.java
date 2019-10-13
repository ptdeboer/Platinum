/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.exceptions;

public class VTxRuntimeException extends RuntimeException {

    public VTxRuntimeException(String message) {
        super(message);
    }

    public VTxRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public VTxRuntimeException(Throwable cause) {
        super(cause);
    }

}
