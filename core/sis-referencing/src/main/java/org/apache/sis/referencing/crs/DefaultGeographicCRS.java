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
import javax.xml.bind.annotation.XmlTransient;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.crs.GeographicCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.AbstractReferenceSystem;
import org.apache.sis.io.wkt.Formatter;


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
     *   <caption>Recognized properties (non exhaustive list)</caption>
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
     * <div class="note"><b>Note for implementors:</b>
     * Subclasses usually do not need to override this method since GeoAPI does not define {@code GeographicCRS}
     * sub-interface. Overriding possibility is left mostly for implementors who wish to extend GeoAPI with their
     * own set of interfaces.</div>
     *
     * @return {@code GeographicCRS.class} or a user-defined sub-interface.
     */
    @Override
    public Class<? extends GeographicCRS> getInterface() {
        return GeographicCRS.class;
    }

    /**
     * Returns the geodetic datum associated to this geographic CRS.
     * This is the datum given at construction time.
     *
     * @return The geodetic datum associated to this geographic CRS.
     */
    @Override
    public final GeodeticDatum getDatum() {
        return super.getDatum();
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
     * Formats this CRS as a <cite>Well Known Text</cite> {@code GeodeticCRS[…]} element.
     *
     * <div class="note"><b>Example:</b> Well-Known Text (version 2)
     * of a geographic coordinate reference system using the WGS 84 datum.
     *
     * {@preformat wkt
     *   GeodeticCRS["WGS 84",
     *      Datum["World Geodetic System 1984",
     *        Ellipsoid["WGS84", 6378137.0, 298.257223563, LengthUnit["metre", 1]]],
     *        PrimeMeridian["Greenwich", 0.0, AngleUnit["degree", 0.017453292519943295]],
     *      CS["ellipsoidal", 2],
     *        Axis["Latitude", north],
     *        Axis["Longitude", east],
     *        AngleUnit["degree", 0.017453292519943295],
     *      Area["World"],
     *      BBox[-90.00, -180.00, 90.00, 180.00],
     *      Scope["Used by GPS satellite navigation system."]
     *      Id["EPSG", 4326, Citation["OGP"], URI["urn:ogc:def:crs:EPSG::4326"]]]
     * }
     *
     * <p>Same coordinate reference system using WKT 1.</p>
     *
     * {@preformat wkt
     *   GEOGCS["WGS 84"
     *      DATUM["World Geodetic System 1984"
     *        SPHEROID["WGS84", 6378137.0, 298.257223563]]
     *      PRIMEM["Greenwich", 0.0]
     *      UNIT["degree", 0.017453292519943295]
     *      AXIS["Latitude", NORTH],
     *      AXIS["Longitude", EAST],
     *      AUTHORITY["EPSG", "4326"]]
     * }
     * </div>
     *
     * @return {@code "GeodeticCRS"} (WKT 2) or {@code "GeogCS"} (WKT 1).
     */
    @Override
    protected String formatTo(final Formatter formatter) {
        return super.formatTo(formatter);
    }
}
