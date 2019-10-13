package nl.esciencecenter.vbrowser.vrs;

import java.io.IOException;

public interface VCloseable {

    boolean close() throws IOException;

}
