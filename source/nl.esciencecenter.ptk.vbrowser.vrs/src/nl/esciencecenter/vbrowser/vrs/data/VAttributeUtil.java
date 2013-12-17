package nl.esciencecenter.vbrowser.vrs.data;

import nl.esciencecenter.ptk.presentation.Presentation;

/**
 * Attribute parsing and factory methods. 
 */
public class VAttributeUtil
{
    
    public static Attribute createFromAssignment(String stat)
    {
        String strs[] = stat.split("[ ]*=[ ]*");

        if ((strs == null) || (strs.length < 2))
            return null;

        // parse result
        return new Attribute(strs[0], strs[1]);
    }

    public static Attribute parseFromString(AttributeType attrType, String attrName, String valueStr) throws Exception
    {
        Object value=Attribute.parseString(attrType,valueStr);
        return new Attribute(attrType,attrName,value); 
    }

    /**
     * Type safe factory method. Object must have specified type
     */
    public static Attribute createFrom(AttributeType type, String name, Object value)
    {
        // null value is allowed: 
        if (value==null)
            return new Attribute(type,name, null); 
        
        if (type==AttributeType.ANY)
        {
            return new Attribute(type, name, value);
        }
        
        AttributeType objType = AttributeType.getObjectType(value, null);
        if (objType != type)
            throw new Error("Incompatible Object Type. Specified type=" + type + ", object type=" + objType);

        return new Attribute(type, name, value);
    }

    public static Attribute createEnumerate(String name, String values[], String value)
    {
        return new Attribute(name, values, value);
    }

    /**
     * Create DateTime Attribute from nr of millis since Epoch.
     */
    public static Attribute createDateFromMilliesSinceEpoch(String name, long millis)
    {
        // store as normalized time string:
        //String timeStr = Presentation.createNormalizedDateTimeString(millis);
        // return new Attribute(AttributeType.DATETIME, name, timeStr);
        return new Attribute(AttributeType.DATETIME, name, Presentation.createDate(millis));
    }

    /** 
     * Create Attribute and get type from object value.
     */
    public static Attribute createFrom(String name, Object obj)
    {
        AttributeType newtype = AttributeType.getObjectType(obj, AttributeType.STRING);
        Attribute attr = new Attribute(newtype, name, obj);
        // check?
        return attr;
    }
    
    /** 
     * Create a deep copy of an Attribute Array 
     */
    public static Attribute[] duplicateArray(Attribute[] attrs)
    {
        if (attrs == null)
            return null;

        Attribute newAttrs[] = new Attribute[attrs.length];

        for (int i = 0; i < attrs.length; i++)
        {
            if (attrs[i] != null)
                newAttrs[i] = attrs[i].duplicate(false); // deep copy
            else
                newAttrs[i] = null; // be robust: accept null attributes
        }

        return newAttrs;
    }

}
