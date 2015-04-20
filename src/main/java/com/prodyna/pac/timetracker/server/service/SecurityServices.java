/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service;

import com.prodyna.pac.timetracker.pojo.ChangePassword;
import com.prodyna.pac.timetracker.server.exception.PasswordChangeException;
import java.security.NoSuchAlgorithmException;

/**
 * {@code Interface} for security related services.
 *
 * @author apatrikis
 */
public interface SecurityServices {

    /**
     * Change the user password.
     *
     * @param settings All required values for changing the password.
     * @throws PasswordChangeException in case the current password is wrong or
     * the new password is idetical to the current password.
     * @throws java.security.NoSuchAlgorithmException if the provided password
     * cannot be hashed.
     */
    void changePassword(ChangePassword settings) throws PasswordChangeException, NoSuchAlgorithmException;
}
