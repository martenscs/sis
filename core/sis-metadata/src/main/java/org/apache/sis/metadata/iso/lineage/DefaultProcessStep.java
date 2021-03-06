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
package org.apache.sis.metadata.iso.lineage;

import java.util.Date;
import java.util.Collection;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.lineage.ProcessStepReport;
import org.opengis.metadata.lineage.Processing;
import org.opengis.util.InternationalString;
import org.opengis.metadata.lineage.Source;
import org.opengis.metadata.lineage.ProcessStep;
import org.opengis.metadata.citation.ResponsibleParty;
import org.apache.sis.metadata.iso.ISOMetadata;
import org.apache.sis.util.iso.Types;
import org.apache.sis.xml.Namespaces;

import static org.apache.sis.internal.metadata.MetadataUtilities.toDate;
import static org.apache.sis.internal.metadata.MetadataUtilities.toMilliseconds;


/**
 * Description of the event, including related parameters or tolerances.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Touraïvane (IRD)
 * @author  Cédric Briançon (Geomatys)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
@XmlType(name = "LI_ProcessStep_Type", propOrder = {
    "description",
    "rationale",
    "date",
    "processors",
    "sources",
    "outputs",
    "processingInformation",
    "reports"
})
@XmlRootElement(name = "LI_ProcessStep")
@XmlSeeAlso(org.apache.sis.internal.jaxb.gmi.LE_ProcessStep.class)
public class DefaultProcessStep extends ISOMetadata implements ProcessStep {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -3511714360929580873L;

    /**
     * Description of the event, including related parameters or tolerances.
     */
    private InternationalString description;

    /**
     * Requirement or purpose for the process step.
     */
    private InternationalString rationale;

    /**
     * Date and time or range of date and time on or over which the process step occurred,
     * in milliseconds elapsed since January 1st, 1970. If there is no such date, then this
     * field is set to the special value {@link Long#MIN_VALUE}.
     */
    private long date = Long.MIN_VALUE;

    /**
     * Identification of, and means of communication with, person(s) and
     * organization(s) associated with the process step.
     */
    private Collection<ResponsibleParty> processors;

    /**
     * Information about the source data used in creating the data specified by the scope.
     */
    private Collection<Source> sources;

    /**
     * Description of the product generated as a result of the process step.
     */
    private Collection<Source> outputs;

    /**
     * Comprehensive information about the procedure by which the algorithm was applied
     * to derive geographic data from the raw instrument measurements, such as datasets,
     * software used, and the processing environment.
     */
    private Processing processingInformation;

    /**
     * Report generated by the process step.
     */
    private Collection<ProcessStepReport> reports;

    /**
     * Creates an initially empty process step.
     */
    public DefaultProcessStep() {
    }

    /**
     * Creates a process step initialized to the given description.
     *
     * @param description Description of the event, including related parameters or tolerances.
     */
    public DefaultProcessStep(final CharSequence description) {
        this.description = Types.toInternationalString(description);
    }

    /**
     * Constructs a new instance initialized with the values from the specified metadata object.
     * This is a <cite>shallow</cite> copy constructor, since the other metadata contained in the
     * given object are not recursively copied.
     *
     * @param object The metadata to copy values from, or {@code null} if none.
     *
     * @see #castOrCopy(ProcessStep)
     */
    public DefaultProcessStep(final ProcessStep object) {
        super(object);
        if (object != null) {
            description           = object.getDescription();
            rationale             = object.getRationale();
            date                  = toMilliseconds(object.getDate());
            processors            = copyCollection(object.getProcessors(), ResponsibleParty.class);
            sources               = copyCollection(object.getSources(), Source.class);
            outputs               = copyCollection(object.getOutputs(), Source.class);
            processingInformation = object.getProcessingInformation();
            reports               = copyCollection(object.getReports(), ProcessStepReport.class);
        }
    }

    /**
     * Returns a SIS metadata implementation with the values of the given arbitrary implementation.
     * This method performs the first applicable action in the following choices:
     *
     * <ul>
     *   <li>If the given object is {@code null}, then this method returns {@code null}.</li>
     *   <li>Otherwise if the given object is already an instance of
     *       {@code DefaultProcessStep}, then it is returned unchanged.</li>
     *   <li>Otherwise a new {@code DefaultProcessStep} instance is created using the
     *       {@linkplain #DefaultProcessStep(ProcessStep) copy constructor}
     *       and returned. Note that this is a <cite>shallow</cite> copy operation, since the other
     *       metadata contained in the given object are not recursively copied.</li>
     * </ul>
     *
     * @param  object The object to get as a SIS implementation, or {@code null} if none.
     * @return A SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    public static DefaultProcessStep castOrCopy(final ProcessStep object) {
        if (object == null || object instanceof DefaultProcessStep) {
            return (DefaultProcessStep) object;
        }
        return new DefaultProcessStep(object);
    }

    /**
     * Returns the description of the event, including related parameters or tolerances.
     *
     * @return Description of the event, or {@code null}.
     */
    @Override
    @XmlElement(name = "description", required = true)
    public InternationalString getDescription() {
        return description;
    }

    /**
     * Sets the description of the event, including related parameters or tolerances.
     *
     * @param newValue The new description.
     */
    public void setDescription(final InternationalString newValue) {
        checkWritePermission();
        description = newValue;
    }

    /**
     * Returns the requirement or purpose for the process step.
     *
     * @return Requirement or purpose for the process step, or {@code null}.
     */
    @Override
    @XmlElement(name = "rationale")
    public InternationalString getRationale() {
        return rationale;
    }

    /**
     * Sets the requirement or purpose for the process step.
     *
     * @param newValue The new rationale.
     */
    public void setRationale(final InternationalString newValue) {
        checkWritePermission();
        rationale = newValue;
    }

    /**
     * Returns the date and time or range of date and time on or over which the process step occurred.
     *
     * @return Date on or over which the process step occurred, or {@code null}.
     */
    @Override
    @XmlElement(name = "dateTime")
    public Date getDate() {
        return toDate(date);
    }

    /**
     * Sets the date and time or range of date and time on or over which the process step occurred.
     *
     * @param newValue The new date.
     */
    public void setDate(final Date newValue) {
        checkWritePermission();
        date = toMilliseconds(newValue);
    }

    /**
     * Returns the identification of, and means of communication with, person(s) and
     * organization(s) associated with the process step.
     *
     * @return Means of communication with person(s) and organization(s) associated with the process step.
     */
    @Override
    @XmlElement(name = "processor")
    public Collection<ResponsibleParty> getProcessors() {
        return processors = nonNullCollection(processors, ResponsibleParty.class);
    }

    /**
     * Identification of, and means of communication with, person(s) and
     * organization(s) associated with the process step.
     *
     * @param newValues The new processors.
     */
    public void setProcessors(final Collection<? extends ResponsibleParty> newValues) {
        processors = writeCollection(newValues, processors, ResponsibleParty.class);
    }

    /**
     * Returns the information about the source data used in creating the data specified by the scope.
     *
     * @return Information about the source data used in creating the data.
     */
    @Override
    @XmlElement(name = "source")
    public Collection<Source> getSources() {
        return sources = nonNullCollection(sources, Source.class);
    }

    /**
     * Information about the source data used in creating the data specified by the scope.
     *
     * @param newValues The new sources.
     */
    public void setSources(final Collection<? extends Source> newValues) {
        sources = writeCollection(newValues, sources, Source.class);
    }

    /**
     * Returns the description of the product generated as a result of the process step.
     *
     * @return Product generated as a result of the process step.
     */
    @Override
    @XmlElement(name = "output", namespace = Namespaces.GMI)
    public Collection<Source> getOutputs() {
        return outputs = nonNullCollection(outputs, Source.class);
    }

    /**
     * Sets the description of the product generated as a result of the process step.
     *
     * @param newValues The new output values.
     */
    public void setOutputs(final Collection<? extends Source> newValues) {
        outputs = writeCollection(newValues, outputs, Source.class);
    }

    /**
     * Returns the comprehensive information about the procedure by which the algorithm
     * was applied to derive geographic data from the raw instrument measurements, such
     * as datasets, software used, and the processing environment.
     *
     * @return Procedure by which the algorithm was applied to derive geographic data, or {@code null}.
     */
    @Override
    @XmlElement(name = "processingInformation", namespace = Namespaces.GMI)
    public Processing getProcessingInformation() {
        return processingInformation;
    }

    /**
     * Sets the comprehensive information about the procedure by which the algorithm was
     * applied to derive geographic data from the raw instrument measurements, such as
     * datasets, software used, and the processing environment.
     *
     * @param newValue The new processing information value.
     */
    public void setProcessingInformation(final Processing newValue) {
        checkWritePermission();
        processingInformation = newValue;
    }

    /**
     * Returns the report generated by the process step.
     *
     * @return Report generated by the process step.
     */
    @Override
    @XmlElement(name = "report", namespace = Namespaces.GMI)
    public Collection<ProcessStepReport> getReports() {
        return reports = nonNullCollection(reports, ProcessStepReport.class);
    }

    /**
     * Sets the report generated by the process step.
     *
     * @param newValues The new process step report values.
     */
    public void setReports(final Collection<? extends ProcessStepReport> newValues) {
        reports = writeCollection(newValues, reports, ProcessStepReport.class);
    }
}
