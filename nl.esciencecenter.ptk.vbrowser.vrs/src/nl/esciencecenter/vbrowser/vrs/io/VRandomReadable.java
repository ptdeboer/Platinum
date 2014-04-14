package nl.esciencecenter.vbrowser.vrs.io;

import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

public interface VRandomReadable
{
    
    RandomReadable createRandomReadable() throws VrsException; 

}
