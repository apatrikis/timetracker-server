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
 * Test class for testing the {@code REST interface} {@link ProjectsREST}.
 *
 * @author apatrikis
 */
@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProjectsRESTTest {

    private static Employee employee;
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
     * Create needed {@link Employee} objects.
     */
    @BeforeClass
    public static void setUpClass() {
        RESTClientHelper.ensureWebAppAndDefaultAdmin();
        helpCreateEmployee();
    }

    /**
     * Delete {@link Employee} objects.
     */
    @AfterClass
    public static void tearDownClass() {
        helpDeleteEmployee();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test crating a {@link Project}.
     */
    @Test
    @RunAsClient
    public void test01_CreateOne() {
        Assume.assumeNotNull(employee);
        project = RESTClientHelper.createProject("test-project", employee);
        Entity<Project> json = Entity.json(project);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Test reading a {@link Project}.
     */
    @Test
    @RunAsClient
    public void test02_ReadOne() {
        Assume.assumeNotNull(employee, project);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS_PATH, employee);
        Response post = target.path(project.getProjectId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.OK.getStatusCode());

        Project projectResponse = null;
        try {
            projectResponse = post.readEntity(Project.class);
        }
        catch (Exception e) {
            Assert.fail("Expected Employee.class: " + e.getMessage());
        }
        Assert.assertTrue(String.format("Expected Project id [%s], received: %s", project.getProjectId(), projectResponse.getProjectId()), project.getProjectId().equals(projectResponse.getProjectId()));
    }

    /**
     * Test updating a {@link Project}.
     */
    @Test
    @RunAsClient
    public void test03_UpdateOne() {
        Assume.assumeNotNull(employee, project);

        // read
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS_PATH, employee);
        Response post = target.path(project.getProjectId()).request(MediaType.APPLICATION_JSON).get();
        Project projectResponse = post.readEntity(Project.class);

        // update
        String newDescription = "another description";
        projectResponse.setDescription(newDescription);
        Entity<Project> json = Entity.json(projectResponse);
        Response put = target.request().put(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), put.getStatus(), put.toString()), put.getStatus() == Response.Status.OK.getStatusCode());

        // check
        Response get = target.path(project.getProjectId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());
        projectResponse = get.readEntity(Project.class);
        Assert.assertTrue(String.format("Expected Project description [%s], received: %s", newDescription, projectResponse.getDescription()), newDescription.equals(projectResponse.getDescription()));
        Assert.assertTrue(String.format("Expected Employee email [%s], received: %s", employee.getEmail(), projectResponse.getOwner().getEmail()), employee.getEmail().equals(projectResponse.getOwner().getEmail()));
    }

    /**
     * Test deleting a {@link Project}.
     */
    @Test
    @RunAsClient
    public void test04_DeleteOne() {
        Assume.assumeNotNull(project);

        // delete
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS_PATH);
        Response delete = target.path(project.getProjectId()).request().delete();
        Assert.assertTrue(String.format("Response code not expected (%d): %s", delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // check
        Response get = target.path(project.getProjectId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Test reading all {@link Project}s.
     */
    @Test
    @RunAsClient
    public void test02_ReadAll() {
        Assume.assumeNotNull(employee, project);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS_PATH, employee);
        Response get = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Project> projects = null;
        try {
            projects = get.readEntity(new GenericType<List<Project>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", projects);
        Assert.assertTrue(String.format("Expected List with one element, received: %d", projects.size()), projects.size() == 1);
        Assert.assertTrue(String.format("Expected Project ID [%s], received: %s", project.getProjectId(), projects.get(0).getProjectId()), projects.get(0).getProjectId().equals(project.getProjectId()));
    }

    /**
     * Test fining all {@link Project}s matching a search pattern.
     */
    @Test
    @RunAsClient
    public void test02_Find() {
        Assume.assumeNotNull(employee, project);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS_PATH, employee);
        Response get = target.path("find").path(project.getProjectId().substring(2, 5)).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Project> projects = null;
        try {
            projects = get.readEntity(new GenericType<List<Project>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", projects);
        Assert.assertTrue(String.format("Expected List with one element, received: %d", projects.size()), projects.size() == 1);
        Assert.assertTrue(String.format("Expected Project ID [%s], received: %s", project.getProjectId(), projects.get(0).getProjectId()), projects.get(0).getProjectId().equals(project.getProjectId()));
    }

    /**
     * Test fining all {@link Project}s matching a search pattern.
     */
    @Test
    @RunAsClient
    public void test02_FindByManager() {
        Assume.assumeNotNull(employee, project);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.PROJECTS_PATH, employee);
        Response get = target.path(RESTConfig.EMPLOYEES_PATH).path(employee.getEmail()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Project> projects = null;
        try {
            projects = get.readEntity(new GenericType<List<Project>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", projects);
        Assert.assertTrue(String.format("Expected List with one element, received: %d", projects.size()), projects.size() == 1);
        Assert.assertTrue(String.format("Expected Project ID [%s], received: %s", project.getProjectId(), projects.get(0).getProjectId()), projects.get(0).getProjectId().equals(project.getProjectId()));
    }

    /**
     * Test crating an incomplete {@link Project}.
     */
    @Test
    @RunAsClient
    public void testCreateOne_failNotNullConstraint() {
        Project prj = new Project();
        prj.setProjectId("ValuesAreMissing");
        Entity<Project> json = Entity.json(prj);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) (%s)", Response.Status.CONFLICT.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [SearchParametersException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("EntityDataException"));
    }

    /**
     * Test deleting an undefined {@link Project}.
     */
    @Test
    @RunAsClient
    public void testDeleteOne_failUnknownID() {
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.PROJECTS_PATH);
        Response delete = target.path("unknownPID").request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, recieved: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Helper for creating a required {@link Employee} object.
     */
    private static void helpCreateEmployee() {
        // create employee
        employee = RESTClientHelper.createEmployee("projects", "test");
        Entity<Employee> json = Entity.json(employee);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());

        // create employee role assignment
        Employee2Role employeeRole = RESTClientHelper.createEmployee2Role(employee, EmployeeRole.MANAGER);
        employeeRole.setId("id-" + employee.getEmail());
        Entity<Employee2Role> jsonER = Entity.json(employeeRole);

        target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        post = target.request().post(jsonER);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Helper for deleting a {@link Employee} object.
     */
    private static void helpDeleteEmployee() {
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
}
