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
package org.apache.sis.referencing.datum;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import javax.xml.bind.JAXBException;
import org.apache.sis.metadata.iso.ImmutableIdentifier;
import org.apache.sis.metadata.iso.citation.HardCodedCitations;
import org.apache.sis.test.XMLTestCase;
import org.junit.Test;

import static org.apache.sis.test.ReferencingAssert.*;
import static org.apache.sis.test.TestUtilities.getSingleton;


/**
 * Tests the {@link DefaultTemporalDatum} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.4
 * @version 0.4
 * @module
 */
public final strictfp class DefaultTemporalDatumTest extends XMLTestCase {
    /**
     * An XML file in this package containing a vertical datum definition.
     */
    private static final String XML_FILE = "Modified Julian.xml";

    /**
     * November 17, 1858 at 00:00 UTC as a Java timestamp.
     */
    private static final long ORIGIN = -40587 * (24*60*60*1000L);

    /**
     * Tests XML marshalling.
     *
     * @throws JAXBException If an error occurred during marshalling.
     */
    @Test
    public void testMarshalling() throws JAXBException {
        final Map<String,Object> properties = new HashMap<String,Object>(4);
        assertNull(properties.put(DefaultTemporalDatum.IDENTIFIERS_KEY,
                new ImmutableIdentifier(HardCodedCitations.SIS, "SIS", "MJ")));
        assertNull(properties.put(DefaultTemporalDatum.NAME_KEY, "Modified Julian"));
        assertNull(properties.put(DefaultTemporalDatum.SCOPE_KEY, "History."));
        assertNull(properties.put(DefaultTemporalDatum.REMARKS_KEY,
                "Time measured as days since November 17, 1858 at 00:00 UTC."));

        final DefaultTemporalDatum datum = new DefaultTemporalDatum(properties, new Date(ORIGIN));
        assertMarshalEqualsFile(XML_FILE, datum, "xlmns:*", "xsi:schemaLocation");
    }

    /**
     * Tests XML unmarshalling.
     *
     * @throws JAXBException If an error occurred during unmarshalling.
     */
    @Test
    public void testUnmarshalling() throws JAXBException {
        final DefaultTemporalDatum datum = unmarshalFile(DefaultTemporalDatum.class, XML_FILE);
        assertIdentifierEquals("identifier", "SIS", "SIS", null, "MJ",
                getSingleton(datum.getIdentifiers()));
        assertEquals("name", "Modified Julian",
                datum.getName().getCode());
        assertEquals("remarks", "Time measured as days since November 17, 1858 at 00:00 UTC.",
                datum.getRemarks().toString());
        assertEquals("scope", "History.",
                datum.getScope().toString());
        assertEquals("origin", new Date(ORIGIN), datum.getOrigin());
    }
}
