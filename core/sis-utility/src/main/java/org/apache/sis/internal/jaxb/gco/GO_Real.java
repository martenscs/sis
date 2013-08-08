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
package org.apache.sis.internal.jaxb.gco;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * Surrounds double values by {@code <gco:Real>}.
 * The ISO-19139 standard requires most types to be surrounded by an element representing the value type.
 * The JAXB default behavior is to marshal primitive Java types directly, without such wrapper element.
 * The role of this class is to add the {@code <gco:…>} wrapper element required by ISO 19139.
 *
 * {@section Relationship with <code>GO_Decimal</code>}
 * This adapter is identical to {@link GO_Decimal} except for the element name, which is {@code "Real"}
 * instead than {@code "Decimal"}. This adapter is the most widely used one in IS 19139 XML schema.
 * The few exceptions are documented in {@link GO_Decimal}.
 *
 * @author  Cédric Briançon (Geomatys)
 * @since   0.3 (derived from geotk-2.5)
 * @version 0.3
 * @module
 */
public final class GO_Real extends XmlAdapter<GO_Real, Double> {
    /**
     * The double value to handle.
     */
    @XmlElement(name = "Real")
    public Double value;

    /**
     * Empty constructor used only by JAXB.
     */
    public GO_Real() {
    }

    /**
     * Constructs an adapter for this value.
     *
     * @param value The value.
     */
    private GO_Real(final Double value) {
        this.value = value;
    }

    /**
     * Allows JAXB to generate a Double object using the value found in the adapter.
     *
     * @param value The value extract from the adapter.
     * @return A double object.
     */
    @Override
    public Double unmarshal(final GO_Real value) {
        return (value != null) ? value.value : null;
    }

    /**
     * Allows JAXB to change the result of the marshalling process, according to the
     * ISO-19139 standard and its requirements about primitive types.
     *
     * @param value The double value we want to surround by an element representing its type.
     * @return An adaptation of the double value, that is to say a double value surrounded
     *         by {@code <gco:Real>} element.
     */
    @Override
    public GO_Real marshal(final Double value) {
        return (value != null) ? new GO_Real(value) : null;
    }
}
