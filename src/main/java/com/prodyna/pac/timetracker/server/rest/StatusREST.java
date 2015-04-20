/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Employee2Role;
import com.prodyna.pac.timetracker.entity.EmployeeRole;
import com.prodyna.pac.timetracker.server.exception.PrimaryKeyException;
import com.prodyna.pac.timetracker.server.monitoring.BusinessServiceMXBean;
import com.prodyna.pac.timetracker.server.monitoring.StatusMonitor;
import com.prodyna.pac.timetracker.server.service.Employee2RoleServices;
import com.prodyna.pac.timetracker.server.service.EmployeeServices;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;

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
    private Logger log;

    @Inject
    private EmployeeServices employeeServices;

    @Inject
    private Employee2RoleServices employeeRoleServices;

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
        ensureDefaultAdmin();

        return String.format("<b>alive</b>: %s<br/>see: <a href=\"%s\">WADL</a>",
                new Date().toString(),
                "application.wadl"); // https://wikis.oracle.com/display/Jersey/WADL
    }

    /**
     * Ensure a default adminstration account exists.
     */
    synchronized private void ensureDefaultAdmin() {
        log.debug("check if a ADMIN user is available");
        List<Employee2Role> admins = employeeRoleServices.find(EmployeeRole.ADMIN);
        if (admins.isEmpty()) {
            log.info("create the default ADMIN user");
            try {
                Employee defaultAdmin = new Employee();
                defaultAdmin.setEmail("ad.min@tt.com");
                defaultAdmin.setFirstName("ad");
                defaultAdmin.setLastName("min");
                defaultAdmin.setPassword("secret");
                employeeServices.create(defaultAdmin);

                Employee2Role e2r = new Employee2Role();
                e2r.setEmployee(defaultAdmin);
                e2r.setRoleName(EmployeeRole.ROLE_ADMIN);
                employeeRoleServices.create(e2r);
            }
            catch (NoSuchAlgorithmException | PrimaryKeyException ex) {
                log.error("Exception while creating default admin", ex);
            }
        }
    }

    @Override
    public BusinessServiceMXBean getMonitorBean() {
        return jmxMonitor;
    }
}
