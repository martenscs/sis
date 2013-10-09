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
package org.apache.sis.internal.util;

import java.util.Arrays;
import org.apache.sis.math.MathFunctions;
// No BigDecimal dependency - see class javadoc


/**
 * Basic arithmetic methods for extended precision numbers using the <cite>double-double</cite> algorithm.
 * This class implements some of the methods published in the following paper:
 *
 * <ul>
 *   <li>Yozo Hida, Xiaoye S. Li, David H. Bailey.
 *       <a href="http://web.mit.edu/tabbott/Public/quaddouble-debian/qd-2.3.4-old/docs/qd.pdf">Library
 *       for Double-Double and Quad-Double arithmetic</a>, 2007.</li>
 *   <li>Jonathan R. Shewchuk. Adaptive precision floating-point arithmetic and fast robust geometric predicates.
 *       Discrete & Computational Geometry, 18(3):305–363, 1997.</li>
 * </ul>
 *
 * {@code DoubleDouble} is used as an alternative to {@link java.math.BigDecimal} when we do not need arbitrary
 * precision, we do not want to convert from base 2 to base 10, we need support for NaN and infinities, we want
 * more compact storage and better performance. {@code DoubleDouble} can be converted to {@code BigDecimal} as
 * below:
 *
 * {@preformat java
 *     BigDecimal decimal = new BigDecimal(dd.value).add(new BigDecimal(dd.error));
 * }
 *
 * We do not provide convenience method for the above in order to avoid dependency to {@code BigDecimal}.
 *
 * {@section Impact of availability of FMA instructions}
 * If <cite>fused multiply-add</cite> (FMA) instruction are available in a future Java version
 * (see <a href="https://issues.apache.org/jira/browse/SIS-136">SIS-136</a> on Apache SIS JIRA),
 * then the following methods should be revisited:
 *
 * <ul>
 *   <li>{@link #setToProduct(double, double)} - revisit with [Hida & al.] algorithm 7.</li>
 * </ul>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.4
 * @version 0.4
 * @module
 *
 * @see <a href="http://en.wikipedia.org/wiki/Double-double_%28arithmetic%29#Double-double_arithmetic">Double-double arithmetic</a>
 */
public final class DoubleDouble extends Number {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -7602414219228638550L;

    /**
     * {@code true} for disabling the extended precision. This variable should always be {@code false},
     * except for testing purpose. If set to {@code true}, then all double-double arithmetic operations
     * are immediately followed by a clearing of {@link DoubleDouble#error}.  The result should then be
     * identical to computation performed using the normal {@code double} arithmetic.
     *
     * <p>Since this flag is static final, all expressions of the form {@code if (DISABLED)} should be
     * omitted by the compiler from the class files in normal operations.</p>
     *
     * <p>Setting this flag to {@code true} causes some JUnit tests to fail. This is normal. The main
     * purpose of this flag is to allow {@link org.apache.sis.referencing.operation.matrix.MatrixTestCase}
     * to perform strict comparisons of matrix operation results with JAMA, which is taken as the reference
     * implementation. Since JAMA uses {@code double} arithmetic, SIS needs to disable {@code double-double}
     * arithmetic if the results are to be compared for strict equality.</p>
     */
    public static final boolean DISABLED = false;

    /**
     * When computing <var>a</var> - <var>b</var> as a double-double (106 significand bits) value,
     * if the amount of non-zero significand bits is equals or lower than that amount, consider the
     * result as zero.
     */
    private static final int ZERO_THRESHOLD = 3;

    /**
     * The split constant used as part of multiplication algorithms. The split algorithm is as below
     * (we have to inline it in multiplication methods because Java can not return multi-values):
     *
     * {@preformat java
     *     private void split(double a) {
     *         double t   = SPLIT * a;
     *         double ahi = t - (t - a);
     *         double alo = a - ahi;
     *     }
     * }
     *
     * <p>Source: [Hida & al.] page 4 algorithm 5, itself reproduced from [Shewchuk] page 325.</p>
     */
    private static final double SPLIT = (1 << 27) + 1;

    /**
     * Maximal value that can be handled by {@link #multiply(double, double)}.
     * If a multiplication is using a value greater than {@code MAX_VALUE},
     * then the result will be infinity or NaN.
     */
    public static final double MAX_VALUE = Double.MAX_VALUE / SPLIT;

    /**
     * Pre-defined constants frequently used in SIS. SIS often creates matrices for unit conversions,
     * and most conversion factors are defined precisely in base 10. For example the conversion from
     * feet to metre is defined by a factor of exactly 0.3048, which can not be represented precisely
     * as a {@code double}. Consequently if a value of 0.3048 is given, we can assume that the intend
     * was to provide the "feet to metres" conversion factor and complete the double-double instance
     * accordingly.
     *
     * <p>Elements in this array shall be sorted in strictly increasing order.
     * For any value at index <var>i</var>, the associated error is {@code ERRORS[i]}.
     */
    private static final double[] VALUES = {
        // Some of the following constants have more fraction digits than necessary. We declare the extra
        // digits for documentation purpose, and in order to have identical content than DoubleDoubleTest
        // so that a plain copy-and-paste can be performed between those two classes.
         0.000001,
         0.00001,
         0.0001,
         0.00027777777777777777777777777777777778,  // Second to degrees
         0.001,
         0.002777777777777777777777777777777778,    // 1/360°
         0.01,
         0.01666666666666666666666666666666667,     // Minute to degrees
         0.01745329251994329576923690768488613,     // Degrees to radians
         0.1,
         0.201168,                                  // Link to metres
         0.3048,                                    // Feet to metres
         0.785398163397448309615660845819876,       // π/4
         0.9,                                       // Degrees to gradians
         0.9144,                                    // Yard to metres
         1.111111111111111111111111111111111,       // Gradian to degrees
         1.570796326794896619231321691639751,       // π/2
         1.8288,                                    // Fathom to metres
         2.356194490192344928846982537459627,       // π * 3/4
         2.54,                                      // Inch to centimetres
         3.14159265358979323846264338327950,        // π
         6.28318530717958647692528676655901,        // 2π
        20.1168,                                    // Chain to metres
        57.2957795130823208767981548141052          // Radians to degrees
    };

    /**
     * The errors associated to the values in the {@link #VALUES} array.
     *
     * <p>Tips:</p>
     * <ul>
     *   <li>To compute a new value in this array, just put zero and execute
     *       {@code DoubleDoubleTest.testErrorForWellKnownValue()}.
     *       The error message will give the expected value.</li>
     *   <li>If a computed value is zero, then there is no point to create an entry
     *       in the {@code (VALUES, ERRORS)} arrays for that value.</li>
     * </ul>
     */
    private static final double[] ERRORS = {
        /*  0.000001  */  4.525188817411374E-23,
        /*  0.00001   */ -8.180305391403131E-22,
        /*  0.0001    */ -4.79217360238593E-21,
        /*  0.000266… */  2.4093381610788987E-22,
        /*  0.001     */ -2.0816681711721686E-20,
        /*  0.002666… */ -1.0601087908747154E-19,
        /*  0.01      */ -2.0816681711721684E-19,
        /*  0.016666… */  2.312964634635743E-19,
        /*  0.017453… */  2.9486522708701687E-19,
        /*  0.1       */ -5.551115123125783E-18,
        /*  0.201168  */ -1.3471890270011499E-17,
        /*  0.3048    */ -1.5365486660812166E-17,
        /*  0.785398… */  3.061616997868383E-17,
        /*  0.9       */ -2.2204460492503132E-17,
        /*  0.9144    */  9.414691248821328E-18,
        /*  1.111111… */ -4.9343245538895844E-17,
        /*  1.570796… */  6.123233995736766E-17,
        /*  1.8288    */  1.8829382497642655E-17,
        /*  2.356194… */  9.184850993605148E-17,
        /*  2.54      */ -3.552713678800501E-17,
        /*  3.141592… */  1.2246467991473532E-16,
        /*  6.283185… */  2.4492935982947064E-16,
        /* 20.1168    */ -1.3471890270011499E-15,
        /* 57.295779… */ -1.9878495670576283E-15
    };

    /**
     * The main value, minus the {@link #error}.
     */
    public double value;

    /**
     * The error that shall be added to the main {@link #value} in order to get the
     * "<cite>real</cite>" (actually "<cite>the most accurate that we can</cite>") value.
     */
    public double error;

    /**
     * Creates a new value initialized to zero.
     */
    public DoubleDouble() {
    }

    /** Returns {@link #value}. */
    @Override public double doubleValue() {return value;}
    @Override public float  floatValue()  {return (float) value;}
    @Override public long   longValue()   {return Math.round(value);}
    @Override public int    intValue()    {return (int) longValue();}

    /**
     * If the given value is one of the well known constants, returns the error for that value.
     * Otherwise returns 0.
     *
     * @param  value The value for which to get this error.
     * @return The error for the given value, or 0 if unknown. In the later case,
     *         the given value is assumed to be the most accurate available representation.
     */
    public static double errorForWellKnownValue(final double value) {
        if (DISABLED) return 0;
        final int i = Arrays.binarySearch(VALUES, Math.abs(value));
        return (i >= 0) ? MathFunctions.xorSign(ERRORS[i], value) : 0;
    }

    /**
     * Returns {@code true} if this {@code DoubleDouble} is equals to zero.
     *
     * @return {@code true} if this {@code DoubleDouble} is equals to zero.
     */
    public boolean isZero() {
        return value == 0 && error == 0;
    }

    /**
     * Resets the {@link #value} and {@link #error} terms to zero.
     */
    public void clear() {
        value = 0;
        error = 0;
    }

    /**
     * Sets this {@code DoubleDouble} to the same value than the given instance.
     *
     * @param other The instance to copy.
     */
    public void setFrom(final DoubleDouble other) {
        value = other.value;
        error = other.error;
    }

    /**
     * Sets the {@link #value} and {@link #error} terms to values read from the given array.
     * This is a convenience method for a frequently used operation, implemented as below:
     *
     * {@preformat java
     *   value = array[index];
     *   error = array[index + errorOffset];
     * }
     *
     * @param array        The array from which to get the value and error.
     * @param index        Index of the value in the given array.
     * @param errorOffset  Offset to add to {@code index} in order to get the index of the error in the given array.
     */
    public void setFrom(final double[] array, final int index, final int errorOffset) {
        value = array[index];
        error = array[index + errorOffset];
    }

    /**
     * Equivalent to a call to {@code setToQuickSum(value, error)} inlined.
     * This is invoked after addition or multiplication operations.
     */
    final void normalize() {
        error += (value - (value += error));
        if (DISABLED) error = 0;
    }

    /**
     * Sets this {@code DoubleDouble} to the sum of the given numbers,
     * to be used only when {@code abs(a) >= abs(b)}.
     *
     * <p>Source: [Hida & al.] page 4 algorithm 3, itself reproduced from [Shewchuk] page 312.</p>
     *
     * @param a The first number to add.
     * @param b The second number to add, which must be smaller than {@code a}.
     */
    public void setToQuickSum(final double a, final double b) {
        value = a + b;
        error = b - (value - a);
        if (DISABLED) error = 0;
    }

    /**
     * Sets this {@code DoubleDouble} to the sum of the given numbers.
     *
     * <p>Source: [Hida & al.] page 4 algorithm 4, itself reproduced from [Shewchuk] page 314.</p>
     *
     * @param a The first number to add.
     * @param b The second number to add.
     */
    public void setToSum(final double a, final double b) {
        value = a + b;
        final double v = value - a;
        error = (a - (value - v)) + (b - v);
        if (DISABLED) error = 0;
    }

    /**
     * Sets this {@code DoubleDouble} to the product of the given numbers.
     * The given numbers shall not be greater than {@value #MAX_VALUE} in magnitude.
     *
     * <p>Source: [Hida & al.] page 4 algorithm 6, itself reproduced from [Shewchuk] page 326.</p>
     *
     * @param a The first number to multiply.
     * @param b The second number to multiply.
     */
    public void setToProduct(final double a, final double b) {
        value = a * b;
        double t = SPLIT * a;
        final double ahi = t - (t - a);
        final double alo = a - ahi;
        t = SPLIT * b;
        final double bhi = t - (t - b);
        final double blo = b - bhi;
        error = ((ahi*bhi - value) + ahi*blo + alo*bhi) + alo*blo;
        if (DISABLED) error = 0;
    }

    /**
     * Stores the {@link #value} and {@link #error} terms in the given array.
     * This is a convenience method for a frequently used operation, implemented as below:
     *
     * {@preformat java
     *   array[index] = value;
     *   array[index + errorOffset] = error;
     * }
     *
     * @param array        The array where to store the value and error.
     * @param index        Index of the value in the given array.
     * @param errorOffset  Offset to add to {@code index} in order to get the index of the error in the given array.
     */
    public void storeTo(final double[] array, final int index, final int errorOffset) {
        array[index] = value;
        array[index + errorOffset] = error;
    }

    /**
     * Swaps two double-double values in the given array.
     *
     * @param array        The array where to swap the values and errors.
     * @param i0           Index of the first value to swap.
     * @param i1           Index of the second value to swap.
     * @param errorOffset  Offset to add to the indices in order to get the error indices in the given array.
     *
     * @see org.apache.sis.util.ArraysExt#swap(double[], int, int)
     */
    public static void swap(final double[] array, int i0, int i1, final int errorOffset) {
        double t = array[i0];
        array[i0] = array[i1];
        array[i1] = t;
        t = array[i0 += errorOffset];
        array[i0] = array[i1 += errorOffset];
        array[i1] = t;
    }

    /**
     * Set this number to {@code -this}.
     */
    public void negate() {
        value = -value;
        error = -error;
    }

    /**
     * Adds an other double-double value to this {@code DoubleDouble}.
     * This is a convenience method for:
     *
     * {@preformat java
     *    add(other.value, other.error);
     * }
     *
     * @param other The other value to add to this value.
     */
    public void add(final DoubleDouble other) {
        add(other.value, other.error);
    }

    /**
     * Adds an other double-double value to this {@code DoubleDouble}.
     * The result is stored in this instance.
     *
     * {@section Implementation}
     * If <var>a</var> and <var>b</var> are {@code DoubleDouble} instances, then:
     *
     *   <blockquote>(a + b)</blockquote>
     *
     * can be computed as:
     *
     *   <blockquote>(a.value + a.error) + (b.value + b.error)<br>
     *             = (a.value + b.value) + (a.error + b.error)</blockquote>
     *
     * keeping in mind that the result of (a.value + b.value) has itself an error
     * which needs to be added to (a.error + b.error). In Java code:
     *
     * {@preformat java
     *   final double thisError = this.error;
     *   setToSum(value, otherValue);
     *   error += thisError;
     *   error += otherError;
     *   setToQuickSum(value, error);
     * }
     *
     * @param otherValue The other value to add to this {@code DoubleDouble}.
     * @param otherError The error of the other value to add to this {@code DoubleDouble}.
     */
    public void add(final double otherValue, final double otherError) {
        // Inline expansion of the code in above javadoc.
        double v = value;
        value += otherValue;
        error += v - (value + (v -= value)) + (otherValue + v);
        error += otherError;
        if (value == 0 && error != 0) {
            /*
             * The two values almost cancelled, only their error terms are different.
             * The number of significand bits (mantissa) in the IEEE 'double' representation is 52,
             * not counting the hidden bit. So estimate the accuracy of the double-double number as
             * the accuracy of the 'double' value (which is 1 ULP) scaled as if we had 52 additional
             * significand bits (we ignore some more bits if ZERO_THRESHOLD is greater than 1).
             * If the error is not greater than that value, then assume that it is not significant.
             */
            if (Math.abs(error) <= Math.scalb(Math.ulp(otherValue), ZERO_THRESHOLD - Numerics.SIGNIFICAND_SIZE)) {
                error = 0;
                return;
            }
        }
        normalize();
    }

    /**
     * Adds an other double-double value to this {@code DoubleDouble}, reading the values from an array.
     * This is a convenience method for a frequently used operation, implemented as below:
     *
     * {@preformat java
     *    add(array[index], array[index + errorOffset]);
     * }
     *
     * @param array        The array from which to get the value and error.
     * @param index        Index of the value in the given array.
     * @param errorOffset  Offset to add to {@code index} in order to get the index of the error in the given array.
     */
    public void add(final double[] array, final int index, final int errorOffset) {
        add(array[index], array[index + errorOffset]);
    }

    /**
     * Subtracts an other double-double value from this {@code DoubleDouble}.
     * This is a convenience method for:
     *
     * {@preformat java
     *    subtract(other.value, other.error);
     * }
     *
     * @param other The other value to subtract from this value.
     */
    public void subtract(final DoubleDouble other) {
        subtract(other.value, other.error);
    }

    /**
     * Subtracts an other double-double value from this {@code DoubleDouble}.
     * The result is stored in this instance.
     *
     * @param otherValue The other value to subtract from this {@code DoubleDouble}.
     * @param otherError The error of the other value to subtract from this {@code DoubleDouble}.
     */
    public void subtract(final double otherValue, final double otherError) {
        add(-otherValue, -otherError);
    }

    /**
     * Subtracts an other double-double value from this {@code DoubleDouble}, reading the values from an array.
     * This is a convenience method for a frequently used operation, implemented as below:
     *
     * {@preformat java
     *    subtract(array[index], array[index + errorOffset]);
     * }
     *
     * @param array        The array from which to get the value and error.
     * @param index        Index of the value in the given array.
     * @param errorOffset  Offset to add to {@code index} in order to get the index of the error in the given array.
     */
    public void subtract(final double[] array, final int index, final int errorOffset) {
        subtract(array[index], array[index + errorOffset]);
    }

    /**
     * Multiplies this {@code DoubleDouble} by an other double-double value.
     * This is a convenience method for:
     *
     * {@preformat java
     *    multiply(other.value, other.error);
     * }
     *
     * @param other The other value to add to this value.
     */
    public void multiply(final DoubleDouble other) {
        multiply(other.value, other.error);
    }

    /**
     * Multiplies this {@code DoubleDouble} by an other double-double value.
     * The result is stored in this instance.
     *
     * {@section Implementation}
     * If <var>a</var> and <var>b</var> are {@code DoubleDouble} instances, then:
     *
     *   <blockquote>(a * b)</blockquote>
     *
     * can be computed as:
     *
     *   <blockquote>(a.value + a.error) * (b.value + b.error)<br>
     *             = (a.value * b.value) + (a.error * b.value) + (a.value * b.error) + (a.error * b.error)<br>
     *             ≅ (a.value * b.value) + (a.error * b.value) + (a.value * b.error)</blockquote>
     *
     * The first term is the main product. All other terms are added to the error, keeping in mind that the main
     * product has itself an error. The last term (the product of errors) is ignored because presumed very small.
     * In Java code:
     *
     * {@preformat java
     *   final double thisValue = this.value;
     *   final double thisError = this.error;
     *   setToProduct(thisValue, otherValue);
     *   error += otherError * thisValue;
     *   error += otherValue * thisError;
     *   setToQuickSum(value, error);
     * }
     *
     * @param otherValue The other value by which to multiply this {@code DoubleDouble}.
     * @param otherError The error of the other value by which to multiply this {@code DoubleDouble}.
     */
    public void multiply(final double otherValue, final double otherError) {
        final double thisValue = this.value;
        final double thisError = this.error;
        setToProduct(thisValue, otherValue);
        error += otherError * thisValue;
        error += otherValue * thisError;
        normalize();
    }

    /**
     * Multiplies this {@code DoubleDouble} by an other double-double value stored in the given array.
     * This is a convenience method for a frequently used operation, implemented as below:
     *
     * {@preformat java
     *    multiply(array[index], array[index + errorOffset]);
     * }
     *
     * @param array        The array from which to get the value and error.
     * @param index        Index of the value in the given array.
     * @param errorOffset  Offset to add to {@code index} in order to get the index of the error in the given array.
     */
    public void multiply(final double[] array, final int index, final int errorOffset) {
        multiply(array[index], array[index + errorOffset]);
    }

    /**
     * Divides this {@code DoubleDouble} by an other double-double value.
     * This is a convenience method for:
     *
     * {@preformat java
     *    divide(other.value, other.error);
     * }
     *
     * @param other The other value to add to this value.
     */
    public void divide(final DoubleDouble other) {
        divide(other.value, other.error);
    }

    /**
     * Divides this {@code DoubleDouble} by an other double-double value.
     * The result is stored in this instance.
     *
     * @param denominatorValue The other value by which to divide this {@code DoubleDouble}.
     * @param denominatorError The error of the other value by which to divide this {@code DoubleDouble}.
     */
    public void divide(final double denominatorValue, final double denominatorError) {
        if (DISABLED) {
            value /= denominatorValue;
            error  = 0;
            return;
        }
        final double numeratorValue = value;
        final double numeratorError = error;
        value = denominatorValue;
        error = denominatorError;
        inverseDivide(numeratorValue, numeratorError);
    }

    /**
     * Divides this {@code DoubleDouble} by an other double-double value stored in the given array.
     * This is a convenience method for a frequently used operation, implemented as below:
     *
     * {@preformat java
     *    divide(array[index], array[index + errorOffset]);
     * }
     *
     * @param array        The array from which to get the value and error.
     * @param index        Index of the value in the given array.
     * @param errorOffset  Offset to add to {@code index} in order to get the index of the error in the given array.
     */
    public void divide(final double[] array, final int index, final int errorOffset) {
        divide(array[index], array[index + errorOffset]);
    }

    /**
     * Divides the given double-double value by this {@code DoubleDouble}.
     * This is a convenience method for:
     *
     * {@preformat java
     *    inverseDivide(other.value, other.error);
     * }
     *
     * @param other The other value to add to this value.
     */
    public void inverseDivide(final DoubleDouble other) {
        inverseDivide(other.value, other.error);
    }

    /**
     * Divides the given double-double value by this {@code DoubleDouble}.
     * The result is stored in this instance.
     *
     * {@section Implementation}
     * If <var>a</var> and <var>b</var> are {@code DoubleDouble} instances, then we estimate:
     *
     *   <blockquote>(a / b) = (a.value / b.value) + remainder / b</blockquote>
     *
     * where:
     *
     *   <blockquote>remainder = a - b * (a.value / b.value)</blockquote>
     *
     * @param numeratorValue The other value to divide by this {@code DoubleDouble}.
     * @param numeratorError The error of the other value to divide by this {@code DoubleDouble}.
     */
    public void inverseDivide(final double numeratorValue, final double numeratorError) {
        if (DISABLED) {
            value = numeratorValue / value;
            error = 0;
            return;
        }
        final double denominatorValue = value;
        /*
         * The 'b * (a.value / b.value)' part in the method javadoc.
         */
        final double quotient = numeratorValue / denominatorValue;
        multiply(quotient, 0);
        /*
         * Compute 'remainder' as 'a - above_product'.
         */
        final double productError = error;
        setToSum(numeratorValue, -value);
        error -= productError;  // Complete the above subtraction
        error += numeratorError;
        /*
         * Adds the 'remainder / b' term, using 'remainder / b.value' as an approximation
         * (otherwise we would have to invoke this method recursively). The approximation
         * is assumed okay since the second term is small compared to the first one.
         */
        setToQuickSum(quotient, (value + error) / denominatorValue);
    }

    /**
     * Divides the given double-double value by this {@code DoubleDouble}.
     * This is a convenience method for a frequently used operation, implemented as below:
     *
     * {@preformat java
     *    inverseDivide(array[index], array[index + errorOffset]);
     * }
     *
     * @param array        The array from which to get the value and error.
     * @param index        Index of the value in the given array.
     * @param errorOffset  Offset to add to {@code index} in order to get the index of the error in the given array.
     */
    public void inverseDivide(final double[] array, final int index, final int errorOffset) {
        inverseDivide(array[index], array[index + errorOffset]);
    }

    /**
     * Returns a string representation of this number for debugging purpose.
     * The returned string does not need to contains all digits that this {@code DoubleDouble} can handle.
     *
     * @return A string representation of this number.
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}