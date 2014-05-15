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

import java.io.Serializable;
import org.opengis.util.GenericName;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.maintenance.ScopeCode;
import org.apache.sis.util.Debug;
import org.apache.sis.util.Classes;
import org.apache.sis.util.ArgumentChecks;

// Related to JDK7
import org.apache.sis.internal.jdk7.Objects;


/**
 * An instance of an {@linkplain DefaultAttributeType attribute type} containing the value of an attribute in a feature.
 * {@code Attribute} holds two main information:
 *
 * <ul>
 *   <li>A reference to an {@linkplain DefaultAttributeType attribute type}
 *       which define the base Java type and domain of valid values.</li>
 *   <li>A value.</li>
 * </ul>
 *
 * {@section Usage in multi-thread environment}
 * {@code DefaultAttribute} are <strong>not</strong> thread-safe.
 * Synchronization, if needed, shall be done externally by the caller.
 *
 * @param <T> The type of attribute values.
 *
 * @author  Johann Sorel (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.5
 * @version 0.5
 * @module
 *
 * @see DefaultAttributeType
 */
public class DefaultAttribute<T> extends Property implements Cloneable, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -8103788797446754561L;

    /**
     * Information about the attribute (base Java class, domain of values, <i>etc.</i>).
     */
    private final DefaultAttributeType<T> type;

    /**
     * The attribute value.
     */
    private T value;

    /**
     * Creates a new attribute of the given type initialized to the
     * {@linkplain DefaultAttributeType#getDefaultValue() default value}.
     *
     * @param type Information about the attribute (base Java class, domain of values, <i>etc.</i>).
     */
    public DefaultAttribute(final DefaultAttributeType<T> type) {
        ArgumentChecks.ensureNonNull("type", type);
        this.type  = type;
        this.value = type.getDefaultValue();
    }

    /**
     * Creates a new attribute of the given type initialized to the given value.
     * Note that a {@code null} value may not the same as the default value.
     *
     * @param type  Information about the attribute (base Java class, domain of values, <i>etc.</i>).
     * @param value The initial value (may be null {@code null}).
     */
    public DefaultAttribute(final DefaultAttributeType<T> type, final Object value) {
        ArgumentChecks.ensureNonNull("type", type);
        this.type  = type;
        this.value = type.getValueClass().cast(value);
    }

    /**
     * Returns the name of this attribute as defined by its {@linkplain #getType() type}.
     * This convenience method delegates to {@link DefaultAttributeType#getName()}.
     *
     * @return The attribute name specified by its type.
     */
    @Override
    public GenericName getName() {
        return type.getName();
    }

    /**
     * Returns information about the attribute (base Java class, domain of values, <i>etc.</i>).
     *
     * <div class="warning"><b>Warning:</b> In a future SIS version, the return type may be changed
     * to {@code org.opengis.feature.AttributeType}. This change is pending GeoAPI revision.</div>
     *
     * @return Information about the attribute.
     */
    public DefaultAttributeType<T> getType() {
        return type;
    }

    /**
     * Returns the attribute value.
     *
     * @return The attribute value (may be {@code null}).
     *
     * @see DefaultFeature#getPropertyValue(String)
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the attribute value.
     *
     * {@section Validation}
     * The amount of validation performed by this method is implementation dependent.
     * Usually, only the most basic constraints are verified. This is so for performance reasons
     * and also because some rules may be temporarily broken while constructing a feature.
     * A more exhaustive verification can be performed by invoking the {@link #validate()} method.
     *
     * @param value The new value.
     *
     * @see DefaultFeature#setPropertyValue(String, Object)
     */
    public void setValue(final T value) {
        this.value = value;
    }

    /**
     * Verifies if the current attribute value mets the constraints defined by the attribute type.
     * This method returns {@linkplain org.apache.sis.metadata.iso.quality.DefaultDataQuality#getReports()
     * reports} for all constraint violations found, if any.
     *
     * <div class="note"><b>Example:</b> given an attribute named “population” with [1 … 1] cardinality,
     * if no value has been assigned to that attribute, then this {@code validate()} method will return
     * the following data quality report:
     *
     * {@preformat text
     *   Data quality
     *     ├─Scope
     *     │   └─Level………………………………………………… Attribute
     *     └─Report
     *         ├─Measure identification
     *         │   └─Code………………………………………… population
     *         ├─Evaluation method type…… Direct internal
     *         └─Result
     *             ├─Explanation……………………… Missing value for “population” property.
     *             └─Pass………………………………………… false
     * }
     * </div>
     *
     * This attribute is valid if this method does not report any
     * {@linkplain org.apache.sis.metadata.iso.quality.DefaultConformanceResult conformance result} having a
     * {@linkplain org.apache.sis.metadata.iso.quality.DefaultConformanceResult#pass() pass} value of {@code false}.
     *
     * @return Reports on all constraint violations found.
     *
     * @see DefaultFeature#validate()
     */
    /*
     * API NOTE: this method is final for now because if we allowed users to override it, users would
     * expect their method to be invoked by DefaultFeature.validate(). But this is not yet the case.
     */
    public final DataQuality validate() {
        final Validator v = new Validator(ScopeCode.ATTRIBUTE);
        v.validate(type, value);
        return v.quality;
    }

    /**
     * Returns a shallow copy of this attribute.
     * The attribute {@linkplain #getValue() value} is <strong>not</strong> cloned.
     *
     * @return A clone of this attribute.
     */
    @Override
    @SuppressWarnings("unchecked")
    public DefaultAttribute<T> clone() {
        final DefaultAttribute<T> clone;
        try {
            clone = (DefaultAttribute<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // Should never happen since we are cloneable.
        }
        return clone;
    }

    /**
     * Returns a hash code value for this attribute type.
     *
     * @return A hash code value.
     */
    @Override
    public int hashCode() {
        return type.hashCode() + Objects.hashCode(value);
    }

    /**
     * Compares this attribute with the given object for equality.
     *
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == getClass()) {
            final DefaultAttribute<?> that = (DefaultAttribute<?>) obj;
            return type.equals(that.type) &&
                   Objects.equals(value, that.value);
        }
        return false;
    }

    /**
     * Returns a string representation of this attribute.
     * The returned string is for debugging purpose and may change in any future SIS version.
     *
     * @return A string representation of this attribute for debugging purpose.
     */
    @Debug
    @Override
    public String toString() {
        final StringBuilder buffer = type.toString("Attribute", Classes.getShortName(type.getValueClass()));
        if (value != null) {
            buffer.append(" = ").append(value);
        }
        return buffer.toString();
    }
}
