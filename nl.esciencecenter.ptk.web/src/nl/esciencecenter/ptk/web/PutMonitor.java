package nl.esciencecenter.ptk.web;

/** 
 * Put Monitor which can be used to monitor the progress of a HttpPut call. 
 */
public interface PutMonitor
{
    void bytesWritten(long bytes); 
    
    void putDone(); 
}
