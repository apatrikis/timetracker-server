/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.ArquillianHelper;
import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Employee2Role;
import com.prodyna.pac.timetracker.entity.EmployeeRole;
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
import org.junit.Assert;
import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * Test class for testing the {@code REST interface} {@link EmployeesREST}.
 *
 * @author apatrikis
 */
@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmployeesRESTTest extends AbstractRESTTest {

    private static Employee employee;
    private static Employee2Role employeeRole;

    /**
     * Create a deployment archive for {@code Arquillian} test execution.
     *
     * @return The crated {@link WebArchive}.
     */
    @Deployment
    public static WebArchive createDeployment() {
        return ArquillianHelper.createDeployment();
    }

    @Override
    @Test
    public void initTest_CreateBaseObjects() throws Exception {
        ensureDefaultAdmin();
    }

    @Override
    public void test00_CreateRequiredObjects() {
    }

    @Override
    public void test99_DeleteRequiredObjects() {
    }

    /**
     * Test creating a {@link Employee}.
     */
    @Test
    @RunAsClient
    public void test01_CreateOne() {
        // create empolyee
        employee = createEmployee("employee", "test");
        Entity<Employee> json = Entity.json(employee);

        WebTarget target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response post = target.request().post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());

        // create employee role assignment
        employeeRole = createEmployee2Role(employee, EmployeeRole.USER);
        Entity<Employee2Role> jsonER = Entity.json(employeeRole);

        target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        post = target.request().post(jsonER);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CREATED.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CREATED.getStatusCode());
    }

    /**
     * Test reading a {@link Employee}.
     */
    @Test
    @RunAsClient
    public void test02_ReadOneEmployee() {
        Assume.assumeNotNull(employee);
        WebTarget target = createBasicAuthenticationClient(RESTConfig.EMPLOYEES_PATH, employee);
        Response get = target.path(employee.getEmail()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        Employee employeeResponse = null;
        try {
            employeeResponse = get.readEntity(Employee.class);
        }
        catch (Exception e) {
            Assert.fail("Expected Emplpoyee.class: " + e.getMessage());
        }
        Assert.assertTrue(String.format("Expected Employee email [%s], received: %s", employee.getEmail(), employeeResponse.getEmail()), employee.getEmail().equals(employeeResponse.getEmail()));
    }

    /**
     * Test reading a {@link Employee2Role}.
     */
    @Test
    @RunAsClient
    public void test02_ReadOneEmployeeRoleAssignment() {
        Assume.assumeNotNull(employeeRole);
        WebTarget target = createBasicAuthenticationClient(RESTConfig.EMPLOYEES2ROLES_PATH, employee);
        Response get = target.path(employeeRole.getId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        Employee2Role employeeRoleResponse = null;
        try {
            employeeRoleResponse = get.readEntity(Employee2Role.class);
        }
        catch (Exception e) {
            Assert.fail("Expected Employee2Role.class: " + e.getMessage());
        }
        Assert.assertTrue(String.format("Expected Employee role [%s], received: %s", employeeRoleResponse.getRoleName(), employeeRoleResponse.getRoleName()), employeeRole.getRoleName().equals(employeeRoleResponse.getRoleName()));
    }

    /**
     * Test updating a {@link Employee}.
     */
    @Test
    @RunAsClient
    public void test03_UpdateOneAllwaysFail() {
        Assume.assumeNotNull(employee);

        // read
        WebTarget target = createBasicAuthenticationClient(RESTConfig.EMPLOYEES_PATH, employee);
        Response get = target.path(employee.getEmail()).request(MediaType.APPLICATION_JSON).get();
        Employee employeeResponse = get.readEntity(Employee.class);

        // update will allways fail
        String newFirstName = "newFirst";
        employeeResponse.setFirstName(newFirstName);
        Entity<Employee> json = Entity.json(employeeResponse);

        Response put = target.request().put(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: %d (%s)", Response.Status.BAD_REQUEST.getStatusCode(), put.getStatus(), put.toString()), put.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (put.getHeaderString("X-ServerException") != null) && (put.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (put.getHeaderString("X-ServerException-Type") != null) && (put.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [ServerRESTException] contend expected, found [%s]" + put.getHeaderString("X-ServerException-Type"), put.getHeaderString("X-ServerException-Type").contains("ServerRESTException"));
    }

    /**
     * Test deleting a {@link Employee} fails when a {@link EmployeeRole} is
     * assigned.
     */
    @Test
    @RunAsClient
    public void test04_a_failDeleteEmployeeWithAssignedRole() {
        Assume.assumeNotNull(employee);

        // delete with FK constraint violation
        WebTarget target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response delete = target.path(employee.getEmail()).request().delete();

        String responseBody = delete.readEntity(String.class);
        Assert.assertTrue("Expected 'RollbackException', received: " + responseBody, responseBody.contains("RollbackException"));
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    /**
     * Test deleting a {@link Employee}.
     */
    @Test
    @RunAsClient
    public void test04_b_DeleteOne() {
        Assume.assumeNotNull(employee, employeeRole);
        // delete employ role assignment
        WebTarget target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);
        Response delete = target.path(employeeRole.getId()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: %d (%s)", Response.Status.OK.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // check
        Response get = target.path(employeeRole.getId()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.NOT_FOUND.getStatusCode());

        // delete employee
        target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        delete = target.path(employee.getEmail()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // check
        get = target.path(employee.getEmail()).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.NOT_FOUND.getStatusCode());

    }

    /**
     * Test reading all {@link Employee}s.
     */
    @Test
    @RunAsClient
    public void test02_ReadAll() {
        WebTarget target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response get = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Employee> employees = null;
        try {
            employees = get.readEntity(new GenericType<List<Employee>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", employees);
        Assert.assertTrue(String.format("Expected List with two elements, received: %d", employees.size()), employees.size() == 2);
    }

    /**
     * Test fining all {@link Employee}s matching a search pattern.
     */
    @Test
    @RunAsClient
    public void test02_Find() {
        Assume.assumeNotNull(employee);
        WebTarget target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response get = target.path("find").path(employee.getEmail().substring(2, 5)).request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());

        List<Employee> employees = null;
        try {
            employees = get.readEntity(new GenericType<List<Employee>>() {
            });
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull("List expected", employees);
        Assert.assertTrue(String.format("Expected List with one element, received: %d", employees.size()), employees.size() == 1);
        Assert.assertTrue(String.format("Expected Employee email [%s], received: %s", employee.getEmail(), employees.get(0).getEmail()), employees.get(0).getEmail().equals(employee.getEmail()));
    }

    /**
     * Test creating an incomplete {@link Employee}.
     */
    @Test
    @RunAsClient
    public void testCreateOne_failNotNullConstraint() {
        Employee emp = new Employee();
        emp.setEmail("dummy");
        emp.setPassword("dummy");
        Entity<Employee> json = Entity.json(emp);

        WebTarget target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response post = target.request().post(json);
        String responseBody = post.readEntity(String.class);
        Assert.assertTrue("Expected 'TransactionRolledback', received: " + responseBody, responseBody.contains("TransactionRolledback"));
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    /**
     * Test deleting an undefined {@link Employee}.
     */
    @Test
    @RunAsClient
    public void testDeleteOne_failUnknownID() {
        WebTarget target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        Response delete = target.path("unknown").request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, recieved: (%d) %s", Response.Status.NOT_FOUND.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }
}
