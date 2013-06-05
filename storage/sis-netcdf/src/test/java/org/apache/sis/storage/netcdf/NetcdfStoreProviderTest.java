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
package org.apache.sis.storage.netcdf;

import java.io.IOException;
import ucar.nc2.NetcdfFile;
import org.opengis.wrapper.netcdf.IOTestCase;
import org.apache.sis.internal.netcdf.Decoder;
import org.apache.sis.internal.netcdf.impl.ChannelDecoder;
import org.apache.sis.internal.netcdf.ucar.DecoderWrapper;
import org.apache.sis.internal.netcdf.impl.ChannelDecoderTest;
import org.apache.sis.storage.DataStoreConnection;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.test.DependsOn;
import org.junit.Test;

import static org.opengis.test.Assert.*;
import static org.opengis.wrapper.netcdf.IOTestCase.NCEP;


/**
 * Tests {@link NetcdfStoreProvider}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3
 * @version 0.3
 * @module
 */
@DependsOn({
    ChannelDecoderTest.class
})
public final strictfp class NetcdfStoreProviderTest extends IOTestCase {
    /**
     * Tests {@link NetcdfStoreProvider#canOpen(DataStoreConnection)} for an input stream which shall
     * be recognized as a classic NetCDF file.
     *
     * @throws DataStoreException Should never happen.
     */
    @Test
    public void testCanOpenFromStream() throws DataStoreException {
        final DataStoreConnection c = new DataStoreConnection(IOTestCase.class.getResourceAsStream(NCEP));
        final NetcdfStoreProvider provider = new NetcdfStoreProvider();
        assertTrue(provider.canOpen(c));
        c.closeAllExcept(null);
    }

    /**
     * Tests {@link NetcdfStoreProvider#canOpen(DataStoreConnection)} for a UCAR {@link NetcdfFile} object.
     *
     * @throws IOException If an error occurred while opening the NetCDF file.
     * @throws DataStoreException Should never happen.
     */
    @Test
    public void testCanOpenFromUCAR() throws IOException, DataStoreException {
        final NetcdfFile file = open(NCEP);
        final DataStoreConnection c = new DataStoreConnection(file);
        final NetcdfStoreProvider provider = new NetcdfStoreProvider();
        assertTrue(provider.canOpen(c));
        file.close();
    }

    /**
     * Tests {@link NetcdfStoreProvider#decoder(WarningProducer, DataStoreConnection)} for an input stream which
     * shall be recognized as a classic NetCDF file. The provider shall instantiate a {@link ChannelDecoder}.
     *
     * @throws IOException If an error occurred while opening the NetCDF file.
     * @throws DataStoreException Should never happen.
     */
    @Test
    public void testDecoderFromStream() throws IOException, DataStoreException {
        final DataStoreConnection c = new DataStoreConnection(IOTestCase.class.getResourceAsStream(NCEP));
        final Decoder decoder = NetcdfStoreProvider.decoder(null, c);
        assertInstanceOf(NCEP, ChannelDecoder.class, decoder);
        decoder.close();
    }

    /**
     * Tests {@link NetcdfStoreProvider#decoder(WarningProducer, DataStoreConnection)} for a UCAR
     * {@link NetcdfFile} object. The provider shall instantiate a {@link DecoderWrapper}.
     *
     * @throws IOException If an error occurred while opening the NetCDF file.
     * @throws DataStoreException Should never happen.
     */
    @Test
    public void testDecoderFromUCAR() throws IOException, DataStoreException {
        final DataStoreConnection c = new DataStoreConnection(open(NCEP));
        final Decoder decoder = NetcdfStoreProvider.decoder(null, c);
        assertInstanceOf(NCEP, DecoderWrapper.class, decoder);
        decoder.close();
    }
}