package nl.esciencecenter.vbrowser.vrs.io;

import nl.esciencecenter.ptk.io.RandomWritable;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

public interface VRandomWritable {

    RandomWritable createRandomWritable() throws VrsException;

}
