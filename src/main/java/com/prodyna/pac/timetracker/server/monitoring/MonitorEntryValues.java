/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.monitoring;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * A {@code POJO} for storing the values to be displayed in a
 * {@code JMX Console}.
 *
 * @author apatrikis
 */
public class MonitorEntryValues {

    private final String uuid;
    private final String serviceClass;
    private final String serviceMethod;
    private final Date timestamp;
    private final long executionTime;

    /**
     * Constructor.
     *
     * @param serviceClass The name of the called class.
     * @param serviceMethod The service name of the call (e. g. a method name or
     * REST method name).
     * @param executionTime The elapsed execution time.
     */
    public MonitorEntryValues(String serviceClass, String serviceMethod, long executionTime) {
        this.uuid = MDC.get("UUID");
        this.serviceClass = serviceClass;
        this.serviceMethod = serviceMethod;
        this.timestamp = new Date();
        this.executionTime = executionTime;
    }

    /**
     * Get the {@link UUID} of the processed {@code Request}.
     *
     * @return The ID of the processed {@code Request}.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Get the name of the called class.
     *
     * @return The name of the called class.
     */
    public String getServiceClass() {
        return serviceClass;
    }

    /**
     * Get the service name of the call.
     *
     * @return The service name of the call.
     */
    public String getServiceMethod() {
        return serviceMethod;
    }

    /**
     * Get the timestamo this object was created.
     *
     * @return The timestamo this object was created.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Get the execution time of the {@code Request} processing.
     *
     * @return The execution time of the {@code Request} processing.
     */
    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.uuid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MonitorEntryValues other = (MonitorEntryValues) obj;
        return Objects.equals(this.uuid, other.uuid);
    }

    @Override
    public String toString() {
        return "MonitorEntryValues{" + "uuid=" + uuid + ", serviceClass=" + serviceClass + ", serviceMethod=" + serviceMethod + ", timestamp=" + timestamp + ", executionTime=" + executionTime + '}';
    }

    /**
     * A {@link Comparator} for {@link MonitorEntryValues}, which is needed for
     * sorting objects:
     * <ol>
     * <li>compare by {@link MonitorEntryValues#getTime()</li>
     * <li>compare by {@link MonitorEntryValues#getTimestamp()</li>
     * </ol>
     * In case of equal values, the newer entry is preferred.
     */
    static class MonitorEntryComparator implements Comparator<MonitorEntryValues> {

        private final boolean sortAscending;

        /**
         * Constructor.
         *
         * @param sortAscending {@code true} when the objects should be sorted
         * ascending, {@code fale} for descending order.
         */
        public MonitorEntryComparator(boolean sortAscending) {
            super();
            this.sortAscending = sortAscending;
        }

        @Override
        public int compare(MonitorEntryValues o1, MonitorEntryValues o2) {
            int comp;
            if (o1.getExecutionTime() < o2.getExecutionTime()) {
                comp = (sortAscending) ? -1 : 1;
            } else if (o1.getExecutionTime() > o2.getExecutionTime()) {
                comp = (sortAscending) ? 1 : -1;
            } else {
                int byDate = o1.getTimestamp().compareTo(o2.getTimestamp());
                comp = (sortAscending) ? byDate : (byDate * -1);
            }

            return comp;
        }
    }
}
