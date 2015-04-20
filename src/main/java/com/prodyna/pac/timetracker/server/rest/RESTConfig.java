/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * The main server class which {@code extends} the {@link Application} class.
 * The main {@code REST Interface} is available under {@link #REST_PATH}. All
 * other {@code REST Interface} classes will have a path based on this one.
 *
 * @author apatrikis
 * @see <a href="http://www.restapitutorial/lessons/httpmethods.html">HTTP
 * Methods</a>
 */
@ApplicationPath(RESTConfig.REST_PATH)
public class RESTConfig extends Application {

    /**
     * Main {@code REST} path.
     */
    public static final String REST_PATH = "rest";

    /**
     * {@code REST} path for {@code status REST Interface}.
     */
    public static final String STATUS_PATH = "status";

    /**
     * {@code REST} path for {@code employees REST Interface}.
     */
    public static final String EMPLOYEES_PATH = "employees";

    /**
     * {@code REST} path for {@code e2r REST Interface}.
     */
    public static final String EMPLOYEES2ROLES_PATH = "e2r";

    /**
     * {@code REST} path for {@code projects REST Interface}.
     */
    public static final String PROJECTS_PATH = "projects";

    /**
     * {@code REST} path for {@code p2s REST Interface}.
     */
    public static final String PROJECTS2EMPLOYEES_PATH = "p2e";

    /**
     * {@code REST} path for {@code timerecords REST Interface}.
     */
    public static final String TIMERECORDS_PATH = "timerecords";

    /**
     * {@code REST} path for {@code security REST Interface}.
     */
    public static final String SECURITY_PATH = "security";
}
