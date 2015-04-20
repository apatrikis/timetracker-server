/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.monitoring;

import java.util.List;

/**
 * {@code Interface} for a {@code MXBean} that can display the following
 * features:
 * <ul>
 * <li>successful calls with the execution time</li>
 * <li>failed calls</li>
 * <li>a list containing the fastest calls</li>
 * <li>a list containing the slowest calls</li>
 * <li>a list containing the latest issued calls</li>
 * <li>the posibility to resize the lists</li>
 * <li>the average time of all calls</li>
 * </ul>
 *
 * @author apatrikis
 */
public interface BusinessServiceMXBean {

    /**
     * Default {@link List} size of the fastest, solwest and latest calls.
     */
    public static final int DEFAULT_LIST_SIZE = 100;

    /**
     * Logs a successful call.
     *
     * @param serviceClass The name of the called class.
     * @param serviceMethod The service name of the call (e. g. a method name or
     * REST method name).
     * @param executionTime The elapsed execution time.
     */
    public void addSuccessfulCall(String serviceClass, String serviceMethod, long executionTime);

    /**
     * Logs a failed call.
     */
    public void addFailedCall();

    /**
     * Get the total number of calls (successful and failed calls).
     *
     * @return The total number of calls.
     */
    public int getTotalNumberOfCalls();

    /**
     * Get the number of failed calls.
     *
     * @return The number of failed calls.
     */
    public int getNumberOfFailedCalls();

    /**
     * Get the mimimum execution time, in milliseconds.
     *
     * @return The mimimum execution time.
     */
    public long getMinimumTime();

    /**
     * Get the maximum execution time, in milliseconds.
     *
     * @return The maximum execution time.
     */
    public long getMaximumTime();

    /**
     * Get the avefrage execution time, in milliseconds.
     *
     * @return The average execution time.
     */
    public double getAverageTime();

    /**
     * Reset all counters.
     */
    public void restetCounters();

    /**
     * Set the {@link List} size to use. Must be a positive number, {@code 0}
     * deactivates the usage. Be aware that bigger numbers consume more memory
     * and time (assigning into the various lists).
     *
     * @param listSize The {@link List} size.
     */
    public void setLogListSize(int listSize);

    /**
     * Get the log list size.
     *
     * @return The log list size.
     */
    public int getLogListSize();

    /**
     * Get a {@link List} of the fastest successful calls. The size is limited
     * to {@link #getLogListSize()}.
     *
     * @return The fastest logs.
     */
    public List<MonitorEntryValues> getFastestList();

    /**
     * Get a {@link List} of the slowest successful calls. The size is limited
     * to {@link #getLogListSize()}.
     *
     * @return The slowest logs.
     */
    public List<MonitorEntryValues> getSlowestList();

    /**
     * Get a {@link List} of the latest successful calls. The size is limited to
     * {@link #getLogListSize()}.
     *
     * @return The latest logs.
     */
    public List<MonitorEntryValues> getLatestList();
}
