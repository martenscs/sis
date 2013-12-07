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

import org.opengis.referencing.datum.VerticalDatumType;
import org.apache.sis.internal.jaxb.gml.CodeListAdapter;


/**
 * JAXB adapter mapping implementing class to the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.4 (derived from geotk-3.20)
 * @version 0.4
 * @module
 */
public final class CD_VerticalDatumType extends CodeListAdapter<VerticalDatumType> {
    /**
     * Empty constructor for JAXB only.
     */
    public CD_VerticalDatumType() {
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code VerticalDatumType.class}
     */
    @Override
    protected Class<VerticalDatumType> getCodeListClass() {
        return VerticalDatumType.class;
    }
}
