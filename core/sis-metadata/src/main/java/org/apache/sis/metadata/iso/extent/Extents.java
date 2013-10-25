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
package org.apache.sis.metadata.iso.extent;

import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.BoundingPolygon;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.apache.sis.measure.Longitude;
import org.apache.sis.util.resources.Vocabulary;
import org.apache.sis.util.Static;

import static java.lang.Math.*;
import static org.apache.sis.internal.metadata.MetadataUtilities.getInclusion;
import static org.apache.sis.internal.metadata.ReferencingServices.AUTHALIC_RADIUS;


/**
 * Convenience static methods for extracting information from {@link Extent} objects.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-2.2)
 * @version 0.4
 * @module
 *
 * @see org.apache.sis.geometry.Envelopes
 */
public final class Extents extends Static {
    /**
     * Do no allow instantiation of this class.
     */
    private Extents() {
    }

    /**
     * A geographic extent ranging from 180°W to 180°E and 90°S to 90°N.
     * This extent has no vertical and no temporal components.
     */
    public static final Extent WORLD;
    static {
        final DefaultGeographicBoundingBox box = new DefaultGeographicBoundingBox(-180, 180, -90, 90);
        box.freeze();
        final DefaultExtent world = new DefaultExtent(
                Vocabulary.formatInternational(Vocabulary.Keys.World), box, null, null);
        world.freeze();
        WORLD = world;
    }

    /**
     * Returns a single geographic bounding box from the specified extent.
     * If no bounding box is found, then this method returns {@code null}.
     * If a single bounding box is found, then that box is returned directly.
     * If more than one box is found, then all those boxes are
     * {@linkplain DefaultGeographicBoundingBox#add added} together.
     *
     * @param  extent The extent to convert to a geographic bounding box, or {@code null}.
     * @return A geographic bounding box extracted from the given extent, or {@code null}
     *         if the given extent was {@code null}.
     */
    public static GeographicBoundingBox getGeographicBoundingBox(final Extent extent) {
        GeographicBoundingBox candidate = null;
        if (extent != null) {
            DefaultGeographicBoundingBox modifiable = null;
            for (final GeographicExtent element : extent.getGeographicElements()) {
                final GeographicBoundingBox bounds;
                if (element instanceof GeographicBoundingBox) {
                    bounds = (GeographicBoundingBox) element;
                } else if (element instanceof BoundingPolygon) {
                    // TODO: iterates through all polygons and invoke Polygon.getEnvelope();
                    continue;
                } else {
                    continue;
                }
                /*
                 * A single geographic bounding box has been extracted. Now add it to previous
                 * ones (if any). All exclusion boxes before the first inclusion box are ignored.
                 */
                if (candidate == null) {
                    /*
                     * Reminder: 'inclusion' is a mandatory attribute, so it should never be
                     * null for a valid metadata object.  If the metadata object is invalid,
                     * it is better to get an exception than having a code doing silently
                     * some probably inappropriate work.
                     */
                    if (getInclusion(bounds.getInclusion())) {
                        candidate = bounds;
                    }
                } else {
                    if (modifiable == null) {
                        modifiable = new DefaultGeographicBoundingBox();
                        modifiable.setBounds(candidate);
                        candidate = modifiable;
                    }
                    modifiable.add(bounds);
                }
            }
        }
        return candidate;
    }

    /**
     * Returns the intersection of the given geographic bounding boxes. If any of the arguments is {@code null},
     * then this method returns the other argument (which may be null). Otherwise this method returns a box which
     * is the intersection of the two given boxes.
     *
     * <p>This method never modify the given boxes, but may return directly one of the given arguments if it
     * already represents the intersection result.</p>
     *
     * @param  b1 The first bounding box, or {@code null}.
     * @param  b2 The second bounding box, or {@code null}.
     * @return The intersection (may be any of the {@code b1} or {@code b2} argument if unchanged),
     *         or {@code null} if the two given boxes are null.
     * @throws IllegalArgumentException If the {@linkplain DefaultGeographicBoundingBox#getInclusion() inclusion status}
     *         is not the same for both boxes.
     *
     * @see DefaultGeographicBoundingBox#intersect(GeographicBoundingBox)
     *
     * @since 0.4
     */
    public static GeographicBoundingBox intersection(final GeographicBoundingBox b1, final GeographicBoundingBox b2) {
        if (b1 == null) return b2;
        if (b2 == null || b2 == b1) return b1;
        final DefaultGeographicBoundingBox box = new DefaultGeographicBoundingBox(b1);
        box.intersect(b2);
        return box;
    }

    /**
     * Returns an <em>estimation</em> of the area (in square metres) of the given bounding box.
     * Since {@code GeographicBoundingBox} provides only approximative information (for example
     * it does not specify the datum), the value returned by this method is also approximative.
     *
     * <p>The current implementation performs its computation on the
     * {@linkplain org.apache.sis.referencing.GeodeticObjects#SPHERE GRS 1980 Authalic Sphere}.
     * However this may change in any future SIS version.</p>
     *
     * @param  box The geographic bounding box for which to compute the area, or {@code null}.
     * @return An estimation of the geographic area in the given bounding box,
     *         or {@linkplain Double#NaN NaN} if the given box was null.
     *
     * @since 0.4
     */
    public static double area(final GeographicBoundingBox box) {
        if (box == null) {
            return Double.NaN;
        }
        double Δλ = box.getEastBoundLongitude() - box.getWestBoundLongitude(); // Negative if spanning the anti-meridian
        Δλ -= floor(Δλ / (Longitude.MAX_VALUE - Longitude.MIN_VALUE)) * (Longitude.MAX_VALUE - Longitude.MIN_VALUE);
        return (AUTHALIC_RADIUS * AUTHALIC_RADIUS) * toRadians(Δλ) *
               max(0, sin(toRadians(box.getNorthBoundLatitude())) -
                      sin(toRadians(box.getSouthBoundLatitude())));
    }
}
