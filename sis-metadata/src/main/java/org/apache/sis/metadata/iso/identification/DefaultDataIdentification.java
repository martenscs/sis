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
package org.apache.sis.metadata.iso.identification;

import java.util.Collection;
import java.util.Locale;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.identification.Resolution;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.spatial.SpatialRepresentationType;
import org.opengis.util.InternationalString;


/**
 * Information required to identify a dataset.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Touraïvane (IRD)
 * @author  Cédric Briançon (Geomatys)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
@XmlType(name = "MD_DataIdentification_Type", propOrder = {
    "spatialRepresentationTypes",
    "spatialResolutions",
    "languages",
    "characterSets",
    "topicCategories",
    "environmentDescription",
    "extents",
    "supplementalInformation"
})
@XmlRootElement(name = "MD_DataIdentification")
public class DefaultDataIdentification extends AbstractIdentification implements DataIdentification {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 2099051218533426785L;

    /**
     * Method used to spatially represent geographic information.
     */
    private Collection<SpatialRepresentationType> spatialRepresentationTypes;

    /**
     * Factor which provides a general understanding of the density of spatial data in the dataset.
     */
    private Collection<Resolution> spatialResolutions;

    /**
     * Language(s) used within the dataset.
     */
    private Collection<Locale> languages;

    /**
     * Full name of the character coding standard used for the dataset.
     */
    private Collection<CharacterSet> characterSets;

    /**
     * Main theme(s) of the datset.
     */
    private Collection<TopicCategory> topicCategories;

    /**
     * Description of the dataset in the producers processing environment, including items
     * such as the software, the computer operating system, file name, and the dataset size
     */
    private InternationalString environmentDescription;

    /**
     * Additional extent information including the bounding polygon, vertical, and temporal
     * extent of the dataset.
     */
    private Collection<Extent> extents;

    /**
     * Any other descriptive information about the dataset.
     */
    private InternationalString supplementalInformation;

    /**
     * Constructs an initially empty data identification.
     */
    public DefaultDataIdentification() {
    }

    /**
     * Creates a data identification initialized to the specified values.
     *
     * @param citation      The citation data for the resource(s), or {@code null} if none.
     * @param abstracts     A brief narrative summary of the content of the resource(s), or {@code null} if none.
     * @param language      The language used within the dataset, or {@code null} if none.
     * @param topicCategory The main theme of the dataset, or {@code null} if none.
     */
    public DefaultDataIdentification(final Citation citation,
                                     final InternationalString abstracts,
                                     final Locale language,
                                     final TopicCategory topicCategory)
    {
        super(citation, abstracts);
        languages = singleton(Locale.class, language);
        topicCategories = singleton(TopicCategory.class, topicCategory);
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
    public static DefaultDataIdentification castOrCopy(final DataIdentification object) {
        if (object == null || object instanceof DefaultDataIdentification) {
            return (DefaultDataIdentification) object;
        }
        final DefaultDataIdentification copy = new DefaultDataIdentification();
        copy.shallowCopy(object);
        return copy;
    }

    /**
     * Returns the method used to spatially represent geographic information.
     */
    @Override
    @XmlElement(name = "spatialRepresentationType")
    public synchronized Collection<SpatialRepresentationType> getSpatialRepresentationTypes() {
        return spatialRepresentationTypes = nonNullCollection(spatialRepresentationTypes, SpatialRepresentationType.class);
    }

    /**
     * Sets the method used to spatially represent geographic information.
     *
     * @param newValues The new spatial representation types.
     */
    public synchronized void setSpatialRepresentationTypes(final Collection<? extends SpatialRepresentationType> newValues) {
        spatialRepresentationTypes = copyCollection(newValues, spatialRepresentationTypes, SpatialRepresentationType.class);
    }

    /**
     * Returns the factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
    @Override
    @XmlElement(name = "spatialResolution")
    public synchronized Collection<Resolution> getSpatialResolutions() {
        return spatialResolutions = nonNullCollection(spatialResolutions, Resolution.class);
    }

    /**
     * Sets the factor which provides a general understanding of the density of spatial data
     * in the dataset.
     *
     * @param newValues The new spatial resolutions.
     */
    public synchronized void setSpatialResolutions(final Collection<? extends Resolution> newValues) {
        spatialResolutions = copyCollection(newValues, spatialResolutions, Resolution.class);
    }

    /**
     * Returns the language(s) used within the dataset.
     */
    @Override
    @XmlElement(name = "language", required = true)
    public synchronized Collection<Locale> getLanguages() {
        return languages = nonNullCollection(languages, Locale.class);
    }

    /**
     * Sets the language(s) used within the dataset.
     *
     * @param newValues The new languages.
     */
    public synchronized void setLanguages(final Collection<? extends Locale> newValues)  {
        languages = copyCollection(newValues, languages, Locale.class);
    }

    /**
     * Returns the full name of the character coding standard used for the dataset.
     */
    @Override
    @XmlElement(name = "characterSet")
    public synchronized Collection<CharacterSet> getCharacterSets() {
        return characterSets = nonNullCollection(characterSets, CharacterSet.class);
    }

    /**
     * Sets the full name of the character coding standard used for the dataset.
     *
     * @param newValues The new character sets.
     */
    public synchronized void setCharacterSets(final Collection<? extends CharacterSet> newValues) {
        characterSets = copyCollection(newValues, characterSets, CharacterSet.class);
    }

    /**
     * Returns the main theme(s) of the dataset.
     */
    @Override
    @XmlElement(name = "topicCategory")
    public synchronized Collection<TopicCategory> getTopicCategories()  {
        return topicCategories = nonNullCollection(topicCategories, TopicCategory.class);
    }

    /**
     * Sets the main theme(s) of the dataset.
     *
     * @param newValues The new topic categories.
     */
    public synchronized void setTopicCategories(final Collection<? extends TopicCategory> newValues) {
        topicCategories = copyCollection(newValues, topicCategories, TopicCategory.class);
    }

    /**
     * Returns a description of the dataset in the producer's processing environment. This includes
     * items such as the software, the computer operating system, file name, and the dataset size.
     */
    @Override
    @XmlElement(name = "environmentDescription")
    public synchronized InternationalString getEnvironmentDescription() {
        return environmentDescription;
    }

    /**
     * Sets the description of the dataset in the producers processing environment.
     *
     * @param newValue The new environment description.
     */
    public synchronized void setEnvironmentDescription(final InternationalString newValue)  {
        checkWritePermission();
        environmentDescription = newValue;
    }

    /**
     * Returns additional extent information including the bounding polygon, vertical, and temporal
     * extent of the dataset.
     */
    @Override
    @XmlElement(name = "extent")
    public synchronized Collection<Extent> getExtents() {
        return extents = nonNullCollection(extents, Extent.class);
    }

    /**
     * Sets additional extent information.
     *
     * @param newValues The new extents
     */
    public synchronized void setExtents(final Collection<? extends Extent> newValues) {
        extents = copyCollection(newValues, extents, Extent.class);
    }

    /**
     * Any other descriptive information about the dataset.
     */
    @Override
    @XmlElement(name = "supplementalInformation")
    public synchronized InternationalString getSupplementalInformation() {
        return supplementalInformation;
    }

    /**
     * Sets any other descriptive information about the dataset.
     *
     * @param newValue The new supplemental information.
     */
    public synchronized void setSupplementalInformation(final InternationalString newValue) {
        checkWritePermission();
        supplementalInformation = newValue;
    }
}