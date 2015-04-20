/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.monitoring;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.slf4j.Logger;

/**
 * Default implementation of the
 * {@link BusinessServiceMXBean} {@code Interface}. This class is abstract in
 * order to force providing a node and leaf name for the constructor.
 *
 * @see
 * <a href="http://docs.oracle.com/javase/7/docs/api/javax/management/MXBean.html">MX
 * Bean</a>
 * @see
 * <a href="http://www.adam-bien.com/roller/abien/entry/singleton_the_simplest_possible_jmx">Singelton</a>
 * @author apatrikis
 */
public abstract class AbstractBusinessServiceMonitor implements BusinessServiceMXBean {

    /**
     * The {@link Logger} to use.
     */
    @Inject
    protected Logger log;

    /**
     * A {@code Collection} of the fastest calls.
     */
    protected final TreeSet<MonitorEntryValues> fastestSet;

    /**
     * A {@code Collection} of the slowest calls.
     */
    protected final TreeSet<MonitorEntryValues> slowestSet;

    /**
     * A {@code Collection} of the latest calls.
     */
    protected final LinkedList<MonitorEntryValues> latestQueue;

    /**
     * The default {@code Collection} size to use.
     */
    protected int listSize = BusinessServiceMXBean.DEFAULT_LIST_SIZE;

    /**
     * A counter for the total number of calls.
     */
    protected int totalNumberOfCalls;

    /**
     * A counter for the number of failed calls.
     */
    protected int numberOfFailedCalls;

    /**
     * The average processing time of the stored calls.
     */
    protected double averageTime;

    /**
     * The used {@code MK Bean} domain name for creating a {@link ObjectName}.
     */
    protected final String domainName;

    /**
     * The used {@code MK Bean} leaf name for creating a {@link ObjectName}.
     */
    protected final String leafName;

    /**
     * Constructor. The {@code MK Bean} will be displayed under the domain
     * "timetracker-server" and the provided leaf name.
     *
     * @param leafName The leaf name to use for diplaying the {@code MK Bean}.
     */
    public AbstractBusinessServiceMonitor(final String leafName) {
        this("timetracker-server", leafName);
    }

    /**
     * Constructor. The {@code MK Bean} will be displayed under the provided
     * node and leaf name.
     *
     * @param domainName The domain name to use for diplaying the
     * {@code MK Bean}.
     * @param leafName The leaf name to use for diplaying the {@code MK Bean}.
     */
    public AbstractBusinessServiceMonitor(final String domainName, final String leafName) {
        fastestSet = new TreeSet<>(new MonitorEntryValues.MonitorEntryComparator(true));
        slowestSet = new TreeSet<>(new MonitorEntryValues.MonitorEntryComparator(false));
        latestQueue = new LinkedList<>();
        this.domainName = domainName;
        this.leafName = leafName;
    }

    @Override
    public void addSuccessfulCall(String serviceClass, String serviceMethod, long time) {
        synchronized (fastestSet) {
            fastestSet.add(new MonitorEntryValues(serviceClass, serviceMethod, time));
        }
        synchronized (slowestSet) {
            slowestSet.add(new MonitorEntryValues(serviceClass, serviceMethod, time));
        }
        synchronized (latestQueue) {
            latestQueue.addFirst(new MonitorEntryValues(serviceClass, serviceMethod, time));
        }
        // check if shrink is needed
        checkShrinkCollections();

        averageTime = (averageTime * totalNumberOfCalls + time) / (++totalNumberOfCalls);
    }

    @Override
    public void addFailedCall() {
        totalNumberOfCalls++;
        numberOfFailedCalls++;
    }

    @Override
    public int getTotalNumberOfCalls() {
        return totalNumberOfCalls;
    }

    @Override
    public int getNumberOfFailedCalls() {
        return numberOfFailedCalls;
    }

    @Override
    public long getMinimumTime() {
        return fastestSet.first().getExecutionTime();
    }

    @Override
    public long getMaximumTime() {
        return slowestSet.first().getExecutionTime();
    }

    @Override
    public double getAverageTime() {
        return averageTime;
    }

    @Override
    public void restetCounters() {
        synchronized (fastestSet) {
            fastestSet.clear();
        }
        synchronized (slowestSet) {
            slowestSet.clear();
        }
        synchronized (latestQueue) {
            latestQueue.clear();
        }
        totalNumberOfCalls = 0;
        numberOfFailedCalls = 0;
        averageTime = 0;
    }

    @Override
    public void setLogListSize(int listSize) {
        if (listSize < 0) {
            throw new IndexOutOfBoundsException("List size must be >= 0");
        }

        // check if shrink is needed
        checkShrinkCollections();
        this.listSize = listSize;
    }

    @Override
    public int getLogListSize() {
        return listSize;
    }

    @Override
    public List<MonitorEntryValues> getFastestList() {
        return Collections.synchronizedList(new ArrayList<>(fastestSet));
    }

    @Override
    public List<MonitorEntryValues> getSlowestList() {
        return Collections.synchronizedList(new ArrayList<>(slowestSet));
    }

    @Override
    public List<MonitorEntryValues> getLatestList() {
        return Collections.synchronizedList(new ArrayList<>(latestQueue));
    }

    /**
     * Register the {@code MX Bean} in the {@link MBeanServer}.
     */
    @PostConstruct
    public void registerInJMX() {
        try {
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            platformMBeanServer.registerMBean(this, getObjectName());
            log.info("Registered MBean: {}", getObjectName().toString());
        }
        catch (MalformedObjectNameException | MBeanRegistrationException | NotCompliantMBeanException e) {
            throw new IllegalStateException("Problem during registration of Monitoring into JMX:" + e);
        }
        catch (InstanceAlreadyExistsException iaee) {
            log.warn("Caught InstanceAlreadyExistsException: {}", iaee.getMessage());
        }
    }

    /**
     * Unregister the {@code MX Bean} from the {@link MBeanServer}.
     */
    @PreDestroy
    public void unregisterFromJMX() {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(getObjectName());
        }
        catch (InstanceNotFoundException | MalformedObjectNameException | MBeanRegistrationException e) {
            throw new IllegalStateException("Problem during unregistration of Monitoring into JMX:" + e);
        }
    }

    /**
     * Get the object name for lookup in the {@link MBeanServer}.
     *
     * @return The object name.
     * @throws MalformedObjectNameException When the node or leaf name provided
     * in the constructor contain invalid characters.
     */
    private ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName(domainName, "type", leafName);
    }

    /**
     * Check the collections if they are above the limit and resize them.
     */
    private void checkShrinkCollections() {
        checkShrink(fastestSet, listSize, true);
        checkShrink(slowestSet, listSize, true);
        checkShrink(latestQueue, listSize, true);
    }

    /**
     * Check and shrink a {@link NavigableSet}.
     *
     * @param set The {@link NavigableSet} to check.
     * @param maxElements The limit to check.
     * @param removeFromTail {@code true} if elemets above the limit should be
     * removed from the tail end, else {@code false} to removce from the head
     * end.
     */
    private void checkShrink(final NavigableSet<?> set, int maxElements, boolean removeFromTail) {
        synchronized (set) {
            int aboveThreshold = set.size() - maxElements;
            for (; aboveThreshold > 0; aboveThreshold--) {
                if (removeFromTail) {
                    set.pollLast();
                } else {
                    set.pollFirst();
                }
            }
        }
    }

    /**
     * Check and shrink a {@link Deque}.
     *
     * @param set The {@link Deque} to check.
     * @param maxElements The limit to check.
     * @param removeFromTail {@code true} if elemets above the limit should be
     * removed from the tail end, else {@code false} to removce from the head
     * end.
     */
    private void checkShrink(final Deque<?> queue, int maxElements, boolean removeFromTail) {
        synchronized (queue) {
            int aboveThreshold = queue.size() - maxElements;
            for (; aboveThreshold > 0; aboveThreshold--) {
                if (removeFromTail) {
                    queue.pollLast();
                } else {
                    queue.pollFirst();
                }
            }
        }
    }
}
