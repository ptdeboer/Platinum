package nl.esciencecenter.vbrowser.vrs.exceptions;

public class ValueParseException extends ValueException
{
    private static final long serialVersionUID = -7502543475120242843L;

    public ValueParseException(String message)
    {
        super(message,null);
    }

    public ValueParseException(String message,Throwable t)
    {
        super(message,t); 
    }
    
    
}
