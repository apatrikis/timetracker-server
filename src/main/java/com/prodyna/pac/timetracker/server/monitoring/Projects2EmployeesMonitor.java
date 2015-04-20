/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.monitoring;

import com.prodyna.pac.timetracker.server.rest.Projects2EmployeesREST;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * The {@code MX Bean} for {@code JMX} monitoring of
 * {@link Projects2EmployeesREST} calls.
 *
 * @author apatrikis
 */
@Startup
@Singleton
public class Projects2EmployeesMonitor extends AbstractBusinessServiceMonitor {

    /**
     *
     */
    public Projects2EmployeesMonitor() {
        super(Projects2EmployeesREST.class.getSimpleName());
    }
}
