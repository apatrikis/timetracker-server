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
import java.time.Instant;
import java.util.Date;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Assert;

/**
 * Utility class for {@code REST} test tasks.
 *
 * @author apatrikis
 */
public class RESTClientHelper {

    public static final String BASE_URL_JUNIT = "http://localhost:8080/timetracker-server/" + RESTConfig.REST_PATH;
    public static final String BASE_URL_ARQUILLIAN = "http://localhost:18080/" + ArquillianHelper.ARCHIVE_FILE_NAME + "/" + RESTConfig.REST_PATH;
    public static String BASE_URL = BASE_URL_JUNIT;

    public static final String DEFAULT_ADMIN_USER = "ad.min@tt.com";
    public static final String DEFAULT_ADMIN_PASSWORD = "secret";

    /**
     * Create a {@link WebTarget} without authentication information.
     *
     * @param urlRelativePath The extension to the {@link #BASE_URL}.
     * @return The created {@link WebTarget}.
     */
    public static final WebTarget createNoAuthenticationClient(String urlRelativePath) {
        Client client = ClientBuilder.newClient();
        return client.target(BASE_URL).path(urlRelativePath);
    }

    /**
     * Create a {@link WebTarget} for {@code BASIC} authentication.
     *
     * @param urlRelativePath The extension to the {@link #BASE_URL}.
     * @param userName The user name to use
     * @param password The password to use. It is provided in clear text an will
     * be hashed on the server side.
     * @return The created {@link WebTarget}.
     */
    public static final WebTarget createBasicAuthenticationClient(String urlRelativePath, String userName, String password) {
        WebTarget target = createNoAuthenticationClient(urlRelativePath);

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(userName, password);
        target.register(feature);

        return target;
    }

    /**
     * Create a {@link WebTarget} for {@code BASIC} authentication.
     *
     * @param urlRelativePath The extension to the {@link #BASE_URL}.
     * @param employee The {@link Employee} containing the user name and
     * password.
     * @return The created {@link WebTarget}.
     */
    public static final WebTarget createBasicAuthenticationClient(String urlRelativePath, Employee employee) {
        return createBasicAuthenticationClient(urlRelativePath, employee.getEmail(), employee.getPassword());
    }

    /**
     * Create a {@link WebTarget} for {@code BASIC} authentication of the
     * defined default admin user account.
     *
     * @param urlRelativePath The extension to the {@link #BASE_URL}.
     * @return The created {@link WebTarget}.
     */
    public static final WebTarget createBasicAuthenticationClientForDefaultAdmin(String urlRelativePath) {
        return createBasicAuthenticationClient(urlRelativePath, DEFAULT_ADMIN_USER, DEFAULT_ADMIN_PASSWORD);
    }

    /**
     * Ensure the WebApp is up an running, and the default admin user is
     * available.
     */
    public static void ensureWebAppAndDefaultAdmin() {
        WebTarget target = RESTClientHelper.createNoAuthenticationClient(RESTConfig.STATUS_PATH);
        Response get = target.request().get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());
    }

    /**
     * Create a {@link Employee} object with default values for testing.
     *
     * @param firstName The fist name to use.
     * @param lastName The last name to use.
     * @return The created {@link Employee}. The email (which is the primary
     * key) is {@code <firstName>.<lastName>@tt.com}.
     */
    public static final Employee createEmployee(String firstName, String lastName) {
        Employee emp = new Employee();
        emp.setEmail(String.format("%s.%s@tt.com", firstName, lastName));
        emp.setPassword(emp.getEmail());
        emp.setFirstName(firstName);
        emp.setLastName(lastName);
        return emp;
    }

    /**
     * Create a {@link Employee2Role} object for testing.
     *
     * @param employee The {@link Employee} to use for crating the relation.
     * @param role The {@link EmployeeRole} to use for crating the relation.
     * @return The created {@link Employee2Role}.
     */
    public static final Employee2Role createEmployee2Role(Employee employee, EmployeeRole role) {
        Employee2Role e2r = new Employee2Role();
        e2r.setEmployee(employee);
        e2r.setRoleName(role.toString());
        return e2r;
    }

    /**
     * Create a {@link Project} object for testing.
     *
     * @param projectName The project name to use. This will be the primary key.
     * @param owner The ownig {@link Employee} of the project.
     * @return The created {@link Project}.
     */
    public static final Project createProject(String projectName, Employee owner) {
        Project pro = new Project();
        pro.setProjectId(projectName);
        pro.setTitle("Title of " + projectName);
        pro.setDescription("Description for " + projectName);
        pro.setLocked(false);
        pro.setOwner(owner);
        pro.setStartDate(Date.from(Instant.parse("2015-01-01T09:00:00.00Z")));
        pro.setEndDate(Date.from(Instant.parse("2024-12-31T17:00:00.00Z")));
        return pro;
    }

    /**
     * Create a {@link TimeRecord} object for testing.
     *
     * @param owner The ownig {@link Employee} of the time record.
     * @param project The {@link Project} to which the booking belongs to.
     * @param startDate The start timestamp of the booking.
     * @param endDate The end timestamp of the booking.
     * @return The created {@link TimeRecord}.
     */
    public static final TimeRecord createTimeRecord(Employee owner, Project project, Date startDate, Date endDate) {
        TimeRecord tr = new TimeRecord();
        tr.setOwner(owner);
        tr.setProject(project);
        tr.setStartTime(startDate);
        tr.setEndTime(endDate);
        tr.setPauseMinutes(30);
        tr.setRecordStatus(TimeRecordStatus.EDITING);
        return tr;
    }

    /**
     * Create a {@link TimeRecord} object for testing. By default, the following
     * timestamps are used:
     * <ul>
     * <li>start: 2015-04-01T09:00:00.00Z</li>
     * <li>end: 2015-04-01T17:00:00.00Z</li>
     * </ul>
     *
     * @param owner The ownig {@link Employee} of the time record.
     * @param project The {@link Project} to which the booking belongs to.
     * @return The created {@link TimeRecord}.
     */
    public static final TimeRecord createTimeRecord(Employee owner, Project project) {
        return createTimeRecord(owner, project, Date.from(Instant.parse("2015-04-01T09:00:00.00Z")), Date.from(Instant.parse("2015-04-01T17:00:00.00Z")));
    }
}
