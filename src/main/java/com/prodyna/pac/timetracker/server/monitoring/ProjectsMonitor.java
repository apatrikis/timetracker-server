/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.monitoring;

import com.prodyna.pac.timetracker.server.rest.ProjectsREST;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * The {@code MX Bean} for {@code JMX} monitoring of {@link ProjectsREST} calls.
 *
 * @author apatrikis
 */
@Startup
@Singleton
public class ProjectsMonitor extends AbstractBusinessServiceMonitor {

    /**
     *
     */
    public ProjectsMonitor() {
        super(ProjectsREST.class.getSimpleName());
    }
}
