/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.simple;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.io.Serializable;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.extent.Extent;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.ReferenceIdentifier;
import org.apache.sis.internal.util.Citations;
import org.apache.sis.util.iso.DefaultNameSpace;
import org.apache.sis.util.LenientComparable;
import org.apache.sis.util.ComparisonMode;
import org.apache.sis.util.Utilities;

// Related to JDK7
import org.apache.sis.internal.jdk7.Objects;


/**
 * A trivial implementation of {@link IdentifiedObject} containing only a primary name.
 *
 * @author  Guilhem Legal (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.00)
 * @version 0.3
 * @module
 */
public class SimpleIdentifiedObject implements IdentifiedObject, LenientComparable, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 4773397959809985024L;

    /**
     * The primary name by which this object is identified.
     */
    protected ReferenceIdentifier referenceSystemIdentifier;

    /**
     * Creates an identified object without identifier.
     * This constructor is mainly for JAXB.
     */
    protected SimpleIdentifiedObject() {
    }

    /**
     * Creates an identified object with the same identifier than the given one.
     *
     * @param object The identified object to partially copy.
     */
    public SimpleIdentifiedObject(final IdentifiedObject object) {
        referenceSystemIdentifier = object.getName();
    }

    /**
     * Creates an identified object with the given identifier.
     *
     * @param name The primary name by which this object is identified.
     */
    public SimpleIdentifiedObject(final ReferenceIdentifier name) {
        referenceSystemIdentifier = name;
    }

    /**
     * Returns the primary name by which this object is identified.
     *
     * @return The identifier given at construction time.
     */
    @Override
    public ReferenceIdentifier getName() {
        return referenceSystemIdentifier;
    }

    /**
     * Method required by the {@link IdentifiedObject} interface.
     * Current implementation returns an empty set.
     *
     * @return The identifiers, or an empty set if none.
     */
    @Override
    public Set<ReferenceIdentifier> getIdentifiers() {
        return Collections.emptySet();
    }

    /**
     * Method required by the {@link IdentifiedObject} interface.
     * Current implementation returns an empty set.
     *
     * @return The aliases, or an empty set if none.
     */
    @Override
    public Collection<GenericName> getAlias() {
        return Collections.emptySet();
    }

    /**
     * Method required by most {@link IdentifiedObject} sub-interfaces.
     * Current implementation returns {@code null}.
     *
     * @return The domain of validity, or {@code null} if none.
     */
    public Extent getDomainOfValidity() {
        return null;
    }

    /**
     * Method required by most {@link IdentifiedObject} sub-interfaces.
     * Current implementation returns {@code null}.
     *
     * @return The scope, or {@code null} if none.
     */
    public InternationalString getScope() {
        return null;
    }

    /**
     * Method required by the {@link IdentifiedObject} interface.
     * Current implementation returns {@code null}.
     *
     * @return The remarks, or {@code null} if none.
     */
    @Override
    public InternationalString getRemarks() {
        return null;
    }

    /**
     * Returns a hash code value for this object.
     */
    @Override
    public final int hashCode() {
        int code = (int) serialVersionUID;
        final ReferenceIdentifier id = referenceSystemIdentifier;
        if (id != null) {
            code ^= id.hashCode();
        }
        return code;
    }

    /**
     * Compares this object with the given one for equality.
     *
     * @param  object The object to compare with this reference system.
     * @return {@code true} if both objects are equal.
     */
    @Override
    public final boolean equals(final Object object) {
        return equals(object, ComparisonMode.STRICT);
    }

    /**
     * Compares this object with the given one for equality.
     *
     * @param  object The object to compare with this reference system.
     * @param  mode The strictness level of the comparison.
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final Object object, final ComparisonMode mode) {
        if (object == this) {
            return true;
        }
        if (mode == ComparisonMode.STRICT) {
            if (object != null && object.getClass() == getClass()) {
                final SimpleIdentifiedObject that = (SimpleIdentifiedObject) object;
                return Objects.equals(referenceSystemIdentifier, that.referenceSystemIdentifier);
            }
        } else {
            if (object instanceof IdentifiedObject) {
                final IdentifiedObject that = (IdentifiedObject) object;
                return Utilities.deepEquals(referenceSystemIdentifier, that.getName(), mode);
            }
        }
        return false;
    }

    /**
     * Throws an exception in all cases, since this object can't be formatted in a valid WKT.
     *
     * @return The Well Known Text.
     * @throws UnsupportedOperationException Always thrown.
     */
    @Override
    public String toWKT() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a pseudo-WKT representation.
     */
    @Override
    public String toString() {
        final String code, codespace;
        final Citation authority;
        final ReferenceIdentifier id = referenceSystemIdentifier;
        if (id != null) {
            code      = id.getCode();
            codespace = id.getCodeSpace();
            authority = id.getAuthority();
        } else {
            code      = null;
            codespace = null;
            authority = null;
        }
        return toString("OBJECT", authority, codespace, code, false);
    }

    /**
     * Returns a pseudo-WKT representation.
     *
     * @param  type       The WKT heading text.
     * @param  authority  The authority to write in the {@code "AUTHORITY"} element.
     * @param  codespace  Usually an abbreviation of the authority name.
     * @param  code       The code to write in the {@code "AUTHORITY"} element, or {@code null} if none.
     * @param  deprecated {@code true} if the object to format is deprecated.
     * @return The pseudo-WKT.
     */
    public static String toString(final String type, final Citation authority,
            final String codespace, final String code, final boolean deprecated)
    {
        final StringBuilder buffer = new StringBuilder(type).append("[\"");
        if (codespace != null) {
            buffer.append(codespace).append(DefaultNameSpace.DEFAULT_SEPARATOR);
        }
        buffer.append(code).append('"');
        if (authority != null) {
            buffer.append(", AUTHORITY[\"").append(Citations.getIdentifier(authority)).append("\"]");
        }
        if (deprecated) {
            buffer.append(", DEPRECATED");
        }
        return buffer.append(']').toString();
    }
}
