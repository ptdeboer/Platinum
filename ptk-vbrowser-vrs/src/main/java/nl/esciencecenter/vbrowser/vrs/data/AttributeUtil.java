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

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.object.Duplicatable;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.Date;
import java.util.List;

/**
 * Attribute parsing and factory methods.
 */
public class AttributeUtil {

    public static Attribute createFromAssignment(String stat) {

        String[] strs = stat.split("[ ]*=[ ]*");

        if ((strs == null) || (strs.length < 2))
            return null;

        // parse result
        return new Attribute(strs[0], strs[1]);
    }

    public static Attribute parseFromString(AttributeType attrType, String attrName, String valueStr) throws Exception {
        Object value = Attribute.parseString(attrType, valueStr);
        return new Attribute(attrType, attrName, value);
    }

    public static Attribute parseFromString(AttributeType attrType, String attrName, String valueStr,
                                            String[] optEnumValues) throws Exception {
        if (attrType == AttributeType.ENUM) {
            return createEnumerate(attrName, optEnumValues, valueStr);
        } else {
            Object value = Attribute.parseString(attrType, valueStr);
            return new Attribute(attrType, attrName, value);
        }
    }

    public static Attribute createFrom(AttributeType type, String name, Object value, String[] enumValues) {
        if (type == AttributeType.ENUM) {
            return createEnumerate(name, enumValues, value.toString());
        } else {
            return createFrom(type, name, value);
        }
    }

    /**
     * Static factory method for Integer Attribute
     */
    public static Attribute createIntegerAttribute(String name, int val) {
        return new Attribute(name, val);
    }

    /**
     * Static factory method for String Attribute.
     * <p>
     * Use {@link Attribute#getStringListValue()} to retrieve the list.
     *
     * @param editable
     * @see Attribute#getStringListValue()
     */
    public static Attribute createStringListAttribute(String name, List<String> values, boolean editable) {
        StringList list = new StringList(values);
        Attribute attr = new Attribute(name, list.toString(","), editable);
        return attr;
    }

    /**
     * Static factory method for String Attribute
     *
     * @param editable
     */
    public static Attribute createStringAttribute(String name, String value, boolean editable) {
        Attribute attr = new Attribute(name, value);
        attr.setEditable(editable);
        return attr;
    }

    /**
     * Type safe factory method. Object must have specified type
     */
    public static Attribute createFrom(AttributeType type, String name, Object value) {
        // null value is allowed:
        if (value == null) {
            return new Attribute(type, name, null);
        }

        if (type == AttributeType.ANY) {
            return new Attribute(type, name, value);
        }

        AttributeType objType = AttributeType.getObjectType(value, null);

        if (objType != type) {
            throw new Error("Incompatible Object Type. Specified type=" + type + ", object type=" + objType);
        }

        return new Attribute(type, name, value);
    }

    public static Attribute createEnumerate(String name, String[] values, String value) {
        return new Attribute(name, values, value);
    }

    /**
     * Create DateTime Attribute from nr of millis since Epoch.
     */
    public static Attribute createDateFromMilliesSinceEpoch(String name, long millis) {
        // store as normalized time string:
        // String timeStr = Presentation.createNormalizedDateTimeString(millis);
        // return new Attribute(AttributeType.DATETIME, name, timeStr);
        return new Attribute(AttributeType.DATETIME, name, Presentation.createDate(millis));
    }

    /**
     * Create Attribute and get type from object value.
     */
    public static Attribute createFrom(String name, Object obj) {
        // avoid nesting of attributes as objects !
        if (obj instanceof Attribute) {
            return ((Attribute) obj).duplicate();
        }

        AttributeType newtype = AttributeType.getObjectType(obj, AttributeType.STRING);
        Attribute attr = new Attribute(newtype, name, obj);
        // check?
        return attr;
    }

    /**
     * Create a deep copy of an Attribute Array
     */
    public static Attribute[] duplicateArray(Attribute[] attrs) {
        if (attrs == null)
            return null;

        Attribute[] newAttrs = new Attribute[attrs.length];

        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i] != null)
                newAttrs[i] = attrs[i].duplicate(false); // deep copy
            else
                newAttrs[i] = null; // be robust: accept null attributes
        }

        return newAttrs;
    }

    /**
     * Create a deep copy of an Attribute Array
     */
    public static List<Attribute> duplicate(List<Attribute> attrs) {
        if (attrs == null) {
            return null;
        }

        ExtendedList<Attribute> newList = new ExtendedList<Attribute>(attrs);

        return newList;
    }

    /**
     * If object is a know object type, create a non-shallow duplicate (clone).
     *
     * @param value - primitive object to be copiedl
     * @return copied object or NULL if object couldn't be copied !
     */
    public static Object duplicateObject(Object value) {
        AttributeType type = AttributeType.getObjectType(value, null);

        if ((type == null) || (type == AttributeType.ANY)) {
            return null;
        }
        // create specific copy of object:
        return duplicateValue(type, value);
    }

    public static Object duplicateValue(AttributeType type, Object object) {
        if (object == null) {
            return null;
        }

        switch (type) {
            case BOOLEAN: {
                return new Boolean((Boolean) object);
            }
            case INT: {
                return new Integer((Integer) object);
            }
            case LONG: {
                return new Long((Long) object);
            }
            case FLOAT: {
                return new Float((Float) object);
            }
            case DOUBLE: {
                return new Double((Double) object);
            }
            case ENUM: // enums are stored as String
            {
                return object;
            }
            case DATETIME: {
                if (object instanceof Date) {
                    return ((Date) object).clone();
                } else if (object instanceof String) {
                    return object;
                } else {
                    throw new Error("Invalid DATETIME Type:" + object.getClass());
                }
            }
            case STRING: {
                return object;
            }
            case VRL: {
                return ((VRL) object).duplicate();
            }
            case ANY:
            default: {
                if (object instanceof Duplicatable) {
                    return ((Duplicatable<?>) object).duplicate(false);
                }

                throw new Error("Cannot clone/duplicate value object:" + object);
            }
        }
    }

    /**
     * Create VRL Attribute or ANY type NULL attribute (= nill attribute).
     */
    public static Attribute createVRLAttribute(String name, VRL vrl, boolean isNullAllowed) {
        if (vrl == null) {
            if (isNullAllowed) {
                return new Attribute(name, (String) null);
            } else {
                throw new NullPointerException("Cannot create NULL VRL Attribute if null value is not allowed");
            }
        }
        return new Attribute(name, vrl);
    }

}
