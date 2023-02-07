/*-
 * Copyright (C) 2002, 2018, Oracle and/or its affiliates. All rights reserved.
 *
 * This file was distributed by Oracle as part of a version of Oracle Berkeley
 * DB Java Edition made available at:
 *
 * http://www.oracle.com/technetwork/database/database-technologies/berkeleydb/downloads/index.html
 *
 * Please see the LICENSE file included in the top-level directory of the
 * appropriate version of Oracle Berkeley DB Java Edition for a copy of the
 * license and additional information.
 */

package com.sleepycat.persist.model;

import java.io.Serializable;

/**
 * The metadata for a key field.  This class defines common properties for
 * singular and composite key fields.
 *
 * <p>{@code FieldMetadata} objects are thread-safe.  Multiple threads may
 * safely call the methods of a shared {@code FieldMetadata} object.</p>
 *
 * @author Mark Hayes
 */
public class FieldMetadata implements Serializable {

    private static final long serialVersionUID = -9037650229184174279L;

    private String name;
    private String className;
    private String declaringClassName;

    /**
     * Used by an {@code EntityModel} to construct field metadata.
     *
     * @param name the field name.
     * @param className the class name.
     * @param declaringClassName the name of the class where the field is
     * declared.
     */
    public FieldMetadata(String name,
                         String className,
                         String declaringClassName) {
        this.name = name;
        this.className = className;
        this.declaringClassName = declaringClassName;
    }

    /**
     * Returns the field name.
     *
     * @return the field name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the class name of the field type.
     *
     * @return the class name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the name of the class where the field is declared.
     *
     * @return the name of the class where the field is declared.
     */
    public String getDeclaringClassName() {
        return declaringClassName;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FieldMetadata) {
            FieldMetadata o = (FieldMetadata) other;
            return ClassMetadata.nullOrEqual(name, o.name) &&
                   ClassMetadata.nullOrEqual(className, o.className) &&
                   ClassMetadata.nullOrEqual(declaringClassName,
                                             o.declaringClassName);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ClassMetadata.hashCode(name) +
               ClassMetadata.hashCode(className) +
               ClassMetadata.hashCode(declaringClassName);
    }

    @Override
    public String toString() {
        return "[FieldMetadata name: " + name + " className: " + className +
               " declaringClassName: " + declaringClassName + ']';
    }
}
