/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.ArquillianHelper;
import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Employee2Role;
import com.prodyna.pac.timetracker.entity.EmployeeRole;
import com.prodyna.pac.timetracker.pojo.ChangePassword;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
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
 * Test class for testing the {@code REST interface} {@link SecurityREST}.
 *
 * @author apatrikis
 */
@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecurityRESTTest extends AbstractRESTTest {

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
    @Test
    @RunAsClient
    public void test00_CreateRequiredObjects() {
        helpCreateEmployee();
    }

    @Override
    @Test
    @RunAsClient
    public void test99_DeleteRequiredObjects() {
        helpDeleteEmployee();
    }

    /**
     * Test checking login creedentials.
     */
    @Test
    @RunAsClient
    public void test01_CheckCredentials() {
        Assume.assumeNotNull(employee);

        WebTarget target = createBasicAuthenticationClient(RESTConfig.SECURITY_PATH, employee);
        Response get = target.path(EmployeeRole.ROLE_USER).request().get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.OK.getStatusCode());
    }

    /**
     * Test retrieve own information.
     */
    @Test
    @RunAsClient
    public void test01_WhoAmI() {
        Assume.assumeNotNull(employee);

        WebTarget target = createBasicAuthenticationClient(RESTConfig.SECURITY_PATH, employee);
        Response get = target.request(MediaType.APPLICATION_JSON).get();
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
     * Test checking login creedentials with a missing role.
     */
    @Test
    @RunAsClient
    public void test01_failCheckCredentialsMissingRole() {
        Assume.assumeNotNull(employee);

        WebTarget target = createBasicAuthenticationClient(RESTConfig.SECURITY_PATH, employee);
        Response get = target.path(EmployeeRole.ROLE_MANAGER).request().get();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), get.getStatus(), get.toString()), get.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    /**
     * Test changing a password.
     */
    @Test
    @RunAsClient
    public void test02_ChangePasswd() {
        Assume.assumeNotNull(employee);
        ChangePassword changePassword = new ChangePassword();
        changePassword.setEmail(employee.getEmail());
        changePassword.setCurrentPassword(employee.getPassword());
        changePassword.setNewPassword("change-" + employee.getPassword());
        Entity<ChangePassword> json = Entity.json(changePassword);

        WebTarget target = createBasicAuthenticationClient(RESTConfig.SECURITY_PATH, employee);
        Response post = target.path("change").request(MediaType.APPLICATION_JSON).post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.OK.getStatusCode());
    }

    /**
     * Test changing a password.
     */
    @Test
    @RunAsClient
    public void test02_ResetPasswd() {
        Assume.assumeNotNull(employee);
        ChangePassword changePassword = new ChangePassword();
        changePassword.setEmail(employee.getEmail());
        changePassword.setCurrentPassword("this_will_be_ignored");
        changePassword.setNewPassword("reset-" + employee.getPassword());
        Entity<ChangePassword> json = Entity.json(changePassword);

        WebTarget target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.SECURITY_PATH);
        Response post = target.path("reset").request(MediaType.APPLICATION_JSON).post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.OK.getStatusCode());
        employee.setPassword(changePassword.getNewPassword());
    }

    /**
     * Test changing a password: this will fail becaus the execuing user must be
     * an ADMIN.
     */
    @Test
    @RunAsClient
    public void test02_failResetPasswd() {
        Assume.assumeNotNull(employee);
        ChangePassword changePassword = new ChangePassword();
        changePassword.setEmail(employee.getEmail());
        changePassword.setCurrentPassword("this_will_be_ignored");
        changePassword.setNewPassword("reset-" + employee.getPassword());
        Entity<ChangePassword> json = Entity.json(changePassword);

        WebTarget target = createBasicAuthenticationClient(RESTConfig.SECURITY_PATH, employee);
        Response post = target.path("reset").request(MediaType.APPLICATION_JSON).post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    /**
     * Test changing a password with identical old and new password.
     */
    @Test
    @RunAsClient
    public void test03_FailChangePasswordIdenticalOldNew() {
        Assume.assumeNotNull(employee);
        ChangePassword changePassword = new ChangePassword();
        changePassword.setEmail(employee.getEmail());
        changePassword.setCurrentPassword(employee.getEmail());
        changePassword.setNewPassword(employee.getEmail());
        Entity<ChangePassword> json = Entity.json(changePassword);

        WebTarget target = createBasicAuthenticationClient(RESTConfig.SECURITY_PATH, employee);
        Response post = target.path("change").request(MediaType.APPLICATION_JSON).post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.CONFLICT.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [PasswordChangeException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("PasswordChangeException"));
    }

    /**
     * Test changing a password with identical old and new password.
     */
    @Test
    @RunAsClient
    public void test03_FailChangeWrongCurrentPassword() {
        Assume.assumeNotNull(employee);
        ChangePassword changePassword = new ChangePassword();
        changePassword.setEmail(employee.getEmail());
        changePassword.setCurrentPassword("wrong-" + employee.getEmail());
        changePassword.setNewPassword("new-" + employee.getEmail());
        Entity<ChangePassword> json = Entity.json(changePassword);

        WebTarget target = createBasicAuthenticationClient(RESTConfig.SECURITY_PATH, employee);
        Response post = target.path("change").request(MediaType.APPLICATION_JSON).post(json);
        Assert.assertTrue(String.format("Response code (%d) expected, recieved: (%d) %s", Response.Status.CONFLICT.getStatusCode(), post.getStatus(), post.toString()), post.getStatus() == Response.Status.CONFLICT.getStatusCode());
        Assert.assertTrue("Header X-ServerException with contend expected", (post.getHeaderString("X-ServerException") != null) && (post.getHeaderString("X-ServerException").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type with contend expected", (post.getHeaderString("X-ServerException-Type") != null) && (post.getHeaderString("X-ServerException-Type").length() > 0));
        Assert.assertTrue("Header X-ServerException-Type of type [PasswordChangeException] contend expected, found [%s]" + post.getHeaderString("X-ServerException-Type"), post.getHeaderString("X-ServerException-Type").contains("PasswordChangeException"));
    }

    /**
     * Helper for creating a required {@link Employee} object.
     */
    private void helpCreateEmployee() {
        // create Employee
        employee = createEmployee("security", "test");
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
     * Helper for deleting a {@link Employee} object.
     */
    private void helpDeleteEmployee() {
        Assume.assumeNotNull(employeeRole, employee);
        // delete employee role assignment
        WebTarget target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES2ROLES_PATH);

        Response delete = target.path(employeeRole.getId()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());

        // delete employee
        target = createBasicAuthenticationClientForDefaultAdmin(RESTConfig.EMPLOYEES_PATH);
        delete = target.path(employee.getEmail()).request().delete();
        Assert.assertTrue(String.format("Response code (%d) expected, received: (%d) %s", Response.Status.OK.getStatusCode(), delete.getStatus(), delete.toString()), delete.getStatus() == Response.Status.OK.getStatusCode());
    }
}
