/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.util.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.UnknownFormatConversionException;
import java.util.Vector;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import nl.esciencecenter.ptk.data.StringHolder;


/**
 * Recording Log Handler which records logging events. 
 */ 
public class RecordingLogHandler extends Handler
{
    public class RecordElement
    { 
        final LogRecord record;
        final long time; 
        
        public RecordElement(long timeInMillies,LogRecord logRecord)
        {
            time=timeInMillies;
            this.record=logRecord; 
        }
        
        public void printTo(PrintStream stream)  
        {
            print(stream,this.record); 
        }
        
        public void format(Formatter formatter)
        {
            RecordingLogHandler.this.format(formatter,this); 
        }
        
    }
    
    // ========================================================================
    //
    // ========================================================================
    
    protected Vector<RecordElement> records=new Vector<RecordElement>();
    
    // private Formatter formatter;
    
    // private StringBuilder stringBuilder; 
    
    private boolean printLogLevel=false;
    
    private boolean printExceptions=false; 
    
    public RecordingLogHandler()
    {
        init(); 
    }
    
    private void init()
    {
        //stringBuilder=new StringBuilder(); 
        // formatter=new Formatter(stringBuilder); 
    }
    
    @Override
    public void close() throws SecurityException
    {
        
    }

    @Override
    public void flush()
    {
        
    }
    
    /** Remove first numRecords from list */  
    public void delete(int numRecords) 
    {
        synchronized(this.records)
        {
            int len=records.size(); 
            // move: 
            for (int i=numRecords;i<len;i++)
                records.set(i-numRecords,records.get(i));
            // truncate: 
            records.setSize(numRecords); 
        }
    }
    
    /** Return number of log events currently recorded. */
    public int getLogCount()
    {
        synchronized(this.records)
        {
            return this.records.size();
        }
    }
    
    @Override
    public void publish(LogRecord record)
    {
       // System.err.println("publish:"+record); 
        
        synchronized(records)
        {
            RecordElement el=new RecordElement(System.currentTimeMillis(),record);
            records.add(el);
            //el.printTo(System.err); 
        }
    }

    public void reset()
    {
        init(); 
    }
    
    /** 
     * Returns recorded events since <code>startEventNumber</code> as a single String text into the StringHolder.  
     * Optionally resets recorded log events if clearLogBuffer==true.
     * This method can be used to get the new recorded log records since the last 
     * time this method was called by specifying the startEventNumber. 
     * The most recent event number is returned so this method van be used to get incremental updates. 
     *  
     * @param resetLogBugger - clears log buffer.
     * @param startEventNumber - offset into the LogEvents buffer to start from.   
     * @return - returns current log event number included in  the logtext. 
     *  
     */ 
    public int getLogText(boolean clearLogBuffer,int startEventNumber,StringHolder logText)
    {
        
        synchronized(records)
        {
            StringBuilder stringBuilder=new StringBuilder(); 
            Formatter formatter=new Formatter(stringBuilder);
            
            if (records.size()<=0)
            {
                formatter.close();
                return 0; // empty log 
            }
            
            int len=records.size(); 
            
            // format records 
            for (int i=startEventNumber;i<len;i++)
            {
                format(formatter,records.get(i));  
            }
            
            if (clearLogBuffer==true)
            {
                this.records.clear();
                reset();
            }
            
            logText.value=stringBuilder.toString(); 
            formatter.close();
        }
        
        return records.size(); 
    }

    protected void format(Formatter formatter, RecordElement recordElement)
    {
        LogRecord record=recordElement.record;
        
        Level level = record.getLevel();
        String name=record.getLoggerName(); 
        String format=record.getMessage();
        Object args[]=record.getParameters();
        Throwable t=record.getThrown();

        String lvlStr; 

        // Convert some Level to more appropriate names: 
        if (level==Level.FINE)
            lvlStr="DEBUG"; 
        else if (level==Level.FINER)
            lvlStr="DEBUG2"; 
        else if (level==Level.FINEST)
            lvlStr="DEBUG3"; 
        else if (level==Level.WARNING)
            lvlStr="WARN "; 
        else if (level==Level.SEVERE)
            lvlStr="ERROR"; 
        else 
            lvlStr=level.toString(); 
       
        if (printLogLevel) 
            format=lvlStr+":"+name+":"+format;
        
        try
        {
            formatter.format(format,args);
        }
        catch (UnknownFormatConversionException ex)
        {   // SEVERE: can not rely on logger to log this exception !   
            System.err.println("Failed to parse format:"+format);
            ex.printStackTrace();
        }
        
        if ( (printExceptions) && (t!=null) ) 
        {
           // todo: printout formatted stack trace;      
        }
        
    }

    
    public static void print(PrintStream stream,LogRecord record)
    {
        Level level = record.getLevel();
        String name=record.getLoggerName(); 
        String format=record.getMessage();
        Object args[]=record.getParameters();
        
        String lvlStr; 

        // Convert some Level to more appropriate names: 
        if (level==Level.FINE)
            lvlStr="DEBUG"; 
        else if (level==Level.FINER)
            lvlStr="DEBUG2"; 
        else if (level==Level.FINEST)
            lvlStr="DEBUG3"; 
        else if (level==Level.WARNING)
            lvlStr="WARN "; 
        else if (level==Level.SEVERE)
            lvlStr="ERROR"; 
        else 
            lvlStr=level.toString(); 
        
        // has message ?  
        if (format!=null)
            stream.printf(lvlStr+":"+name+":"+format,args);
        
        /// print stacktrace of Exception 
        Throwable t=record.getThrown();
        if (t!=null)
        {
            stream.println("--- StackTrace ---"); 
            t.printStackTrace(stream);
        }
    }
    
    /**
     * Return INTERNAL Vector containing RecordElements. To get a copy of the 
     * actual LogRecords use getRecordArray().   
     *  
     * @return
     */
    public Vector<RecordElement> getRecordElements()
    {
        return this.records; 
    }
    
    /**
     * Returns Copy of RecordElements recorded up to now or since the 
     * latest reset() call.  
     */
    public ArrayList<RecordElement> getRecordsArray()
    {
        int len=this.records.size(); 
        ArrayList<RecordElement> recArray=new ArrayList<RecordElement>(len); 
        for (int i=0;i<len;i++)
            recArray.set(i,records.get(i)); 
            
        return recArray;  
    }
    
}
