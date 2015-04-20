/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.timetracker.entity.EmployeeRole;
import com.prodyna.pac.timetracker.pojo.ChangePassword;
import com.prodyna.pac.timetracker.pojo.LoginInfo;
import com.prodyna.pac.timetracker.server.exception.LoginException;
import com.prodyna.pac.timetracker.server.exception.PasswordChangeException;
import com.prodyna.pac.timetracker.server.monitoring.BusinessServiceMXBean;
import com.prodyna.pac.timetracker.server.service.SecurityServices;
import java.security.NoSuchAlgorithmException;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * {@code REST Interface} for {@code security} related actions. The
 * {@code REST Interface} is available under {@link RESTConfig#SECURITY_PATH}.
 *
 * @author apatrikis
 */
@Path(RESTConfig.SECURITY_PATH)
@Stateless
public class SecurityREST extends AbstractREST {

    @Inject
    private SecurityServices securityServices;

    /**
     * Check if the login as {@link EmployeeRole#ROLE_USER} is possible
     *
     * @param settings The login values.
     * @return The {@link Response.Status#OK}
     * @throws LoginException in case the {@link LoginInfo} is invalid.
     */
    @POST
    @Path(EmployeeRole.ROLE_USER)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_USER)
    public Response checkCredentialsUser(LoginInfo settings) throws LoginException {
        // noting to do: all checks made by app server
        return Response.ok().build();
    }

    /**
     * Check if the login as {@link EmployeeRole#ROLE_MANAGER} is possible
     *
     * @param settings The login values.
     * @return The {@link Response.Status#OK}
     * @throws LoginException in case the {@link LoginInfo} is invalid.
     */
    @POST
    @Path(EmployeeRole.ROLE_MANAGER)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_MANAGER)
    public Response checkCredentialsManager(LoginInfo settings) throws LoginException {
        // noting to do: all checks made by app server
        return Response.ok().build();
    }

    /**
     * Check if the login as {@link EmployeeRole#ROLE_ADMIN} is possible
     *
     * @param settings The login values.
     * @return The {@link Response.Status#OK}
     * @throws LoginException in case the {@link LoginInfo} is invalid.
     */
    @POST
    @Path(EmployeeRole.ROLE_ADMIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(EmployeeRole.ROLE_ADMIN)
    public Response checkCredentialsAdmin(LoginInfo settings) throws LoginException {
        // noting to do: all checks made by app server
        return Response.ok().build();
    }

    /**
     * Change the password of a user.
     *
     * @param settings The values for changing the password.
     * @return The {@link Response.Status#OK}
     * @throws PasswordChangeException in case the current password is wrong or
     * the new password is idetical to the current password.
     * @throws java.security.NoSuchAlgorithmException if the provided password
     * cannot be hashed.
     */
    @POST
    @Path("change")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_USER})
    public Response changePasswd(ChangePassword settings) throws PasswordChangeException, NoSuchAlgorithmException {
        securityServices.changePassword(settings);
        return Response.ok().build();
    }

    @Override
    public BusinessServiceMXBean getMonitorBean() {
        return null;
    }
}
