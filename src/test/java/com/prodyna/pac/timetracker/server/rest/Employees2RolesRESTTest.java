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
public class Employees2RolesRESTTest {

    private static Employee employee;
    private static Employee2Role employeeRoleUser;
    private static Employee2Role employeeRoleAdmin;

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
        helpCreateEmployee();
    }

    /**
     * Delete {@link Project} and {@link Employee} objects.
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
     * Test creating a {@link Employee2Role}.
     */
    @Test
    @RunAsClient
    public void test01_CreateOne() {
        Assume.assumeNotNull(employee);

        // assign ADMIN role
        employeeRoleAdmin = new Employee2Role();
        employeeRoleAdmin.setEmployee(employee);
        employeeRoleAdmin.setRoleName(EmployeeRole.ADMIN.toString());
        Entity<Employee2Role> json = Entity.json(employeeRoleAdmin);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());

        // assign USER role
        employeeRoleUser = new Employee2Role();
        employeeRoleUser.setEmployee(employee);
        employeeRoleUser.setRoleName(EmployeeRole.USER.toString());
        json = Entity.json(employeeRoleUser);

        post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Test reading a {@link Employee2Role}.
     */
    @Test
    @RunAsClient
    public void test02_ReadOne() {
        Assume.assumeNotNull(employee, employeeRoleAdmin);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClient(RESTConfig.EMPLOYEES2ROLES_PATH, employee);
        Response get = target.path(employeeRoleAdmin.getId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        Employee2Role employeeRole = null;
        try {
            employeeRole = get.readEntity(Employee2Role.class);
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(String.format("Expected EmployeeRole (%s), received: (%s)", employeeRoleAdmin.getRoleName(), employeeRole.getRoleName()), employeeRoleAdmin.getRoleName().equals(employeeRole.getRoleName()));
    }

    /**
     * Test finding {@link EmployeeRole}s of a {@link Employee}.
     */
    @Test
    @RunAsClient
    public void test02_FindByEmployee() {
        Assume.assumeNotNull(employee);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        Response get = target.path("employees").path(employee.getEmail()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Employee2Role> roles = null;
        try {
            roles = get.readEntity(new GenericType<List<Employee2Role>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", roles);
        Assert.assertTrue(String.format("Expected List with two elements, received: %d", roles.size()), roles.size() == 2);
    }

    /**
     * Test finding {@link Employee}s with a {@link EmployeeRole}.
     */
    @Test
    @RunAsClient
    public void test02_FindByRole() {
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        Response get = target.path("roles").path(EmployeeRole.USER.toString()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Employee2Role> roles = null;
        try {
            roles = get.readEntity(new GenericType<List<Employee2Role>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", roles);
        Assert.assertTrue(String.format("Expected List with one element, received: %d", roles.size()), roles.size() == 1);
    }

    /**
     * Test finding {@link EmployeeRole}s of a {@link Employee}, which is not
     * assigned.
     */
    @Test
    @RunAsClient
    public void test02_FindByEmployeeEmpty() {
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        Response get = target.path("employees").path("invalid").request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Employee2Role> roles = null;
        try {
            roles = get.readEntity(new GenericType<List<Employee2Role>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", roles);
        Assert.assertTrue(String.format("Expected empty List, received: %d", roles.size()), roles.isEmpty());
    }

    /**
     * Test finding {@link Employee}s with a {@link EmployeeRole}, which is not
     * assigned.
     */
    @Test
    @RunAsClient
    public void test02_FindByRoleEmpty() {
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        Response get = target.path("roles").path(EmployeeRole.MANAGER.toString()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Employee2Role> roles = null;
        try {
            roles = get.readEntity(new GenericType<List<Employee2Role>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", roles);
        Assert.assertTrue(String.format("Expected empty List, received: %d", roles.size()), roles.size() == 0);
    }

    /**
     * Test reading all {@link EmployeeRole}s.
     */
    @Test
    @RunAsClient
    public void test02_ReadAllRoles() {
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        Response get = target.path("roles").request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<EmployeeRole> roles = null;
        try {
            roles = get.readEntity(new GenericType<List<EmployeeRole>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", roles);
        Assert.assertTrue(String.format("Expected List with three element, received: %d", roles.size()), roles.size() == 3);
    }

    /**
     * Test deleting a {@link Employee2Role}.
     */
    @Test
    @RunAsClient
    public void test03_DeleteOne() {
        Assume.assumeNotNull(employeeRoleAdmin, employeeRoleUser);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);

        // delete ADMIN role assignment
        Response delete = target.path(employeeRoleAdmin.getId()).request().delete();
        Assert.assertTrue(String.format("Response code not expected (%d): %s", delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // check
        Response get = target.path(employeeRoleAdmin.getId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.NOT_FOUND.getStatusCode());

        // delete USER role assignment
        delete = target.path(employeeRoleUser.getId()).request().delete();
        Assert.assertTrue(String.format("Response code not expected (%d): %s", delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // check
        get = target.path(employeeRoleUser.getId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Test reading a {@link Employee2Role}.
     */
    @Test
    @RunAsClient
    public void test02_failReadUnassignedRole() {
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        Response get = target.path("undefined").request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, recieved: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Test reading a {@link Employee2Role}.
     */
    @Test
    @RunAsClient
    public void test02_failAssignRoleTwice() {
        Assume.assumeNotNull(employee);
        Employee2Role employeeRole = new Employee2Role();
        employeeRole.setEmployee(employee);
        employeeRole.setRoleName(EmployeeRole.ADMIN.toString());
        Entity<Employee2Role> json = Entity.json(employeeRole);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code not expected (%d): %s", post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [PrimaryKeyException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("PrimaryKeyException"));
    }

    /**
     * Helper for creating a required {@link Employee} object.
     */
    private static void helpCreateEmployee() {
        employee = RESTClientHelper.createEmployee("employee-role", "test");
        Entity<Employee> json = Entity.json(employee);

        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Helper for deleting a {@link Employee} object.
     */
    private static void helpDeleteEmployee() {
        Assume.assumeNotNull(employee);
        WebTarget target = RESTClientHelper.createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response delete = target.path(employee.getEmail()).request().delete();
        Assert.assertTrue(String.format("Response code not expected (%d): %s", delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());
    }
}
