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
package org.apache.sis.referencing.operation.transform;

import java.util.List;
import java.util.Collections;
import java.awt.geom.AffineTransform;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.MathTransformFactory;
import org.apache.sis.internal.referencing.DirectPositionView;
import org.apache.sis.internal.referencing.j2d.AffineTransform2D;
import org.apache.sis.referencing.operation.matrix.AffineTransforms2D;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.util.Static;

import static org.apache.sis.util.ArgumentChecks.*;


/**
 * Utility methods creating or working on {@link MathTransform} instances.
 * This class centralizes in one place some of the most commonly used functions this package.
 * The {@code MathTransforms} class provides the following services:
 *
 * <ul>
 *   <li>Create various SIS implementations of {@link MathTransform}.</li>
 *   <li>Perform non-standard operations on arbitrary instances.</li>
 * </ul>
 *
 * The factory static methods are provided as convenient alternatives to the GeoAPI {@link MathTransformFactory}
 * interface. However users seeking for more implementation neutrality are encouraged to limit themselves to the
 * GeoAPI factory interfaces instead.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.5 (derived from geotk-3.20)
 * @version 0.5
 * @module
 *
 * @see MathTransformFactory
 */
public final class MathTransforms extends Static {
    /**
     * Do not allow instantiation of this class.
     */
    private MathTransforms() {
    }

    /**
     * Returns an identity transform of the specified dimension.
     *
     * <p>Special cases:</p>
     * <ul>
     *   <li>If {@code dimension == 1}, then the returned transform implements {@link MathTransform1D}.</li>
     *   <li>If {@code dimension == 2}, then the returned transform implements {@link MathTransform2D}.</li>
     * </ul>
     *
     * @param dimension The dimension of the transform to be returned.
     * @return An identity transform of the specified dimension.
     */
    public static LinearTransform identity(final int dimension) {
        ensureStrictlyPositive("dimension", dimension);
        return IdentityTransform.create(dimension);
    }

    /**
     * Creates a one-dimensional affine transform for the given coefficients.
     * Input values <var>x</var> will be converted into output values <var>y</var> using the following equation:
     *
     * <blockquote><var>y</var>  =  <var>x</var> ⋅ {@code scale} + {@code offset}</blockquote>
     *
     * @param  scale  The {@code scale}  term in the linear equation.
     * @param  offset The {@code offset} term in the linear equation.
     * @return The linear transform for the given scale and offset.
     */
    public static LinearTransform linear(final double scale, final double offset) {
        return LinearTransform1D.create(scale, offset);
    }

    /**
     * Creates an arbitrary linear transform from the specified matrix. Usually the matrix
     * {@linkplain org.apache.sis.referencing.operation.matrix.MatrixSIS#isAffine() is affine},
     * but this is not mandatory. Non-affine matrix will define a projective transform.
     *
     * <p>If the transform input dimension is {@code M}, and output dimension is {@code N},
     * then the given matrix shall have size {@code [N+1][M+1]}.
     * The +1 in the matrix dimensions allows the matrix to do a shift, as well as a rotation.
     * The {@code [M][j]} element of the matrix will be the <var>j</var>'th ordinate of the moved origin.</p>
     *
     * @param  matrix The matrix used to define the linear transform.
     * @return The linear (usually affine) transform.
     *
     * @see org.opengis.referencing.operation.MathTransformFactory#createAffineTransform(Matrix)
     */
    public static LinearTransform linear(final Matrix matrix) {
        ensureNonNull("matrix", matrix);
        final int sourceDimension = matrix.getNumCol() - 1;
        final int targetDimension = matrix.getNumRow() - 1;
        if (sourceDimension == targetDimension) {
            if (matrix.isIdentity()) {
                return identity(sourceDimension);
            }
            if (Matrices.isAffine(matrix)) {
                switch (sourceDimension) {
                    case 1: return linear(matrix.getElement(0,0), matrix.getElement(0,1));
                    case 2: return new AffineTransform2D(
                            matrix.getElement(0,0), matrix.getElement(1,0),
                            matrix.getElement(0,1), matrix.getElement(1,1),
                            matrix.getElement(0,2), matrix.getElement(1,2));
                }
            } else if (sourceDimension == 2) {
                return new ProjectiveTransform2D(matrix);
            }
        }
        final LinearTransform candidate = CopyTransform.create(matrix);
        if (candidate != null) {
            return candidate;
        }
        return new ProjectiveTransform(matrix);
    }

    /**
     * Concatenates the two given transforms. The returned transform will implement
     * {@link MathTransform1D} or {@link MathTransform2D} if the dimensions of the
     * concatenated transform are equal to 1 or 2 respectively.
     *
     * @param tr1 The first math transform.
     * @param tr2 The second math transform.
     * @return    The concatenated transform.
     * @throws MismatchedDimensionException if the output dimension of the first transform
     *         does not match the input dimension of the second transform.
     *
     * @see MathTransformFactory#createConcatenatedTransform(MathTransform, MathTransform)
     */
    public static MathTransform concatenate(final MathTransform tr1, final MathTransform tr2)
            throws MismatchedDimensionException
    {
        ensureNonNull("tr1", tr1);
        ensureNonNull("tr2", tr2);
        return ConcatenatedTransform.create(tr1, tr2);
    }

    /**
     * Concatenates the given one-dimensional transforms. This is a convenience methods
     * delegating to {@link #concatenate(MathTransform, MathTransform)} and casting the
     * result to a {@link MathTransform1D} instance.
     *
     * @param tr1 The first math transform.
     * @param tr2 The second math transform.
     * @return    The concatenated transform.
     * @throws MismatchedDimensionException if the output dimension of the first transform
     *         does not match the input dimension of the second transform.
     */
    public static MathTransform1D concatenate(MathTransform1D tr1, MathTransform1D tr2)
            throws MismatchedDimensionException
    {
        return (MathTransform1D) concatenate((MathTransform) tr1, (MathTransform) tr2);
    }

    /**
     * Concatenates the given two-dimensional transforms. This is a convenience methods
     * delegating to {@link #concatenate(MathTransform, MathTransform)} and casting the
     * result to a {@link MathTransform2D} instance.
     *
     * @param tr1 The first math transform.
     * @param tr2 The second math transform.
     * @return    The concatenated transform.
     * @throws MismatchedDimensionException if the output dimension of the first transform
     *         does not match the input dimension of the second transform.
     */
    public static MathTransform2D concatenate(MathTransform2D tr1, MathTransform2D tr2)
            throws MismatchedDimensionException
    {
        return (MathTransform2D) concatenate((MathTransform) tr1, (MathTransform) tr2);
    }

    /**
     * Concatenates the three given transforms. This is a convenience methods doing its job
     * as two consecutive concatenations.
     *
     * @param tr1 The first math transform.
     * @param tr2 The second math transform.
     * @param tr3 The third math transform.
     * @return    The concatenated transform.
     * @throws MismatchedDimensionException if the output dimension of a transform
     *         does not match the input dimension of next transform.
     */
    public static MathTransform concatenate(MathTransform tr1, MathTransform tr2, MathTransform tr3)
            throws MismatchedDimensionException
    {
        ensureNonNull("tr1", tr1);
        ensureNonNull("tr2", tr2);
        ensureNonNull("tr3", tr3);
        return concatenate(concatenate(tr1, tr2), tr3);
    }

    /**
     * Concatenates the three given one-dimensional transforms. This is a convenience methods
     * delegating to {@link #concatenate(MathTransform, MathTransform, MathTransform)} and
     * casting the result to a {@link MathTransform1D} instance.
     *
     * @param tr1 The first math transform.
     * @param tr2 The second math transform.
     * @param tr3 The third math transform.
     * @return    The concatenated transform.
     * @throws MismatchedDimensionException if the output dimension of a transform
     *         does not match the input dimension of next transform.
     */
    public static MathTransform1D concatenate(MathTransform1D tr1, MathTransform1D tr2, MathTransform1D tr3)
            throws MismatchedDimensionException
    {
        return (MathTransform1D) concatenate((MathTransform) tr1, (MathTransform) tr2, (MathTransform) tr3);
    }

    /**
     * Concatenates the three given two-dimensional transforms. This is a convenience methods
     * delegating to {@link #concatenate(MathTransform, MathTransform, MathTransform)} and
     * casting the result to a {@link MathTransform2D} instance.
     *
     * @param tr1 The first math transform.
     * @param tr2 The second math transform.
     * @param tr3 The third math transform.
     * @return    The concatenated transform.
     * @throws MismatchedDimensionException if the output dimension of a transform
     *         does not match the input dimension of next transform.
     */
    public static MathTransform2D concatenate(MathTransform2D tr1, MathTransform2D tr2, MathTransform2D tr3)
            throws MismatchedDimensionException
    {
        return (MathTransform2D) concatenate((MathTransform) tr1, (MathTransform) tr2, (MathTransform) tr3);
    }

    /**
     * Returns all single components of the given (potentially concatenated) transform.
     * This method makes the following choice:
     *
     * <ul>
     *   <li>If {@code transform} is {@code null}, returns an empty list.</li>
     *   <li>Otherwise if {@code transform} is the result of a call to a {@code concatenate(…)} method,
     *       returns all components. All nested concatenated transforms (if any) will be expanded.</li>
     *   <li>Otherwise returns the given transform in a list of size 1.</li>
     * </ul>
     *
     * @param  transform The transform for which to get the components, or {@code null}.
     * @return All single math transforms performed by this concatenated transform.
     */
    public static List<MathTransform> getSteps(final MathTransform transform) {
        if (transform != null) {
            if (transform instanceof ConcatenatedTransform) {
                return ((ConcatenatedTransform) transform).getSteps();
            } else {
                return Collections.singletonList(transform);
            }
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * If the given transform is linear, returns its coefficients as a matrix.
     * More specifically:
     *
     * <ul>
     *   <li>If the given transform is an instance of {@link LinearTransform},
     *       returns {@link LinearTransform#getMatrix()}.</li>
     *   <li>Otherwise if the given transform is an instance of {@link AffineTransform},
     *       returns its coefficients in a {@link org.apache.sis.referencing.operation.matrix.Matrix3} instance.</li>
     *   <li>Otherwise returns {@code null}.</li>
     * </ul>
     *
     * @param  transform The transform for which to get the matrix, or {@code null}.
     * @return The matrix of the given transform, or {@code null} if none.
     */
    public static Matrix getMatrix(final MathTransform transform) {
        if (transform instanceof LinearTransform) {
            return ((LinearTransform) transform).getMatrix();
        }
        if (transform instanceof AffineTransform) {
            return AffineTransforms2D.toMatrix((AffineTransform) transform);
        }
        return null;
    }

    /**
     * A buckle method for calculating derivative and coordinate transformation in a single step.
     * The transform result is stored in the given destination array, and the derivative matrix
     * is returned. Invoking this method is equivalent to the following code, except that it may
     * execute faster with some {@code MathTransform} implementations:
     *
     * {@preformat java
     *     DirectPosition ptSrc = ...;
     *     DirectPosition ptDst = ...;
     *     Matrix matrixDst = derivative(ptSrc);
     *     ptDst = transform(ptSrc, ptDst);
     * }
     *
     * @param transform The transform to use.
     * @param srcPts The array containing the source coordinate.
     * @param srcOff The offset to the point to be transformed in the source array.
     * @param dstPts the array into which the transformed coordinate is returned.
     * @param dstOff The offset to the location of the transformed point that is stored in the destination array.
     * @return The matrix of the transform derivative at the given source position.
     * @throws TransformException If the point can't be transformed or if a problem occurred
     *         while calculating the derivative.
     */
    public static Matrix derivativeAndTransform(final MathTransform transform,
                                                final double[] srcPts, final int srcOff,
                                                final double[] dstPts, final int dstOff)
            throws TransformException
    {
        if (transform instanceof AbstractMathTransform) {
            return ((AbstractMathTransform) transform).transform(srcPts, srcOff, dstPts, dstOff, true);
        }
        // Must be calculated before to transform the coordinate.
        final Matrix derivative = transform.derivative(new DirectPositionView(srcPts, srcOff, transform.getSourceDimensions()));
        if (dstPts != null) {
            transform.transform(srcPts, srcOff, dstPts, dstOff, 1);
        }
        return derivative;
    }
}
