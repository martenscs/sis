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
package org.apache.sis.console;

import java.util.Locale;
import org.apache.sis.util.resources.Errors;


/**
 * A command-line option.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3
 * @version 0.3
 * @module
 */
enum Option {
    /**
     * The output format. Examples: {@code "xml"}, {@code "text"}.
     */
    FORMAT(true),

    /**
     * The locale for the output produced by the command.
     */
    LOCALE(true),

    /**
     * The timezone for the dates to be formatted.
     */
    TIMEZONE(true),

    /**
     * The encoding for the output produced by the command.
     */
    ENCODING(true),

    /**
     * Whether colorized output shall be enabled.
     */
    COLORS(true),

    /**
     * Whether the output should contains only brief information.
     * This option expects no value.
     */
    BRIEF(false),

    /**
     * Whether the output should contains more detailed information.
     * This option expects no value.
     */
    VERBOSE(false),

    /**
     * Lists the options accepted by a command.
     */
    HELP(false);

    /**
     * The prefix to prepend to option names.
     */
    static final String PREFIX = "--";

    /**
     * Boolean values accepted on the command line. Values at even indices are {@code false}
     * and values at odd indices are {@code true}.
     *
     * @see #parseBoolean(String)
     */
    private static final String[] BOOLEAN_VALUES = {
        "false", "true",
        "off",   "on",
        "yes",   "no"
    };

    /**
     * Whether this option expects a value.
     */
    final boolean hasValue;

    /**
     * Creates a new option.
     *
     * @param Whether this option expects a value.
     */
    private Option(final boolean hasValue) {
        this.hasValue = hasValue;
    }

    /**
     * Parses the given value as a boolean.
     *
     * @param  value The value to parse.
     * @return The value as a boolean.
     * @throws InvalidOptionException If the given value is not recognized as a boolean.
     */
    boolean parseBoolean(final String value) throws InvalidOptionException {
        for (int i=0; i<BOOLEAN_VALUES.length; i++) {
            if (value.equalsIgnoreCase(BOOLEAN_VALUES[i])) {
                return (i & 1) != 0;
            }
        }
        final String name = name().toLowerCase(Locale.US);
        throw new InvalidOptionException(Errors.format(Errors.Keys.IllegalOptionValue_2, name, value), name);
    }
}
