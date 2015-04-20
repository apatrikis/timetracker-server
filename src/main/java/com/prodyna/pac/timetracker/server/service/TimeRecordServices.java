/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Project;
import com.prodyna.pac.timetracker.entity.TimeRecord;
import com.prodyna.pac.timetracker.pojo.TimeRecordSearch;
import com.prodyna.pac.timetracker.server.exception.EntityDataException;
import com.prodyna.pac.timetracker.server.exception.SearchParametersException;
import java.util.List;

/**
 * {@code Interface} for {@link TimeRecord} services.
 *
 * @author apatrikis
 */
public interface TimeRecordServices {

    /**
     * Create a {@link TimeRecord} database entity.
     *
     * @param timeRecord The entity to create.
     * @throws EntityDataException if the provided data is invalid, e. g. the
     * provided timestamps are invalid or too long.
     * @throws SearchParametersException e. g. if the {@link Project} or
     * {@link Employee} is not specified.
     */
    void create(TimeRecord timeRecord) throws EntityDataException, SearchParametersException;

    /**
     * Read a {@link Project} database entity.
     *
     * @param id The {@code primary key}.
     * @return The {@link TimeRecord} entity.
     */
    TimeRecord read(String id);

    /**
     * Update a {@link TimeRecord} database entity.
     *
     * @param timeRecord The entity to update.
     * @throws EntityDataException if the provided data is invalid, e. g. the
     * provided timestamps are invalid or too long.
     * @throws SearchParametersException e. g. if the {@link Project} or
     * {@link Employee} is not specified.
     */
    void update(TimeRecord timeRecord) throws EntityDataException, SearchParametersException;

    /**
     * Delete a {@link TimeRecord} database entity.
     *
     * @param id The {@code primary key}.
     * @return The deleted {@link TimeRecord} entity.
     * @throws EntityDataException if the data cannot be deleted, e. g. the
     * status does not allow deleting.
     */
    TimeRecord delete(String id) throws EntityDataException;

    /**
     * Find {@link TimeRecord} database entities by using a search pattern.
     *
     * @param searchPattern The serch values that could be used. Any combination
     * of values may be used, even no values at all or {@code null}.
     * @return The {@link List} of matching {@link TimeRecord}s.
     * @throws SearchParametersException e. g. if the {@link Project} or
     * {@link Employee} is not specified, or the provided timestamps are
     * invalid.
     */
    List<TimeRecord> find(TimeRecordSearch searchPattern) throws SearchParametersException;
}
