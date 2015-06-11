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
import com.prodyna.pac.timetracker.server.monitoring.Employees2RolesMonitor;
import com.prodyna.pac.timetracker.server.service.Employee2RoleServices;
import com.prodyna.pac.timetracker.server.service.EmployeeServices;
import java.net.URI;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * {@code REST Interface} for {@link Employee2Role} related actions. The
 * {@code REST Interface} is available under
 * {@link RESTConfig#EMPLOYEES2ROLES_PATH}.
 *
 * @author apatrikis
 */
@Path(RESTConfig.EMPLOYEES2ROLES_PATH)
@Stateless
public class Employees2RolesREST extends AbstractREST {

    @Inject
    private Employee2RoleServices employeeRoleServices;

    @Inject
    private EmployeeServices employeeServices;

    @Inject
    private Employees2RolesMonitor jmxMonitor;

    /**
     * Store a {@link Employee2Role} entry.
     *
     * @param employeeRole The {@link Employee2Role} to store.
     * @return A {@link URI} to the newly created entry.
     * @throws PrimaryKeyException in case the assingment of a
     * {@link EmployeeRole} to a {@link Employee} already exists.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_ADMIN)
    public Response createOne(Employee2Role employeeRole) throws PrimaryKeyException {
        employeeRoleServices.create(employeeRole);
        URI newObjectURI = URI.create(RESTConfig.EMPLOYEES2ROLES_PATH + "/" + getURLEncodedString(employeeRole.getId()));
        return Response.created(newObjectURI).build();
    }

    /**
     * Retrieve a {@link Employee2Role} entry.
     *
     * @param id The {@code primary key}.
     * @return The {@link Employee2Role} or {@link Response.Status#NOT_FOUND}.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response readOne(@PathParam("id") String id) {
        Employee2Role employeeRole = employeeRoleServices.read(id);
        return (employeeRole != null)
                ? Response.ok(employeeRole).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Delete a {@link Employee2Role} entry.
     *
     * @param id The {@code primary key}.
     * @return The {@link Response.Status#OK} or
     * {@link Response.Status#NOT_FOUND}.
     */
    @DELETE
    @Path("{id}")
    @RolesAllowed(EmployeeRole.ROLE_ADMIN)
    public Response deleteOne(@PathParam("id") String id) {
        Employee2Role employeeRole = employeeRoleServices.delete(id);
        return (employeeRole != null)
                ? Response.ok().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Read all {@link Employee2Role}s for the specified role name.
     *
     * @param roleName The role to lookup.
     * @return The {@link List} of {@link Employee2Role}s of the provided role.
     */
    @GET
    @Path("roles/{roleName}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER})
    public Response findByRole(@PathParam("roleName") String roleName) {
        List<Employee2Role> employees = employeeRoleServices.find(EmployeeRole.valueOf(roleName));

        GenericEntity<List<Employee2Role>> responseEntity = new GenericEntity<List<Employee2Role>>(employees) {
        };
        return Response.ok(responseEntity).build();
    }

    /**
     * Read all {@link Employee2Role}s for the specified employee.
     *
     * @param email The employees email to lookup.
     * @return The {@link List} of {@link Employee2Role}s of the provided
     * employee.
     */
    @GET
    @Path("employees/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response findByEmployee(@PathParam("email") String email) {
        List<Employee2Role> employees = employeeRoleServices.find(employeeServices.read(email));

        GenericEntity<List<Employee2Role>> responseEntity = new GenericEntity<List<Employee2Role>>(employees) {
        };
        return Response.ok(responseEntity).build();
    }

    /**
     * Get all available {@link EmployeeRole}s.
     *
     * @return The {@link List} of all {@link EmployeeRole}s.
     */
    @GET
    @Path("roles")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_ADMIN)
    public Response getAllRoles() {
        List<EmployeeRole> allRoles = employeeRoleServices.getAllRoles();

        GenericEntity<List<EmployeeRole>> responseEntity = new GenericEntity<List<EmployeeRole>>(allRoles) {
        };
        return Response.ok(responseEntity).build();
    }

    @Override
    public BusinessServiceMXBean getMonitorBean() {
        return jmxMonitor;
    }
}
