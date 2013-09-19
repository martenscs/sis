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
package org.apache.sis.referencing.operation.matrix;

import org.apache.sis.test.DependsOn;

import static org.junit.Assert.*;


/**
 * Tests the {@link NonSquareMatrix} implementation.
 * This class inherits all tests defined in {@link MatrixTestCase}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.4
 * @version 0.4
 * @module
 */
@DependsOn(GeneralMatrixTest.class)
public final strictfp class NonSquareMatrixTest extends MatrixTestCase {
    /**
     * Number of rows and columns, initialized by {@link #initialize(String, boolean)}.
     */
    private final int numRow, numCol;

    /**
     * Creates a test with a random size for the matrix and ensure that the matrix is not square.
     */
    public NonSquareMatrixTest() {
        numRow = 5 + random.nextInt(8); // Matrix sizes from 5 to 12 inclusive.
        int n;
        do n = 5 + random.nextInt(8);
        while (n == numRow);
        numCol = n;
    }

    /** {@inheritDoc} */ @Override int getNumRow() {return numRow;}
    /** {@inheritDoc} */ @Override int getNumCol() {return numCol;}

    /**
     * Ensures that the given matrix is an instance of the expected type.
     */
    @Override
    void validate(final MatrixSIS matrix) {
        super.validate(matrix);
        assertEquals(NonSquareMatrix.class, matrix.getClass());
    }

    /**
     * TODO: inverse transform not yet implemented for non-square matrix.
     */
    @Override
    @org.junit.Ignore
    public void testInverse() throws NoninvertibleMatrixException {
    }
}
