package nl.esciencecenter.ptk.vbrowser.ui.dnd;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.ArrayList;
import java.util.List;

public class CopyBuffer {

    public class CopyBufferElement {
        final private List<VRL> vrls = new ArrayList<>();
        private final boolean copyIsCut;

        public CopyBufferElement(List<VRL> vrls, boolean isCut) {
            this.vrls.addAll(vrls);
            this.copyIsCut = isCut;
        }

        public List<VRL> getVrls() {
            return vrls;
        }

        public boolean isCut() {
            return copyIsCut;
        }

        public boolean isCopy() {
            return !copyIsCut;
        }
    }

    private CopyBufferElement buffer; // could be collection...

    public void store(List<VRL> list, boolean isCut) {
        this.buffer = new CopyBufferElement(list, isCut);
    }

    public CopyBufferElement getFirst() {
        return this.buffer;
    }

    public void clear() {
        this.buffer = null;
    }

    public boolean hasBuffer() {
        return (this.buffer != null);
    }

}
