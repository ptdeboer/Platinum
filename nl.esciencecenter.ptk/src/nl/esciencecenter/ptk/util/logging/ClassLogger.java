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

// Only allow default java imports. Almost all classes refer to this class!
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * java.util.logging.Logger compatible subclass for class level logging.
 * <p>
 */
public class ClassLogger extends FormattingLogger
{
    // ==================
    // Class Fields 
    // ==================
    
    private static Map<String,ClassLogger> classLoggers=new Hashtable<String,ClassLogger>(); 
    
    private static ClassLogger rootLogger=null; 
    
    // ==================
    // Class Methods 
    // ==================

    public static synchronized ClassLogger getLogger(String name) 
    {
        synchronized(classLoggers)
        {
            ClassLogger logger=classLoggers.get(name); 

            if (logger==null)
            {
                logger=new ClassLogger(name);
                classLoggers.put(name,logger);

                // Important: Since loggers are hierarchical, set parent
                // of new logger to the root logger for default log messages.
                logger.setParent(rootLogger);
            }
            
            return logger; 
        }
    }

    public static ClassLogger getLogger(Class<?> clazz, ClassLogger parentLogger)
    {
        ClassLogger logger=getLogger(clazz);
        logger.setParent(parentLogger); 
        return logger; 
    }
    
    public static ClassLogger getLogger(Class<?> clazz)
    {
        return getLogger(clazz.getCanonicalName()); 
    }
    
    static
    {
        // Toplevel Logging! 
        Logger javaRootLogger=Logger.getLogger(""); 
        javaRootLogger.setLevel(Level.SEVERE); 
        
        //java.util.logging.LogManager.getLogManager().getLogger(null).setLevel(ERROR); 
        rootLogger=new ClassLogger(DEFAULT_RESOURCEBUNDLENAME); 
        rootLogger.setLevel(ERROR); 
        
        // Default root handler which prints out messages to STDERR: 
        rootLogger.addHandler(new StderrLogHandler(System.err));
        
        Level lvl=Level.SEVERE;
        
        if (lvl!=null)
            rootLogger.setLevel(lvl);
    }

    public static ClassLogger getRootLogger()
    {
        return rootLogger;
    }
    
    // ==================
    // Instance 
    // ==================
    
    protected ClassLogger(String name, String resourceBundleName)
    {
        super(name, resourceBundleName);
    }
    
    protected ClassLogger(String name)
    {
        // todo: resourcebundle names 
        super(name,null);
    }
    
    // ==========================================================================================
    // Backward compatible methods/legacy  
    // ==========================================================================================

    // Old style object class top string formatter
    private String object2classString(Object obj)
    {
        String source; 
        
        if (obj == null)
        {
            source="[NULL]";  
        }
        else
        {
            if (obj instanceof String)
            {
                // Object is already in string form
                source=(String)obj;
            }
            else
            {
                Class<?> clazz=null; 
                
                if (obj instanceof Class<?>)
                {
                    // Object is class name: is a  call
                    // use classname
                    clazz=(Class<?>)obj; 
                }
                else
                {
                    // instance call from object: get class of object:
                    clazz=obj.getClass(); 
                }
                
                if (clazz.isAnonymousClass())
                {
                    Class<?> supC = clazz.getSuperclass(); 
                    source="[Anon]"+clazz.getEnclosingClass().getSimpleName()+".<? extends "+supC.getSimpleName()+">"; 
                }
                else
                {
                    source=clazz.getSimpleName();
                }
            }
        }
        
        return source; 
    }
    
    /**
     * Warning: this method is a  legacy method which takes the source object as argument. 
     * This method is slower then the recommend logging methods.
     */
    public void logPrintf(Level level, Object obj, String format, Object... args)
    {
        if (this.isLoggable(level)==false) 
            return; 
        
        String source=this.object2classString(obj); 
        log(level,source+":"+format,args);
    }
  
    public void logException(Level level,Object source, Throwable e, String format, Object... args)
    {
        if (this.isLoggable(level)==false)
            return; 

        String srcstr=this.object2classString(source); 
        this.logException(level, e, srcstr+":"+format, args);
    }

    public boolean hasDebugLevel()
    {
        return isLoggable(ClassLogger.DEBUG);
    }
    
}
