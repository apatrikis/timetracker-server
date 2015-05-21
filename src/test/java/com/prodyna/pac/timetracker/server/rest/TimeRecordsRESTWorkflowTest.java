/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.ArquillianHelper;
import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Employee2Role;
import com.prodyna.pac.timetracker.entity.EmployeeRole;
import com.prodyna.pac.timetracker.entity.Project;
import com.prodyna.pac.timetracker.entity.TimeRecord;
import com.prodyna.pac.timetracker.entity.TimeRecordStatus;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * Test class for testing the {@code REST interface} {@link TimeRecordsREST},
 * especially the worklflow aspects.
 *
 * @author apatrikis
 */
@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TimeRecordsRESTWorkflowTest {

    private static Employee user;
    private static Employee manager;
    private static Project project;

    private static TimeRecord recordBefore;
    private static TimeRecord recordAfter;

    /**
     * Create a deployment archive for {@code Arquillian} test execution.
     *
     * @return The crated {@link WebArchive}.
     */
    @Deployment
    public static WebArchive createDeployment() {
        return ArquillianHelper.createDeployment();
    }

    /**
     * Create needed {@link Project} and {@link Employee} objects.
     */
    @BeforeClass
    public static void setUpClass() {
        RESTClientHelper.ensureWebAppAndDefaultAdmin();
        user = helpCreateEmployee(EmployeeRole.USER);
        manager = helpCreateEmployee(EmployeeRole.MANAGER);
        helpCreateProject();
        helpAssignProject();
    }

    /**
     * Delete {@link Project} and {@link Employee} objects.
     */
    @AfterClass
    public static void tearDownClass() {
        helpUnassignProject();
        helpDeleteProject();
        helpDeleteEmployee(user);
        helpDeleteEmployee(manager);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test creating a {@link TimeRecord}.
     */
    @Test
    @RunAsClient
    public void test01_CreateOne() {
        Assume.assumeNotNull(user, project);
        TimeRecord timeRecord = RESTClientHelper.createTimeRecord(user, project);
        timeRecord.setStartTime(Date.from(ZonedDateTime.parse("2015-01-31T09:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        timeRecord.setEndTime(Date.from(ZonedDateTime.parse("2015-01-31T17:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        Entity<TimeRecord> json = Entity.json(timeRecord);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Test creating another {@link TimeRecord}, earlier in time.
     */
    @Test
    @RunAsClient
    public void test02_CreateOneBefore() {
        Assume.assumeNotNull(user, project);
        TimeRecord timeRecord = RESTClientHelper.createTimeRecord(user, project);
        timeRecord.setStartTime(Date.from(ZonedDateTime.parse("2015-01-30T09:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        timeRecord.setEndTime(Date.from(ZonedDateTime.parse("2015-01-30T17:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        Entity<TimeRecord> json = Entity.json(timeRecord);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
        recordBefore = timeRecord;
    }

    /**
     * Test creating another {@link TimeRecord}, later in time.
     */
    @Test
    @RunAsClient
    public void test02_CreateOneAfter() {
        Assume.assumeNotNull(user, project);
        TimeRecord timeRecord = RESTClientHelper.createTimeRecord(user, project);
        timeRecord.setStartTime(Date.from(ZonedDateTime.parse("2015-02-01T09:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        timeRecord.setEndTime(Date.from(ZonedDateTime.parse("2015-02-01T17:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        Entity<TimeRecord> json = Entity.json(timeRecord);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
        recordAfter = timeRecord;
    }

    /**
     * Test creating a {@link TimeRecord}, overlapping the start time.
     */
    @Test
    @RunAsClient
    public void test03_failCreateOneOverlapStart() {
        Assume.assumeNotNull(user, project);
        TimeRecord timeRecord = RESTClientHelper.createTimeRecord(user, project);
        timeRecord.setStartTime(Date.from(ZonedDateTime.parse("2015-01-31T08:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        timeRecord.setEndTime(Date.from(ZonedDateTime.parse("2015-01-31T12:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        Entity<TimeRecord> json = Entity.json(timeRecord);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [EntityDataException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test creating a {@link TimeRecord}, overlapping the end time.
     */
    @Test
    @RunAsClient
    public void test03_failCreateOneOverlapEnd() {
        Assume.assumeNotNull(user, project);
        TimeRecord timeRecord = RESTClientHelper.createTimeRecord(user, project);
        timeRecord.setStartTime(Date.from(ZonedDateTime.parse("2015-01-31T16:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        timeRecord.setEndTime(Date.from(ZonedDateTime.parse("2015-01-31T20:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        Entity<TimeRecord> json = Entity.json(timeRecord);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [EntityDataException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test creating a {@link TimeRecord}, which has start and end times within
     * another record.
     */
    @Test
    @RunAsClient
    public void test03_failCreateOneWithinOther() {
        Assume.assumeNotNull(user, project);
        TimeRecord timeRecord = RESTClientHelper.createTimeRecord(user, project);
        timeRecord.setStartTime(Date.from(ZonedDateTime.parse("2015-01-31T10:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        timeRecord.setEndTime(Date.from(ZonedDateTime.parse("2015-01-31T14:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        Entity<TimeRecord> json = Entity.json(timeRecord);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [EntityDataException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test creating a {@link TimeRecord}, which has start and end times beyond
     * another record.
     */
    @Test
    @RunAsClient
    public void test03_failCreateOneOverlayOther() {
        Assume.assumeNotNull(user, project);
        TimeRecord timeRecord = RESTClientHelper.createTimeRecord(user, project);
        timeRecord.setStartTime(Date.from(ZonedDateTime.parse("2015-01-31T06:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        timeRecord.setEndTime(Date.from(ZonedDateTime.parse("2015-01-31T22:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        Entity<TimeRecord> json = Entity.json(timeRecord);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [EntityDataException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test the worklflow taransition matrix.
     */
    @Test
    @RunAsClient
    public void test04_CheckWorkflowTransitios() {
        Assert.assertFalse(TimeRecordStatus.WORKFLOW_TRANSITIONS[TimeRecordStatus.EDITING.ordinal()][TimeRecordStatus.REWORK.ordinal()]);
        Assert.assertTrue(TimeRecordStatus.WORKFLOW_TRANSITIONS[TimeRecordStatus.REWORK.ordinal()][TimeRecordStatus.EDITING.ordinal()]);
        Assert.assertFalse(TimeRecordStatus.WORKFLOW_TRANSITIONS[TimeRecordStatus.BOOKED.ordinal()][TimeRecordStatus.BOOKED.ordinal()]);
    }

    /**
     * Test update a {@link TimeRecord}'s {@link TimeRecordStatus}.
     */
    @Test
    @RunAsClient
    public void test05_ValidWorkflowTransition() {
        Assume.assumeNotNull(user, recordBefore);
        recordBefore.setRecordStatus(TimeRecordStatus.READY_FOR_APPROVAL);
        Entity<TimeRecord> json = Entity.json(recordBefore);

        // update
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response put = target.request().put(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", put.getStatus(), put.toString()), put.getStatus() == Response.Status.OK.getStatusCode());

        // check
        Response get = target.path(recordBefore.getId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        recordBefore = get.readEntity(TimeRecord.class);
        Assert.assertTrue(String.format("Expected TimeRecord status [%s], received: %s", TimeRecordStatus.READY_FOR_APPROVAL, recordBefore.getRecordStatus().toString()), recordBefore.getRecordStatus().equals(TimeRecordStatus.READY_FOR_APPROVAL));
    }

    /**
     * Test update a {@link TimeRecord}'s {@link TimeRecordStatus} with an
     * invalid transition.
     */
    @Test
    @RunAsClient
    public void test05_InvalidWorkflowTransition() {
        Assume.assumeNotNull(user, recordAfter);
        recordAfter.setRecordStatus(TimeRecordStatus.REWORK);
        Entity<TimeRecord> json = Entity.json(recordAfter);

        // update
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response put = target.request().put(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", put.getStatus(), put.toString()), put.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (put.getHeaderString("X-ServerException") != null) && (put.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (put.getHeaderString("X-ServerException-Type") != null) && (put.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [EntityDataException] contend expected, found [%s]" + put.getHeaderString("X-ServerException-Type"), put.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test update a {@link TimeRecord} to overlap with times from another
     * record.
     */
    @Test
    @RunAsClient
    public void test06_InvalidMoveToOverlap() {
        Assume.assumeNotNull(user, recordAfter);
        recordAfter.setRecordStatus(TimeRecordStatus.EDITING);
        recordAfter.setStartTime(Date.from(ZonedDateTime.parse("2015-01-31T06:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        recordAfter.setEndTime(Date.from(ZonedDateTime.parse("2015-01-31T22:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        Entity<TimeRecord> json = Entity.json(recordAfter);

        // Update
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response put = target.request().put(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", put.getStatus(), put.toString()), put.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (put.getHeaderString("X-ServerException") != null) && (put.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (put.getHeaderString("X-ServerException-Type") != null) && (put.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [EntityDataException] contend expected, found [%s]" + put.getHeaderString("X-ServerException-Type"), put.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test update a {@link TimeRecord} on a locked {@link Project} will fail.
     */
    @Test
    @RunAsClient
    public void test07_failUpdateforLockedProject() {
        Assume.assumeNotNull(manager, user, project, recordBefore);

        // lock project
        project.setLocked(true);
        Entity<Project> jsonProject = Entity.json(project);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS_PATH, manager);
        Response put = target.request().put(jsonProject);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), put.getStatus(), put.toString()), put.getStatus() == Response.Status.OK.getStatusCode());

        // update on locked project
        // NOTE: "recordBefore" has it's own copy of "project" which is NOT locked, this will be handeled by the server
        recordBefore.setRecordStatus(TimeRecordStatus.EDITING);
        Entity<TimeRecord> json = Entity.json(recordBefore);

        target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        put = target.request().put(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CONFLICT.getStatusCode(), put.getStatus(), put.toString()), put.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (put.getHeaderString("X-ServerException") != null) && (put.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (put.getHeaderString("X-ServerException-Type") != null) && (put.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [EntityDataException] contend expected, found [%s]" + put.getHeaderString("X-ServerException-Type"), put.getHeaderString("X-ServerException-Type").contains("EntityDataException"));

        // unlock project
        project.setLocked(false);
        jsonProject = Entity.json(project);

        target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS_PATH, manager);
        put = target.request().put(jsonProject);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), put.getStatus(), put.toString()), put.getStatus() == Response.Status.OK.getStatusCode());
    }

    /**
     * Test delete mutliple {@link TimeRecord}s.
     */
    @Test
    @RunAsClient
    public void test08_DeleteAll() {
        Assume.assumeNotNull(user);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.TIMERECORDS_PATH);
        Response get = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<TimeRecord> timeRecords = null;
        try {
            timeRecords = get.readEntity(new GenericType<List<TimeRecord>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", timeRecords);
        Assert.assertTrue(String.format("Expected List with three elements, received: %d", timeRecords.size()), timeRecords.size() == 3);

        target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        for (TimeRecord record : timeRecords) {
            Response delete = target.path(record.getId()).request().delete();
            Assert.assertTrue(String.format("Response code not expected (%d): %s", delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());
        }
    }

    /**
     * Helper for creating a required {@link Employee} object.
     */
    private static Employee helpCreateEmployee(EmployeeRole role) {
        // create Employee
        Employee employee = RESTClientHelper.createEmployee("time-records-workflow-" + role.toString(), "test");
        Entity<Employee> json = Entity.json(employee);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());

        // create employee role assignment
        Employee2Role employeeRole = RESTClientHelper.createEmployee2Role(employee, role);
        employeeRole.setId("id-" + employee.getEmail());
        Entity<Employee2Role> jsonER = Entity.json(employeeRole);

        target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        post = target.request().post(jsonER);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());

        return employee;
    }

    /**
     * Helper for deleting a {@link Employee} object.
     */
    private static void helpDeleteEmployee(Employee employee) {
        Assume.assumeNotNull(employee);

        // delete employ role assignment
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        Response delete = target.path("id-" + employee.getEmail()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: %d (%s)", Response.Status.OK.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // delete employee
        target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        delete = target.path(employee.getEmail()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());
    }

    /**
     * Helper for creating a required {@link Project} object.
     */
    private static void helpCreateProject() {
        Assume.assumeNotNull(manager);
        project = RESTClientHelper.createProject("test-timerecord", manager);
        Entity<Project> json = Entity.json(project);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Helper for deleting a {@link Employee} object.
     */
    private static void helpDeleteProject() {
        Assume.assumeNotNull(project);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS_PATH);
        Response delete = target.path(project.getProjectId()).request().delete();
        Assert.assertTrue(String.format("Response code not expected (%d): %s", delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());
    }

    /**
     * Assign the employee from the project.
     */
    private static void helpAssignProject() {
        Assume.assumeNotNull(user, project);
        Entity<Project> json = Entity.json(project);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS2EMPLOYEES_PATH, manager);
        target = target.path(RESTConfig.EMPLOYEES_PATH);
        Response post = target.path(user.getEmail()).path(RESTConfig.PROJECTS_PATH).request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Unassign the employee from the project.
     */
    private static void helpUnassignProject() {
        Assume.assumeNotNull(user, project);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS2EMPLOYEES_PATH, manager);
        target = target.path(RESTConfig.EMPLOYEES_PATH);
        Response delete = target.path(user.getEmail()).path(RESTConfig.PROJECTS_PATH).path(project.getProjectId()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());
    }
}
