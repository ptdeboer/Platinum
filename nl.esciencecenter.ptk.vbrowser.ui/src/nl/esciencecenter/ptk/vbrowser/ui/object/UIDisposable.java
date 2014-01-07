package nl.esciencecenter.ptk.vbrowser.ui.object;

import nl.esciencecenter.ptk.object.Disposable;

/** 
 * Interface for UI disposable objects. 
 */
public interface UIDisposable extends Disposable 
{
    /**
     * Expose UI object. 
     * Release UI resources. 
     * Object won't be used after this call, but this method might be called more then once. 
     */
    public void dispose();
    
}
