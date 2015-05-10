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

import java.net.URI;
import java.net.URL;
import java.util.Date;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Basic Attribute Types.
 */
public enum AttributeType {

    ANY("Any", Object.class), // 
    BOOLEAN("Boolean", Boolean.class), // 
    INT("Integer", Integer.class), //
    LONG("Long", Long.class), // 
    FLOAT("Float", Float.class), // 
    DOUBLE("Double", Double.class), //
    STRING("String", String.class), //
    /** Enum type stores its values as String */
    ENUM("Enum", String.class), //
    /** Store date time as unified date-time string */
    DATETIME("DateTime", String.class), // 
    VRL("VRL", VRL.class)//
    ;

    // === // 

    private final String enumName;

    private final Class<? extends Object> storageClass;

    private AttributeType(String name, Class<?> storageClass) {
        this.enumName = name;
        this.storageClass = storageClass;
    }

    public String getName() {
        return this.enumName;
    }

    public Class<? extends Object> getStorageClass() {
        return this.storageClass;
    }

    public static AttributeType getObjectType(Object object, AttributeType defaultType) {
        if (object == null)
            return AttributeType.ANY;

        if (object instanceof Integer)
            return AttributeType.INT;

        if (object instanceof Boolean)
            return AttributeType.BOOLEAN;

        if (object instanceof Long)
            return AttributeType.LONG;

        if (object instanceof Float)
            return AttributeType.FLOAT;

        if (object instanceof Double)
            return AttributeType.DOUBLE;

        if (object instanceof String)
            return AttributeType.STRING;

        if (object instanceof Date)
            return AttributeType.DATETIME;

        if (object instanceof VRL)
            return AttributeType.VRL;

        if (object instanceof URL)
            return AttributeType.VRL;

        if (object instanceof URI)
            return AttributeType.VRL;

        return defaultType;
    }

}