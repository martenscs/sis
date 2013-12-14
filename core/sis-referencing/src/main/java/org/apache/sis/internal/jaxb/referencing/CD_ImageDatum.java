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
package org.apache.sis.internal.jaxb.referencing;

import javax.xml.bind.annotation.XmlElement;
import org.opengis.referencing.datum.ImageDatum;
import org.apache.sis.internal.jaxb.gco.PropertyType;
import org.apache.sis.referencing.datum.DefaultImageDatum;


/**
 * JAXB adapter mapping implementing class to the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @author  Cédric Briançon (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.4 (derived from geotk-3.05)
 * @version 0.4
 * @module
 */
public final class CD_ImageDatum extends PropertyType<CD_ImageDatum, ImageDatum> {
    /**
     * Empty constructor for JAXB only.
     */
    public CD_ImageDatum() {
    }

    /**
     * Returns the GeoAPI interface which is bound by this adapter.
     * This method is indirectly invoked by the private constructor
     * below, so it shall not depend on the state of this object.
     *
     * @return {@code ImageDatum.class}
     */
    @Override
    protected Class<ImageDatum> getBoundType() {
        return ImageDatum.class;
    }

    /**
     * Constructor for the {@link #wrap} method only.
     */
    private CD_ImageDatum(final ImageDatum metadata) {
        super(metadata);
    }

    /**
     * Invoked by {@link PropertyType} at marshalling time for wrapping the given value
     * in a {@code <gml:imageDatum>} XML element.
     *
     * @param  value The element to marshall.
     * @return A {@code PropertyType} wrapping the given the element.
     */
    @Override
    protected CD_ImageDatum wrap(final ImageDatum value) {
        return new CD_ImageDatum(value);
    }

    /**
     * Invoked by JAXB at marshalling time for getting the actual element to write
     * inside the {@code <gml:imageDatum>} XML element.
     * This is the value or a copy of the value given in argument to the {@code wrap} method.
     *
     * @return The element to be marshalled.
     */
    @XmlElement(name = "ImageDatum")
    public DefaultImageDatum getElement() {
        return DefaultImageDatum.castOrCopy(metadata);
    }

    /**
     * Invoked by JAXB at unmarshalling time for storing the result temporarily.
     *
     * @param element The unmarshalled element.
     */
    public void setElement(final DefaultImageDatum element) {
        this.metadata = element;
    }
}
