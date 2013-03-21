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
package org.apache.sis.metadata.iso;

import java.util.Collection;
import java.io.Serializable;
import org.opengis.metadata.Identifier;
import net.jcip.annotations.ThreadSafe;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.ModifiableMetadata;


/**
 * The base class of ISO 19115 implementation classes. Each sub-classes implements one
 * of the ISO Metadata interface provided by <a href="http://www.geoapi.org">GeoAPI</a>.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
@ThreadSafe
public class ISOMetadata extends ModifiableMetadata implements Serializable {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 5730550742604669102L;

    /**
     * All identifiers associated with this metadata, or {@code null} if none.
     * This field is initialized to a non-null value when first needed.
     */
    protected Collection<Identifier> identifiers;

    /**
     * Constructs an initially empty metadata.
     */
    protected ISOMetadata() {
        super();
    }

    /**
     * Returns the metadata standard implemented by subclasses,
     * which is {@linkplain MetadataStandard#ISO_19115 ISO 19115}.
     */
    @Override
    public MetadataStandard getStandard() {
        return MetadataStandard.ISO_19115;
    }
}
