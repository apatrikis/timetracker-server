/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.timetracker.entity.EmployeeRole;
import com.prodyna.pac.timetracker.entity.Project;
import com.prodyna.pac.timetracker.server.exception.EntityDataException;
import com.prodyna.pac.timetracker.server.monitoring.BusinessServiceMXBean;
import com.prodyna.pac.timetracker.server.monitoring.ProjectsMonitor;
import com.prodyna.pac.timetracker.server.service.EmployeeServices;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * {@code REST Interface} for {@link Project} related actions. The
 * {@code REST Interface} is available under {@link RESTConfig#PROJECTS_PATH}.
 *
 * @author apatrikis
 */
@Path(RESTConfig.PROJECTS_PATH)
@Stateless
public class ProjectsREST extends AbstractREST {

    @Inject
    private ProjectServices projectService;

    @Inject
    private EmployeeServices employeeServices;

    @Inject
    private ProjectsMonitor jmxMonitor;

    /**
     * Store a {@link Project} entry.
     *
     * @param project The {@link Project} to store.
     * @return A {@link URI} to the newly created entry.
     * @throws EntityDataException in case the date ranges are invalid.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_ADMIN)
    public Response createOne(Project project) throws EntityDataException {
        project.checkDateValues(); // throws exception
        projectService.create(project);
        URI newObjectURI = URI.create(RESTConfig.PROJECTS_PATH + "/" + getURLEncodedString(project.getTitle()));
        return Response.created(newObjectURI).build();
    }

    /**
     * Retrieve a {@link Project} entry.
     *
     * @param projectID The {@code primary key}.
     * @return The {@link Project} or {@link Response.Status#NOT_FOUND}.
     */
    @GET
    @Path("{projectid}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response readOne(@PathParam("projectid") String projectID) {
        Project project = projectService.read(projectID);
        return (project != null)
                ? Response.ok(project).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Update a {@link Project} entry.
     *
     * @param project The {@link Project} to update.
     * @return The {@link Response.Status#OK}.
     * @throws EntityDataException in case the date ranges are invalid.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_MANAGER)
    public Response updateOne(Project project) throws EntityDataException {
        project.checkDateValues();
        projectService.update(project);
        return Response.ok().build();
    }

    /**
     * Delete a {@link Project} entry.
     *
     * @param projectID The {@code primary key}.
     * @return The {@link Response.Status#OK} or
     * {@link Response.Status#NOT_FOUND}.
     */
    @DELETE
    @Path("{projectid}")
    @RolesAllowed(EmployeeRole.ROLE_ADMIN)
    public Response deleteOne(@PathParam("projectid") String projectID) {
        Project project = projectService.delete(projectID);
        return (project != null)
                ? Response.ok().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Read all {@link Project}s.
     *
     * @return The {@link List} of {@link Project}s.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER})
    public Response readAll() {
        List<Project> projects = projectService.find(null);
        GenericEntity<List<Project>> responseEntity = new GenericEntity<List<Project>>(projects) {
        };
        return Response.ok(responseEntity).build();
    }

    /**
     * Read all {@link Project}s matching a search pattern.
     *
     * @param searchPattern The search pattern for the {@link Project}s to find.
     * The search is made for:
     * <ul>
     * <li>project ID</li>
     * <li>project title</li>
     * <li>project description</li>
     * </ul>
     * @return The {@link List} of matching {@link Project}s.
     */
    @GET
    @Path("find/{searchPattern}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER})
    public Response find(@PathParam("searchPattern") String searchPattern) {
        List<Project> projects = projectService.find(searchPattern);
        GenericEntity<List<Project>> responseEntity = new GenericEntity<List<Project>>(projects) {
        };
        return Response.ok(responseEntity).build();
    }

    @GET
    @Path(RESTConfig.EMPLOYEES_PATH + "/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER})
    public Response findByManager(@PathParam("email") String email) {
        List<Project> projects = projectService.findByManager(employeeServices.read(email));
        GenericEntity<List<Project>> responseEntity = new GenericEntity<List<Project>>(projects) {
        };
        return Response.ok(responseEntity).build();
    }

    @Override
    public BusinessServiceMXBean getMonitorBean() {
        return jmxMonitor;
    }
}
