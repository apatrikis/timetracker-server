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
 * Test class for testing the {@code REST interface} {@link TimeRecordsREST}.
 *
 * @author apatrikis
 */
@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TimeRecordsRESTTest {

    private static TimeRecord timeRecord;
    private static Employee user;
    private static Employee manager;
    private static Project project;

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
     * Test creating a {@link TimeRecord} while the assignment from employee to
     * project is missing.
     */
    @Test
    @RunAsClient
    public void test01_a_CreateOneFailAsignmenMissing() {
        Assume.assumeNotNull(user, project);
        Date fromDate = Date.from(ZonedDateTime.parse("2015-01-31T09:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant());
        Date throughDate = Date.from(ZonedDateTime.parse("2015-01-31T17:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant());
        timeRecord = RESTClientHelper.createTimeRecord(user, project, fromDate, throughDate);
        Entity<TimeRecord> json = Entity.json(timeRecord);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) (%s)", Response.Status.CONFLICT.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [SearchParametersException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test creating a {@link TimeRecord}.
     */
    @Test
    @RunAsClient
    public void test01_b_CreateOne() {
        Assume.assumeNotNull(user, project, timeRecord);

        // assign
        Entity<Project> json = Entity.json(project);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS2EMPLOYEES_PATH, manager);
        target = target.path(RESTConfig.EMPLOYEES_PATH);
        Response post = target.path(user.getEmail()).path(RESTConfig.PROJECTS_PATH).request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());

        // create, reunsing the object form the failed call
        Entity<TimeRecord> jsonTR = Entity.json(timeRecord);

        target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        post = target.request().post(jsonTR);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Test reading a {@link TimeRecord}.
     */
    @Test
    @RunAsClient
    public void test02_ReadOne() {
        Assume.assumeNotNull(user, timeRecord);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.path(timeRecord.getId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.OK.getStatusCode());

        TimeRecord timeRecordResponse = null;

        try {
            timeRecordResponse = post.readEntity(TimeRecord.class);
        }
        catch (Exception e) {
            Assert.fail("Expected TimeRecord.class: " + e.getMessage());
        }
        Assert.assertTrue(String.format("Expected TimeRecord ID [%s], received: %s", timeRecord.getId(), timeRecordResponse.getId()), timeRecordResponse.getId().equals(timeRecord.getId()));
    }

    /**
     * Test updating a {@link TimeRecord}.
     */
    @Test
    @RunAsClient
    public void test03_UpdateOne() {
        Assume.assumeNotNull(user, project, timeRecord);

        // read
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.path(timeRecord.getId()).request(MediaType.APPLICATION_JSON).get();
        TimeRecord timeRecordResponse = post.readEntity(TimeRecord.class);

        // update
        int newPauseMinutes = 30;
        timeRecordResponse.setPauseMinutes(newPauseMinutes);
        Entity<TimeRecord> json = Entity.json(timeRecordResponse);
        Response put = target.request().put(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", put.getStatus(), put.toString()), put.getStatus() == Response.Status.OK.getStatusCode());

        // check
        Response get = target.path(timeRecord.getId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());
        timeRecordResponse = get.readEntity(TimeRecord.class);

        Assert.assertTrue(String.format("Expected TimeRecord pauseMinutes [%d], received: %d", newPauseMinutes, timeRecordResponse.getPauseMinutes()), timeRecordResponse.getPauseMinutes() == newPauseMinutes);
        Assert.assertTrue(String.format("Expected Employee email [%s], received: %s", user.getEmail(), timeRecordResponse.getOwner().getEmail()), timeRecordResponse.getOwner().getEmail().equals(user.getEmail()));
        Assert.assertTrue(String.format("Expected Project id [%s], received: %s", project.getProjectId(), timeRecordResponse.getProject().getProjectId()), timeRecordResponse.getProject().getProjectId().equals(project.getProjectId()));
    }

    /**
     * Test deleting a {@link Project} fails when a {@link TimeRecord} is
     * assigned.
     */
    @Test
    @RunAsClient
    public void test04_a_failDeleteProjectWithAssignedTimeRecord() {
        Assume.assumeNotNull(project);

        // delete with FK constraint violation
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS_PATH);
        Response delete = target.path(project.getProjectId()).request().delete();

        String responseBody = delete.readEntity(String.class);
        Assert.assertTrue("Expected 'RollbackException', received: " + responseBody, responseBody.contains("RollbackException"));
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    /**
     * Test deleting a {@link TimeRecord}.
     */
    @Test
    @RunAsClient
    public void test04_b_DeleteOne() {
        Assume.assumeNotNull(user, timeRecord);

        // delete
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response delete = target.path(timeRecord.getId()).request().delete();
        Assert.assertTrue(String.format("Response code not expected (%d): %s", delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // check
        Response get = target.path(timeRecord.getId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Test reading all {@link TimeRecord}s.
     */
    @Test
    @RunAsClient
    public void test02_ReadAll() {
        Assume.assumeNotNull(timeRecord);
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
        Assert.assertTrue(String.format("Expected List with one element, received: %d", timeRecords.size()), timeRecords.size() == 1);
        Assert.assertTrue(String.format("Expected TimeRecord ID [%s], received: %s", timeRecord.getId(), timeRecords.get(0).getId()), timeRecords.get(0).getId().equals(timeRecord.getId()));
    }

    /**
     * Test finding all {@link TimeRecord}s assigned to a {@link Employee}.
     */
    @Test
    @RunAsClient
    public void test02_FindByOwner() {
        Assume.assumeNotNull(user, timeRecord);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response get = target.path(String.format("find/owner/%s", user.getEmail())).request(MediaType.APPLICATION_JSON).get();
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
        Assert.assertTrue(String.format("Expected List with one element, received: %d", timeRecords.size()), timeRecords.size() == 1);
        Assert.assertTrue(String.format("Expected TimeRecord id [%s], received: %s", timeRecord.getId(), timeRecords.get(0).getId()), timeRecords.get(0).getId().equals(timeRecord.getId()));
    }

    /**
     * Test finding all {@link TimeRecord}s assigned to a {@link Project}.
     */
    @Test
    @RunAsClient
    public void test02_FindByProject() {
        Assume.assumeNotNull(user, project, timeRecord);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response get = target.path(String.format("find/project/%s", project.getProjectId())).request(MediaType.APPLICATION_JSON).get();
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
        Assert.assertTrue(String.format("Expected List with one element, received: %d", timeRecords.size()), timeRecords.size() == 1);
        Assert.assertTrue(String.format("Expected TimeRecord id [%s], received: %s", timeRecord.getId(), timeRecords.get(0).getId()), timeRecords.get(0).getId().equals(timeRecord.getId()));
    }

    /**
     * Test finding all {@link TimeRecord}s assigned to a {@link Employee} and
     * {@link Project}.
     */
    @Test
    @RunAsClient
    public void test02_FindByOwnerAndProject() {
        Assume.assumeNotNull(user, project, timeRecord);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response get = target.path(String.format("find/owner/%s/project/%s", user.getEmail(), project.getProjectId())).request(MediaType.APPLICATION_JSON).get();
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
        Assert.assertTrue(String.format("Expected List with one element, received: %d", timeRecords.size()), timeRecords.size() == 1);
        Assert.assertTrue(String.format("Expected TimeRecord id [%s], received: %s", timeRecord.getId(), timeRecords.get(0).getId()), timeRecords.get(0).getId().equals(timeRecord.getId()));
    }

    /**
     * Test finding all {@link TimeRecord}s assigned to a
     * {@link Employee}, {@link Project} and time frame.
     */
    @Test
    @RunAsClient
    public void test02_FindByOwnerProjectDate() {
        Assume.assumeNotNull(user, project, timeRecord);
        String fromISODateTime = "2015-01-31T08:00:00.000Z";
        String throughISODateTime = "2015-01-31T20:00:00.000Z";

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response get = target.path(String.format("find/owner/%s/project/%s/from/%s/through/%s", user.getEmail(), project.getProjectId(), fromISODateTime, throughISODateTime)).request(MediaType.APPLICATION_JSON).get();
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
        Assert.assertTrue(String.format("Expected List with one element, received: %d", timeRecords.size()), timeRecords.size() == 1);
        Assert.assertTrue(String.format("Expected TimeRecord id [%s], received: %s", timeRecord.getId(), timeRecords.get(0).getId()), timeRecords.get(0).getId().equals(timeRecord.getId()));
    }

    /**
     * Test finding all {@link TimeRecord}s assigned to a
     * {@link Employee}, {@link Project} and invalid time frame.
     */
    @Test
    @RunAsClient
    public void test02_FindByOwnerProjectDateEmpty() {
        Assume.assumeNotNull(user, project);
        String fromISODateTime = "2015-01-21T08:00:00.000Z";
        String throughISODateTime = "2015-01-21T20:00:00.000Z";

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response get = target.path(String.format("find/owner/%s/project/%s/from/%s/through/%s", user.getEmail(), project.getProjectId(), fromISODateTime, throughISODateTime)).request(MediaType.APPLICATION_JSON).get();
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
        Assert.assertTrue(String.format("Expected empty List, received: %d", timeRecords.size()), timeRecords.isEmpty());
    }

    /**
     * Test finding all {@link TimeRecord}s wuth an incorrect time frame.
     */
    @Test
    @RunAsClient
    public void test02_failFindDaterangeMismatch() {
        Assume.assumeNotNull(user, project);
        String fromISODateTime = "2015-01-31T20:00:00.000Z";
        String throughISODateTime = "2015-01-31T08:00:00.000Z";

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response get = target.path(String.format("find/owner/%s/project/%s/from/%s/through/%s", user.getEmail(), project.getProjectId(), fromISODateTime, throughISODateTime)).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code not expected (%d): %s", get.getStatus(), get.toString()), get.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (get.getHeaderString("X-ServerException") != null) && (get.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (get.getHeaderString("X-ServerException-Type") != null) && (get.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [SearchParametersException] contend expected, found [%s]" + get.getHeaderString("X-ServerException-Type"), get.getHeaderString("X-ServerException-Type").contains("SearchParametersException"));
    }

    /**
     * Test creating an incomplete {@link TimeRecord}.
     */
    @Test
    @RunAsClient
    public void testCreateOne_failMissingData() {
        Assume.assumeNotNull(user);
        TimeRecord timeRecordFail = new TimeRecord();
        timeRecordFail.setId("whatever");
        Entity<TimeRecord> json = Entity.json(timeRecordFail);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [SearchParametersException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test creating {@link TimeRecord} with too long duration.
     */
    @Test
    @RunAsClient
    public void testCreateOne_failTimeIntervalTooLong() {
        Assume.assumeNotNull(user, project);
        TimeRecord timeRecordFail = RESTClientHelper.createTimeRecord(user, project);
        timeRecordFail.setStartTime(Date.from(ZonedDateTime.parse("2015-01-30T09:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        timeRecordFail.setEndTime(Date.from(ZonedDateTime.parse("2015-01-31T09:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        Entity<TimeRecord> json = Entity.json(timeRecordFail);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [EntityDataException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test creating {@link TimeRecord} with too long pause time.
     */
    @Test
    @RunAsClient
    public void testCreateOne_failPauseMinutesTooLong() {
        Assume.assumeNotNull(user, project);
        TimeRecord timeRecordFail = RESTClientHelper.createTimeRecord(user, project);
        timeRecordFail.setStartTime(Date.from(ZonedDateTime.parse("2015-01-30T09:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        timeRecordFail.setEndTime(Date.from(ZonedDateTime.parse("2015-01-30T10:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME).toInstant()));
        timeRecordFail.setPauseMinutes(60);
        Entity<TimeRecord> json = Entity.json(timeRecordFail);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [EntityDataException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test deleting an undefined {@link TimeRecord}.
     */
    @Test
    @RunAsClient
    public void testDeleteOne_failUnknownID() {
        Assume.assumeNotNull(user);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.TIMERECORDS_PATH, user);
        Response delete = target.path("unknownTimeRecordID").request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, recieved: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Helper for creating a required {@link Employee} object.
     */
    private static Employee helpCreateEmployee(EmployeeRole role) {
        // create Employee
        Employee employee = RESTClientHelper.createEmployee("time-records-" + role.toString(), "test");
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
