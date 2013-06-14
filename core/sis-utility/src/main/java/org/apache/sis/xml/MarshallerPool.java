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
package org.apache.sis.xml;

import java.util.Map;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import net.jcip.annotations.ThreadSafe;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.internal.util.DelayedExecutor;
import org.apache.sis.internal.util.DelayedRunnable;
import org.apache.sis.internal.jaxb.AdapterReplacement;
import org.apache.sis.internal.jaxb.TypeRegistration;
import org.apache.sis.util.ArgumentChecks;


/**
 * Creates and configures {@link Marshaller} or {@link Unmarshaller} objects for use with SIS.
 * Users fetch (un)marshallers by calls to the {@link #acquireMarshaller()} or
 * {@link #acquireUnmarshaller()} methods, and can restitute the (un)marshaller to the pool
 * after usage like below:
 *
 * {@preformat java
 *     Marshaller marshaller = pool.acquireMarshaller();
 *     marshaller.marchall(...);
 *     pool.recycle(marshaller);
 * }
 *
 * {@section Configuring (un)marshallers}
 * The (un)marshallers created by this class can optionally by configured with the SIS-specific
 * properties defined in the {@link XML} class, in addition to JAXB standard properties.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.00)
 * @version 0.3
 * @module
 *
 * @see XML
 * @see <a href="http://jaxb.java.net/guide/Performance_and_thread_safety.html">JAXB Performance and thread-safety</a>
 */
@ThreadSafe
public class MarshallerPool {
    /**
     * Amount of nanoseconds to wait before to remove unused (un)marshallers.
     * This is a very approximative value: actual timeout will not be shorter,
     * but may be twice longer.
     */
    private static final long TIMEOUT = 15000000000L; // 15 seconds.

    /**
     * Kind of JAXB implementations.
     */
    private static final byte INTERNAL = 0, ENDORSED = 1, OTHER = 2;

    /**
     * The JAXB context to use for creating marshaller and unmarshaller.
     */
    private final JAXBContext context;

    /**
     * {@link #INTERNAL} if the JAXB implementation is the one bundled in the JDK,
     * {@link #ENDORSED} if the TAXB implementation is the endorsed JAXB (Glassfish), or
     * {@link #OTHER} if unknown.
     */
    private final byte implementation;

    /**
     * The mapper between namespaces and prefix.
     */
    private final Object mapper;

    /**
     * The pool of marshaller. This pool is initially empty and will be filled with elements as needed.
     * Marshallers (if any) shall be fetched using the {@link Deque#poll()} method and, after use,
     * given back to the pool using the {@link Deque#push(Object)} method.
     *
     * <p>This queue must be a thread-safe implementation, since it will not be invoked in
     * synchronized block.</p>
     *
     * @see #acquireMarshaller()
     * @see #recycle(Marshaller)
     */
    private final Deque<Marshaller> marshallers;

    /**
     * The pool of unmarshaller. This pool is initially empty and will be filled with elements as needed.
     * Unmarshallers (if any) shall be fetched using the {@link Deque#poll()} method and, after use,
     * given back to the pool using the {@link Deque#push(Object)} method.
     *
     * <p>This queue must be a thread-safe implementation, since it will not be invoked in
     * synchronized block.</p>
     *
     * @see #acquireUnmarshaller()
     * @see #recycle(Unmarshaller)
     */
    private final Deque<Unmarshaller> unmarshallers;

    /**
     * {@code true} if a task has been scheduled for removing expired (un)marshallers,
     * or {@code false} if no removal task is currently scheduled.
     *
     * @see #scheduleRemoval()
     */
    private final AtomicBoolean isRemovalScheduled;

    /**
     * Creates a new factory using the SIS default {@code JAXBContext} instance.
     * The keys in the {@code properties} map can be one or many of following constants:
     *
     * <ul>
     *   <li>{@link XML#DEFAULT_NAMESPACE} for specifying the default namespace of the XML document to write.</li>
     * </ul>
     *
     * @param  properties    The set of properties to be given to the pool, or {@code null} if none.
     * @throws JAXBException If the JAXB context can not be created.
     */
    public MarshallerPool(final Map<String,String> properties) throws JAXBException {
        this(TypeRegistration.getSharedContext(), properties);
    }

    /**
     * Creates a new factory using the given JAXB context.
     * The keys in the {@code properties} map can be one or many of following constants:
     *
     * <ul>
     *   <li>{@link XML#DEFAULT_NAMESPACE} for specifying the default namespace of the XML document to write.</li>
     * </ul>
     *
     * @param  context       The JAXB context.
     * @param  properties    The set of properties to be given to the pool, or {@code null} if none.
     * @throws JAXBException If the marshaller pool can not be created.
     */
    @SuppressWarnings({"unchecked", "rawtypes"}) // Generic array creation
    public MarshallerPool(final JAXBContext context, final Map<String,String> properties) throws JAXBException {
        ArgumentChecks.ensureNonNull("context", context);
        this.context = context;
        String rootNamespace = "";
        if (properties != null) {
            rootNamespace = properties.get(XML.DEFAULT_NAMESPACE);
            if (rootNamespace == null) {
                rootNamespace = "";
            }
        }
        /*
         * Detects if we are using the endorsed JAXB implementation (i.e. the one provided in
         * separated JAR files) or the one bundled in JDK 6. We use the JAXB context package
         * name as a criterion:
         *
         *   JAXB endorsed JAR uses    "com.sun.xml.bind"
         *   JAXB bundled in JDK uses  "com.sun.xml.internal.bind"
         */
        String classname = context.getClass().getName();
        if (classname.startsWith("com.sun.xml.internal.bind.")) {
            classname = "org.apache.sis.xml.OGCNamespacePrefixMapper";
            implementation = INTERNAL;
        } else if (classname.startsWith(Pooled.ENDORSED_PREFIX)) {
            classname = "org.apache.sis.xml.OGCNamespacePrefixMapper_Endorsed";
            implementation = ENDORSED;
        } else {
            classname = null;
            implementation = OTHER;
        }
        /*
         * Instantiates the OGCNamespacePrefixMapper appropriate for the implementation
         * we just detected. Note that we may get NoClassDefFoundError instead than the
         * usual ClassNotFoundException if the class was found but its parent class has
         * not been found.
         */
        if (classname == null) {
            mapper = null;
        } else try {
            mapper = Class.forName(classname).getConstructor(String.class).newInstance(rootNamespace);
        } catch (Throwable exception) { // (ReflectiveOperationException | NoClassDefFoundError) on JDK7 branch.
            throw new JAXBException(exception);
        }
        marshallers        = new LinkedBlockingDeque<Marshaller>();
        unmarshallers      = new LinkedBlockingDeque<Unmarshaller>();
        isRemovalScheduled = new AtomicBoolean();
    }

    /**
     * Marks the given marshaller or unmarshaller available for further reuse.
     * This method:
     *
     * <ul>
     *   <li>{@link Pooled#reset() Resets} the (un)marshaller to its initial state.</li>
     *   <li>{@linkplain Deque#push(Object) Pushes} the (un)marshaller in the given queue.</li>
     *   <li>Registers a delayed task for disposing expired (un)marshallers after the timeout.</li>
     * </ul>
     */
    private <T> void recycle(final Deque<T> queue, final T marshaller) {
        try {
            ((Pooled) marshaller).reset();
        } catch (JAXBException exception) {
            // Not expected to happen because we are supposed
            // to reset the properties to their initial values.
            Logging.unexpectedException(MarshallerPool.class, "recycle", exception);
            return;
        }
        queue.push(marshaller);
        scheduleRemoval();
    }

    /**
     * Schedule a new task for removing expired (un)marshallers if no such task is currently
     * registered. If a task is already registered, then this method does nothing. Note that
     * this task will actually wait for a longer time than the {@link #TIMEOUT} value before
     * to execute, in order to increase the chances to process many (un)marshallers at once.
     */
    private void scheduleRemoval() {
        if (isRemovalScheduled.compareAndSet(false, true)) {
            DelayedExecutor.schedule(new DelayedRunnable(System.nanoTime() + 2*TIMEOUT) {
                @Override public void run() {
                    removeExpired();
                }
            });
        }
    }

    /**
     * Invoked from the task scheduled by {@link #scheduleRemoval()} for removing expired
     * (un)marshallers. If some (un)marshallers remain after execution of this task, then
     * this method will reschedule a new task for checking again later.
     */
    final void removeExpired() {
        isRemovalScheduled.set(false);
        final long now = System.nanoTime();
        if (!removeExpired(marshallers, now) | // Really |, not ||
            !removeExpired(unmarshallers, now))
        {
            scheduleRemoval();
        }
    }

    /**
     * Removes expired (un)marshallers from the given queue.
     *
     * @param  <T>   Either {@code Marshaller} or {@code Unmarshaller} type.
     * @param  queue The queue from which to remove expired (un)marshallers.
     * @param  now   Current value of {@link System#nanoTime()}.
     * @return {@code true} if the queue became empty as a result of this method call.
     */
    private static <T> boolean removeExpired(final Deque<T> queue, final long now) {
        T next;
        while ((next = queue.peekLast()) != null) {
            /*
             * The above line fetched the oldest (un)marshaller without removing it.
             * If the timeout is not yet elapsed, do not remove that (un)marshaller.
             * Since marshallers are enqueued in chronological order, the next ones
             * should be yet more recent, so it is not worth to continue the search.
             */
            if (now - ((Pooled) next).resetTime < TIMEOUT) {
                return false;
            }
            /*
             * Now remove the (un)marshaller and check again the timeout. This second
             * check is because the oldest (un)marshaller may have changed concurrently
             * (depending on the queue implementation, which depends on the target JDK).
             * If such case, restore the (un)marshaller on the queue.
             */
            next = queue.pollLast();
            if (now - ((Pooled) next).resetTime < TIMEOUT) {
                queue.addLast(next);
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a JAXB marshaller from the pool. If there is no marshaller currently available
     * in the pool, then this method will {@linkplain #createMarshaller() create} a new one.
     *
     * <p>This method shall be used as below:</p>
     *
     * {@preformat java
     *     Marshaller marshaller = pool.acquireMarshaller();
     *     marshaller.marchall(...);
     *     pool.recycle(marshaller);
     * }
     *
     * Note that {@link #recycle(Marshaller)} shall not be invoked in case of exception,
     * since the marshaller may be in an invalid state.
     *
     * @return A marshaller configured for formatting OGC/ISO XML.
     * @throws JAXBException If an error occurred while creating and configuring a marshaller.
     */
    public Marshaller acquireMarshaller() throws JAXBException {
        Marshaller marshaller = marshallers.poll();
        if (marshaller == null) {
            marshaller = new PooledMarshaller(createMarshaller(), implementation == INTERNAL);
        }
        return marshaller;
    }

    /**
     * Returns a JAXB unmarshaller from the pool. If there is no unmarshaller currently available
     * in the pool, then this method will {@linkplain #createUnmarshaller() create} a new one.
     *
     * <p>This method shall be used as below:</p>
     *
     * {@preformat java
     *     Unmarshaller unmarshaller = pool.acquireUnmarshaller();
     *     Unmarshaller.unmarchall(...);
     *     pool.recycle(unmarshaller);
     * }
     *
     * Note that {@link #recycle(Unmarshaller)} shall not be invoked in case of exception,
     * since the unmarshaller may be in an invalid state.
     *
     * @return A unmarshaller configured for parsing OGC/ISO XML.
     * @throws JAXBException If an error occurred while creating and configuring the unmarshaller.
     */
    public Unmarshaller acquireUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = unmarshallers.poll();
        if (unmarshaller == null) {
            unmarshaller = new PooledUnmarshaller(createUnmarshaller(), implementation == INTERNAL);
        }
        return unmarshaller;
    }

    /**
     * Declares a marshaller as available for reuse.
     * The caller should not use anymore the given marshaller after this method call,
     * since the marshaller may be re-used by another thread at any time after recycle.
     *
     * {@section Cautions}
     * <ul>
     *   <li>Do not invoke this method if the marshaller threw an exception, since the
     *       marshaller may be in an invalid state. In particular, this method should not
     *       be invoked in a {@code finally} block.</li>
     *   <li>Do not invoke this method twice for the same marshaller, unless the marshaller
     *       has been obtained by a new call to {@link #acquireMarshaller()}.
     *       In case of doubt, it is better to not recycle the marshaller at all.</li>
     * </ul>
     *
     * Note that this method does not close any output stream.
     * Closing the marshaller stream is caller's or JAXB responsibility.
     *
     * @param marshaller The marshaller to return to the pool.
     */
    public void recycle(final Marshaller marshaller) {
        recycle(marshallers, marshaller);
    }

    /**
     * Declares a unmarshaller as available for reuse.
     * The caller should not use anymore the given unmarshaller after this method call,
     * since the unmarshaller may be re-used by another thread at any time after recycle.
     *
     * {@section Cautions}
     * <ul>
     *   <li>Do not invoke this method if the unmarshaller threw an exception, since the
     *       unmarshaller may be in an invalid state. In particular, this method should not
     *       be invoked in a {@code finally} block.</li>
     *   <li>Do not invoke this method twice for the same unmarshaller, unless the unmarshaller
     *       has been obtained by a new call to {@link #acquireUnmarshaller()}.
     *       In case of doubt, it is better to not recycle the unmarshaller at all.</li>
     * </ul>
     *
     * Note that this method does not close any input stream.
     * Closing the unmarshaller stream is caller's or JAXB responsibility.
     *
     * @param unmarshaller The unmarshaller to return to the pool.
     */
    public void recycle(final Unmarshaller unmarshaller) {
        recycle(unmarshallers, unmarshaller);
    }

    /**
     * Creates an configure a new JAXB marshaller.
     * This method is invoked only when no existing marshaller is available in the pool.
     * Subclasses can override this method if they need to change the marshaller configuration.
     *
     * @return A new marshaller configured for formatting OGC/ISO XML.
     * @throws JAXBException If an error occurred while creating and configuring the marshaller.
     */
    protected Marshaller createMarshaller() throws JAXBException {
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        switch (implementation) {
            case INTERNAL: {
                marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", mapper);
                break;
            }
            case ENDORSED: {
                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", mapper);
                break;
            }
            // Do nothing for the OTHER case.
        }
        synchronized (AdapterReplacement.PROVIDER) {
            for (final AdapterReplacement adapter : AdapterReplacement.PROVIDER) {
                adapter.register(marshaller);
            }
        }
        return marshaller;
    }

    /**
     * Creates an configure a new JAXB unmarshaller.
     * This method is invoked only when no existing unmarshaller is available in the pool.
     * Subclasses can override this method if they need to change the unmarshaller configuration.
     *
     * @return A new unmarshaller configured for parsing OGC/ISO XML.
     * @throws JAXBException If an error occurred while creating and configuring the unmarshaller.
     */
    protected Unmarshaller createUnmarshaller() throws JAXBException {
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        synchronized (AdapterReplacement.PROVIDER) {
            for (final AdapterReplacement adapter : AdapterReplacement.PROVIDER) {
                adapter.register(unmarshaller);
            }
        }
        return unmarshaller;
    }
}
