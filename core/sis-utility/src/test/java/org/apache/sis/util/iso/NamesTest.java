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
package org.apache.sis.util.iso;

import org.opengis.util.LocalName;
import org.apache.sis.test.DependsOn;
import org.apache.sis.test.TestCase;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests the {@link Names} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.5
 * @version 0.5
 * @module
 */
@DependsOn(DefaultNameFactoryTest.class)
public final strictfp class NamesTest extends TestCase {
    /**
     * Tests {@link Names#createLocalName(CharSequence, String, CharSequence)}.
     */
    @Test
    public void testCreateLocalName() {
        final LocalName name = Names.createLocalName("http://www.opengis.net/gml/srs/epsg.xml", "#", "4326");
        assertEquals("http://www.opengis.net/gml/srs/epsg.xml",       name.scope().name().toString());
        assertEquals("4326",                                          name.toString());
        assertEquals("http://www.opengis.net/gml/srs/epsg.xml#4326",  name.toFullyQualifiedName().toString());
        assertEquals("{http://www.opengis.net/gml/srs/epsg.xml}4326", Names.toExpandedString(name));
    }
}
