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
package org.apache.sis.metadata.iso.maintenance;

import java.util.Date;
import java.util.Collection;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.maintenance.MaintenanceFrequency;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.maintenance.ScopeDescription;
import org.opengis.temporal.PeriodDuration;
import org.opengis.util.InternationalString;
import org.apache.sis.metadata.iso.ISOMetadata;

import static org.apache.sis.internal.metadata.MetadataUtilities.toDate;
import static org.apache.sis.internal.metadata.MetadataUtilities.toMilliseconds;


/**
 * Information about the scope and frequency of updating.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Touraïvane (IRD)
 * @author  Cédric Briançon (Geomatys)
 * @author  Guilhem Legal (Geomatys)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
@XmlType(name = "MD_MaintenanceInformation_Type", propOrder = {
    "maintenanceAndUpdateFrequency",
    "dateOfNextUpdate",
    "userDefinedMaintenanceFrequency",
    "updateScopes",
    "updateScopeDescriptions",
    "maintenanceNotes",
    "contacts"
})
@XmlRootElement(name = "MD_MaintenanceInformation")
public class DefaultMaintenanceInformation extends ISOMetadata implements MaintenanceInformation {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -5134544727860361898L;

    /**
     * Frequency with which changes and additions are made to the resource after the
     * initial resource is completed.
     */
    private MaintenanceFrequency maintenanceAndUpdateFrequency;

    /**
     * Scheduled revision date for resource, in milliseconds elapsed
     * since January 1st, 1970. If there is no such date, then this field
     * is set to the special value {@link Long#MIN_VALUE}.
     */
    private long dateOfNextUpdate = Long.MIN_VALUE;

    /**
     * Maintenance period other than those defined, in milliseconds.
     */
    private PeriodDuration userDefinedMaintenanceFrequency;

    /**
     * Scope of data to which maintenance is applied.
     */
    private Collection<ScopeCode> updateScopes;

    /**
     * Additional information about the range or extent of the resource.
     */
    private Collection<ScopeDescription> updateScopeDescriptions;

    /**
     * Information regarding specific requirements for maintaining the resource.
     */
    private Collection<InternationalString> maintenanceNotes;

    /**
     * Identification of, and means of communicating with,
     * person(s) and organization(s) with responsibility for maintaining the metadata
     */
    private Collection<ResponsibleParty> contacts;

    /**
     * Creates a an initially empty maintenance information.
     */
    public DefaultMaintenanceInformation() {
    }

    /**
     * Creates a maintenance information.
     *
     * @param maintenanceAndUpdateFrequency The frequency with which changes and additions are
     *        made to the resource after the initial resource is completed, or {@code null} if none.
     */
    public DefaultMaintenanceInformation(final MaintenanceFrequency maintenanceAndUpdateFrequency) {
        this.maintenanceAndUpdateFrequency = maintenanceAndUpdateFrequency;
    }

    /**
     * Constructs a new instance initialized with the values from the specified metadata object.
     * This is a <cite>shallow</cite> copy constructor, since the other metadata contained in the
     * given object are not recursively copied.
     *
     * @param object The metadata to copy values from, or {@code null} if none.
     *
     * @see #castOrCopy(MaintenanceInformation)
     */
    public DefaultMaintenanceInformation(final MaintenanceInformation object) {
        super(object);
        if (object != null) {
            maintenanceAndUpdateFrequency   = object.getMaintenanceAndUpdateFrequency();
            dateOfNextUpdate                = toMilliseconds(object.getDateOfNextUpdate());
            userDefinedMaintenanceFrequency = object.getUserDefinedMaintenanceFrequency();
            updateScopes                    = copyCollection(object.getUpdateScopes(), ScopeCode.class);
            updateScopeDescriptions         = copyCollection(object.getUpdateScopeDescriptions(), ScopeDescription.class);
            maintenanceNotes                = copyCollection(object.getMaintenanceNotes(), InternationalString.class);
            contacts                        = copyCollection(object.getContacts(), ResponsibleParty.class);
        }
    }

    /**
     * Returns a SIS metadata implementation with the values of the given arbitrary implementation.
     * This method performs the first applicable actions in the following choices:
     *
     * <ul>
     *   <li>If the given object is {@code null}, then this method returns {@code null}.</li>
     *   <li>Otherwise if the given object is already an instance of
     *       {@code DefaultMaintenanceInformation}, then it is returned unchanged.</li>
     *   <li>Otherwise a new {@code DefaultMaintenanceInformation} instance is created using the
     *       {@linkplain #DefaultMaintenanceInformation(MaintenanceInformation) copy constructor}
     *       and returned. Note that this is a <cite>shallow</cite> copy operation, since the other
     *       metadata contained in the given object are not recursively copied.</li>
     * </ul>
     *
     * @param  object The object to get as a SIS implementation, or {@code null} if none.
     * @return A SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    public static DefaultMaintenanceInformation castOrCopy(final MaintenanceInformation object) {
        if (object == null || object instanceof DefaultMaintenanceInformation) {
            return (DefaultMaintenanceInformation) object;
        }
        return new DefaultMaintenanceInformation(object);
    }

    /**
     * Returns the frequency with which changes and additions are made to the resource
     * after the initial resource is completed.
     *
     * @return Frequency with which changes and additions are made to the resource, or {@code null}.
     */
    @Override
    @XmlElement(name = "maintenanceAndUpdateFrequency", required = true)
    public MaintenanceFrequency getMaintenanceAndUpdateFrequency() {
        return maintenanceAndUpdateFrequency;
    }

    /**
     * Sets the frequency with which changes and additions are made to the resource
     * after the initial resource is completed.
     *
     * @param newValue The new maintenance frequency.
     */
    public void setMaintenanceAndUpdateFrequency(final MaintenanceFrequency newValue) {
        checkWritePermission();
        maintenanceAndUpdateFrequency = newValue;
    }

    /**
     * Returns the scheduled revision date for resource.
     *
     * @return Scheduled revision date, or {@code null}.
     */
    @Override
    @XmlElement(name = "dateOfNextUpdate")
    public Date getDateOfNextUpdate() {
        return toDate(dateOfNextUpdate);
    }

    /**
     * Sets the scheduled revision date for resource.
     *
     * @param newValue The new date of next update.
     */
    public void setDateOfNextUpdate(final Date newValue) {
        checkWritePermission();
        dateOfNextUpdate = toMilliseconds(newValue);
    }

    /**
     * Returns the maintenance period other than those defined.
     *
     * @return The Maintenance period, or {@code null}.
     */
    @Override
    @XmlElement(name = "userDefinedMaintenanceFrequency")
    public PeriodDuration getUserDefinedMaintenanceFrequency() {
        return userDefinedMaintenanceFrequency;
    }

    /**
     * Sets the maintenance period other than those defined.
     *
     * @param newValue The new user defined maintenance frequency.
     */
    public void setUserDefinedMaintenanceFrequency(final PeriodDuration newValue) {
        checkWritePermission();
        userDefinedMaintenanceFrequency = newValue;
    }

    /**
     * Returns the scope of data to which maintenance is applied.
     *
     * @return Scope of data to which maintenance is applied.
     */
    @Override
    @XmlElement(name = "updateScope")
    public Collection<ScopeCode> getUpdateScopes() {
        return updateScopes = nonNullCollection(updateScopes, ScopeCode.class);
    }

    /**
     * Sets the scope of data to which maintenance is applied.
     *
     * @param newValues The new update scopes.
     */
    public void setUpdateScopes(final Collection<? extends ScopeCode> newValues) {
        updateScopes = writeCollection(newValues, updateScopes, ScopeCode.class);
    }

    /**
     * Returns additional information about the range or extent of the resource.
     *
     * @return Additional information about the range or extent of the resource.
     */
    @Override
    @XmlElement(name = "updateScopeDescription")
    public Collection<ScopeDescription> getUpdateScopeDescriptions() {
        return updateScopeDescriptions = nonNullCollection(updateScopeDescriptions, ScopeDescription.class);
    }

    /**
     * Sets additional information about the range or extent of the resource.
     *
     * @param newValues The new update scope descriptions.
     */
    public void setUpdateScopeDescriptions(final Collection<? extends ScopeDescription> newValues) {
        updateScopeDescriptions = writeCollection(newValues, updateScopeDescriptions, ScopeDescription.class);
    }

    /**
     * Returns information regarding specific requirements for maintaining the resource.
     *
     * @return Information regarding specific requirements for maintaining the resource.
     */
    @Override
    @XmlElement(name = "maintenanceNote")
    public Collection<InternationalString> getMaintenanceNotes() {
        return maintenanceNotes = nonNullCollection(maintenanceNotes, InternationalString.class);
    }

    /**
     * Sets information regarding specific requirements for maintaining the resource.
     *
     * @param newValues The new maintenance notes.
     */
    public void setMaintenanceNotes(final Collection<? extends InternationalString> newValues) {
        maintenanceNotes = writeCollection(newValues, maintenanceNotes, InternationalString.class);
    }

    /**
     * Returns identification of, and means of communicating with,
     * person(s) and organization(s) with responsibility for maintaining the metadata.
     *
     * @return Means of communicating with person(s) and organization(s) with responsibility
     *         for maintaining the metadata.
     */
    @Override
    @XmlElement(name = "contact")
    public Collection<ResponsibleParty> getContacts() {
        return contacts = nonNullCollection(contacts, ResponsibleParty.class);
    }

    /**
     * Sets identification of, and means of communicating with,
     * person(s) and organization(s) with responsibility for maintaining the metadata.
     *
     * @param newValues The new contacts
     */
    public void setContacts(final Collection<? extends ResponsibleParty> newValues) {
        contacts = writeCollection(newValues, contacts, ResponsibleParty.class);
    }
}
