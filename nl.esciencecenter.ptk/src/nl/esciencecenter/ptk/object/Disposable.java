package nl.esciencecenter.ptk.object;

/** 
 * Interface for Objects which must be explicit disposed. 
 */
public interface Disposable
{
    /** 
     * Dispose object. 
     * The Object may not be used anymore after this call.  
     * This method may be called multiple times.  
     * Call this method also in 'finalize()' for example to make sure resources are closed. 
     */
    public void dispose(); 
}
