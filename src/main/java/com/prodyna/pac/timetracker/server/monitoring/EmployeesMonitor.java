/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.monitoring;

import com.prodyna.pac.timetracker.server.rest.EmployeesREST;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * The {@code MX Bean} for {@code JMX} monitoring of {@link EmployeesREST}
 * calls.
 *
 * @author apatrikis
 */
@Startup
@Singleton
public class EmployeesMonitor extends AbstractBusinessServiceMonitor {

    /**
     *
     */
    public EmployeesMonitor() {
        super(EmployeesREST.class.getSimpleName());
    }
}
