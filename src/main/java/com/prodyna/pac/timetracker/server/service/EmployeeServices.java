/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service;

import com.prodyna.pac.timetracker.entity.Employee;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * {@code Interface} for {@link Employee} services.
 *
 * @author apatrikis
 */
public interface EmployeeServices {

    /**
     * Create a {@link Employee} database entity.
     *
     * @param employee The entity to create.
     * @throws java.security.NoSuchAlgorithmException if the provided password
     * cannot be hashed.
     */
    void create(Employee employee) throws NoSuchAlgorithmException;

    /**
     * Read a {@link Employee} database entity.
     *
     * @param email The {@code primary key}.
     * @return The {@link Employee} entity.
     */
    Employee read(String email);

    /**
     * Update a {@link Employee} database entity.
     *
     * @param employee The entity to update.
     */
    void update(Employee employee);

    /**
     * Delete a {@link Employee} database entity.
     *
     * @param email The {@code primary key}.
     * @return The deleted {@link Employee} entity.
     */
    Employee delete(String email);

    /**
     * Find {@link Employee} database entities by using a search pattern.
     *
     * @param searchPattern The search pattern for the {@link Employee}s to
     * find. The search is made for:
     * <ul>
     * <li>first name</li>
     * <li>last name</li>
     * <li>email</li>
     * </ul>
     * @return The {@link List} of matching {@link Employee}s.
     */
    List<Employee> find(String searchPattern);
}
