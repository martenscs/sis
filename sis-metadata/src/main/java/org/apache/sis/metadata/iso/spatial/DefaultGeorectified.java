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
package org.apache.sis.metadata.iso.spatial;

import java.util.List;
import java.util.Collection;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.spatial.GCP;
import org.opengis.metadata.spatial.Georectified;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.geometry.primitive.Point;
import org.opengis.util.InternationalString;
import org.apache.sis.xml.Namespaces;


/**
 * Grid whose cells are regularly spaced in a geographic (i.e., lat / long) or map
 * coordinate system defined in the Spatial Referencing System (SRS) so that any cell
 * in the grid can be geolocated given its grid coordinate and the grid origin, cell spacing,
 * and orientation indication of whether or not geographic.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Touraïvane (IRD)
 * @author  Cédric Briançon (Geomatys)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
@XmlType(name = "MD_Georectified_Type", propOrder = {
    "checkPointAvailable",
    "checkPointDescription",
    "cornerPoints",
    "centerPoint",
    "pointInPixel",
    "transformationDimensionDescription",
    "transformationDimensionMapping",
    "checkPoints"
})
@XmlRootElement(name = "MD_Georectified")
@XmlSeeAlso(org.apache.sis.internal.jaxb.gmi.MI_Georectified.class)
public class DefaultGeorectified extends DefaultGridSpatialRepresentation implements Georectified {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -4467097498958444505L;

    /**
     * Indication of whether or not geographic position points are available to test the
     * accuracy of the georeferenced grid data.
     */
    private boolean checkPointAvailable;

    /**
     * Description of geographic position points used to test the accuracy of the
     * georeferenced grid data.
     */
    private InternationalString checkPointDescription;

    /**
     * Earth location in the coordinate system defined by the Spatial Reference System
     * and the grid coordinate of the cells at opposite ends of grid coverage along two
     * diagonals in the grid spatial dimensions. There are four corner points in a
     * georectified grid; at least two corner points along one diagonal are required.
     */
    private List<Point> cornerPoints;

    /**
     * Earth location in the coordinate system defined by the Spatial Reference System
     * and the grid coordinate of the cell halfway between opposite ends of the grid in the
     * spatial dimensions.
     */
    private Point centerPoint;

    /**
     * Point in a pixel corresponding to the Earth location of the pixel.
     */
    private PixelOrientation pointInPixel;

    /**
     * Description of the information about which grid dimensions are the spatial dimensions.
     */
    private InternationalString transformationDimensionDescription;

    /**
     * Information about which grid dimensions are the spatial dimensions.
     */
    private Collection<InternationalString> transformationDimensionMapping;

    /**
     * Geographic references used to validate georectification of the data.
     */
    private Collection<GCP> checkPoints;

    /**
     * Constructs an initially empty georectified object.
     */
    public DefaultGeorectified() {
    }

    /**
     * Returns a SIS metadata implementation with the same values than the given arbitrary
     * implementation. If the given object is {@code null}, then this method returns {@code null}.
     * Otherwise if the given object is already a SIS implementation, then the given object is
     * returned unchanged. Otherwise a new SIS implementation is created and initialized to the
     * property values of the given object, using a <cite>shallow</cite> copy operation
     * (i.e. properties are not cloned).
     *
     * @param  object The object to get as a SIS implementation, or {@code null} if none.
     * @return A SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    public static DefaultGeorectified castOrCopy(final Georectified object) {
        if (object == null || object instanceof DefaultGeorectified) {
            return (DefaultGeorectified) object;
        }
        final DefaultGeorectified copy = new DefaultGeorectified();
        copy.shallowCopy(object);
        return copy;
    }

    /**
     * Returns an indication of whether or not geographic position points are available to test the
     * accuracy of the georeferenced grid data.
     */
    @Override
    @XmlElement(name = "checkPointAvailability", required = true)
    public synchronized boolean isCheckPointAvailable() {
        return checkPointAvailable;
    }

    /**
     * Sets an indication of whether or not geographic position points are available to test the
     * accuracy of the georeferenced grid data.
     *
     * @param newValue {@code true} if check points are available.
     */
    public synchronized void setCheckPointAvailable(final boolean newValue) {
        checkWritePermission();
        checkPointAvailable = newValue;
    }

    /**
     * Returns a description of geographic position points used to test the accuracy of the
     * georeferenced grid data.
     */
    @Override
    @XmlElement(name = "checkPointDescription")
    public synchronized InternationalString getCheckPointDescription() {
        return checkPointDescription;
    }

    /**
     * Sets the description of geographic position points used to test the accuracy of the
     * georeferenced grid data.
     *
     * @param newValue The new check point description.
     */
    public synchronized void setCheckPointDescription(final InternationalString newValue) {
        checkWritePermission();
        checkPointDescription = newValue;
    }

    /**
     * Returns the Earth location in the coordinate system defined by the Spatial Reference System
     * and the grid coordinate of the cells at opposite ends of grid coverage along two
     * diagonals in the grid spatial dimensions. There are four corner points in a
     * georectified grid; at least two corner points along one diagonal are required.
     */
    @Override
    @XmlElement(name = "cornerPoints", required = true)
    public synchronized List<Point> getCornerPoints() {
        return cornerPoints = nonNullList(cornerPoints, Point.class);
    }

    /**
     * Sets the corner points.
     *
     * @param newValues The new corner points.
     */
    public synchronized void setCornerPoints(final List<? extends Point> newValues) {
        cornerPoints = copyList(newValues, cornerPoints, Point.class);
    }

    /**
     * Returns the Earth location in the coordinate system defined by the Spatial Reference System
     * and the grid coordinate of the cell halfway between opposite ends of the grid in the
     * spatial dimensions.
     */
    @Override
    @XmlElement(name = "centerPoint")
    public synchronized Point getCenterPoint() {
        return centerPoint;
    }

    /**
     * Sets the center point.
     *
     * @param newValue The new center point.
     */
    public synchronized void setCenterPoint(final Point newValue) {
        checkWritePermission();
        centerPoint = newValue;
    }

    /**
     * Returns the point in a pixel corresponding to the Earth location of the pixel.
     */
    @Override
    @XmlElement(name = "pointInPixel", required = true)
    public synchronized PixelOrientation getPointInPixel() {
        return pointInPixel;
    }

    /**
     * Sets the point in a pixel corresponding to the Earth location of the pixel.
     *
     * @param newValue The new point in a pixel.
     */
    public synchronized void setPointInPixel(final PixelOrientation newValue) {
        checkWritePermission();
        pointInPixel = newValue;
    }

    /**
     * Returns a description of the information about which grid dimensions are the spatial
     * dimensions.
     */
    @Override
    @XmlElement(name = "transformationDimensionDescription")
    public synchronized InternationalString getTransformationDimensionDescription() {
        return transformationDimensionDescription;
    }

    /**
     * Sets the description of the information about which grid dimensions are the spatial
     * dimensions.
     *
     * @param newValue The new transformation dimension description.
     */
    public synchronized void setTransformationDimensionDescription(final InternationalString newValue) {
        checkWritePermission();
        transformationDimensionDescription = newValue;
    }

    /**
     * Returns information about which grid dimensions are the spatial dimensions.
     */
    @Override
    @XmlElement(name = "transformationDimensionMapping")
    public synchronized Collection<InternationalString> getTransformationDimensionMapping() {
        return transformationDimensionMapping = nonNullCollection(transformationDimensionMapping, InternationalString.class);
    }

    /**
     * Sets information about which grid dimensions are the spatial dimensions.
     *
     * @param newValues The new transformation mapping.
     */
    public synchronized void setTransformationDimensionMapping(final Collection<? extends InternationalString> newValues) {
        transformationDimensionMapping = copyCollection(newValues, transformationDimensionMapping, InternationalString.class);
    }

    /**
     * Returns the geographic references used to validate georectification of the data.
     */
    @Override
    @XmlElement(name = "checkPoint", namespace = Namespaces.GMI)
    public synchronized Collection<GCP> getCheckPoints() {
        return checkPoints = nonNullCollection(checkPoints, GCP.class);
    }

    /**
     * Sets the geographic references used to validate georectification of the data.
     *
     * @param newValues The new check points values.
     */
    public synchronized void setCheckPoints(final Collection<? extends GCP> newValues) {
        checkPoints = copyCollection(newValues, checkPoints, GCP.class);
    }
}