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
 * Test class for testing the {@code REST interface}
 * {@link Projects2EmployeesREST}.
 *
 * @author apatrikis
 */
@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Projects2EmployeesRESTTest {

    private static Employee user;
    private static Employee manager;
    private static Project project1;
    private static Project project2;

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
        user = helpCreateEmployee("projectEmployee2", "test", EmployeeRole.USER);
        manager = helpCreateEmployee("projectEmployee1", "test", EmployeeRole.MANAGER);
        project1 = helpCreateProject("project-1-test", user);
        project2 = helpCreateProject("project-2-test", manager);
    }

    /**
     * Delete {@link Project} and {@link Employee} objects.
     */
    @AfterClass
    public static void tearDownClass() {
        helpDeleteProject(project1);
        helpDeleteProject(project2);
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
     * Test assign {@link Project} to {@link Employee}.
     */
    @Test
    @RunAsClient
    public void test01_AssignProject() {
        Assume.assumeNotNull(project1, project2, manager, user);

        // #1
        Entity<Project> json = Entity.json(project1);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS2EMPLOYEES_PATH);
        target = target.path(RESTConfig.EMPLOYEES_PATH);
        Response post = target.path(user.getEmail()).path(RESTConfig.PROJECTS_PATH).request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());

        // #2
        json = Entity.json(project2);
        post = target.path(manager.getEmail()).path(RESTConfig.PROJECTS_PATH).request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Test assign {@link Employee} to {@link Project}.
     */
    @Test
    @RunAsClient
    public void test01_AssignEmployee() {
        Assume.assumeNotNull(project1, manager);
        Entity<Employee> json = Entity.json(manager);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS2EMPLOYEES_PATH);
        target = target.path(RESTConfig.PROJECTS_PATH);
        Response post = target.path(project1.getProjectId()).path(RESTConfig.EMPLOYEES_PATH).request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Test read {@link Project}s by for {@link Employee}.
     */
    @Test
    @RunAsClient
    public void test02_ReadAllProjetcs() {
        Assume.assumeNotNull(manager, user);

        // #1
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS2EMPLOYEES_PATH, manager);
        target = target.path(RESTConfig.EMPLOYEES_PATH);
        Response get = target.path(user.getEmail()).path(RESTConfig.PROJECTS_PATH).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Project> assignments = null;
        try {
            assignments = get.readEntity(new GenericType<List<Project>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", assignments);
        Assert.assertTrue(String.format("Expected List with one element, received: %d", assignments.size()), assignments.size() == 1);
        Assert.assertTrue(String.format("Expected Project id [%s], received: %s", project1.getProjectId(), assignments.get(0).getProjectId()), assignments.get(0).getProjectId().equals(project1.getProjectId()));

        // #2
        get = target.path(manager.getEmail()).path(RESTConfig.PROJECTS_PATH).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        assignments = null;
        try {
            assignments = get.readEntity(new GenericType<List<Project>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", assignments);
        Assert.assertTrue(String.format("Expected List with two elements, received: %d", assignments.size()), assignments.size() == 2);
    }

    /**
     * Test read {@link Employee}s by for {@link Project}.
     */
    @Test
    @RunAsClient
    public void test02_ReadAllEmployees() {
        Assume.assumeNotNull(project1, project2, manager);

        // #1
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS2EMPLOYEES_PATH, manager);
        target = target.path(RESTConfig.PROJECTS_PATH);
        Response get = target.path(project2.getProjectId()).path(RESTConfig.EMPLOYEES_PATH).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Employee> assignments = null;
        try {
            assignments = get.readEntity(new GenericType<List<Employee>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", assignments);
        Assert.assertTrue(String.format("Expected List with one element, received: %d", assignments.size()), assignments.size() == 1);
        Assert.assertTrue(String.format("Expected Employee email [%s], received: %s", manager.getEmail(), assignments.get(0).getEmail()), assignments.get(0).getEmail().equals(manager.getEmail()));

        // #2
        get = target.path(project1.getProjectId()).path(RESTConfig.EMPLOYEES_PATH).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        assignments = null;
        try {
            assignments = get.readEntity(new GenericType<List<Employee>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", assignments);
        Assert.assertTrue(String.format("Expected List with two elements, received: %d", assignments.size()), assignments.size() == 2);
    }

    /**
     * Test unassign {@link Project}s from {@link Employee}.
     */
    @Test
    @RunAsClient
    public void test03_UnassignProject() {
        Assume.assumeNotNull(project1, project2, manager);

        // #1
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS2EMPLOYEES_PATH);
        target = target.path(RESTConfig.EMPLOYEES_PATH);
        Response delete = target.path(user.getEmail()).path(RESTConfig.PROJECTS_PATH).path(project1.getProjectId()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // #2
        delete = target.path(manager.getEmail()).path(RESTConfig.PROJECTS_PATH).path(project2.getProjectId()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // check
        delete = target.path(manager.getEmail()).path(RESTConfig.PROJECTS_PATH).path(project2.getProjectId()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Test unassign {@link Employee}s from {@link Project}.
     */
    @Test
    @RunAsClient
    public void test03_UnassignEmployee() {
        Assume.assumeNotNull(project1, manager);

        // #1
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS2EMPLOYEES_PATH);
        target = target.path(RESTConfig.PROJECTS_PATH);
        Response delete = target.path(project1.getProjectId()).path(RESTConfig.EMPLOYEES_PATH).path(manager.getEmail()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // check
        delete = target.path(project1.getProjectId()).path(RESTConfig.EMPLOYEES_PATH).path(manager.getEmail()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Helper for creating a required {@link Employee} object.
     *
     * @param email The {@code primary key}.
     * @return The created {@link Employee} object.
     */
    private static Employee helpCreateEmployee(String fistName, String lastName, EmployeeRole role) {
        // cretate employee
        Employee retEmployee = RESTClientHelper.createEmployee(fistName, lastName);
        Entity<Employee> json = Entity.json(retEmployee);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());

        // create employee role assignment
        Employee2Role employeeRole = RESTClientHelper.createEmployee2Role(retEmployee, role);
        employeeRole.setId("id-" + retEmployee.getEmail());
        Entity<Employee2Role> jsonER = Entity.json(employeeRole);

        target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        post = target.request().post(jsonER);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
        return retEmployee;
    }

    /**
     * Helper for deleting a {@link Employee} object.
     *
     * @param email The {@code primary key}.
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
     *
     * @param projectID The {@code primary key}.
     * @param owner The {@link Employee} who is the {@link Project} owner.
     * @return The created {@link Project} object.
     */
    private static Project helpCreateProject(String projectID, Employee owner) {
        Assume.assumeNotNull(owner);
        Project retProject = RESTClientHelper.createProject(projectID, owner);
        Entity<Project> json = Entity.json(retProject);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());

        return retProject;
    }

    /**
     * Helper for deleting a {@link Employee} object.
     *
     * @param projectID The {@code primary key}.
     */
    private static void helpDeleteProject(Project project) {
        Assume.assumeNotNull(project);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS_PATH);
        Response delete = target.path(project.getProjectId()).request().delete();
        Assert.assertTrue(String.format("Response code not expected (%d): %s", delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());
    }
}
