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

package nl.esciencecenter.ptk.presentation;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.JTable;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.object.Duplicatable;

/**
 * Custom Presentation and formatting methods to show values, attributes, etc. 
 * 
 * @author P.T. de Boer
 */
public class Presentation implements Duplicatable<Presentation>
{
    // =======================================================================
    // Static 
    // =======================================================================
	public static enum SortOption
	{
		NEVER(false,false,false),
		DEFAULT(true,false,false), 
		SORT(true,false,false),
		SORT_REVERSE(true,false,true),
		SORT_IGNORE_CASE(true,true,false), 
		SORT_REVERSE_IGNORE_CASE(true,true,true)
		;
		
	    private boolean doSort=true;
	    private boolean ignoreCase=false;
	    private boolean reverseSort=false;  
		
		private SortOption(boolean doSort,boolean ignoreCase,boolean reverseSort)
		{
			this.doSort=doSort; 
			this.ignoreCase=ignoreCase;
			this.reverseSort=reverseSort;
		}
		
		public boolean getIgnoreCase()
		{
			return this.ignoreCase; 
		}
		
		public boolean getDoSort()
		{
			return this.doSort;  
		}
		
		public boolean getAllowSort()
		{
			if (this==NEVER)
				return false;
			
			return doSort; 
		}
		
		public boolean getReverseSort()
		{
		    return this.reverseSort; 
		}
	}
	
    // =======================================================================
    // Static Fields
    // =======================================================================

    protected static Hashtable<String, Presentation> presentationStore = new Hashtable<String, Presentation>();

    /** 
     * Format number to 00-99 format. 
     */
    public static String to2decimals(long val)
    {
        String str = "";

        if (val < 0)
        {
            str = "-";
            val = -val;
        }

        if (val < 10)
            return str + "0" + val;
        else
            return str + val;
    }

    /**
     * Format number to 000-999 format.
     * If the number is bigger the 999 the string will be bigger also
     */
    public static String to3decimals(long val)
    {
        String str = "";

        if (val < 0)
        {
            str = "-";
            val = -val;
        }

        if (val < 10)
            return str = "00" + val;
        else if (val < 100)
            return str + "0" + val;
        else
            return str + val;
    }
    
    /** Format number to [-]0000-9999 format */
    public static String to4decimals(long val)
    {
        String str = "";

        if (val < 0)
        {
            str = "-";
            val = -val;
        }

        if (val < 10)
            return str + "000" + val;
        else if (val < 100)
            return str + "00" + val;
        else if (val < 1000)
            return str + "0" + val;
        else
            return str + val;
    }
    /**
     * Returns time string relative to current time in millis since 'epoch'. If,
     * for example, the date is 'today' it will print 'today hh:mm:ss' if the
     * year is this year, the year will be ommitted.
     * 
     * @param date
     * @return
     */
    public static String relativeTimeString(Date dateTime)
    {
        if (dateTime == null)
            return "?";

        long current = System.currentTimeMillis();
        GregorianCalendar now = new GregorianCalendar();
        now.setTimeInMillis(current);

        // convert to local timezone !
        TimeZone localTZ = now.getTimeZone();
        GregorianCalendar time = new GregorianCalendar();
        time.setTime(dateTime);
        time.setTimeZone(localTZ);

        String tstr = "";

        int y = time.get(GregorianCalendar.YEAR);
        int M = time.get(GregorianCalendar.MONTH);
        int D = time.get(GregorianCalendar.DAY_OF_MONTH);
        int cont = 0;

        if (y != now.get(GregorianCalendar.YEAR))
        {
            tstr = "" + y + " ";
            cont = 1;
        }

        if ((cont == 1) || (M != now.get(GregorianCalendar.MONTH)) || (D != now.get(GregorianCalendar.DAY_OF_MONTH)))
        {
            tstr = tstr + PresentationConst.getMonthNames()[M];

            tstr += " " + to2decimals(D);
        }
        else
        {
            tstr += "today ";
        }

        tstr += " " + to2decimals(time.get(GregorianCalendar.HOUR_OF_DAY)) + ":"
                + to2decimals(time.get(GregorianCalendar.MINUTE)) + ":"
                + to2decimals(time.get(GregorianCalendar.SECOND));

        // add timezone string:
        tstr += " (" + localTZ.getDisplayName(true, TimeZone.SHORT) + ")";
        return tstr;
    }

    /** 
     * @see getPresentationFor(String, String, String, boolean) 
     */
    public static Presentation getPresentation(String key, boolean autoCreate)
    {
        synchronized (presentationStore)
        {
            Presentation pres = presentationStore.get(key);
            if (pres != null)
                return pres;

            if (autoCreate)
            {
                pres = createDefault();
                presentationStore.put(key, pres);
                return pres;
            }
            
            return null; 
        }
    }

    public static Presentation getPresentationForSchemeType(String scheme, String type, Boolean autoCreate)
    {
        return getPresentation(createKey(scheme,null, type), autoCreate);
    }

    public static Presentation getPresentationFor(String scheme, String host, String type, boolean autoCreate)
    {
        return getPresentation(createKey(scheme,host, type), autoCreate);
    }
    
    protected static String createKey(String scheme, String host, String type)
    {
        if (scheme == null)
            scheme = "";
        if (type == null)
            type = "";
        if (host==null)
            host= "";

        return scheme + "-" + host+"-"+type;
    }

    public static void storeSchemeType(String scheme, String type, Presentation pres)
    {
        storePresentation(createKey(scheme, null,type), pres);
    }

    public static void storePresentation(String key, Presentation pres)
    {
        if (pres == null)
            return;

        synchronized (presentationStore)
        {
            presentationStore.put(key, pres);
        }
    }

    /**
     * Returns size in xxx.yyy[KMGTP] format. argument base1024 specifies wether
     * unit base is 1024 or 1000.
     * 
     * @param size
     *            actual size
     * @param base1024
     *            whether to use unit = base 1024 (false is base 1000)
     * @param unitScaleThreshold
     *            at which 1000 unit to show ISO term (1=K,2=M,3=G,etc)
     * @param nrDecimalsBehindPoint
     *            number of decimals behind the point.
     */
    public static String createSizeString(long size, boolean base1024, int unitScaleThreshold, int nrDecimals)
    {
        // boolean negative;
        String prestr = "";

        if (size < 0)
        {
            size = -size;
            // negative = true;
            prestr = "-";
        }

        long base = 1000;

        if (base1024)
            base = 1024;

        String unitstr = "";
        int scale = 0; //

        if (size < base)
        {
            unitstr = "";
            scale = 0;
        }

        scale = (int) Math.floor(Math.log(size) / Math.log(base));

        switch ((int) scale)
        {
            default:
            case 0:
                unitstr = "";
                break;
            case 1:
                unitstr = "K";
                break;
            case 2:
                unitstr = "M";
                break;
            case 3:
                unitstr = "G";
                break;
            case 4:
                unitstr = "T";
                break;
            case 5:
                unitstr = "P";
                break;
        }
        if (base1024 == false)
            unitstr += "i"; // ISO Ki = Real Kilo, Mi=Million Gi = real Giga.

        // 1024^5 fits in long !
        double norm = (double) size / (Math.pow(base, scale));

        // unitScaleThreshold = upto '1'

        if (scale < unitScaleThreshold)
            return "" + size;

        // format to xxx.yyy<UNIT>
        double fracNorm = Math.pow(10, nrDecimals);
        norm = Math.floor((norm * fracNorm)) / fracNorm;

        return prestr + norm + unitstr;
    }

    public static Presentation createDefault()
    {
        return new Presentation(); // return default object;
    }

    /**
     * Size of Strings, at which they are consider to be 'big'. Currentlty this
     * value determines when the AttributeViewer pop-ups.
     * 
     * @return
     */
    public static int getBigStringSize()
    {
        return 42;
    }

    /** 
     * Convert millis since Epoch to Date object. 
     */
    public static Date createDate(long millis)
    {
        if (millis < 0)
            return null;

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(millis);
        return cal.getTime();
    }

    /** 
     * Convert System millies to Date. 
     */
    public static Date now()
    {
        long millis = System.currentTimeMillis();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(millis);
        return cal.getTime();
    }

    /**
     * Create GMT Normalized Date Time String from millis since Epoch.
     * <p>
     * Normalized time = YYYYYY MM DD hh:mm:ss.mss <br>
     * Normalized Timezone is GMT.<br>
     */
    public static String createNormalizedDateTimeString(long millis)
    {
        if (millis < 0)
            return null;

        Date date = createDate(millis);
        return Presentation.createNormalizedDateTimeString(date);
    }

    /**
     * Normalized date time string: "[-]YYYYYY-MM-DD hh:mm:ss.milllis".<br>
     * Normalized Timezone is GMT.<br>
     */
    public static Date createDateFromNormalizedDateTimeString(String value)
    {
        if (value == null)
            return null;

        int yearSign=1; 
        
        if (value.startsWith("-"))
        {
            // Support negative years as in years B.C. 
            // Year -1 = 1 B.C
            // Year 0 = year 0 
            // Year 1 = 1 A.C. 
            value=value.substring(1); 
            yearSign=-1;
        }
        String strs[] = value.split("[ :-]");

        int year = new Integer(strs[0]);
        int month = new Integer(strs[1]) - 1; // January=0!
        int day = new Integer(strs[2]);
        
        int hours =0;
        if (strs.length>3)
        {
            hours=new Integer(strs[3]);
        }
        
        int minutes = 0;
        if (strs.length>4)
        {
            minutes=new Integer(strs[4]);
        }
        
        double secondsD = 0;
        if (strs.length > 5)
            secondsD = new Double(strs[5]);
        
        // String tzStr=null;
        TimeZone timeZone = TimeZone.getTimeZone("GMT");

        // Optional TimeZone! 
        if (strs.length > 6)
        {
            String timeZonestr=strs[6];
            timeZone=TimeZone.getTimeZone(timeZonestr); 
        }

        /*
         * if (strs.length>6) { tzStr=strs[6];
         * 
         * if (tzStr!=null) storedTimeZone=TimeZone.getTimeZone(tzStr); }
         */
        int seconds = (int) Math.floor(secondsD);
        // Warning: millis is in exact 3 digits, but Double create
        // floating point offsets to approximate 3 digit precizion!
        int millis = (int) Math.round(((secondsD - Math.floor(secondsD)) * 1000));

        GregorianCalendar now = new GregorianCalendar();
        // TimeZone localTMZ=now.getTimeZone();

        now.clear();
        // respect timezone:
        now.setTimeZone(timeZone);
        if (yearSign<0)
            now.set(GregorianCalendar.ERA, GregorianCalendar.BC);
        
        now.set(year, month, day, hours, minutes, seconds);
        now.set(GregorianCalendar.MILLISECOND, millis); // be precize!
        // convert timezone back to 'local'
        // now.setTimeZone(localTMZ);

        return now.getTime();
    }

    /**
     * Create normalized Date+time String: [YY]YYYY-DD-MM hh:mm:ss.ms 
     * in GMT TimeZone.
     */
    public static String createNormalizedDateTimeString(Date date)
    {
        return createNormalizedDateTimeString(date,null); 
    }
    
    public static String createNormalizedDateTimeString(Date date,String timeZone)
    {
        if (date==null)
            return null; 
        
        GregorianCalendar gmtTime = new GregorianCalendar();
        gmtTime.setTime(date);
        // normalize to GMT:
        if (timeZone==null)
        {
            gmtTime.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        else
        {
            gmtTime.setTimeZone(TimeZone.getTimeZone(timeZone));
        }

        int yearSign=1; 
        if (gmtTime.get(GregorianCalendar.ERA)== GregorianCalendar.BC)
        {
            yearSign=-1; 
        }
        
        int year = gmtTime.get(GregorianCalendar.YEAR);
        int month = 1 + gmtTime.get(GregorianCalendar.MONTH); // January=0!
        int day = gmtTime.get(GregorianCalendar.DAY_OF_MONTH);
        int hours = gmtTime.get(GregorianCalendar.HOUR_OF_DAY);
        int minutes = gmtTime.get(GregorianCalendar.MINUTE);
        int seconds = gmtTime.get(GregorianCalendar.SECOND);
        int millies = gmtTime.get(GregorianCalendar.MILLISECOND);

        String timeStr= to4decimals(yearSign*year) + "-" + to2decimals(month) + "-" + to2decimals(day) + " " + to2decimals(hours) + ":"
               + to2decimals(minutes) + ":" + to2decimals(seconds) + "." + to3decimals(millies);
        
        if (timeZone!=null)
        {
            timeStr+=" "+timeZone; 
        }
        
        return timeStr; 
    }

    /**
     * Create normalized Date String: [YY]YYYY-DD-MM
     * in GMT TimeZone.
     */
    public static String createNormalizedDateString(Date date)
    {
        if (date==null)
            return null; 
        
        GregorianCalendar gmtTime = new GregorianCalendar();
        gmtTime.setTime(date);
        // normalize to GMT:
        gmtTime.setTimeZone(TimeZone.getTimeZone("GMT"));

        int yearSign=1; 
        if (gmtTime.get(GregorianCalendar.ERA)== GregorianCalendar.BC)
            yearSign=-1; 
        
        int year = gmtTime.get(GregorianCalendar.YEAR);
        int month = 1 + gmtTime.get(GregorianCalendar.MONTH); // January=0!
        int day = gmtTime.get(GregorianCalendar.DAY_OF_MONTH);

        return to4decimals(yearSign*year) + "-" + to2decimals(month) + "-" + to2decimals(day); 
    }
    
    /**
     *  Convert Normalized DateTime string to millis since epoch.  
     */
    public static long createMillisFromNormalizedDateTimeString(String value)
    {
        if (value == null)
            return -1;

        Date date = Presentation.createDateFromNormalizedDateTimeString(value);

        if (date == null)
            return -1;

        return date.getTime();
    }

//    public static Date createDateFromString(String sessionDateString)
//    {
//        return null; 
//    }
    
    // =======================================================================
    // Static Initializer!
    // =======================================================================

    static
    {
        staticInitDefaults();
    }

    private static void staticInitDefaults()
    {
        // Presentation pres=new Presentation();
        // pres.setChildAttributeNames(defaultFileAttributeNames);
        // String key=generateKey(AnyFile.FILE_SCHEME,AnyFile.FILE_TYPE);
        // Presentation.store(key);
    }

    // =============================================================
    // Instance
    // =============================================================

    /**
     * Which unit to skip and and which to start. 
     * For example,2 = skip Kilo byte (1), start at Megabytes (2).
     */
    protected Integer defaultUnitScaleThreshold = 2; 

    /** Numbers of decimals behind point */ 
    protected Integer defaultNrDecimals = 1;
    
    /** KiB/Mib versus KB and MB */ 
    protected Boolean useBase1024 = true;

    protected SortOption sortOption=null; 

    /** Attribute names from child (contents) to show by default. See also UIPresentation */ 
    protected StringList childAttributeNames = null;

    protected StringList sortFields = null;

    /** Parent Presentation object. */
    protected Presentation parent = null; // No hierarchical presentation (yet)

    /** Set this to override platform Locale */  
    protected Locale locale = null; 
    
    // Attribute/Table resize mode 
    protected int columnsAutoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS; // .AUTO_RESIZE_OFF;

    protected Map<String, AttributePresentation> attributePresentations = new Hashtable<String, AttributePresentation>();
    
    protected String iconAttributeName="icon"; 
            
    /** Default Presentation. */ 
    public Presentation()
    {
        initDefaults();
    }

    private void initDefaults()
    {
        this.childAttributeNames=new StringList();
            
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_ICON, 32);
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_NAME, 200);
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_RESOURCETTYPE, 90);
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_SCHEME, 48);
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_HOSTNAME, 120);
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_FILELENGTH, 70);
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_PATH, 200);
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_RESOURCE_STATUS,48);
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_ACCESSTIME, 120);
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_MODIFICATION_TIME, 120);
//        SETATTRIBUTEPREFERREDWIDTH(ATTR_CREATION_TIME, 120);
    }   

    protected void copyFrom(Presentation other)
    {
        defaultUnitScaleThreshold = other.defaultUnitScaleThreshold;
        defaultNrDecimals = other.defaultNrDecimals;
        useBase1024 = other.useBase1024;
        sortOption = other.sortOption;
        
        if (other.childAttributeNames!=null)
        {
            childAttributeNames = other.childAttributeNames.duplicate(); 
        }
        else
        {
            childAttributeNames=null;   
        }
        
        if (other.sortFields!=null)
        {
            sortFields = other.sortFields.duplicate(); 
        }
        else
        {
            sortFields=null;
        }
        
        if (other.parent!=null)
        {
            parent = other.parent.duplicate(); 
        }
        
        locale = other.locale;
        columnsAutoResizeMode = other.columnsAutoResizeMode;
        attributePresentations = new Hashtable<String, AttributePresentation>(other.attributePresentations);
        iconAttributeName = other.iconAttributeName;
    }
    
    /**
     * Get which Child Attribute to show by default. Note that it is the PARENT
     * object which holds the presentation information about the child
     * attributes. For example when opening a Directory in Table view the
     * Presentation of the (parent) directory holds the default file attributes
     * to show.
     */
    public String[] getPreferredChildAttributeNames()
    {
        if (childAttributeNames==null)
            return null; 
        return childAttributeNames.toArray();
    }

    /** 
     * Set which child attribute to show.  
     */
    public void setChildAttributeNames(List<String> names)
    {
        childAttributeNames = new StringList(names); // private copy ! 
    }

    /** 
     * Returns sizeString +"[KMG]&lt;uni&gt;>" from "size" bytes per second 
     */
    public String speedString(long size, String unit)
    {
        return sizeString(size) + unit;
    }

    /**
     * Returns size in xxx.yyy[KMGTP] format (base 1024). Uses settings from
     * Presentation instance.
     * 
     * @see #createSizeString(long, boolean, int, int)
     */
    public String sizeString(long size)
    {
        return sizeString(size, useBase1024, this.defaultUnitScaleThreshold, this.defaultNrDecimals);
    }

    /** @see #createSizeString(long, boolean, int, int) */
    public String sizeString(long size, boolean base1024, int unitScaleThreshold, int nrDecimals)
    {
        return createSizeString(size, base1024, unitScaleThreshold, nrDecimals);
    }

    /**
     * Create Relative Time String: "DD (days) hh:mm:ss.ms" time string" from
     * the specified nr of milli seconds.
     */
    public static String createRelativeTimeString(long timeInMillis, boolean showMillis)
    {
        if (timeInMillis<0)
            return "?"; 
        
        String timestr = "";

        if (timeInMillis > 1000L * 24L * 60L * 60L)
        {
            long days = timeInMillis / (1000L * 24L * 60L * 60L);
            timestr += days + " (days) ";
        }

        if (timeInMillis > 1000 * 60 * 60)
        {
            long hours = (timeInMillis / (1000L * 60 * 60)) % 60;
            timestr += timestr + to2decimals(hours) + ":";
        }
        // show it anyway to always show 00:00s format
        // if (time>1000*60)
        {
            long mins = (timeInMillis / (1000 * 60)) % 60;
            timestr += timestr + to2decimals(mins) + ":";
        }

        long secs = (timeInMillis / 1000L) % 60L;
        timestr += to2decimals(secs) + "s";

        if (showMillis)
            timestr += "." + (timeInMillis % 1000);

        return timestr;
    }

    /**
     * Whether automatic sorting is allowed or that the returned order of this
     * node should be kept as-is.
     */
    public boolean getAllowSort()
    {
        if (sortOption==null)
        		return true; 
        
        return sortOption.getAllowSort();  
    }

    /**
     * Whether the contents should be preferably sorted.
     * Note: If getAllowSort()==false the contents may never be sorted.  
     */
    public boolean getDoSort()
    {
        if (this.sortOption == null)
            return true;
        
        return this.sortOption.getDoSort(); 
    }
    
    /**
     * Set Sort Option. 
     */
    public void setSortOption(SortOption sort)
    {
        this.sortOption=sort; 
    }
    
    /**
     * Get Sort Option. 
     */
    public SortOption getSortOption() 
    {
    	return this.sortOption;  
    }
    
    /** 
     * Whether to ignore case when sorting files.
     */
    public boolean getSortIgnoreCase()
    {
    	if (this.sortOption==null)
    		return false; 
    				
    	return this.sortOption.getIgnoreCase(); 
    }
    
    /** 
     * Whether to reverse the sort order. 
     */
    public boolean getReverseSort()
    {
        if (this.sortOption==null)
            return false; 
                    
        return this.sortOption.getReverseSort(); 
    }
    
    public Locale getLocale()
    {
        if (this.locale != null)
            return this.locale;

        return Locale.getDefault();
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * The first month (Jan) is 0 not 1. If month is null of len is < 3 will
     * return -1
     * 
     * @param month
     *            String of len 3 (Jan,Feb..,etc)
     * @return the 0-based month number;
     */
    public static int getMonthNumber(String month)
    {
        if (month == null || month.length() < 3)
        {
            return -1;
        }

        for (int i = 0; i < PresentationConst.getMonthNames().length; i++)
        {
            if (month.substring(0, 3).compareToIgnoreCase(PresentationConst.getMonthNames()[i]) == 0)
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns optional attribute sort fields, by which this contents should be
     * sorted. If set the attribute names will be used to sort the contents of a
     * resource. If sortFields are NULL, then the default Type+Name sort is
     * used.
     */
    public StringList getSortFields()
    {
        return this.sortFields;
    }

    /**
     * Set optional (Attribute) sort fields. If set the attribute names will be
     * used to sort the contents of a resource. If sortFields are null, then the
     * default Type+Name sort is used.
     */
    public void setSortFields(String[] fields)
    {
        this.sortFields = new StringList(fields);
    }
    
    /** 
     * Method will return null if information hasn't been stored ! 
     */
    public Integer getAttributePreferredWidth(String name)
    {
        AttributePresentation attrPres = this.attributePresentations.get(name);
        if (attrPres == null)
            return null;

        if (attrPres.widths == null)
            return null;

        if (attrPres.widths.preferred < 0)
            return null;

        return new Integer(attrPres.widths.preferred);
    }

    /**
     * Returns Integer[]{<Minimum>,<Preferred>,<Maximum>} Triple. Integer value
     * is NULL is it isn't defined. Method will always return an Integer array
     * of size 3, but actual values may be null.
     * 
     */
    public Integer[] getAttributePreferredWidths(String name)
    {
        Integer vals[] = new Integer[3];

        AttributePresentation attrPres = this.attributePresentations.get(name);
        if (attrPres == null)
            return vals;

        if (attrPres.widths == null)
            return vals;

        if (attrPres.widths.minimum >= 0)
            vals[0] = new Integer(attrPres.widths.minimum);

        if (attrPres.widths.preferred >= 0)
            vals[1] = new Integer(attrPres.widths.preferred);

        if (attrPres.widths.maximum >= 0)
            vals[2] = new Integer(attrPres.widths.maximum);

        return vals;
    }

    public void setAttributePreferredWidth(String attrname, int minWidth, int prefWidth, int maxWidth)
    {
        AttributePresentation pres = this.attributePresentations.get(attrname);

        if (pres == null)
            pres = new AttributePresentation();

        pres.widths = new AttributePresentation.PreferredSizes(minWidth, prefWidth, maxWidth);

        this.attributePresentations.put(attrname, pres);
    }

    public void setAttributePreferredWidth(String attrname, int w)
    {
        AttributePresentation pres = this.attributePresentations.get(attrname);

        if (pres == null)
            pres = new AttributePresentation();

        if (pres.widths == null)
            pres.widths = new AttributePresentation.PreferredSizes(-1, w, -1);
        else
            pres.widths.preferred = w;

        this.attributePresentations.put(attrname, pres);// update
    }

    public void setAttributePreferredWidths(String attrname, int[] values)
    {
        AttributePresentation pres = this.attributePresentations.get(attrname);

        if (pres == null)
            pres = new AttributePresentation();

        if (pres.widths == null)
            pres.widths = new AttributePresentation.PreferredSizes(values[0], values[1], values[2]);
        else
            pres.widths.setValues(values);

        this.attributePresentations.put(attrname, pres);// update
    }

    public boolean getAttributeFieldResizable(String attrname)
    {
        AttributePresentation pres = this.attributePresentations.get(attrname);

        if (pres == null)
            return true;

        return pres.attributeFieldResizable;
    }

    public int getColumnsAutoResizeMode()
    {
        return this.columnsAutoResizeMode;
    }

    public void setColumnsAutoResizeMode(int value)
    {
        this.columnsAutoResizeMode = value;
    }
    
    public String toString()
    {
        String str="<UIPresentation>{sortOption="+this.sortOption;
        if (sortFields==null)
        {
            str+=",sortFields=<null>";
        }
        else
        {
            str+=",sortFields="+sortFields.toString(",");
        }
        str+="}";
        return str; 
    }
    
    public void setIconAttributeName(String name)
    {
        iconAttributeName=name; 
    }

    public String getIconAttributeName()
    {
        return iconAttributeName; 
    }

    @Override
    public boolean shallowSupported()
    {
        return false;
    }

    @Override
    public Presentation duplicate()
    {
        Presentation pres=new Presentation();
        pres.copyFrom(this); 
        return pres; 
    }

    @Override
    public Presentation duplicate(boolean shallow)
    {
        return duplicate();
    }
}
