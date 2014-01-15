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
package org.apache.sis.referencing.crs;

import java.util.Map;
import javax.measure.unit.Unit;
import javax.measure.unit.NonSI;
import javax.measure.quantity.Angle;
import javax.xml.bind.annotation.XmlTransient;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.crs.GeographicCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.AbstractReferenceSystem;
import org.apache.sis.internal.referencing.AxisDirections;
import org.apache.sis.io.wkt.Formatter;
import org.apache.sis.measure.Units;


/**
 * A coordinate reference system based on an ellipsoidal approximation of the geoid.
 * This provides an accurate representation of the geometry of geographic features
 * for a large portion of the earth's surface.
 *
 * <p><b>Used with coordinate system type:</b>
 *   {@linkplain org.apache.sis.referencing.cs.DefaultEllipsoidalCS Ellipsoidal}.
 * </p>
 *
 * {@section Immutability and thread safety}
 * This class is immutable and thus thread-safe if the property <em>values</em> (not necessarily the map itself),
 * the coordinate system and the datum instances given to the constructor are also immutable. Unless otherwise noted
 * in the javadoc, this condition holds if all components were created using only SIS factories and static constants.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.4 (derived from geotk-1.2)
 * @version 0.4
 * @module
 */
@XmlTransient
public class DefaultGeographicCRS extends DefaultGeodeticCRS implements GeographicCRS {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 861224913438092335L;

    /**
     * Constructs a new object in which every attributes are set to a null value.
     * <strong>This is not a valid object.</strong> This constructor is strictly
     * reserved to JAXB, which will assign values to the fields using reflexion.
     */
    private DefaultGeographicCRS() {
    }

    /**
     * Creates a coordinate reference system from the given properties, datum and coordinate system.
     * The properties given in argument follow the same rules than for the
     * {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     * The following table is a reminder of main (not all) properties:
     *
     * <table class="sis">
     *   <tr>
     *     <th>Property name</th>
     *     <th>Value type</th>
     *     <th>Returned by</th>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#NAME_KEY}</td>
     *     <td>{@link org.opengis.referencing.ReferenceIdentifier} or {@link String}</td>
     *     <td>{@link #getName()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#ALIAS_KEY}</td>
     *     <td>{@link org.opengis.util.GenericName} or {@link CharSequence} (optionally as array)</td>
     *     <td>{@link #getAlias()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#IDENTIFIERS_KEY}</td>
     *     <td>{@link org.opengis.referencing.ReferenceIdentifier} (optionally as array)</td>
     *     <td>{@link #getIdentifiers()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#REMARKS_KEY}</td>
     *     <td>{@link org.opengis.util.InternationalString} or {@link String}</td>
     *     <td>{@link #getRemarks()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.datum.Datum#DOMAIN_OF_VALIDITY_KEY}</td>
     *     <td>{@link org.opengis.metadata.extent.Extent}</td>
     *     <td>{@link #getDomainOfValidity()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.datum.Datum#SCOPE_KEY}</td>
     *     <td>{@link org.opengis.util.InternationalString} or {@link String}</td>
     *     <td>{@link #getScope()}</td>
     *   </tr>
     * </table>
     *
     * @param properties The properties to be given to the coordinate reference system.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultGeographicCRS(final Map<String,?> properties,
                                final GeodeticDatum datum,
                                final EllipsoidalCS cs)
    {
        super(properties, datum, cs);
    }

    /**
     * Constructs a new coordinate reference system with the same values than the specified one.
     * This copy constructor provides a way to convert an arbitrary implementation into a SIS one
     * or a user-defined one (as a subclass), usually in order to leverage some implementation-specific API.
     *
     * <p>This constructor performs a shallow copy, i.e. the properties are not cloned.</p>
     *
     * @param crs The coordinate reference system to copy.
     *
     * @see #castOrCopy(GeographicCRS)
     */
    protected DefaultGeographicCRS(final GeographicCRS crs) {
        super(crs);
    }

    /**
     * Returns a SIS coordinate reference system implementation with the same values than the given
     * arbitrary implementation. If the given object is {@code null}, then this method returns {@code null}.
     * Otherwise if the given object is already a SIS implementation, then the given object is returned unchanged.
     * Otherwise a new SIS implementation is created and initialized to the attribute values of the given object.
     *
     * @param  object The object to get as a SIS implementation, or {@code null} if none.
     * @return A SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    public static DefaultGeographicCRS castOrCopy(final GeographicCRS object) {
        return (object == null) || (object instanceof DefaultGeographicCRS)
                ? (DefaultGeographicCRS) object : new DefaultGeographicCRS(object);
    }

    /**
     * Returns the GeoAPI interface implemented by this class.
     * The SIS implementation returns {@code GeographicCRS.class}.
     *
     * {@note Subclasses usually do not need to override this method since GeoAPI does not define
     *        <code>GeographicCRS</code> sub-interface. Overriding possibility is left mostly for
     *        implementors who wish to extend GeoAPI with their own set of interfaces.}
     *
     * @return {@code GeographicCRS.class} or a user-defined sub-interface.
     */
    @Override
    public Class<? extends GeographicCRS> getInterface() {
        return GeographicCRS.class;
    }

    /**
     * Returns the coordinate system.
     *
     * @return The coordinate system.
     */
    @Override
    public EllipsoidalCS getCoordinateSystem() {
        return (EllipsoidalCS) super.getCoordinateSystem();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public DefaultGeographicCRS forConvention(final AxesConvention convention) {
        return (DefaultGeographicCRS) super.forConvention(convention);
    }

    /**
     * Returns a coordinate reference system of the same type than this CRS but with different axes.
     */
    @Override
    final AbstractCRS createSameType(final Map<String,?> properties, final CoordinateSystem cs) {
        return new DefaultGeographicCRS(properties, super.getDatum(), (EllipsoidalCS) cs);
    }

    /**
     * Returns the angular unit of the specified coordinate system.
     * The preference will be given to the longitude axis, if found.
     */
    private static Unit<Angle> getAngularUnit(final CoordinateSystem coordinateSystem) {
        Unit<Angle> unit = NonSI.DEGREE_ANGLE;
        for (int i=coordinateSystem.getDimension(); --i>=0;) {
            final CoordinateSystemAxis axis = coordinateSystem.getAxis(i);
            final Unit<?> candidate = axis.getUnit();
            if (Units.isAngular(candidate)) {
                unit = candidate.asType(Angle.class);
                if (AxisDirection.EAST.equals(AxisDirections.absolute(axis.getDirection()))) {
                    break; // Found the longitude axis.
                }
            }
        }
        return unit;
    }

    /**
     * Formats the inner part of a <cite>Well Known Text</cite> (WKT)</a> element.
     *
     * @param  formatter The formatter to use.
     * @return The name of the WKT element type, which is {@code "GEOGCS"}.
     */
    @Override
    protected String formatTo(final Formatter formatter) {
        final Unit<Angle> oldUnit = formatter.getAngularUnit();
        final Unit<Angle> unit = getAngularUnit(getCoordinateSystem());
        final GeodeticDatum datum = getDatum();
        formatter.setAngularUnit(unit);
        formatter.append(datum);
        formatter.append(datum.getPrimeMeridian());
        formatter.append(unit);
        final EllipsoidalCS cs = getCoordinateSystem();
        final int dimension = cs.getDimension();
        for (int i=0; i<dimension; i++) {
            formatter.append(cs.getAxis(i));
        }
        if (!unit.equals(getUnit())) {
            formatter.setInvalidWKT(this);
        }
        formatter.setAngularUnit(oldUnit);
        return "GEOGCS";
    }
}
