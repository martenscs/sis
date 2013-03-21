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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.lineage.ProcessStepReport;
import org.opengis.util.InternationalString;
import org.apache.sis.metadata.iso.ISOMetadata;
import org.apache.sis.xml.Namespaces;


/**
 * Report of what occurred during the process step.
 *
 * @author  Cédric Briançon (Geomatys)
 * @author  Guilhem Legal (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
@XmlType(name = "LE_ProcessStepReport_Type", propOrder = {
    "name",
    "description",
    "fileType"
})
@XmlRootElement(name = "LE_ProcessStepReport", namespace = Namespaces.GMI)
public class DefaultProcessStepReport extends ISOMetadata implements ProcessStepReport {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -7054783651586763896L;

    /**
     * Name of the processing report.
     */
    private InternationalString name;

    /**
     * Textual description of what occurred during the process step.
     */
    private InternationalString description;

    /**
     * Type of file that contains the processing report.
     */
    private InternationalString fileType;

    /**
     * Constructs an initially empty process step report.
     */
    public DefaultProcessStepReport() {
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
    public static DefaultProcessStepReport castOrCopy(final ProcessStepReport object) {
        if (object == null || object instanceof DefaultProcessStepReport) {
            return (DefaultProcessStepReport) object;
        }
        final DefaultProcessStepReport copy = new DefaultProcessStepReport();
        copy.shallowCopy(object);
        return copy;
    }

    /**
     * Returns the name of the processing report.
     */
    @Override
    @XmlElement(name = "name", namespace = Namespaces.GMI, required = true)
    public synchronized InternationalString getName() {
        return name;
    }

    /**
     * Sets the name of the processing report.
     *
     * @param newValue The new name value.
     */
    public synchronized void setName(final InternationalString newValue) {
        checkWritePermission();
        name = newValue;
    }

    /**
     * Returns the textual description of what occurred during the process step.
     * Returns {@code null} if unspecified.
     */
    @Override
    @XmlElement(name = "description", namespace = Namespaces.GMI)
    public synchronized InternationalString getDescription() {
        return description;
    }

    /**
     * Sets the textual description of what occurred during the process step.
     *
     * @param newValue The new description value.
     */
    public synchronized void setDescription(final InternationalString newValue) {
        checkWritePermission();
        description = newValue;
    }

    /**
     * Returns the type of file that contains the processing report. {@code null} if unspecified.
     */
    @Override
    @XmlElement(name = "fileType", namespace = Namespaces.GMI)
    public synchronized InternationalString getFileType() {
        return fileType;
    }

    /**
     * Sets the type of file that contains the processing report.
     *
     * @param newValue The new file type value.
     */
    public synchronized void setFileType(final InternationalString newValue) {
        checkWritePermission();
        fileType = newValue;
    }
}
