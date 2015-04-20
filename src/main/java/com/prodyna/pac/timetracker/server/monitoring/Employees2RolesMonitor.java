/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.monitoring;

import com.prodyna.pac.timetracker.server.rest.Employees2RolesREST;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * The {@code MX Bean} for {@code JMX} monitoring of {@link Employees2RolesREST}
 * calls.
 *
 * @author apatrikis
 */
@Startup
@Singleton
public class Employees2RolesMonitor extends AbstractBusinessServiceMonitor {

    /**
     *
     */
    public Employees2RolesMonitor() {
        super(Employees2RolesREST.class.getSimpleName());
    }
}
