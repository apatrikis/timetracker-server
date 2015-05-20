/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.EmployeeRole;
import com.prodyna.pac.timetracker.entity.Project;
import com.prodyna.pac.timetracker.entity.Project2Employee;
import com.prodyna.pac.timetracker.server.exception.PrimaryKeyException;
import com.prodyna.pac.timetracker.server.monitoring.BusinessServiceMXBean;
import com.prodyna.pac.timetracker.server.monitoring.Projects2EmployeesMonitor;
import com.prodyna.pac.timetracker.server.service.EmployeeServices;
import com.prodyna.pac.timetracker.server.service.Project2EmployeeServices;
import com.prodyna.pac.timetracker.server.service.ProjectServices;
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
import org.slf4j.Logger;

/**
 * {@code REST Interface} for {@link Project2Employee} related actions. The
 * {@code REST Interface} is available under
 * {@link RESTConfig#PROJECTS2EMPLOYEES_PATH}.
 *
 * @author apatrikis
 */
@Path(RESTConfig.PROJECTS2EMPLOYEES_PATH)
@Stateless
public class Projects2EmployeesREST extends AbstractREST {

    @Inject
    private Logger log;

    @Inject
    private ProjectServices projectServices;

    @Inject
    private EmployeeServices employeeServices;

    @Inject
    private Project2EmployeeServices projectEmployeeServices;

    @Inject
    private Projects2EmployeesMonitor jmxMonitor;

    /**
     * Assign a {@link Project} to a {@link Employee}, which is identified by
     * it's {@code email}.
     *
     * @param email The {@link Employee}s {@code email}.
     * @param project The {@link Project} to assign.
     * @return A {@link URI} to the newly created relation.
     * @throws PrimaryKeyException if the {@link Employee} is already assigned
     * to the {@link Project}.
     */
    @POST
    @Path(RESTConfig.EMPLOYEES_PATH + "/{email}/" + RESTConfig.PROJECTS_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_MANAGER)
    public Response assignProject(@PathParam("email") String email, Project project) throws PrimaryKeyException {
        Project2Employee psm = new Project2Employee();
        psm.setProject(project);
        psm.setEmployee(employeeServices.read(email));

        projectEmployeeServices.create(psm);
        URI newObjectURI = URI.create(String.format("%s/%s/%s/%s",
                RESTConfig.EMPLOYEES_PATH,
                getURLEncodedString(email),
                RESTConfig.PROJECTS_PATH,
                getURLEncodedString(psm.getId())
        ));
        return Response.created(newObjectURI).build();
    }

    /**
     * Read all {@link Project}s assigned to a {@link Employee}, which is
     * identified by it's {@code email}.
     *
     * @param email The {@link Employee}s {@code email}.
     * @return The {@link List} of assigned {@link Project}s.
     */
    @GET
    @Path(RESTConfig.EMPLOYEES_PATH + "/{email}/" + RESTConfig.PROJECTS_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER})
    public Response readAllProjetcs(@PathParam("email") String email) {
        List<Project> assignments = projectEmployeeServices.findProjects(employeeServices.read(email));
        GenericEntity<List<Project>> responseEntity = new GenericEntity<List<Project>>(assignments) {
        };
        return Response.ok(responseEntity).build();
    }

    /**
     * Unassign a {@link Project}, which is indentified by it's
     * {@code project ID}, from a {@link Employee}, which is identified by it's
     * {@code email}.
     *
     * @param email The {@link Employee}s {@code email}.
     * @param projectID The {@link Project}s {@code project ID}.
     * @return The {@link Response.Status#OK} or
     * {@link Response.Status#NOT_FOUND}.
     */
    @DELETE
    @Path(RESTConfig.EMPLOYEES_PATH + "/{email}/" + RESTConfig.PROJECTS_PATH + "/{projectid}")
    @RolesAllowed(EmployeeRole.ROLE_MANAGER)
    public Response unassignProject(@PathParam("email") String email, @PathParam("projectid") String projectID) {
        List<Project2Employee> assignment = projectEmployeeServices.find(projectServices.read(projectID), employeeServices.read(email));

        if ((assignment == null) || (assignment.size() != 1)) {
            log.info("Assignment for Employee [{}] and Project [{}] not found", email, projectID);
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            Project2Employee deleted = projectEmployeeServices.delete(assignment.get(0).getId());
            return (deleted != null)
                    ? Response.ok().build()
                    : Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Assign a {@link Employee} to a {@link Project}, which is identified by
     * it's {@code project ID}.
     *
     * @param projectID The {@link Project}s {@code project ID}.
     * @param employee The {@link Employee} to assign.
     * @return A {@link URI} to the newly created relation.
     * @throws PrimaryKeyException if the Employee is already assigned to the
     * Project.
     */
    @POST
    @Path(RESTConfig.PROJECTS_PATH + "/{projectid}/" + RESTConfig.EMPLOYEES_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_MANAGER)
    public Response assignEmployee(@PathParam("projectid") String projectID, Employee employee) throws PrimaryKeyException {
        Project2Employee psm = new Project2Employee();
        psm.setProject(projectServices.read(projectID));
        psm.setEmployee(employee);

        projectEmployeeServices.create(psm);
        URI newObjectURI = URI.create(String.format("%s/%s/%S/%s",
                RESTConfig.PROJECTS_PATH,
                getURLEncodedString(projectID),
                RESTConfig.EMPLOYEES_PATH,
                getURLEncodedString(psm.getId())
        ));
        return Response.created(newObjectURI).build();
    }

    /**
     * Read all {@link Employee}s assigned to a {@link Project}, which is
     * identified by it's {@code project ID}.
     *
     * @param projectID The {@link Project}s {@code project ID}.
     * @return The {@link List} of assigned {@link Employee}s.
     */
    @GET
    @Path(RESTConfig.PROJECTS_PATH + "/{projectid}/" + RESTConfig.EMPLOYEES_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER})
    public Response readAlEmployees(@PathParam("projectid") String projectID) {
        List<Employee> assignments = projectEmployeeServices.findEmployees(projectServices.read(projectID));
        GenericEntity<List<Employee>> responseEntity = new GenericEntity<List<Employee>>(assignments) {
        };
        return Response.ok(responseEntity).build();
    }

    /**
     * Unassign a {@link Employee}, which is indentified by it's {@code email},
     * from a {@link Project}, which is identified by it's {@code project ID}.
     *
     * @param projectID The {@link Project}s {@code project ID}.
     * @param email The {@link Employee}s {@code email}.
     * @return The {@link Response.Status#OK} or
     * {@link Response.Status#NOT_FOUND}.
     */
    @DELETE
    @Path(RESTConfig.PROJECTS_PATH + "/{projectid}/" + RESTConfig.EMPLOYEES_PATH + "/{email}")
    @RolesAllowed(EmployeeRole.ROLE_MANAGER)
    public Response unassignEmployee(@PathParam("projectid") String projectID, @PathParam("email") String email) {
        return unassignProject(email, projectID);
    }

    @Override
    public BusinessServiceMXBean getMonitorBean() {
        return jmxMonitor;
    }
}
