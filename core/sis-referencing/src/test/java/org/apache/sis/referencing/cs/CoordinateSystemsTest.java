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

import javax.measure.converter.ConversionException;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.internal.referencing.AxisDirections;
import org.apache.sis.test.DependsOnMethod;
import org.apache.sis.test.DependsOn;
import org.apache.sis.test.TestCase;
import org.junit.Test;

import static java.lang.StrictMath.*;
import static java.util.Collections.singletonMap;
import static org.opengis.referencing.IdentifiedObject.NAME_KEY;
import static org.apache.sis.referencing.cs.CoordinateSystems.*;
import static org.apache.sis.test.Assert.*;


/**
 * Tests the {@link CoordinateSystems} class.
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   0.4 (derived from geotk-2.2)
 * @version 0.4
 * @module
 */
@DependsOn(DirectionAlongMeridianTest.class)
public final strictfp class CoordinateSystemsTest extends TestCase {
    /**
     * Tolerance threshold for strict floating point comparisons.
     */
    static final double STRICT = 0;

    /**
     * Tests {@link CoordinateSystems#parseAxisDirection(String)}.
     */
    @Test
    public void testParseAxisDirection() {
        assertEquals("NORTH",            AxisDirection.NORTH,            parseAxisDirection("NORTH"));
        assertEquals("north",            AxisDirection.NORTH,            parseAxisDirection("north"));
        assertEquals("  north ",         AxisDirection.NORTH,            parseAxisDirection("  north "));
        assertEquals("east",             AxisDirection.EAST,             parseAxisDirection("east"));
        assertEquals("NORTH_EAST",       AxisDirection.NORTH_EAST,       parseAxisDirection("NORTH_EAST"));
        assertEquals("north-east",       AxisDirection.NORTH_EAST,       parseAxisDirection("north-east"));
        assertEquals("north east",       AxisDirection.NORTH_EAST,       parseAxisDirection("north east"));
        assertEquals("south-south-east", AxisDirection.SOUTH_SOUTH_EAST, parseAxisDirection("south-south-east"));
        assertEquals("South along 180°", parseAxisDirection("South along 180 deg").name());
        assertEquals("South along 180°", parseAxisDirection("South along 180°").name());
        assertEquals("South along 180°", parseAxisDirection(" SOUTH  along  180 ° ").name());
        assertEquals("South along 90°E", parseAxisDirection("south along 90 deg east").name());
        assertEquals("South along 90°E", parseAxisDirection("south along 90°e").name());
        assertEquals("North along 45°E", parseAxisDirection("north along 45 deg e").name());
        assertEquals("North along 45°W", parseAxisDirection("north along 45 deg west").name());
    }

    /**
     * Tests the {@link CoordinateSystems#getCompassAngle(AxisDirection, AxisDirection)} method.
     */
    @Test
    public void testGetCompassAngle() {
        final AxisDirection[] compass = new AxisDirection[] {
            AxisDirection.NORTH,
            AxisDirection.NORTH_NORTH_EAST,
            AxisDirection.NORTH_EAST,
            AxisDirection.EAST_NORTH_EAST,
            AxisDirection.EAST,
            AxisDirection.EAST_SOUTH_EAST,
            AxisDirection.SOUTH_EAST,
            AxisDirection.SOUTH_SOUTH_EAST,
            AxisDirection.SOUTH,
            AxisDirection.SOUTH_SOUTH_WEST,
            AxisDirection.SOUTH_WEST,
            AxisDirection.WEST_SOUTH_WEST,
            AxisDirection.WEST,
            AxisDirection.WEST_NORTH_WEST,
            AxisDirection.NORTH_WEST,
            AxisDirection.NORTH_NORTH_WEST
        };
        assertEquals(compass.length, COMPASS_DIRECTION_COUNT);
        final int base = AxisDirection.NORTH.ordinal();
        final int h = compass.length / 2;
        for (int i=0; i<compass.length; i++) {
            final AxisDirection direction = compass[i];
            final AxisDirection opposite  = AxisDirections.opposite(direction);
            final String        message   = direction.name();
            int io = i+h, in = i;
            if (i >= h) io -= COMPASS_DIRECTION_COUNT;
            if (i >  h) in -= COMPASS_DIRECTION_COUNT;
            assertEquals(message, base + i,  direction.ordinal());
            assertEquals(message, base + io, opposite.ordinal());
            assertEquals(message, 0,     getCompassAngle(direction, direction));
            assertEquals(message, h, abs(getCompassAngle(direction, opposite)));
            assertEquals(message, in,    getCompassAngle(direction, AxisDirection.NORTH));
        }
    }

    /**
     * Tests {@link CoordinateSystems#angle(AxisDirection, AxisDirection)}.
     */
    @Test
    @DependsOnMethod("testGetCompassAngle")
    public void testAngle() {
        assertEquals(    0, angle(AxisDirection.EAST,             AxisDirection.EAST),       STRICT);
        assertEquals(  +90, angle(AxisDirection.EAST,             AxisDirection.NORTH),      STRICT);
        assertEquals(  -90, angle(AxisDirection.NORTH,            AxisDirection.EAST),       STRICT);
        assertEquals(  +90, angle(AxisDirection.WEST,             AxisDirection.SOUTH),      STRICT);
        assertEquals(  -90, angle(AxisDirection.SOUTH,            AxisDirection.WEST),       STRICT);
        assertEquals( -180, angle(AxisDirection.NORTH,            AxisDirection.SOUTH),      STRICT);
        assertEquals(  180, angle(AxisDirection.SOUTH,            AxisDirection.NORTH),      STRICT);
        assertEquals(   45, angle(AxisDirection.NORTH_EAST,       AxisDirection.NORTH),      STRICT);
        assertEquals( 22.5, angle(AxisDirection.NORTH_NORTH_EAST, AxisDirection.NORTH),      STRICT);
        assertEquals(-22.5, angle(AxisDirection.NORTH_NORTH_WEST, AxisDirection.NORTH),      STRICT);
        assertEquals(   45, angle(AxisDirection.SOUTH,            AxisDirection.SOUTH_EAST), STRICT);
    }

    /**
     * Tests {@link CoordinateSystems#angle(AxisDirection, AxisDirection)} using directions parsed from text.
     */
    @Test
    @DependsOnMethod({"testParseAxisDirection", "testAngle"})
    public void testAngleAlongMeridians() {
        compareAngle( 90.0, "West",                    "South");
        compareAngle(-90.0, "South",                   "West");
        compareAngle( 45.0, "South",                   "South-East");
        compareAngle(-22.5, "North-North-West",        "North");
        compareAngle(-22.5, "North_North_West",        "North");
        compareAngle(-22.5, "North North West",        "North");
        compareAngle( 90.0, "North along 90 deg East", "North along 0 deg");
        compareAngle( 90.0, "South along 180 deg",     "South along 90 deg West");
        compareAngle(   90, "North along 90°E",        "North along 0°");
        compareAngle(  135, "North along 90°E",        "North along 45°W");
        compareAngle( -135, "North along 45°W",        "North along 90°E");
    }

    /**
     * Compare the angle between the specified directions.
     */
    private static void compareAngle(final double expected, final String source, final String target) {
        final AxisDirection dir1 = parseAxisDirection(source);
        final AxisDirection dir2 = parseAxisDirection(target);
        assertNotNull(source, dir1);
        assertNotNull(target, dir2);
        assertEquals(expected, angle(dir1, dir2), STRICT);
    }

    /**
     * Tests {@link CoordinateSystems#swapAndScaleAxes(CoordinateSystem, CoordinateSystem)} for (λ,φ) ↔ (φ,λ).
     * This very common conversion is of critical importance to Apache SIS.
     *
     * @throws ConversionException Should not happen.
     */
    @Test
    public void testSwapAndScaleAxes2D() throws ConversionException {
        final CoordinateSystem λφ = new DefaultEllipsoidalCS(singletonMap(NAME_KEY, "(λ,φ)"),
                CommonAxes.GEODETIC_LONGITUDE,
                CommonAxes.GEODETIC_LATITUDE);
        final CoordinateSystem φλ = new DefaultEllipsoidalCS(singletonMap(NAME_KEY, "(φ,λ)"),
                CommonAxes.GEODETIC_LATITUDE,
                CommonAxes.GEODETIC_LONGITUDE);
        final Matrix expected = Matrices.create(3, 3, new double[] {
                0, 1, 0,
                1, 0, 0,
                0, 0, 1});
        assertTrue(swapAndScaleAxes(λφ, λφ).isIdentity());
        assertTrue(swapAndScaleAxes(φλ, φλ).isIdentity());
        assertMatrixEquals("(λ,φ) → (φ,λ)", expected, swapAndScaleAxes(λφ, φλ), STRICT);
        assertMatrixEquals("(φ,λ) → (λ,φ)", expected, swapAndScaleAxes(φλ, λφ), STRICT);
    }

    /**
     * Tests {@link CoordinateSystems#swapAndScaleAxes(CoordinateSystem, CoordinateSystem)} for (λ,φ,h) ↔ (φ,λ,h).
     * This very common conversion is of critical importance to Apache SIS.
     *
     * @throws ConversionException Should not happen.
     */
    @Test
    @DependsOnMethod("testSwapAndScaleAxes2D")
    public void testSwapAndScaleAxes3D() throws ConversionException {
        final CoordinateSystem λφh = new DefaultEllipsoidalCS(singletonMap(NAME_KEY, "(λ,φ,h)"),
                CommonAxes.GEODETIC_LONGITUDE,
                CommonAxes.GEODETIC_LATITUDE,
                CommonAxes.ELLIPSOIDAL_HEIGHT);
        final CoordinateSystem φλh = new DefaultEllipsoidalCS(singletonMap(NAME_KEY, "(φ,λ,h)"),
                CommonAxes.GEODETIC_LATITUDE,
                CommonAxes.GEODETIC_LONGITUDE,
                CommonAxes.ELLIPSOIDAL_HEIGHT);
        final Matrix expected = Matrices.create(4, 4, new double[] {
                0, 1, 0, 0,
                1, 0, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1});
        assertTrue(swapAndScaleAxes(λφh, λφh).isIdentity());
        assertTrue(swapAndScaleAxes(φλh, φλh).isIdentity());
        assertMatrixEquals("(λ,φ,h) → (φ,λ,h)", expected, swapAndScaleAxes(λφh, φλh), STRICT);
        assertMatrixEquals("(φ,λ,h) → (λ,φ,h)", expected, swapAndScaleAxes(φλh, λφh), STRICT);
    }

    /**
     * Tests {@link CoordinateSystems#swapAndScaleAxes(CoordinateSystem, CoordinateSystem)}
     * with a more arbitrary case, which include unit conversions.
     *
     * @throws ConversionException Should not happen.
     */
    @Test
    @DependsOnMethod("testSwapAndScaleAxes3D")
    public void testSwapAndScaleAxes() throws ConversionException {
        final CoordinateSystem hxy = new DefaultCartesianCS(singletonMap(NAME_KEY, "(h,x,y)"),
                CommonAxes.HEIGHT_cm,
                CommonAxes.EASTING,
                CommonAxes.NORTHING);
        final CoordinateSystem yxh = new DefaultCartesianCS(singletonMap(NAME_KEY, "(y,x,h)"),
                CommonAxes.SOUTHING,
                CommonAxes.EASTING,
                CommonAxes.DEPTH);
        assertTrue(swapAndScaleAxes(hxy, hxy).isIdentity());
        assertTrue(swapAndScaleAxes(yxh, yxh).isIdentity());
        assertMatrixEquals("(h,x,y) → (y,x,h)", Matrices.create(4, 4, new double[] {
                0,    0,   -1,    0,
                0,    1,    0,    0,
               -0.01, 0,    0,    0,
                0,    0,    0,    1}), swapAndScaleAxes(hxy, yxh), STRICT);

        assertMatrixEquals("(y,x,h) → (h,x,y)", Matrices.create(4, 4, new double[] {
                0,    0, -100,    0,
                0,    1,    0,    0,
               -1,    0,    0,    0,
                0,    0,    0,    1}), swapAndScaleAxes(yxh, hxy), STRICT);
    }
}
