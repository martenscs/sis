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
package org.apache.sis.util;

import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.Locale;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.MissingResourceException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.sis.util.logging.Logging;
import org.apache.sis.util.resources.Errors;

import static org.apache.sis.util.CharSequences.trimWhitespaces;
import static org.apache.sis.util.collection.Containers.hashMapCapacity;


/**
 * Static methods working on {@link Locale} instances. While this class is documented as
 * providing static methods, a few methods are actually non-static. Those methods need to be
 * invoked on the {@link #ALL} or {@link #SIS} instance in order to specify the scope.
 * Examples:
 *
 * {@preformat java
 *     Locales[] lc1 = Locales.ALL.getAvailableLanguages();  // All languages installed on the JavaVM.
 *     Locales[] lc2 = Locales.SIS.getAvailableLanguages();  // Only the languages known to Apache SIS.
 * }
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.3 (derived from geotk-2.4)
 * @version 0.4
 * @module
 */
public final class Locales extends Static {
    /**
     * A read-only map for canonicalizing the locales. Filled on class
     * initialization in order to avoid the need for synchronization.
     */
    private static final Map<Locale,Locale> POOL;
    static {
        final Locale[] locales = Locale.getAvailableLocales();
        POOL = new HashMap<Locale,Locale>(hashMapCapacity(locales.length));
        for (final Locale lc : locales) {
            POOL.put(lc, lc);
        }
        /*
         * Add the static field constants. This operation may replace some values which
         * were returned by Locale.getAvailableLocales(). This is the desired effect,
         * since we want to give precedence to references to the static constants.
         */
        try {
            for (final Field field : Locale.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (Locale.class.isAssignableFrom(field.getType())) {
                        final Locale toAdd = (Locale) field.get(null);
                        POOL.put(toAdd, toAdd);
                    }
                }
            }
        } catch (IllegalAccessException exception) {
            /*
             * Not a big deal if this operation fails (this is actually just an
             * optimization for reducing memory usage). Log a warning and stop.
             */
            Logging.unexpectedException(Locales.class, "<clinit>", exception);
        }
    }

    /**
     * Bit mask for differentiating language codes from country codes in the {@link #ISO2} and {@link #ISO3} arrays.
     */
    private static final short LANGUAGE = 0, COUNTRY = Short.MIN_VALUE;

    /**
     * Mapping from 3-letters codes to 2-letters codes. We use {@code short} type instead of {@link String}
     * for compactness (conversions is done by {@link #toNumber(String, int)}) and for avoiding references
     * to {@code String} instances.
     *
     * {@note Oracle JDK8 implementation computes the 3-letters codes on-the-fly instead of holding references
     *        to pre-existing strings. If we were holding string references here, we would prevent the garbage
     *        collector to collect the strings for all languages and countries. This would probably be a waste
     *        of resources.}
     */
    private static final short[] ISO3, ISO2;
    static {
        final Short CONFLICT = (short) 0; // Sentinal value for conflicts (paranoiac safety).
        final Map<Short,Short> map = new TreeMap<Short,Short>();
        for (final Locale locale : POOL.values()) {
            short type = LANGUAGE; // 0 for language, or leftmost bit set for country.
            do { // Executed exactly twice: once for language, than once for country.
                final short alpha2 = toNumber((type == LANGUAGE) ? locale.getLanguage() : locale.getCountry(), type);
                if (alpha2 != 0) {
                    final short alpha3;
                    try {
                        alpha3 = toNumber((type == LANGUAGE) ? locale.getISO3Language() : locale.getISO3Country(), type);
                    } catch (MissingResourceException e) {
                        continue; // No 3-letters code to map for this locale.
                    }
                    if (alpha3 != 0 && alpha3 != alpha2) {
                        final Short p = map.put(alpha3, alpha2);
                        if (p != null && p.shortValue() != alpha2) {
                            // We do not expect any conflict. But if it happen anyway, conservatively
                            // remember that we should not perform any substitution for that code.
                            map.put(alpha3, CONFLICT);
                        }
                    }
                }
            } while ((type ^= COUNTRY) != LANGUAGE);
        }
        while (map.values().remove(CONFLICT)); // Remove all conflicts that we may have found.
        ISO3 = new short[map.size()];
        ISO2 = new short[map.size()];
        int i = 0;
        for (final Map.Entry<Short,Short> entry : map.entrySet()) {
            ISO3[i]   = entry.getKey();
            ISO2[i++] = entry.getValue();
        }
    }

    /**
     * All locales available on the JavaVM.
     */
    public static final Locales ALL = new Locales();

    /**
     * Only locales available in the Apache SIS library. They are the locales for which localized
     * resources are provided in the {@link org.apache.sis.util.resources} package.
     */
    public static final Locales SIS = new Locales();

    /**
     * Do not allow instantiation of this class,
     * except for the constants defined in this class.
     */
    private Locales() {
    }

    /**
     * Returns the languages known to the JavaVM ({@link #ALL}) or to the Apache SIS library
     * ({@link #SIS}). In the later case, this method returns only the languages for which
     * localized resources are provided in the {@link org.apache.sis.util.resources} package.
     *
     * @return The list of supported languages.
     */
    public Locale[] getAvailableLanguages() {
        if (this == ALL) {
            return getLanguages(Locale.getAvailableLocales());
        }
        return new Locale[] {
            Locale.ENGLISH,
            Locale.FRENCH
        };
    }

    /**
     * Returns the locales known to the JavaVM ({@link #ALL}) or to the Apache SIS library
     * ({@link #SIS}). In the later case, this method returns only the locales for which
     * localized resources are provided in the {@link org.apache.sis.util.resources} package.
     *
     * @return The list of supported locales.
     */
    public Locale[] getAvailableLocales() {
        if (this == ALL) {
            return Locale.getAvailableLocales();
        }
        Locale[] locales = getAvailableLanguages();
        final String[] languages = new String[locales.length];
        for (int i=0; i<languages.length; i++) {
            languages[i] = locales[i].getLanguage();
        }
        int count = 0;
        locales = Locale.getAvailableLocales();
filter: for (final Locale locale : locales) {
            final String code = locale.getLanguage();
            for (int i=0; i<languages.length; i++) {
                if (code.equals(languages[i])) {
                    locales[count++] = unique(locale);
                    continue filter;
                }
            }
        }
        locales = ArraysExt.resize(locales, count);
        return locales;
    }

    /**
     * Returns the list of {@linkplain #getAvailableLocales() available locales} formatted
     * as strings in the specified locale.
     *
     * @param  locale The locale to use for formatting the strings to be returned.
     * @return String descriptions of available locales.
     *
     * @deprecated Not useful in practice, since we typically also need the original Locale object.
     */
    @Deprecated // Remove for simplifiying the API.
    public String[] getAvailableLocales(final Locale locale) {
        final Locale[] locales = getAvailableLocales();
        final String[] display = new String[locales.length];
        for (int i=0; i<locales.length; i++) {
            display[i] = locales[i].getDisplayName(locale);
        }
        Arrays.sort(display);
        return display;
    }

    /**
     * Returns the languages of the given locales, without duplicated values.
     * The instances returned by this method have no {@linkplain Locale#getCountry() country}
     * and no {@linkplain Locale#getVariant() variant} information.
     *
     * @param  locales The locales from which to get the languages.
     * @return The languages, without country or variant information.
     *
     * @deprecated Users can easily perform this operation themselves, thus avoiding this class initialization.
     */
    @Deprecated // Make this method private for simplifiying the API.
    public static Locale[] getLanguages(final Locale... locales) {
        final Set<String> codes = new LinkedHashSet<String>(hashMapCapacity(locales.length));
        for (final Locale locale : locales) {
            codes.add(locale.getLanguage());
        }
        int i=0;
        final Locale[] languages = new Locale[codes.size()];
        for (final String code : codes) {
            languages[i++] = unique(new Locale(code));
        }
        return languages;
    }

    /**
     * Parses the given language code, optionally followed by country code and variant. The given string can be either
     * the 2 letters or the 3 letters ISO 639 code. It can optionally be followed by the {@code '_'} character and the
     * country code (again either as 2 or 3 letters), optionally followed by {@code '_'} and the variant.
     *
     * <p>This method can be used when the caller wants the same {@code Locale} constants no matter if the language
     * and country codes use 2 or 3 letters. This method tries to convert 3-letters codes to 2-letters code on a
     * <cite>best effort</cite> basis.</p>
     *
     * @param  code The language code, optionally followed by country code and variant.
     * @return The language for the given code (never {@code null}).
     * @throws IllegalArgumentException If the given code doesn't seem to be a valid locale.
     *
     * @see Locale#forLanguageTag(String)
     */
    public static Locale parse(final String code) throws IllegalArgumentException {
        return parse(code, 0);
    }

    /**
     * Parses the given language code and optional complements (country, variant), starting at the given index.
     * All characters before {@code fromIndex} are ignored. Characters from {@code fromIndex} to the end of the
     * string are parsed as documented in the {@link #parse(String)} method. In particular, this method tries to
     * convert 3-letters codes to 2-letters code on a <cite>best effort</cite> basis.
     *
     * {@example This method is useful when language codes are appended to a base property or resource name.
     *           For example a dictionary may define the <code>"remarks"</code> property by values associated
     *           to the <code>"remarks_en"</code> and <code>"remarks_fr"</code> keys, for English and French
     *           locales respectively.}
     *
     * @param  code The language code, which may be followed by country code.
     * @param  fromIndex Index of the first character to parse.
     * @return The language for the given code (never {@code null}).
     * @throws IllegalArgumentException If the given code doesn't seem to be a valid locale.
     *
     * @see Locale#forLanguageTag(String)
     * @see org.apache.sis.util.iso.Types#toInternationalString(Map, String)
     */
    public static Locale parse(final String code, final int fromIndex) throws IllegalArgumentException {
        ArgumentChecks.ensureNonNull("code", code);
        ArgumentChecks.ensurePositive("fromIndex", fromIndex);
        boolean hasMore = false;
        String language, country, variant;
        int ci = code.indexOf('_', fromIndex);
        if (ci < 0) {
            language = (String) trimWhitespaces(code, fromIndex, code.length());
            country  = "";
            variant  = "";
        } else {
            language = (String) trimWhitespaces(code, fromIndex, ci);
            int vi = code.indexOf('_', ++ci);
            if (vi < 0) {
                country = (String) trimWhitespaces(code, ci, code.length());
                variant = "";
            } else {
                country = (String) trimWhitespaces(code, ci, vi);
                variant = (String) trimWhitespaces(code, ++vi, code.length());
                hasMore = code.indexOf('_', vi) >= 0;
            }
        }
        if (hasMore || language.length() > 3 || country.length() > 3) {
            throw new IllegalArgumentException(Errors.format(Errors.Keys.IllegalLanguageCode_1,
                    code.substring(fromIndex)));
        }
        language = toISO2(language, LANGUAGE);
        country  = toISO2(country,  COUNTRY);
        return unique(new Locale(language, country, variant));
    }

    /**
     * Parses the language encoded in the suffix of a property key. This convenience method
     * is used when a property in a {@link java.util.Map} may have many localized variants.
     * For example the {@code "remarks"} property may be defined by values associated to the
     * {@code "remarks_en"} and {@code "remarks_fr"} keys, for English and French locales
     * respectively.
     *
     * <p>This method infers the {@code Locale} from the property {@code key} with the following steps:</p>
     *
     * <ul>
     *   <li>If the given {@code key} is exactly equals to {@code prefix},
     *       then this method returns {@link Locale#ROOT}.</li>
     *   <li>Otherwise if the given {@code key} does not start with the specified {@code prefix}
     *       followed by the {@code '_'} character, then this method returns {@code null}.</li>
     *   <li>Otherwise, the characters after the {@code '_'} are parsed as an ISO language
     *       and country code by the {@link #parse(String, int)} method.</li>
     * </ul>
     *
     * @param  prefix The prefix to skip at the beginning of the {@code key}.
     * @param  key    The property key from which to extract the locale, or {@code null}.
     * @return The locale encoded in the given key name, or {@code null} if the key has not been recognized.
     * @throws IllegalArgumentException if the locale after the prefix is an illegal code.
     *
     * @see org.apache.sis.util.iso.Types#toInternationalString(Map, String)
     *
     * @deprecated Users can easily perform this operation themselves, thus avoiding this class initialization.
     */
    @Deprecated
    public static Locale parseSuffix(final String prefix, final String key) throws IllegalArgumentException {
        ArgumentChecks.ensureNonNull("prefix", prefix);
        if (key != null) { // Tolerance for Map that accept null keys.
            if (key.startsWith(prefix)) {
                final int offset = prefix.length();
                if (key.length() == offset) {
                    return Locale.ROOT;
                }
                if (key.charAt(offset) == '_') {
                    return parse(key, offset + 1);
                }
            }
        }
        return null;
    }

    /**
     * Converts a 3-letters ISO code to a 2-letters one. If the given code is not recognized,
     * then this method returns {@code code} unmodified.
     *
     * @param  code The 3-letters code.
     * @param  type Either {@link #LANGUAGE} or {@link #COUNTRY}.
     * @return The 2-letters code, or {@code null} if none.
     */
    private static String toISO2(final String code, final short type) {
        final short alpha3 = toNumber(code, type);
        if (alpha3 != 0) {
            int alpha2 = Arrays.binarySearch(ISO3, alpha3);
            if (alpha2 >= 0) {
                alpha2 = ISO2[alpha2];
                final int base = (alpha2 & COUNTRY) != 0 ? ('A' - 1) : ('a' - 1);
                alpha2 &= ~COUNTRY;
                int i = 0;
                final char[] c = new char[3]; // 2 should be enough, but our impl. actually allows 3-letters codes too.
                do c[i++] = (char) ((alpha2 & 0x1F) + base);
                while ((alpha2 >>>= 5) != 0);
                return String.valueOf(c, 0, i);
            }
        }
        return code;
    }

    /**
     * Converts the given 1-, 2- or 3- letters alpha code to a 15 bits numbers. Each letter uses 5 bits.
     * If an invalid character is found, then this method returns 0.
     *
     * <p>This method does not use the sign bit. Callers can use it for differentiating language codes
     * from country codes, using the {@link #LANGUAGE} or {@link #COUNTRY} bit masks.</p>
     *
     * @param  code The 1-, 2- or 3- letters alpha code to convert.
     * @param  n Initial bit pattern, either {@link #LANGUAGE} or {@link #COUNTRY}.
     * @return A number for the given code, or 0 if a non alpha characters were found.
     */
    private static short toNumber(final String code, short n) {
        final int length = code.length();
        if (length >= 1 && length <= 3) {
            int shift = 0;
            for (int i=0; i<length; i++) {
                int c = code.charAt(i);
                if (c < 'A' || (c -= (c >= 'a') ? ('a' - 1) : ('A' - 1)) > 26) {
                    return 0;
                }
                n |= c << shift;
                shift += 5;
            }
            return n;
        }
        return 0;
    }

    /**
     * Returns a unique instance of the given locale, if one is available.
     * Otherwise returns the {@code locale} unchanged.
     *
     * @param  locale The locale to canonicalize.
     * @return A unique instance of the given locale, or {@code locale} if
     *         the given locale is not cached.
     */
    public static Locale unique(final Locale locale) {
        final Locale candidate = POOL.get(locale);
        return (candidate != null) ? candidate : locale;
    }
}
