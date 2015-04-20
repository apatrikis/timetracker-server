/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.monitoring;

import com.prodyna.pac.timetracker.server.rest.TimeRecordsREST;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * The {@code MX Bean} for {@code JMX} monitoring of {@link TimeRecordsREST}
 * calls.
 *
 * @author apatrikis
 */
@Startup
@Singleton
public class TimeRecordsMonitor extends AbstractBusinessServiceMonitor {

    /**
     *
     */
    public TimeRecordsMonitor() {
        super(TimeRecordsREST.class.getSimpleName());
    }
}
