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

import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import javax.measure.unit.SI;
import javax.measure.unit.NonSI;
import javax.xml.bind.JAXBException;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.test.Validators;
import org.apache.sis.xml.Namespaces;
import org.apache.sis.metadata.iso.extent.DefaultExtent;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.test.DependsOnMethod;
import org.apache.sis.test.DependsOn;
import org.junit.Test;

import static org.apache.sis.referencing.Assert.*;
import static org.apache.sis.test.TestUtilities.getSingleton;
import static org.apache.sis.test.mock.GeodeticDatumMock.*;
import static org.apache.sis.referencing.GeodeticObjectVerifier.*;


/**
 * Tests the {@link DefaultGeodeticDatum} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.4
 * @version 0.4
 * @module
 */
@DependsOn({
  DefaultPrimeMeridianTest.class,
  DefaultEllipsoidTest.class,
  BursaWolfParametersTest.class
})
public final strictfp class DefaultGeodeticDatumTest extends DatumTestCase {
    /**
     * Tests the creation and serialization of a {@link DefaultGeodeticDatum}.
     */
    @Test
    public void testCreateAndSerialize() {
        final Map<String,Object> properties = new HashMap<String,Object>();
        assertNull(properties.put(DefaultEllipsoid.NAME_KEY, "Asteroid"));
        final DefaultEllipsoid ellipsoid = DefaultEllipsoid.createEllipsoid(properties, 1200, 1000, SI.METRE);

        properties.clear();
        assertNull(properties.put(DefaultEllipsoid.NAME_KEY, "Somewhere"));
        final DefaultPrimeMeridian primeMeridian = new DefaultPrimeMeridian(properties, 12, NonSI.DEGREE_ANGLE);

        properties.clear();
        assertNull(properties.put("name",       "This is a name"));
        assertNull(properties.put("scope",      "This is a scope"));
        assertNull(properties.put("scope_fr",   "Valide pour tel usage"));
        assertNull(properties.put("remarks",    "There is remarks"));
        assertNull(properties.put("remarks_fr", "Voici des remarques"));
        assertNull(properties.put("remarks_ja", "注です。"));
        final DefaultGeodeticDatum datum = new DefaultGeodeticDatum(properties, ellipsoid, primeMeridian);

        validate(datum);
        validate(assertSerializedEquals(datum));
    }

    /**
     * Compares the properties of the given datum objects with the properties set by the
     * {@link #testCreateAndSerialize()} method.
     */
    private static void validate(final DefaultGeodeticDatum datum) {
        Validators.validate(datum);
        assertEquals("name",       "This is a name",        datum.getName   ().getCode());
        assertEquals("scope",      "This is a scope",       datum.getScope  ().toString(Locale.ROOT));
        assertEquals("scope_fr",   "Valide pour tel usage", datum.getScope  ().toString(Locale.FRENCH));
        assertEquals("remarks",    "There is remarks",      datum.getRemarks().toString(Locale.ROOT));
        assertEquals("remarks_fr", "Voici des remarques",   datum.getRemarks().toString(Locale.FRENCH));
        assertEquals("remarks_ja", "注です。",                datum.getRemarks().toString(Locale.JAPANESE));
    }

    /**
     * Tests {@link DefaultGeodeticDatum#isHeuristicMatchForName(String)}.
     */
    @Test
    public void testIsHeuristicMatchForName() {
        final DefaultGeodeticDatum datum = new DefaultGeodeticDatum(WGS84);
        assertFalse(datum.isHeuristicMatchForName("WGS72"));
        assertTrue (datum.isHeuristicMatchForName("WGS84"));
        assertTrue (datum.isHeuristicMatchForName("WGS 84"));
        assertTrue (datum.isHeuristicMatchForName("WGS_84"));
        assertTrue (datum.isHeuristicMatchForName("D_WGS_84"));
        assertFalse(datum.isHeuristicMatchForName("E_WGS_84"));
    }

    /**
     * Tests {@link DefaultGeodeticDatum#getPositionVectorTransformation(GeodeticDatum, Extent)}.
     */
    @Test
    @DependsOnMethod("testCreateAndSerialize")
    public void testGetPositionVectorTransformation() {
        final Map<String,Object> properties = new HashMap<String,Object>();
        assertNull(properties.put(DefaultGeodeticDatum.NAME_KEY, "Invalid dummy datum"));
        /*
         * Associate two BursaWolfParameters, one valid only in a local area and the other one
         * valid globaly.  Note that we are building an invalid set of parameters, because the
         * source datum are not the same in both case. But for this test we are not interrested
         * in datum consistency - we only want any Bursa-Wolf parameters having different area
         * of validity.
         */
        final BursaWolfParameters local  = BursaWolfParametersTest.createED87_to_WGS84();   // Local area (North Sea)
        final BursaWolfParameters global = BursaWolfParametersTest.createWGS72_to_WGS84();  // Global area (World)
        assertNull(properties.put(DefaultGeodeticDatum.BURSA_WOLF_KEY, new BursaWolfParameters[] {local, global}));
        /*
         * Build the datum using WGS 72 ellipsoid (so at least one of the BursaWolfParameters is real).
         */
        final DefaultGeodeticDatum datum = new DefaultGeodeticDatum(properties,
                WGS72.getEllipsoid(), WGS72.getPrimeMeridian());
        /*
         * Search for BursaWolfParameters around the North Sea area.
         */
        final DefaultGeographicBoundingBox areaOfInterest = new DefaultGeographicBoundingBox(-2, 8, 55, 60);
        final DefaultExtent extent = new DefaultExtent("Around the North Sea", areaOfInterest, null, null);
        Matrix matrix = datum.getPositionVectorTransformation(NAD83, extent);
        assertNull("No BursaWolfParameters for NAD83", matrix);
        matrix = datum.getPositionVectorTransformation(WGS84, extent);
        assertNotNull("BursaWolfParameters for WGS84", matrix);
        checkTransformationSignature(local, matrix, 0);
        /*
         * Expand the area of interest to something greater than North Sea, and test again.
         */
        areaOfInterest.setWestBoundLongitude(-8);
        matrix = datum.getPositionVectorTransformation(WGS84, extent);
        assertNotNull("BursaWolfParameters for WGS84", matrix);
        checkTransformationSignature(global, matrix, 0);
        /*
         * Search in the reverse direction.
         */
        final DefaultGeodeticDatum targetDatum = new DefaultGeodeticDatum(WGS84);
        matrix = targetDatum.getPositionVectorTransformation(datum, extent);
        global.invert(); // Expected result is the inverse.
        checkTransformationSignature(global, matrix, 1E-6);
    }

    /**
     * Verifies if the given matrix is for the expected Position Vector transformation.
     * The easiest way to verify that is to check the translation terms (last matrix column),
     * which should have been copied verbatim from the {@code BursaWolfParameters} to the matrix.
     * Other terms in the matrix are modified compared to the {@code BursaWolfParameters} ones.
     */
    private static void checkTransformationSignature(final BursaWolfParameters expected, final Matrix actual,
            final double tolerance)
    {
        assertEquals("tX", expected.tX, actual.getElement(0, 3), tolerance);
        assertEquals("tY", expected.tY, actual.getElement(1, 3), tolerance);
        assertEquals("tZ", expected.tZ, actual.getElement(2, 3), tolerance);
    }

    /**
     * Tests {@link DefaultEllipsoid#toWKT()}.
     */
    @Test
    public void testToWKT() {
        final DefaultGeodeticDatum datum = new DefaultGeodeticDatum(WGS84);
        assertWktEquals(datum,
                "DATUM[“WGS84”,\n" +
                "  SPHEROID[“WGS84”, 6378137.0, 298.257223563]]");
    }

    /**
     * Tests marshalling.
     *
     * @throws JAXBException If an error occurred during marshalling.
     */
    @Test
    public void testMarshalling() throws JAXBException {
        assertXmlEquals(
                "<gml:GeodeticDatum xmlns:gml=\"" + Namespaces.GML + "\">\n" +
                "  <gml:name codeSpace=\"test\">WGS84</gml:name>\n" +
                "  <gml:primeMeridian>\n" +
                "    <gml:PrimeMeridian>\n" +
                "      <gml:name codeSpace=\"test\">Greenwich</gml:name>\n" +
                "      <gml:greenwichLongitude uom=\"urn:ogc:def:uom:EPSG::9102\">0.0</gml:greenwichLongitude>\n" +
                "    </gml:PrimeMeridian>\n" +
                "  </gml:primeMeridian>\n" +
                "  <gml:ellipsoid>\n" +
                "    <gml:Ellipsoid>\n" +
                "      <gml:name codeSpace=\"test\">WGS84</gml:name>\n" +
                "      <gml:semiMajorAxis uom=\"urn:ogc:def:uom:EPSG::9001\">6378137.0</gml:semiMajorAxis>\n" +
                "      <gml:secondDefiningParameter>\n" +
                "        <gml:SecondDefiningParameter>\n" +
                "          <gml:inverseFlattening uom=\"urn:ogc:def:uom:EPSG::9201\">298.257223563</gml:inverseFlattening>\n" +
                "        </gml:SecondDefiningParameter>\n" +
                "      </gml:secondDefiningParameter>\n" +
                "    </gml:Ellipsoid>\n" +
                "  </gml:ellipsoid>\n" +
                "</gml:GeodeticDatum>",
                marshal(new DefaultGeodeticDatum(WGS84)), "xmlns:*");
    }

    /**
     * Tests unmarshalling.
     *
     * @throws JAXBException If an error occurred during unmarshalling.
     */
    @Test
    public void testUnmarshalling() throws JAXBException {
        final DefaultGeodeticDatum datum = unmarshall(DefaultGeodeticDatum.class, "WGS 84.xml");
        assertIsWGS84(datum);
        assertIsWorld((GeographicBoundingBox) getSingleton(datum.getDomainOfValidity().getGeographicElements()));
        /*
         * Values in the following tests are specific to our XML file.
         * The actual texts in the EPSG database are more descriptive.
         */
        assertEquals("remarks", "No distinction between the original and subsequent WGS 84 frames.",
                datum.getRemarks().toString());
        assertEquals("scope", "Satellite navigation.",
                datum.getScope().toString());
        assertEquals("anchorDefinition", "Station coordinates changed by a few centimetres in 1994, 1997, 2002 and 2012.",
                datum.getAnchorPoint().toString());
        assertEquals("realizationEpoch", xmlDate("1984-01-01 00:00:00"),
                datum.getRealizationEpoch());
        assertEquals("remarks", "Defining parameters cited in EPSG database.",
                datum.getEllipsoid().getRemarks().toString());
    }
}
