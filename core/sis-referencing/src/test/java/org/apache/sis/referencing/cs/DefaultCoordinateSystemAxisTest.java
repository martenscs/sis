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
package org.apache.sis.referencing.cs;

import javax.measure.unit.NonSI;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.RangeMeaning;
import org.opengis.test.Validators;
import org.apache.sis.util.ComparisonMode;
import org.apache.sis.test.DependsOnMethod;
import org.apache.sis.test.DependsOn;
import org.apache.sis.test.TestCase;
import org.junit.Test;

import static org.apache.sis.referencing.Assert.*;
import static org.apache.sis.referencing.cs.HardCodedAxes.*;
import static org.apache.sis.referencing.IdentifiedObjects.getProperties;


/**
 * Tests {@link DefaultCoordinateSystemAxis}.
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   0.4 (derived from geotk-2.2)
 * @version 0.4
 * @module
 */
@DependsOn({
    DirectionAlongMeridianTest.class,
    org.apache.sis.referencing.AbstractIdentifiedObjectTest.class
})
public final strictfp class DefaultCoordinateSystemAxisTest extends TestCase {
    /**
     * Validates the {@link HardCodedAxes} constants.
     */
    @Test
    public void validate() {
        Validators.validate(GEODETIC_LONGITUDE);
        Validators.validate(GEODETIC_LATITUDE);
        Validators.validate(LONGITUDE_gon);
        Validators.validate(LATITUDE_gon);
        Validators.validate(ELLIPSOIDAL_HEIGHT);
        Validators.validate(GRAVITY_RELATED_HEIGHT);
        Validators.validate(ALTITUDE);
        Validators.validate(DEPTH);
        Validators.validate(GEOCENTRIC_RADIUS);
        Validators.validate(SPHERICAL_LONGITUDE);
        Validators.validate(SPHERICAL_LATITUDE);
        Validators.validate(X);
        Validators.validate(Y);
        Validators.validate(Z);
        Validators.validate(GEOCENTRIC_X);
        Validators.validate(GEOCENTRIC_Y);
        Validators.validate(GEOCENTRIC_Z);
        Validators.validate(EASTING);
        Validators.validate(WESTING);
        Validators.validate(NORTHING);
        Validators.validate(SOUTHING);
        Validators.validate(TIME);
        Validators.validate(COLUMN);
        Validators.validate(ROW);
        Validators.validate(DISPLAY_X);
        Validators.validate(DISPLAY_Y);
    }

    /**
     * Tests WKT formatting of predefined constants.
     */
    @Test
    public void testWKT() {
        assertWktEquals("AXIS[“x”, EAST]",                   X);
        assertWktEquals("AXIS[“y”, NORTH]",                  Y);
        assertWktEquals("AXIS[“z”, UP]",                     Z);
        assertWktEquals("AXIS[“Longitude”, EAST]",           LONGITUDE_gon);
        assertWktEquals("AXIS[“Latitude”, NORTH]",           LATITUDE_gon);
        assertWktEquals("AXIS[“Altitude”, UP]",              ALTITUDE);
        assertWktEquals("AXIS[“Time”, FUTURE]",              TIME);
        assertWktEquals("AXIS[“Geodetic longitude”, EAST]",  GEODETIC_LONGITUDE);
        assertWktEquals("AXIS[“Spherical longitude”, EAST]", SPHERICAL_LONGITUDE);
        assertWktEquals("AXIS[“Geodetic latitude”, NORTH]",  GEODETIC_LATITUDE);
        assertWktEquals("AXIS[“Spherical latitude”, NORTH]", SPHERICAL_LATITUDE);
    }

    /**
     * Tests the {@link DefaultCoordinateSystemAxis#isHeuristicMatchForName(String)} method.
     */
    @Test
    public void testIsHeuristicMatchForName() {
        assertTrue (LONGITUDE_gon.isHeuristicMatchForName(GEODETIC_LONGITUDE.getName().getCode()));
        assertFalse(LONGITUDE_gon.isHeuristicMatchForName(GEODETIC_LATITUDE .getName().getCode()));
        assertFalse(LONGITUDE_gon.isHeuristicMatchForName(ALTITUDE          .getName().getCode()));
        assertTrue (LATITUDE_gon .isHeuristicMatchForName(GEODETIC_LATITUDE .getName().getCode()));
        assertFalse(X            .isHeuristicMatchForName(LONGITUDE_gon     .getName().getCode()));
        assertFalse(X            .isHeuristicMatchForName(EASTING           .getName().getCode()));
        assertFalse(X            .isHeuristicMatchForName(NORTHING          .getName().getCode()));
    }

    /**
     * Tests the comparison of some axis, ignoring metadata.
     */
    @Test
    @DependsOnMethod("testIsHeuristicMatchForName")
    public void testEqualsIgnoreMetadata() {
        /*
         * Defines, only for the purpose of this test, axis constants identical to
         * (GEODETIC_LONGITUDE, GEODETIC_LATITUDE) except for the name.
         */
        final DefaultCoordinateSystemAxis LONGITUDE = new DefaultCoordinateSystemAxis(getProperties(LONGITUDE_gon),
                "λ", AxisDirection.EAST, NonSI.DEGREE_ANGLE, -180, 180, RangeMeaning.WRAPAROUND);
        final DefaultCoordinateSystemAxis LATITUDE = new DefaultCoordinateSystemAxis(getProperties(LATITUDE_gon),
                "φ", AxisDirection.NORTH, NonSI.DEGREE_ANGLE, -90, 90, RangeMeaning.EXACT);

        assertFalse("X",         X        .equals(GEOCENTRIC_X,        ComparisonMode.IGNORE_METADATA));
        assertFalse("Longitude", LONGITUDE.equals(GEODETIC_LONGITUDE,  ComparisonMode.STRICT));
        assertFalse("Longitude", LONGITUDE.equals(SPHERICAL_LONGITUDE, ComparisonMode.STRICT));
        assertFalse("Longitude", LONGITUDE.equals(SPHERICAL_LONGITUDE, ComparisonMode.IGNORE_METADATA));
        /*
         * Tests aliases in the special "longitude" and "latitude" cases.
         */
        assertTrue ("Longitude", LONGITUDE.equals(GEODETIC_LONGITUDE,  ComparisonMode.IGNORE_METADATA));
        assertTrue ("Latitude",  LATITUDE .equals(GEODETIC_LATITUDE,   ComparisonMode.IGNORE_METADATA));
        assertFalse("Lon/Lat",   LATITUDE .equals(LONGITUDE,           ComparisonMode.IGNORE_METADATA));
        /*
         * Ensures that difference in "wraparound" ranges causes the axes to be considered different.
         */
        assertFalse(GEODETIC_LONGITUDE.equals(SHIFTED_LONGITUDE, ComparisonMode.IGNORE_METADATA));
    }

    /**
     * Tests serialization.
     */
    @Test
    public void testSerialization() {
        assertSerializedEquals(X);
        assertSerializedEquals(GEOCENTRIC_X);
        assertSerializedEquals(GEODETIC_LONGITUDE);
    }
}
