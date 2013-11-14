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
package org.apache.sis.referencing;

import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.apache.sis.test.mock.GeodeticDatumMock;
import org.apache.sis.test.DependsOn;
import org.apache.sis.test.TestCase;
import org.opengis.test.Validators;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests the {@link StandardDefinitions} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.4
 * @version 0.4
 * @module
 */
@DependsOn({
  org.apache.sis.referencing.datum.DefaultEllipsoidTest.class
})
public final strictfp class StandardDefinitionsTest extends TestCase {
    /**
     * Compares the values created by {@link StandardDefinitions#createEllipsoid(short)} against
     * the {@link GeodeticObjects} constants. Actually this is more a {@code GeodeticDatumMock}
     * test than a {@code StandardDefinitions} one - in case of test failure, both classes could
     * be at fault.
     */
    @Test
    public void testCreateEllipsoid() {
        final GeodeticDatum[] mocks = new GeodeticDatum[] {
            GeodeticDatumMock.WGS84,
            GeodeticDatumMock.WGS72,
            GeodeticDatumMock.NAD83,
            GeodeticDatumMock.NAD27,
            GeodeticDatumMock.SPHERE
        };
        final GeodeticObjects[] enums = new GeodeticObjects[] {
            GeodeticObjects.WGS84,
            GeodeticObjects.WGS72,
            GeodeticObjects.NAD83,
            GeodeticObjects.NAD27,
            GeodeticObjects.SPHERE
        };
        assertEquals(mocks.length, enums.length);
        for (int i=0; i<mocks.length; i++) {
            final Ellipsoid mock = mocks[i].getEllipsoid();
            final Ellipsoid def  = StandardDefinitions.createEllipsoid(enums[i].ellipsoid);
            Validators.validate(def);
            assertEquals("semiMajorAxis",     mock.getSemiMajorAxis(),     def.getSemiMajorAxis(), 0);
            assertEquals("semiMinorAxis",     mock.getSemiMinorAxis(),     def.getSemiMinorAxis(), 0);
            assertEquals("inverseFlattening", mock.getInverseFlattening(), def.getInverseFlattening(), mock.isIvfDefinitive() ? 0 : 1E-11);
            assertEquals("isIvfDefinitive",   mock.isIvfDefinitive(),      def.isIvfDefinitive());
            assertEquals("isSphere",          mock.isSphere(),             def.isSphere());
        }
    }
}
