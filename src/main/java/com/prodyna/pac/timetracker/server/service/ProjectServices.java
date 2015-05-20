/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Project;
import java.util.List;

/**
 * {@code Interface} for {@link Project} services.
 *
 * @author apatrikis
 */
public interface ProjectServices {

    /**
     * Create a {@link Project} database entity.
     *
     * @param project The entity to create.
     */
    void create(Project project);

    /**
     * Read a {@link Project} database entity.
     *
     * @param projectID The {@code primary key}.
     * @return The {@link Project} entity.
     */
    Project read(String projectID);

    /**
     * Update a {@link Project} database entity.
     *
     * @param project The entity to update.
     */
    void update(Project project);

    /**
     * Delete a {@link Project} database entity.
     *
     * @param projectID The {@code primary key}.
     * @return The deleted {@link Project} entity.
     */
    Project delete(String projectID);

    /**
     * Find {@link Project} database entities by using a search pattern.
     *
     * @param searchPattern The search pattern for the {@link Project}s to find.
     * The search is made for:
     * <ul>
     * <li>project ID</li>
     * <li>project title</li>
     * <li>project description</li>
     * </ul>
     * @return The {@link List} of matching {@link Project}s.
     */
    List<Project> find(String searchPattern);

    /**
     * Find all {@link Project}s managed by the specified {@link Employee}.
     *
     * @param manager The manager to check.
     * @return The {@link List} of matching {@link Project}s.
     */
    List<Project> findByManager(Employee manager);
}
