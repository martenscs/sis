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
package org.apache.sis.feature;

import java.util.Map;
import java.util.HashMap;
import java.util.ConcurrentModificationException;
import java.io.Serializable;
import org.apache.sis.util.Debug;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.resources.Errors;
import org.apache.sis.util.collection.Containers;

// Related to JDK7
import org.apache.sis.internal.jdk7.JDK7;
import org.apache.sis.internal.jdk7.Objects;


/**
 * An instance of {@linkplain DefaultFeatureType feature type} containing values for a real-world phenomena.
 *
 * @author  Travis L. Pinney
 * @author  Johann Sorel (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.5
 * @version 0.5
 * @module
 */
public class DefaultFeature implements Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 6594295132544357870L;

    /**
     * Information about the feature (name, characteristics, <i>etc.</i>).
     */
    private final DefaultFeatureType type;

    /**
     * The properties (attributes, operations, feature associations) of this feature.
     */
    private final Map<String, DefaultAttribute<?>> properties;

    /**
     * Creates a new features.
     *
     * @param type Information about the feature (name, characteristics, <i>etc.</i>).
     */
    public DefaultFeature(final DefaultFeatureType type) {
        ArgumentChecks.ensureNonNull("type", type);
        this.type = type;
        properties = new HashMap<String, DefaultAttribute<?>>(
                Math.min(16, Containers.hashMapCapacity(type.getCharacteristics().size())));
    }

    /**
     * Returns information about the feature (name, characteristics, <i>etc.</i>).
     *
     * @return Information about the feature.
     */
    public DefaultFeatureType getType() {
        return type;
    }

    /**
     * Returns the value of the attribute of the given name.
     *
     * @param  name The attribute name.
     * @return The value for the given attribute, or {@code null} if none.
     */
    public Object getAttributeValue(final String name) {
        final DefaultAttribute<?> attribute = properties.get(name);
        if (attribute == null) {
            final DefaultAttributeType<?> at = type.getProperty(name);
            if (at == null) {
                throw new IllegalArgumentException(propertyNotFound(name));
            }
            return at.getDefaultValue();
        }
        return attribute.getValue();
    }

    /**
     * Sets the value of the attribute of the given name.
     *
     * @param name  The attribute name.
     * @param value The new value for the given attribute (may be {@code null}).
     */
    @SuppressWarnings("unchecked")
    public void setAttributeValue(final String name, final Object value) {
        DefaultAttribute<?> attribute = properties.get(name);
        if (attribute == null) {
            final DefaultAttributeType<?> at = type.getProperty(name);
            if (at == null) {
                throw new IllegalArgumentException(propertyNotFound(name));
            }
            if (Objects.equals(value, at.getDefaultValue())) {
                return; // Avoid creating the attribute if not necessary.
            }
            attribute = new DefaultAttribute(at);
            if (properties.put(name, attribute) != null) {
                throw new ConcurrentModificationException();
            }
        }
        ArgumentChecks.ensureCanCast(name, attribute.getType().getValueClass(), value);
        ((DefaultAttribute) attribute).setValue(value);
    }

    /**
     * Returns the error message for a property not found.
     */
    private String propertyNotFound(final String name) {
        return Errors.format(Errors.Keys.PropertyNotFound_2, type.getName(), name);
    }

    /**
     * Returns a hash code value for this feature.
     *
     * @return A hash code value.
     */
    @Override
    public int hashCode() {
        return type.hashCode() + 37 * properties.hashCode();
    }

    /**
     * Compares this feature with the given object for equality.
     *
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (super.equals(obj)) {
            final DefaultFeature that = (DefaultFeature) obj;
            return type.equals(that.type) &&
                   properties.equals(that.properties);
        }
        return false;
    }

    /**
     * Returns a string representation of this feature.
     * The returned string is for debugging purpose and may change in any future SIS version.
     *
     * @return A string representation of this feature for debugging purpose.
     */
    @Debug
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final String lineSeparator = JDK7.lineSeparator();
        for (final DefaultAttribute<?> attribute : properties.values()) {
            sb.append(attribute.getType().getName()).append(": ").append(attribute.getValue()).append(lineSeparator);
        }
        return sb.toString();
    }
}
