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
package org.apache.sis.metadata.iso.distribution;

import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.distribution.DataFile;
import org.opengis.util.LocalName;
import org.apache.sis.xml.Namespaces;
import org.apache.sis.metadata.iso.ISOMetadata;


/**
 * Description of a transfer data file.
 *
 * @author  Cédric Briançon (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.03)
 * @version 0.3
 * @module
 */
@XmlType(name = "MX_DataFile_Type", propOrder = {
    "featureTypes",
    "fileFormat"
})
@XmlRootElement(name = "MX_DataFile", namespace = Namespaces.GMX)
public class DefaultDataFile extends ISOMetadata implements DataFile {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 5737775725403867273L;

    /**
     * Provides the list of feature types concerned by the transfer data file. Depending on
     * the transfer choices, a data file may contain data related to one or many feature types.
     * This attribute may be omitted when the dataset is composed of a single file and/or the
     * data does not relate to a feature catalogue.
     */
    private Collection<LocalName> featureTypes;

    /**
     * Defines the format of the transfer data file.
     */
    private Format fileFormat;

    /**
     * Constructs an initially empty data file.
     */
    public DefaultDataFile() {
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
    public static DefaultDataFile castOrCopy(final DataFile object) {
        if (object == null || object instanceof DefaultDataFile) {
            return (DefaultDataFile) object;
        }
        final DefaultDataFile copy = new DefaultDataFile();
        copy.shallowCopy(object);
        return copy;
    }

    /**
     * Returns the list of feature types concerned by the transfer data file. Depending on
     * the transfer choices, a data file may contain data related to one or many feature types.
     * This attribute may be omitted when the dataset is composed of a single file and/or the
     * data does not relate to a feature catalogue.
     */
    @Override
    @XmlElement(name = "featureType", namespace = Namespaces.GMX)
    public synchronized Collection<LocalName> getFeatureTypes() {
        return featureTypes = nonNullCollection(featureTypes, LocalName.class);
    }

    /**
     * Sets the list of feature types concerned by the transfer data file.
     *
     * @param newValues The new feature type values.
     */
    public synchronized void setFeatureTypes(final Collection<? extends LocalName> newValues) {
        featureTypes = copyCollection(newValues, featureTypes, LocalName.class);
    }

    /**
     * Returns the format of the transfer data file.
     */
    @Override
    @XmlElement(name = "fileFormat", namespace = Namespaces.GMX, required = true)
    public synchronized Format getFileFormat() {
        return fileFormat;
    }

    /**
     * Sets the format of the transfer data file.
     *
     * @param newValue The new file format value.
     */
    public synchronized void setFileFormat(final Format newValue) {
        checkWritePermission();
        fileFormat = newValue;
    }
}