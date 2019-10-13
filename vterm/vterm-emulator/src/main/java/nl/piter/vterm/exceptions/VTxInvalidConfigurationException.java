/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.exceptions;

public class VTxInvalidConfigurationException extends VTxRuntimeException {

    public VTxInvalidConfigurationException(String message) {
        super(message);
    }

    public VTxInvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public VTxInvalidConfigurationException(Throwable cause) {
        super(cause);
    }
}
