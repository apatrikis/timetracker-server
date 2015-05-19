/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service.jpa;

import com.prodyna.pac.timetracker.Security;
import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.pojo.ChangePassword;
import com.prodyna.pac.timetracker.server.exception.PasswordChangeException;
import com.prodyna.pac.timetracker.server.service.SecurityServices;
import java.security.NoSuchAlgorithmException;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;

/**
 * Implementation of the {@link SecurityServices} {@code interface}.
 */
@Local(value = SecurityServices.class)
@Stateless
public class SecurityService extends AbstractService implements SecurityServices {

    @Inject
    private Logger log;

    /**
     * Default constructor.
     */
    public SecurityService() {
        // nothing to do
    }

    @Override
    public void changePassword(ChangePassword settings) throws PasswordChangeException, NoSuchAlgorithmException {
        if (settings.getCurrentPassword().equals(settings.getNewPassword())) {
            throw new PasswordChangeException("Old and new password must not match.");
        }

        // check is supplied password is correct
        Employee employee = em.find(Employee.class, settings.getEmail());
        if (employee.getPassword().equals(Security.passwordHashSHA256(settings.getCurrentPassword()))) {
            employee.setPassword(Security.passwordHashSHA256(settings.getNewPassword()));
            em.merge(employee);
            log.info("changed password for {}", settings.getEmail());
        } else {
            throw new PasswordChangeException("Old password is incorrect.");
        }
    }

    @Override
    public void resetPassword(ChangePassword settings) throws NoSuchAlgorithmException {
        Employee employee = em.find(Employee.class, settings.getEmail());
        employee.setPassword(Security.passwordHashSHA256(settings.getNewPassword()));
        em.merge(employee);
        log.info("reset password for {}", settings.getEmail());
    }
}
