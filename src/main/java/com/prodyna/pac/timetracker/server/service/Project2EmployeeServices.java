/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Project;
import com.prodyna.pac.timetracker.entity.Project2Employee;
import com.prodyna.pac.timetracker.server.exception.PrimaryKeyException;
import java.util.List;

/**
 * {@code Interface} for {@link Project2Employee} services.
 *
 * @author apatrikis
 */
public interface Project2EmployeeServices {

    /**
     * Create a {@link Project2Employee} database entity.
     *
     * @param projectEmployee The entity to create.
     * @throws PrimaryKeyException if the {@link Employee} is already assigned
     * to the {@link Project}.
     */
    void create(Project2Employee projectEmployee) throws PrimaryKeyException;

    /**
     * Read a {@link Project2Employee} database entity.
     *
     * @param id The {@code primary key}.
     * @return The {@link Project2Employee} entity.
     */
    Project2Employee read(String id);

    /**
     * Update a {@link Project2Employee} database entity.
     *
     * @param projectEmployee The entity to update.
     */
    void update(Project2Employee projectEmployee);

    /**
     * Delete a {@link Project2Employee} database entity.
     *
     * @param id The {@code primary key}.
     * @return The deleted {@link Project2Employee} entity.
     */
    Project2Employee delete(String id);

    /**
     * Find {@link Project2Employee} database entities by searching for a
     * {@link Employee}
     *
     * @param employee The {@link Employee} to search for.
     * @return The {@link List} of matching {@link Project2Employee} entities.
     */
    List<Project> findProjects(Employee employee);

    /**
     * Find {@link Project2Employee} database entities by searching for a
     * {@link Project}
     *
     * @param project The {@link Project} to search for.
     * @return The {@link List} of matching {@link Project2Employee} entities.
     */
    List<Employee> findEmployees(Project project);

    /**
     * Find {@link Project2Employee} database entities by searching for a
     * {@link Project} and/or {@link Employee}. None, one or both parameters may
     * be specified.
     *
     * @param project The {@link Project} to search for.
     * @param employee The {@link Employee} to search for.
     * @return The {@link List} of matching {@link Project2Employee} entities.
     */
    List<Project2Employee> find(Project project, Employee employee);
}
