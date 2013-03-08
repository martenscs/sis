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
package org.apache.sis.metadata;

import java.util.Map;
import java.util.Set;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;


/**
 * The base class of {@link Map} views.
 *
 * @param <V> The type of values in the map.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.04)
 * @version 0.3
 * @module
 */
abstract class MetadataMap<V> extends AbstractMap<String,V> {
    /**
     * The accessor to use for the metadata.
     */
    final PropertyAccessor accessor;

    /**
     * Determines the string representation of keys in the map.
     */
    final KeyNamePolicy keyPolicy;

    /**
     * A view of the mappings contained in this map.
     */
    transient Set<Map.Entry<String,V>> entrySet;

    /**
     * Creates a new map backed by the given accessor.
     */
    MetadataMap(final PropertyAccessor accessor, final KeyNamePolicy keyPolicy) {
        this.accessor  = accessor;
        this.keyPolicy = keyPolicy;
    }

    /**
     * Returns the number of elements in this map.
     */
    @Override
    public abstract int size();

    /**
     * Returns a view of the mappings contained in this map. Subclasses shall override this method
     * if they define a different entries set class than the default {@link Entries} inner class.
     */
    @Override
    public synchronized Set<Map.Entry<String,V>> entrySet() {
        if (entrySet == null) {
            entrySet = new Entries();
        }
        return entrySet;
    }

    /**
     * Returns an iterator over the entries in this map.
     */
    abstract Iterator<Map.Entry<String,V>> iterator();




    /**
     * The iterator over the elements contained in a {@link Entries} set.
     *
     * @author  Martin Desruisseaux (Geomatys)
     * @since   0.3 (derived from geotk-3.04)
     * @version 0.3
     * @module
     */
    abstract class Iter implements Iterator<Map.Entry<String,V>> {
        /**
         * Creates a new iterator.
         */
        Iter() {
        }

        /**
         * Assumes that the underlying map is unmodifiable.
         * Only {@link PropertyMap} supports this method.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }




    /**
     * Base class of views of the entries contained in the map.
     *
     * @author  Martin Desruisseaux (Geomatys)
     * @since   0.3 (derived from geotk-3.04)
     * @version 0.3
     * @module
     */
    class Entries extends AbstractSet<Map.Entry<String,V>> {
        /**
         * Creates a new entries set.
         */
        Entries() {
        }

        /**
         * Returns true if this collection contains no elements.
         */
        @Override
        public final boolean isEmpty() {
            return MetadataMap.this.isEmpty();
        }

        /**
         * Returns the number of elements in this collection.
         */
        @Override
        public final int size() {
            return MetadataMap.this.size();
        }

        /**
         * Returns an iterator over the elements contained in this collection.
         */
        @Override
        public final Iterator<Map.Entry<String,V>> iterator() {
            return MetadataMap.this.iterator();
        }
    }
}