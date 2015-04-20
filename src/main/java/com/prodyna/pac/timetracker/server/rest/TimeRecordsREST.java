/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.EmployeeRole;
import com.prodyna.pac.timetracker.entity.Project;
import com.prodyna.pac.timetracker.entity.Project2Employee;
import com.prodyna.pac.timetracker.entity.TimeRecord;
import com.prodyna.pac.timetracker.pojo.TimeRecordSearch;
import com.prodyna.pac.timetracker.server.exception.EntityDataException;
import com.prodyna.pac.timetracker.server.exception.SearchParametersException;
import com.prodyna.pac.timetracker.server.monitoring.BusinessServiceMXBean;
import com.prodyna.pac.timetracker.server.monitoring.TimeRecordsMonitor;
import com.prodyna.pac.timetracker.server.service.EmployeeServices;
import com.prodyna.pac.timetracker.server.service.Project2EmployeeServices;
import com.prodyna.pac.timetracker.server.service.ProjectServices;
import com.prodyna.pac.timetracker.server.service.TimeRecordServices;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
 * {@code REST Interface} for {@link TimeRecord} related actions. The
 * {@code REST Interface} is available under
 * {@link RESTConfig#TIMERECORDS_PATH}.
 *
 * @author apatrikis
 */
@Path(RESTConfig.TIMERECORDS_PATH)
@Stateless
public class TimeRecordsREST extends AbstractREST {

    @Inject
    private TimeRecordServices timeRecordServices;

    @Inject
    private EmployeeServices employeeServices;

    @Inject
    private ProjectServices projectServices;

    @Inject
    private Project2EmployeeServices projectEmployeeServices;

    @Inject
    private TimeRecordsMonitor jmxMonitor;

    /**
     * Store a {@link TimeRecord} entry.
     *
     * @param timeRecord The {@link TimeRecord} to store.
     * @return A {@link URI} to the newly created entry.
     * @throws EntityDataException if the provided data is invalid, e. g. the
     * provided timestamps are invalid.
     * @throws SearchParametersException e. g. if the {@link Project} or
     * {@link Employee} is not specified.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_USER)
    public Response createOne(TimeRecord timeRecord) throws EntityDataException, SearchParametersException {
        checkProjectEmployeeAssignment(timeRecord.getProject(), timeRecord.getOwner());
        timeRecordServices.create(timeRecord);
        URI newObjectURI = URI.create(RESTConfig.PROJECTS_PATH + "/" + getURLEncodedString(timeRecord.getId()));
        return Response.created(newObjectURI).build();
    }

    /**
     * Retrieve a {@link TimeRecord} entry.
     *
     * @param id The {@code primary key}.
     * @return The {@link TimeRecord} or {@link Response.Status#NOT_FOUND}.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response readOne(@PathParam("id") String id) {
        TimeRecord timeRecord = timeRecordServices.read(id);
        return (timeRecord != null)
                ? Response.ok(timeRecord).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Update a {@link TimeRecord} entry.
     *
     * @param timeRecord The {@link TimeRecord} to update.
     * @return The {@link Response.Status#OK}.
     * @throws EntityDataException if the provided data is invalid, e. g. the
     * provided timestamps are invalid or too long.
     * @throws SearchParametersException e. g. if the {@link Project} or
     * {@link Employee} is not specified.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response updateOne(TimeRecord timeRecord) throws EntityDataException, SearchParametersException {
        checkProjectEmployeeAssignment(timeRecord.getProject(), timeRecord.getOwner());
        timeRecordServices.update(timeRecord);
        return Response.ok().build();
    }

    /**
     * Delete a {@link TimeRecord} entry.
     *
     * @param id The {@code primary key}.
     * @return The {@link Response.Status#OK} or
     * {@link Response.Status#NOT_FOUND}.
     * @throws EntityDataException if the data cannot be deleted, e. g. the
     * status does not allow deleting.
     */
    @DELETE
    @Path("{id}")
    @RolesAllowed(EmployeeRole.ROLE_USER)
    public Response deleteOne(@PathParam("id") String id) throws EntityDataException {
        TimeRecord timeRecord = timeRecordServices.delete(id);
        return (timeRecord != null)
                ? Response.ok().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Read all {@link TimeRecord}s.
     *
     * @return The {@link List} of {@link TimeRecord}s.
     * @throws SearchParametersException e. g. if the {@link Project} or
     * {@link Employee} is not specified.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_ADMIN)
    public Response readAll() throws SearchParametersException {
        List<TimeRecord> timeRecords = timeRecordServices.find(null);
        GenericEntity<List<TimeRecord>> responseEntity = new GenericEntity<List<TimeRecord>>(timeRecords) {
        };
        return Response.ok(responseEntity).build();
    }

    /**
     * Read all {@link TimeRecord}s for the specified owner.
     *
     * @param ownerEMail The {@code primary key}.
     * @return The {@link List} of {@link TimeRecord}s that belong to the owner.
     * @throws SearchParametersException <i>this will never be thrown for this
     * method</i>
     */
    @GET
    @Path("find/owner/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response findByOwner(@PathParam("email") String ownerEMail) throws SearchParametersException {
        return find(ownerEMail, null, null, null);
    }

    /**
     * Read all {@link TimeRecord}s for the specified project.
     *
     * @param projectID The {@code primary key}.
     * @return The {@link List} of {@link TimeRecord}s that belong to the
     * project.
     * @throws SearchParametersException <i>this will never be thrown for this
     * method</i>
     */
    @GET
    @Path("find/project/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response findByProject(@PathParam("id") String projectID) throws SearchParametersException {
        return find(null, projectID, null, null);
    }

    /**
     * Read all {@link TimeRecord}s for the specified owner and project.
     *
     * @param ownerEMail The {@code primary key}.
     * @param projectID The {@code primary key}.
     * @return The {@link List} of {@link TimeRecord}s that belong to the owner
     * and project.
     * @throws SearchParametersException <i>this will never be thrown for this
     * method</i>
     */
    @GET
    @Path("find/owner/{email}/project/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response find(@PathParam("email") String ownerEMail, @PathParam("id") String projectID) throws SearchParametersException {
        return find(ownerEMail, projectID, null, null);
    }

    /**
     * Read all {@link TimeRecord}s for the specified owner and project in the
     * given time frame.
     *
     * @param ownerEMail The {@code primary key}.
     * @param projectID The {@code primary key}.
     * @param fromISODateTime The begin of the time frame.
     * @param throughISODateTime The end of the time frame.
     * @return The {@link List} of {@link TimeRecord}s that belong to the owner
     * and project in the given time frame.
     * @throws SearchParametersException e. g. is the provided timestamps are
     * invalid.
     */
    @GET
    @Path("find/owner/{email}/project/{id}/from/{from}/through/{through}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response find(@PathParam("email") String ownerEMail, @PathParam("id") String projectID,
            @PathParam("from") String fromISODateTime, @PathParam("through") String throughISODateTime) throws SearchParametersException {
        TimeRecordSearch searchPattern = new TimeRecordSearch();

        if (ownerEMail != null) {
            searchPattern.setEmployee(employeeServices.read(ownerEMail));
        }
        if (projectID != null) {
            searchPattern.setProject(projectServices.read(projectID));
        }
        if (fromISODateTime != null) {
            ZonedDateTime zdt = ZonedDateTime.parse(fromISODateTime, DateTimeFormatter.ISO_DATE_TIME);
            searchPattern.setFrom(Date.from(zdt.toInstant()));
        }
        if (throughISODateTime != null) {
            ZonedDateTime zdt = ZonedDateTime.parse(throughISODateTime, DateTimeFormatter.ISO_DATE_TIME);
            searchPattern.setThrough(Date.from(zdt.toInstant()));
        }

        List<TimeRecord> timeRecords = timeRecordServices.find(searchPattern);
        GenericEntity<List<TimeRecord>> responseEntity = new GenericEntity<List<TimeRecord>>(timeRecords) {
        };
        return Response.ok(responseEntity).build();
    }

    @Override
    public BusinessServiceMXBean getMonitorBean() {
        return jmxMonitor;
    }

    /**
     * Check if the desired assignment of employee to project exists.
     *
     * @param project The {@link Project} to book.
     * @param employee The {@link Employee} to book.
     * @throws EntityDataException in case the project or user in invalid, or
     * the assignment does not exist.
     */
    private void checkProjectEmployeeAssignment(Project project, Employee employee) throws EntityDataException {
        if (project == null) {
            throw new EntityDataException("The project is not specified");
        } else if (employee == null) {
            throw new EntityDataException("The employee is not specified");
        } else {
            List<Project2Employee> assignments = projectEmployeeServices.find(project, employee);
            if (assignments.size() != 1) {
                throw new EntityDataException(String.format("The employee [%s] is not assigned to project [%s]", employee.getEmail(), project.getProjectId()));
            }
        }
    }
}
