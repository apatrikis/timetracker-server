/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.EmployeeRole;
import com.prodyna.pac.timetracker.entity.Project;
import com.prodyna.pac.timetracker.server.exception.ServerRESTException;
import com.prodyna.pac.timetracker.server.monitoring.BusinessServiceMXBean;
import com.prodyna.pac.timetracker.server.monitoring.EmployeesMonitor;
import com.prodyna.pac.timetracker.server.service.EmployeeServices;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * {@code REST Interface} for {@link Employee} related actions. The
 * {@code REST Interface} is available under {@link RESTConfig#EMPLOYEES_PATH}.
 *
 * @author apatrikis
 */
@Path(RESTConfig.EMPLOYEES_PATH)
@Stateless
public class EmployeesREST extends AbstractREST {

    @Inject
    private EmployeeServices employeeServices;

    @Inject
    private EmployeesMonitor jmxMonitor;

    /**
     * Store a {@link Employee} entry.
     *
     * @param employee The {@link Employee} to store.
     * @return A {@link URI} to the newly created entry.
     * @throws java.security.NoSuchAlgorithmException if the provided password
     * cannot be hashed.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_ADMIN)
    public Response createOne(Employee employee) throws NoSuchAlgorithmException {
        employeeServices.create(employee);
        URI newObjectURI = URI.create(RESTConfig.EMPLOYEES_PATH + "/" + getURLEncodedString(employee.getEmail()));
        return Response.created(newObjectURI).build();
    }

    /**
     * Retrieve a {@link Employee} entry.
     *
     * @param email The {@code primary key}.
     * @return The {@link Employee} or {@link Response.Status#NOT_FOUND}.
     */
    @GET
    @Path("{email}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response readOne(@PathParam("email") String email) {
        Employee employee = employeeServices.read(email);
        return (employee != null)
                ? Response.ok(employee).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Update a {@link Employee} entry.
     *
     * @param employee The {@link Employee} to update.
     * @return The {@link Response.Status#OK}.
     * @throws ServerRESTException <b>ALWAYS</b> throws this exception.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_USER)
    public Response updateOne(Employee employee) throws ServerRESTException {
        throw new ServerRESTException("UPDATE not supported for Employee. Password changes via Security REST.");
    }

    /**
     * Delete a {@link Employee} entry.
     *
     * @param email The {@code primary key}.
     * @return The {@link Response.Status#OK} or
     * {@link Response.Status#NOT_FOUND}.
     */
    @DELETE
    @Path("{email}")
    @RolesAllowed(EmployeeRole.ROLE_ADMIN)
    public Response deleteOne(@PathParam("email") String email) {
        Employee employee = employeeServices.delete(email);
        return (employee != null)
                ? Response.ok().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Read all {@link Employee}s.
     *
     * @return The {@link List} of {@link Employee}s.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER})
    public Response readAll() {
        List<Employee> employees = employeeServices.find(null);
        GenericEntity<List<Employee>> responseEntity = new GenericEntity<List<Employee>>(employees) {
        };
        return Response.ok(responseEntity).build();
    }

    /**
     * Read all {@link Project}s matching a search pattern.
     *
     * @param searchPattern The search pattern for the {@link Employee}s to
     * find. The search is made for:
     * <ul>
     * <li>first name</li>
     * <li>last name</li>
     * <li>email</li>
     * </ul>
     * @return The {@link List} of matching {@link Employee}s.
     */
    @GET
    @Path("find/{searchPattern}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER})
    public Response find(@PathParam("searchPattern") String searchPattern) {
        List<Employee> employees = employeeServices.find(searchPattern);
        GenericEntity<List<Employee>> responseEntity = new GenericEntity<List<Employee>>(employees) {
        };
        return Response.ok(responseEntity).build();
    }

    @Override
    public BusinessServiceMXBean getMonitorBean() {
        return jmxMonitor;
    }
}
