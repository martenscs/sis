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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.measure.unit.NonSI;
import org.opengis.referencing.cs.AxisDirection;
import org.apache.sis.util.iso.Types;
import org.apache.sis.measure.Longitude;
import org.apache.sis.internal.util.Numerics;
import org.apache.sis.internal.referencing.AxisDirections;
import org.apache.sis.io.wkt.FormattableObject;
import org.apache.sis.io.wkt.Formatter;


/**
 * Parses {@linkplain AxisDirection axis direction} of the kind "<cite>South along 90 deg East</cite>".
 * Those directions are used in the EPSG database for polar stereographic projections.
 *
 * {@section Reference meridian}
 * This class does not know whether the meridian is relative to Greenwich or any other reference meridian.
 * The reference meridian shall be inferred from the geodetic datum of the {@code GeographicCRS} instance
 * that contains (through its coordinate system) the axes having those directions. This is consistent with
 * ISO 19162 §7.5.4(iv) - WKT 2 formatting.
 *
 * {@section Immutability and thread safety}
 * This final class is immutable and thus inherently thread-safe.
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   0.4 (derived from geotk-2.4)
 * @version 0.4
 * @module
 */
final class DirectionAlongMeridian extends FormattableObject implements Comparable<DirectionAlongMeridian> {
    /**
     * A parser for EPSG axis names. Examples:
     *
     * <ul>
     *   <li>"<cite>South along 180 deg</cite>"</li>
     *   <li>"<cite>South along 90 deg East</cite>"</li>
     * </ul>
     */
    private static final Pattern EPSG = Pattern.compile(
            "(\\p{Graph}+)\\s+along\\s+([\\-\\p{Digit}\\.]+)(?:\\s+deg|\\s*°)\\s*(\\p{Graph}+)?",
            Pattern.CASE_INSENSITIVE);

    /**
     * The base directions we are interested in.
     * Any direction not in this group will be rejected by our parser.
     */
    private static final AxisDirection[] BASE_DIRECTIONS = {
        AxisDirection.NORTH,
        AxisDirection.SOUTH,
        AxisDirection.EAST,
        AxisDirection.WEST
    };

    /**
     * The direction. Will be created only when first needed.
     *
     * @see #getDirection()
     */
    private transient AxisDirection direction;

    /**
     * The base direction, which must be {@link AxisDirection#NORTH} or {@link AxisDirection#SOUTH}.
     */
    public final AxisDirection baseDirection;

    /**
     * The meridian, in degrees.
     */
    public final double meridian;

    /**
     * Creates a direction.
     */
    private DirectionAlongMeridian(final AxisDirection baseDirection, final double meridian) {
        this.baseDirection = baseDirection;
        this.meridian      = meridian;
    }

    /**
     * Returns the direction along meridian for the specified axis direction, or {@code null} if none.
     *
     * <p>TIP: caller can check {@link AxisDirections#isUserDefined(AxisDirection)} before to invoke this method
     * for avoiding {@code DirectionAlongMeridian} initialization in the common case where it is not needed.</p>
     */
    public static DirectionAlongMeridian parse(final AxisDirection direction) {
        final DirectionAlongMeridian candidate = parse(direction.name());
        if (candidate != null) {
            candidate.direction = direction;
        }
        return candidate;
    }

    /**
     * If the specified name is a direction along some specific meridian,
     * returns information about that. Otherwise returns {@code null}.
     */
    public static DirectionAlongMeridian parse(final String name) {
        final Matcher m = EPSG.matcher(name);
        if (!m.matches()) {
            // Not the expected pattern.
            return null;
        }
        String group = m.group(1);
        final AxisDirection baseDirection = AxisDirections.find(group, BASE_DIRECTIONS);
        if (baseDirection == null || !AxisDirection.NORTH.equals(AxisDirections.absolute(baseDirection))) {
            // We expected "North" or "South" direction.
            return null;
        }
        group = m.group(2);
        double meridian;
        try {
            meridian = Double.parseDouble(group);
        } catch (NumberFormatException exception) {
            // Not a legal axis direction. Just ignore the exception,
            // since we are supposed to return 'null' in this situation.
            return null;
        }
        if (!(meridian >= Longitude.MIN_VALUE && meridian <= Longitude.MAX_VALUE)) {
            // Meridian is NaN or is not in the valid range.
            return null;
        }
        group = m.group(3);
        if (group != null) {
            final AxisDirection sgn = AxisDirections.find(group, BASE_DIRECTIONS);
            if (sgn == null) {
                return null;
            }
            final AxisDirection abs = AxisDirections.absolute(sgn);
            if (!AxisDirection.EAST.equals(abs)) {
                // We expected "East" or "West" direction.
                return null;
            }
            if (sgn != abs) {
                meridian = -meridian;
            }
        }
        return new DirectionAlongMeridian(baseDirection, meridian);
    }

    /**
     * Returns the axis direction for this object. If a suitable axis direction already exists,
     * it will be returned. Otherwise a new one is created and returned.
     */
    public AxisDirection getDirection() {
        if (direction == null) {
            final String name = toString();
            direction = AxisDirections.valueOf(name);
            if (direction == null) {
                direction = Types.forCodeName(AxisDirection.class, name, true);
            }
        }
        return direction;
    }

    /**
     * Returns the arithmetic (counterclockwise) angle from this direction to the specified direction,
     * in degrees. This method returns a value between -180° and +180°, or {@link Double#NaN} if the
     * {@linkplain #baseDirection base directions} don't match.
     * A positive angle denote a right-handed system.
     *
     * {@example The angle from "<cite>North along 90 deg East</cite>" to "<cite>North along 0 deg</cite> is 90°.}
     */
    public double angle(final DirectionAlongMeridian other) {
        if (!baseDirection.equals(other.baseDirection)) {
            return Double.NaN;
        }
        /*
         * Example: angle between (NORTH along 90°E, NORTH along 0°) shall be +90°
         */
        double angle = Longitude.normalize(meridian - other.meridian);
        /*
         * Reverses the sign for axis oriented toward SOUTH,
         * so a positive angle is a right-handed system.
         */
        if (AxisDirections.isOpposite(baseDirection)) {
            angle = -angle;
        }
        return angle;
    }

    /**
     * Compares this direction with the specified one for order. This method tries to reproduce
     * the ordering used for the majority of coordinate systems in the EPSG database, i.e. the
     * ordering of a right-handed coordinate system. Examples of ordered pairs that we should
     * get (extracted from the EPSG database):
     *
     * <table class="sis">
     *   <tr><td>North along 90° East,</td>  <td>North along 0°</td></tr>
     *   <tr><td>North along 75° West,</td>  <td>North along 165° West</td></tr>
     *   <tr><td>South along 90° West,</td>  <td>South along 0°</td></tr>
     *   <tr><td>South along 180°,</td>      <td>South along 90° West</td></tr>
     *   <tr><td>North along 130° West</td>  <td>North along 140° East</td></tr>
     * </table>
     */
    @Override
    public int compareTo(final DirectionAlongMeridian that) {
        final int c = baseDirection.compareTo(that.baseDirection);
        if (c != 0) {
            return c;
        }
        final double angle = angle(that);
        if (angle < 0) return +1;  // Really the opposite sign.
        if (angle > 0) return -1;  // Really the opposite sign.
        return 0;
    }

    /**
     * Tests this object for equality with the specified one.
     * This method is used mostly for assertions.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof DirectionAlongMeridian) {
            final DirectionAlongMeridian that = (DirectionAlongMeridian) object;
            return baseDirection.equals(that.baseDirection) &&
                   Numerics.equals(meridian, that.meridian);
        }
        return false;
    }

    /**
     * Returns a hash code value, for consistency with {@link #equals}.
     */
    @Override
    public int hashCode() {
        return Numerics.hashCode(Double.doubleToLongBits(meridian) + baseDirection.hashCode());
    }

    /**
     * Returns a string representation of this direction, using a syntax matching the one used
     * by EPSG. This string representation will be used for creating a new {@link AxisDirection}.
     * The generated name should be identical to EPSG name, but we use the generated one anyway
     * (rather than the one provided by EPSG) in order to make sure that we create a single
     * {@link AxisDirection} for a given direction; we avoid potential differences like lower
     * versus upper cases, amount of white space, <i>etc</i>.
     */
    @Override
    public String toString() {
        String name = baseDirection.name();
        final int length = name.length();
        final StringBuilder buffer = new StringBuilder(length);
        for (int i=0; i<length;) {
            final int c = name.codePointAt(i);
            buffer.appendCodePoint(i == 0 ? Character.toUpperCase(c) : Character.toLowerCase(c));
            i += Character.charCount(c);
        }
        buffer.append(" along ");
        final double md = Math.abs(meridian);
        final int    mi = (int) md;
        if (md == mi) {
            buffer.append(mi);
        } else {
            buffer.append(md);
        }
        buffer.append('°');
        if (md != 0 && md != Longitude.MAX_VALUE) {
            buffer.append(meridian < 0 ? 'W' : 'E');
        }
        name = buffer.toString();
        assert EPSG.matcher(name).matches() : name;
        return name;
    }

    /**
     * Formats this object as a <cite>Well Known Text</cite> {@code Meridian[…]} element.
     * This element contains the meridian value and the unit of measurement.
     * The unit is currently fixed to degrees, but this may change in any future implementation.
     *
     * {@note <code>Meridian</code> is defined in the WKT 2 specification only.}
     *
     * @param  formatter The formatter where to format the inner content of this WKT element.
     * @return {@code "Meridian"}.
     */
    @Override
    protected String formatTo(final Formatter formatter) {
        formatter.append(meridian);
        formatter.append(NonSI.DEGREE_ANGLE);
        return "Meridian";
    }
}
