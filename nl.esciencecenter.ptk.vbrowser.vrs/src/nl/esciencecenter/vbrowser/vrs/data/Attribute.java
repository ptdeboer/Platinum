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

package nl.esciencecenter.vbrowser.vrs.data;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.object.Duplicatable;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ValueParseException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * This class provides a high level interface to Resource Attributes. It is a
 * <code> {name, value}</code> pair with dynamic typing. The Attribute itself
 * does not do any type checking, so casting is possible. For example
 * getStringValue() after setValue(int) will return the string representation of
 * the integer. <br>
 * 
 * @see AttributeType
 * 
 * @author P.T. de Boer
 */
public class Attribute implements Cloneable, Serializable, Duplicatable<Attribute>
{
    private static final long serialVersionUID = 2911511497535338526L;

    private static ClassLogger logger;
    {
        logger = ClassLogger.getLogger(Attribute.class);
    }

    protected static String[] booleanEnumValues =
    { "false", "true" };

    // ========================================================================
    // Class Methods
    // ========================================================================

    /**
     * Parse String to Object.
     * 
     * @throws VRLSyntaxException
     */
    public static Object parseString(AttributeType toType, String strValue) throws ValueParseException, VRLSyntaxException
    {
        if (strValue == null)
        {
            // lazy type checking:
            // if toType isn't String or ANY, null should not be allowed!
            return null;
        }

        switch (toType)
        {
            case BOOLEAN:
                return Boolean.parseBoolean(strValue);
            case INT:
                return Integer.parseInt(strValue);
            case LONG:
                return Long.parseLong(strValue);
            case FLOAT:
                return Float.parseFloat(strValue);
            case DOUBLE:
                return Double.parseDouble(strValue);
            case STRING:
                return strValue;
            case VRL:
                return new VRL(strValue);
            case ENUM:
                return strValue;
            case DATETIME:
            {
                return strValue; // keep normalized DateTime String "as is".
            }
            default:
                throw new ValueParseException("Cannot convert String:'" + strValue + "' to type:" + toType,strValue);
        }
    }

    public static List<Attribute> toList(Attribute[] attrs)
    {
        if (attrs == null)
            return null;

        ArrayList<Attribute> list = new ArrayList<Attribute>(attrs.length);
        for (Attribute attr : attrs)
            list.add(attr);

        return list;
    }

    // ========================================================================
    // Instance
    // ========================================================================

    /**
     * The name of the attribute.
     */
    private String name = null;

    /**
     * Generic Object as value. The default is to use String Representation
     */
    private Object _value = null;

    /**
     * Whether attribute is editable. No runtime checking is done. This
     * attribute is only used in Attribute Editor forms.
     */
    private boolean editable = false;

    /**
     * List of enum values, index enumValue determines which enum value is set
     */
    private String enumValues[] = null;

    /**
     * Index into enumValues[] so that: enumAvalues[enumIndex]==value
     */
    private int enumIndex = -1;

    private boolean changed = false;

    /**
     * Copy Constructor
     */
    public Attribute(Attribute source)
    {
        copyFrom(source);
    }

    protected Attribute(String name)
    {
        this.name = name;
    }

    /** Constructor to create a enum list of string */
    public Attribute(String name, String enumValues[], int enumVal)
    {
        init(name, enumValues, enumVal);
    }

    /**
     * Constructor to create a enum list of string
     */
    public Attribute(String name, List<String> enumValues, int enumVal)
    {
        init(name, enumValues.toArray(new String[0]), enumVal);
    }

    public Attribute(String name, Boolean val)
    {
        init(AttributeType.BOOLEAN, name, val);
    }

    public Attribute(String name, Integer val)
    {
        init(AttributeType.INT, name, val);
    }

    public Attribute(String name, Long val)
    {
        init(AttributeType.LONG, name, val);
    }

    public Attribute(String name, Float val)
    {
        init(AttributeType.FLOAT, name, val);
    }

    public Attribute(String name, Double val)
    {
        init(AttributeType.DOUBLE, name, val);
    }

    public Attribute(String name, VRL vri)
    {
        init(AttributeType.VRL, name, vri);
    }

    public Attribute(String string, URI uri)
    {
        init(AttributeType.VRL, name, new VRL(uri));
    }

    /**
     * Create new Enumerated Attribute with enumVals as possible values and
     * defaultVal (which must be element of enumVals) as default.
     */
    public Attribute(String name, String[] enumVals, String defaultVal)
    {
        int index = 0; // use default of 0!

        if ((enumVals == null) || (enumVals.length <= 0))
        {
            throw new NullPointerException("Cannot not have empty enum value list !");
        }
        
        StringList enums = new StringList(enumVals);

        // robuustness! add defaultVal if not in enumVals!
        enums.add(defaultVal, true);

        index = enums.indexOf(defaultVal);

        if (index < 0)
            index = 0;

        init(name, enums.toArray(), index);
    }

    /**
     * Named String {Type,Value} Tuple
     */
    public Attribute(String name, String value)
    {
        init(AttributeType.STRING, name, value);
    }

    /**
     * Named String {Type,Value} Tuple
     */
    public Attribute(String name, String value, boolean editable)
    {
        init(AttributeType.STRING, name, value);
        this.setEditable(editable);
    }

    /**
     * Custom named & explicit typed Attribute
     */
    public Attribute(AttributeType type, String name, Object value)
    {
        init(type, name, value);
    }

    /**
     * Date attribute, can also be used for Time values.
     */
    public Attribute(String name, Date date)
    {
        init(AttributeType.DATETIME, name, date); // Presentation.createNormalizedDateTimeString(date));
    }

    public AttributeDescription getDescription()
    {
        return new AttributeDescription(this.getName(), this.getType(), this.isEditable(), null);
    }

    /**
     * Main init method to be called by other constructors. <br>
     * This method may only be used by contructors. Object value must be a
     * private owned copy!
     */
    protected void init(AttributeType type, String name, Object value)
    {
        this.name = name;
        this._value = value;
        assertValidType(type, value);
    }

    protected void assertValidType(AttributeType type, Object value)
    {
        if (type == AttributeType.ANY)
        {
            return;
        }

        // Assert here ?
        // Null types are either ANY or String (enums are also STRING)
        if (value == null)
        {
            if ((type != AttributeType.ANY) && (type != AttributeType.STRING)  && (type != AttributeType.ENUM))
            {
                throw new Error("Null objects must resolve to either ANY, String or Enum (type="+type+")");
            }
            else
            {
                if (type == AttributeType.ENUM)
                {
                    // TBI, enum value must be one of allowed enums. 
                    logger.errorPrintf("Warning: NULL Enum value\n"); 
                }
                // null may match against ANY,STRING or ENUM (which are strings).
                return;
            }
        }

        // basic object type checks:
        AttributeType objType = AttributeType.getObjectType(value, AttributeType.STRING);

        // DateTime can be stored as normalized date-time string as well as Date
        // Object.
        if (type == AttributeType.DATETIME && objType == AttributeType.STRING)
        {
            return;
        }

        // Enums are stored as Strings.
        if (type == AttributeType.ENUM && objType == AttributeType.STRING)
        {
            return;
        }

        if (objType != type)
        {
            throw new Error("Object type is not the same as expected. Expected=" + type + ",parsed=" + objType
                    + ",value=" + value);
        }
    }

    /**
     * Initialize as Enum Type
     */
    protected void init(String name, String enumValues[], int enumIndex)
    {
        this.name = name;
        this.enumValues = enumValues;
        this.enumIndex = enumIndex;

        if ((enumValues != null) && (enumIndex >= 0) && (enumIndex < enumValues.length))
        {
            this._value = enumValues[enumIndex];
        }
        else
        {
            logger.errorPrintf("Error enumIndex out of bound:%d\n", enumIndex);
            _value = "";
        }
    }

    /**
     * Master setter method. All set() methods must call this method. If type is
     * specified and is not equal to ANY, the object must be compatible with
     * specified AttributeType.
     */
    private void _setValue(AttributeType type, Object object)
    {
        if (type != null)
        {
            this.assertValidType(type, object);
        }
        this._value = object;
        this.changed = true;
    }

    protected void copyFrom(Attribute source)
    {
        // Duplicate String objects: !
        init(source.getType(), source.name, AttributeUtil.duplicateValue(source.getType(), source.getValue()));

        this.editable = source.editable;
        this.enumIndex = source.enumIndex;
        this.enumValues = source.enumValues;
        this.changed = false; // new Attribute: reset 'changed' flag.
    }

    /** See {@link #duplicate()} */
    public Attribute clone()
    {
        return new Attribute(this);
    }

    /**
     * Return duplicate of this object. This method returns the same class
     * instead of the object.clone() method All values are copied.
     * 
     * @return full non-shallow copy of this Object
     */
    public Attribute duplicate()
    {
        return new Attribute(this);
    }

    // =================================================================
    // Instance Getters/Setters
    // =================================================================

    /**
     * Get Type of Attribute. Value type of ENUM elements is String.
     */
    public AttributeType getType()
    {
        if (isEnum())
        {
            return AttributeType.ENUM;
        }

        return AttributeType.getObjectType(getValue(), AttributeType.ANY);
    }

    public boolean isType(AttributeType type)
    {
        return (getType() == type);
    }

    public boolean isEnum()
    {
        return (this.enumValues != null);
    }

    /**
     * Get Name of Attribute. Note that the Name may never change during the
     * lifetime of an VAttribute !
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Return Actual Object Value.
     */
    public Object getValue()
    {
        return _value;
    }

    /**
     * Explicit return value as String. Performs toString() if object isn't a
     * string type
     */
    public String getStringValue()
    {
        if (_value == null)
            return null;

        if (_value instanceof String)
            return (String) _value;

        // use own normalized time-date string;
        if (_value instanceof Date)
            return Presentation.createNormalizedDateTimeString((Date) _value);

        return _value.toString();
    }

    /**
     * Get String array of enumeration types.
     */
    public String[] getEnumValues()
    {
        if (enumValues != null)
            return enumValues.clone(); // return private copy of array.

        if (getType() == AttributeType.BOOLEAN)
            return booleanEnumValues;

        return null;
    }

    /**
     * Return enum order of current value.
     */
    public int getEnumIndex()
    {
        if (isEnum())
            return enumIndex;

        if (getType() == AttributeType.BOOLEAN)
            return (getBooleanValue() ? 1 : 0);

        return 0;
    }

    /** Searches enum values if it contain the specified option. */
    public boolean hasEnumValue(String val)
    {
        if (enumValues == null)
        {
            return false;
        }

        for (int i = 0; i < enumValues.length; i++)
        {
            if (val.equals(enumValues[i]))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Parse String to Object.
     */
    public void setValueFromString(AttributeType type, String stringValue) throws Exception
    {
        _setValue(type, parseString(type, stringValue));
    }

    public void setValue(int intVal)
    {
        _setValue(AttributeType.INT, new Integer(intVal));
    }

    public void setValue(long longVal)
    {
        _setValue(AttributeType.LONG, new Long(longVal));
    }

    public void setValue(float floatVal)
    {
        _setValue(AttributeType.FLOAT, new Float(floatVal));
    }

    public void setValue(double doubleVal)
    {
        _setValue(AttributeType.DOUBLE, new Double(doubleVal));
    }

    public void setValue(URI uri)
    {
        _setValue(AttributeType.VRL, uri);
    }

    public void setValue(VRL vrl)
    {
        _setValue(AttributeType.VRL, vrl);
    }

    /** Reset changed flag. */
    public void setNotChanged()
    {
        this.changed = false;
    }

    /** Whether the value has changed since the last setNotChanged() */
    public boolean hasChanged()
    {
        return changed;
    }

    /**
     * Return true if this Attribute is supposed to be editable. The set()
     * methods still work, this is just a flag used for Attribute Editor panels.
     * 
     * @return whether this Attribute is supposed to be editable.
     */
    public boolean isEditable()
    {
        return this.editable;
    }

    /**
     * Set whether this Attribute is supposed to be editable. The set() method
     * do not check this and no exception is thrown if the Attribute is set
     * anyway.
     * 
     * @param value
     *            - set whether this Attribute is supposed to be editable.
     */
    public void setEditable(boolean value)
    {
        this.editable = value;
    }

    public int getIntValue()
    {
        Object value = getValue();
        if (value == null)
            return 0; // by definition;

        switch (getType())
        {
            case INT:
                return ((Integer) value).intValue();
            case LONG:
                return ((Long) value).intValue();
            case FLOAT:
                return ((Float) value).intValue();
            case DOUBLE:
                return ((Double) value).intValue();
            default:
                return Integer.parseInt(getStringValue());
        }
    }

    public long getLongValue()
    {
        Object value = getValue();

        if (value == null)
            return 0; // by definition;

        switch (getType())
        {
            case INT:
                return ((Integer) value).longValue();
            case LONG:
                return ((Long) value).longValue();
            case FLOAT:
                return ((Float) value).longValue();
            case DOUBLE:
                return ((Double) value).longValue();
            default:
                return Long.parseLong(getStringValue());
        }
    }

    public float getFloatValue()
    {
        Object value = getValue();

        if (value == null)
            return Float.NaN;

        switch (getType())
        {
            case INT:
                return ((Integer) value).floatValue();
            case LONG:
                return ((Long) value).floatValue();
            case FLOAT:
                return ((Float) value).floatValue();
            case DOUBLE:
                return ((Double) value).floatValue();
            default:
                return Float.parseFloat(getStringValue()); // auto cast !
        }
    }

    public double getDoubleValue()
    {
        Object value = getValue();

        if (value == null)
            return Double.NaN;

        switch (getType())
        {
            case INT:
                return ((Integer) value).doubleValue();
            case LONG:
                return ((Long) value).doubleValue();
            case FLOAT:
                return ((Float) value).doubleValue();
            case DOUBLE:
                return ((Double) value).doubleValue();
            default:
                return Long.parseLong(getStringValue());
        }
    }

    public boolean getBooleanValue()
    {
        Object value = getValue();

        if (value == null)
            return false; // by definition

        switch (getType())
        {
            case INT:
                return (((Integer) value) != 0);
            case LONG:
                return (((Long) value) != 0);
            case FLOAT:
                return (((Float) value) != 0);
            case DOUBLE:
                return (((Double) value).doubleValue() != 0);
            default:
                return Boolean.parseBoolean(getStringValue());
        }
    }

    /**
     * Return as VRL.
     * 
     * @throws VRLSyntaxException
     */
    public VRL getVRL() throws VRLSyntaxException
    {
        Object value = getValue();

        if (value == null)
            return null;

        if (value instanceof VRL)
            return (VRL) value;

        if (value instanceof String)
            return new VRL((String) value);

        return new VRL(value.toString());
    }

    /**
     * Return as VRL. Autocasts value to VRL object if possible. Return nulls
     * otherwise!
     */
    public VRL getVRLorNull()
    {
        Object value = getValue();

        if (value == null)
            return null;

        if (value instanceof VRL)
            return (VRL) value;

        try
        {
            if (value instanceof String)
                return new VRL((String) value);

            return new VRL(value.toString());
        }
        catch (VRLSyntaxException e)
        {
            return null;
        }
    }

    /**
     * Ignore case only makes sense for String like Attributes
     */
    public int compareToIgnoreCase(Attribute attr2)
    {
        return compareTo(attr2, true);
    }

    public int compareTo(Attribute attr2)
    {
        return compareTo(attr2, false);
    }

    /**
     * Compares this value to value of other VAttribute 'attr'. The type of this
     * attribute is used and the other attribute is converted (casted) to this
     * type.
     * 
     * @param other
     * @return
     */
    public int compareTo(Attribute other, boolean ignoreCase)
    {
        Object value = getValue();

        // NULL comparison MUST match String compare !
        if (value == null)
        {
            // / (this.value==null) < (other.value!=null)
            if ((other != null) && (other.getValue() != null))
                return -1;
            else
                return 0; // null equals null
        }

        switch (getType())
        {
            case INT:
            case LONG:
            {
                // use long both for int,long and time (millis!)
                if (this.getLongValue() < other.getLongValue())
                    return -1;
                else if (this.getLongValue() > other.getLongValue())
                    return 1;
                else
                    return 0;
            }
            case DATETIME:
            {
                // Use Date Value:
                return this.getDateValue().compareTo(other.getDateValue());
                // break;
            }
            case FLOAT:
            {
                if (this.getFloatValue() < other.getFloatValue())
                    return -1;
                else if (this.getFloatValue() > other.getFloatValue())
                    return 1;
                else
                    return 0;
                // break;
            }
            case DOUBLE:
            {
                if (this.getDoubleValue() < other.getDoubleValue())
                    return -1;
                else if (this.getDoubleValue() > other.getDoubleValue())
                    return 1;
                else
                    return 0;
            }
            case STRING:
            {
                return StringUtil.compare((String) value, other.getStringValue());
            }
            case VRL:
            {
                VRL vrl = this.getVRLorNull();
                if (vrl == null)
                    return -1;
                // use object compare
                vrl.compareToObject(other.getVRLorNull());
            }
            case ENUM:
            case ANY:
            default:
            {
                String s1 = this.toString();
                String s2 = other.toString();

                // Default use string reprentation
                if (other.getValue() != null)
                {
                    if (ignoreCase)
                    {
                        return s1.compareToIgnoreCase(s2);
                    }
                    else
                    {
                        return s1.compareTo(s2);
                    }
                }
                else
                    return 1; // this >> null
                // break;
            }
        }
    }

    public boolean hasName(String nname)
    {
        return (this.name.compareTo(nname) == 0);
    }

    public void setValue(boolean b)
    {
        _setValue(AttributeType.BOOLEAN, new Boolean(b));
    }

    public void setValue(AttributeType type, Object value)
    {
        _setValue(type, value);
    }

    public Date getDateValue()
    {
        Object value = getValue();

        if (value == null)
            return null;

        if (value instanceof java.util.Date)
            return (Date) value;

        // Millies since epoch:
        if (getType() == AttributeType.LONG)
            Presentation.createDate(this.getLongValue());

        return Presentation.createDateFromNormalizedDateTimeString(getStringValue());
    }

    /**
     * Old style section names are embedded between '[]' like '[config]'.
     * 
     * @param name
     *            - the attribute name to be checked.
     * @return - true for attribute names which are embedded between brackets.
     */
    public static boolean isSectionName(String name)
    {
        if (name == null)
            return false;

        return (name.startsWith("[") || name.startsWith("-["));
    }

    @Override
    public boolean shallowSupported()
    {
        return false;
    }

    @Override
    public Attribute duplicate(boolean shallow)
    {
        if (shallow)
        {
            logger.warnPrintf("Asked for a shallow copy when this isn't supported\n");
        }
        return new Attribute(this);
    }

    /**
     * Set Object value, do not check type.
     */
    public void setObjectValue(Object newValue)
    {
        this._setValue(null, newValue);
    }

    // ===============
    // Misc. Fuctions.
    // ===============

    /**
     * Pretty Print to String.
     */
    public String toString()
    {
        String enumStr = "";

        if (isEnum())
        {
            enumStr = ",{";

            if (this.enumValues != null)
                for (int i = 0; i < this.enumValues.length; i++)
                {
                    enumStr = enumStr + enumValues[i];
                    if (i + 1 < enumValues.length)
                        enumStr = enumStr + ",";
                }

            enumStr = enumStr + "}";
        }

        // convert to Attribute Triplet
        return "{"
                + getType() + "," + name + "," + _value + enumStr
                + ",[" + ((isEditable()) ? "E" : "") + ((hasChanged()) ? "C" : "")
                + "]}";
    }

}
