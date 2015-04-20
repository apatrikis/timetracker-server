/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.monitoring;

import com.prodyna.pac.timetracker.server.rest.StatusREST;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * The {@code MX Bean} for {@code JMX} monitoring of {@link StatusREST} calls.
 *
 * @author apatrikis
 */
@Startup
@Singleton
public class StatusMonitor extends AbstractBusinessServiceMonitor {

    /**
     *
     */
    public StatusMonitor() {
        super(StatusREST.class.getSimpleName());
    }
}
