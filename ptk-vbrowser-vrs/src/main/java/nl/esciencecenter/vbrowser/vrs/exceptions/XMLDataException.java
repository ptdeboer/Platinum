package nl.esciencecenter.vbrowser.vrs.exceptions;

public class XMLDataException extends VrsException
{
    private static final long serialVersionUID = 71955838431929337L;

    public XMLDataException(String message)
    {
        super(message,null);
    }
    
    public XMLDataException(String message,Throwable t)
    {
        super(message,t); 
    }

    
}
