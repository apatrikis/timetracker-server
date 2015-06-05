/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.timetracker.server.monitoring.BusinessServiceMXBean;
import com.prodyna.pac.timetracker.server.monitoring.StatusMonitor;
import java.util.Date;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * {@code REST Interface} for checking the availability of the server. The
 * {@code REST Interface} is available under {@link RESTConfig#STATUS_PATH}.
 *
 * @author apatrikis
 */
@Path(RESTConfig.STATUS_PATH)
@Stateless
public class StatusREST extends AbstractREST {

    @Inject
    private StatusMonitor jmxMonitor;

    /**
     * Test method to check if the server is available.
     *
     * @return A simple {@code HTML} formatted string.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAliveMessage() {
        return String.format("<b>alive</b>: %s<br/>see: <a href=\"%s\">WADL</a>",
                new Date().toString(),
                "application.wadl"); // https://wikis.oracle.com/display/Jersey/WADL
    }

    @Override
    public BusinessServiceMXBean getMonitorBean() {
        return jmxMonitor;
    }
}
