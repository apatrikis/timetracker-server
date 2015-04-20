/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Employee2Role;
import com.prodyna.pac.timetracker.entity.EmployeeRole;
import com.prodyna.pac.timetracker.entity.Project2Employee;
import com.prodyna.pac.timetracker.server.exception.PrimaryKeyException;
import java.util.List;

/**
 * {@code Interface} for {@link Project2Employee} services.
 *
 * @author apatrikis
 */
public interface Employee2RoleServices {

    /**
     * Create a {@link Employee2Role} database entity.
     *
     * @param employeeRole The entity to create.
     * @throws PrimaryKeyException in case the assingment of a
     * {@link EmployeeRole} to a {@link Employee} already exists.
     */
    void create(Employee2Role employeeRole) throws PrimaryKeyException;

    /**
     * Read a {@link Employee2Role} database entity.
     *
     * @param id The {@code primary key}.
     * @return The {@link Employee2Role} entity.
     */
    Employee2Role read(String id);

    /**
     * Delete a {@link Employee2Role} database entity.
     *
     * @param id The {@code primary key}.
     * @return The deleted {@link Employee2Role} entity.
     */
    Employee2Role delete(String id);

    /**
     * Find {@link Employee2Role} database entities by searching for a
     * {@link Employee}
     *
     * @param employee The {@link Employee} to search for.
     * @return The {@link List} of matching {@link Employee2Role} entities.
     */
    List<Employee2Role> find(Employee employee);

    /**
     * Find {@link Employee2Role} database entities by searching for a
     * {@link EmployeeRole}
     *
     * @param role The {@link EmployeeRole} to search for.
     * @return The {@link List} of matching {@link Employee2Role} entities.
     */
    List<Employee2Role> find(EmployeeRole role);

    /**
     * Get all available {@link EmployeeRole}s.
     *
     * @return The {@link List} of matching {@link EmployeeRole} entities.
     */
    List<EmployeeRole> getAllRoles();
}
