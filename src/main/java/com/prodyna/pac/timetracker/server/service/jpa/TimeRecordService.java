/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service.jpa;

import com.prodyna.pac.timetracker.entity.Project;
import com.prodyna.pac.timetracker.entity.TimeRecord;
import com.prodyna.pac.timetracker.entity.TimeRecordStatus;
import com.prodyna.pac.timetracker.pojo.TimeRecordSearch;
import com.prodyna.pac.timetracker.server.exception.EntityDataException;
import com.prodyna.pac.timetracker.server.exception.SearchParametersException;
import com.prodyna.pac.timetracker.server.service.TimeRecordServices;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import org.slf4j.Logger;

/**
 * Implementation of the {@link TimeRecordServices} {@code interface}.
 *
 * @author apatrikis
 */
@Local(value = TimeRecordServices.class)
@Stateless
public class TimeRecordService extends AbstractService implements TimeRecordServices {

    @Inject
    private Logger log;

    @Override
    public void create(TimeRecord timeRecord) throws EntityDataException, SearchParametersException {
        checkTimeRangeIsValid(timeRecord);
        checkProjectCanBeUsedForBoooking(timeRecord);
        em.persist(timeRecord);
    }

    @Override
    public TimeRecord read(String id) {
        return em.find(TimeRecord.class, id);
    }

    @Override
    public void update(TimeRecord timeRecord) throws EntityDataException, SearchParametersException {
        checkTimeRangeIsValid(timeRecord);
        checkProjectCanBeUsedForBoooking(timeRecord);
        checkUpdateIsValid(read(timeRecord.getId()), timeRecord);
        em.merge(timeRecord);
    }

    @Override
    public TimeRecord delete(String id) throws EntityDataException {
        TimeRecord timeRecord = read(id);
        if (timeRecord != null) {
            checkProjectCanBeUsedForBoooking(timeRecord);
            checkDeleteIsValid(timeRecord);
            em.remove(timeRecord);
        }
        return timeRecord;
    }

    @Override
    public List<TimeRecord> find(TimeRecordSearch searchPattern) throws SearchParametersException {
        List<TimeRecord> timeRecords;

        if ((searchPattern == null) || (searchPattern.hasValueSet() == false)) {
            timeRecords = em.createQuery("from TimeRecord tr").getResultList();
        } else {
            searchPattern.validate(); // throws exception

            ArrayList<String> whereClause = new ArrayList<>(5);
            HashMap<String, Object> parameters = new HashMap<>(5);
            if (searchPattern.hasEmployee()) {
                whereClause.add("tr.owner = :employee");
                parameters.put("employee", searchPattern.getEmployee());
            }
            if (searchPattern.hasProject()) {
                whereClause.add("tr.project = :project");
                parameters.put("project", searchPattern.getProject());
            }
            if (searchPattern.hasFrom()) {
                whereClause.add("tr.startTime >= :from");
                parameters.put("from", searchPattern.getFrom());
            }
            if (searchPattern.hasThrough()) {
                whereClause.add("tr.endTime <= :through");
                parameters.put("through", searchPattern.getThrough());
            }

            Query q = em.createQuery("from TimeRecord tr where " + String.join(" and ", whereClause));
            for (Map.Entry<String, Object> e : parameters.entrySet()) {
                q.setParameter(e.getKey(), e.getValue());
            }

            timeRecords = q.getResultList();
        }
        return timeRecords;
    }

    /**
     * Check if the {@link TimeRecord} time range is valid: this means the new
     * record does not overlap with existing entries, and is less then 24 hours.
     *
     * @param timeRecord The new {@link TimeRecord} to check.
     * @throws EntityDataException
     * @throws SearchParametersException
     */
    private void checkTimeRangeIsValid(TimeRecord timeRecord) throws EntityDataException, SearchParametersException {
        checkStateTransitionIsValid(null, timeRecord.getRecordStatus());

        // No booking can span more then 24 hours
        long bookingHours = ChronoUnit.HOURS.between(timeRecord.getStartTime().toInstant(), timeRecord.getEndTime().toInstant());
        if (bookingHours >= 24) {
            String message = String.format("TimeRecord [%s] spans more then 24 hours", timeRecord.toString());
            log.info(message);
            throw new EntityDataException(message);
        }

        // select a bunch of time entries arround the new timeRecord
        TimeRecordSearch searchPattern = new TimeRecordSearch();
        searchPattern.setProject(timeRecord.getProject());
        searchPattern.setEmployee(timeRecord.getOwner());
        searchPattern.setFrom(Date.from(timeRecord.getStartTime().toInstant().minus(2, ChronoUnit.DAYS)));
        searchPattern.setThrough(Date.from(timeRecord.getStartTime().toInstant().plus(2, ChronoUnit.DAYS)));
        List<TimeRecord> timeRecords = find(searchPattern);

        for (TimeRecord current : timeRecords) {
            // in case of update: skip the database record itself
            if (current.getId().equals(timeRecord.getId())) {
                // ok, continue
            } // check 1: new record before current record
            else if (timeRecord.getEndTime().before(current.getStartTime())) {
                // ok, continue
            } // check 2: new record after current record
            else if (timeRecord.getStartTime().after(current.getEndTime())) {
                // ok, continue
            } // overlap
            else {
                String message = String.format("TimeRecord [%s] overlaps with existing entry: [%s]", timeRecord.toString(), current.toString());
                log.info(message);
                throw new EntityDataException(message);
            }
        }
    }

    /**
     * Check if updating the {@link TimeRecord} is valid. Only a
     * {@link TimeRecord} with status {@link TimeRecordStatus#EDITING} may
     * change values.
     *
     * @param currentRecord The current record with the current values.
     * @param newRecord The nes record with the new values, which may only
     * change for a {@link TimeRecordStatus#EDITING} status.
     * @throws EntityDataException
     */
    private void checkUpdateIsValid(TimeRecord currentRecord, TimeRecord newRecord) throws EntityDataException {
        checkStateTransitionIsValid(currentRecord.getRecordStatus(), newRecord.getRecordStatus());

        // values may change only for a EDITING records
        boolean hasChanges = !checkFieldsAreUnchanged(currentRecord, newRecord);
        if (!TimeRecordStatus.EDITING.equals(newRecord.getRecordStatus())
                && hasChanges) {
            String message = String.format("Only a EDITING TimeRecord may be updated: [%s]", currentRecord.toString());
            log.info(message);
            throw new EntityDataException(message);
        }
    }

    /**
     * Check if deleting the {@link TimeRecord} is valid.
     *
     * @param timeRecord The {@link TimeRecord} to check, which is only allowed
     * if the record is not {@link TimeRecordStatus#BOOKED}.
     * @throws EntityDataException
     */
    private void checkDeleteIsValid(TimeRecord timeRecord) throws EntityDataException {
        if (TimeRecordStatus.BOOKED.equals(timeRecord.getRecordStatus())) {
            String message = String.format("A BOOKED TimeRecord cannot be deleted: [%s]", timeRecord.toString());
            log.info(message);
            throw new EntityDataException(message);
        }
    }

    /**
     * Check if the {@link TimeRecord} state transition is valid.
     *
     * @param currentState The current state.
     * @param newState The new state.
     * @throws EntityDataException
     */
    private void checkStateTransitionIsValid(TimeRecordStatus currentState, TimeRecordStatus newState) throws EntityDataException {
        if (currentState == null) {
            if (!TimeRecordStatus.EDITING.equals(newState) && !TimeRecordStatus.READY_FOR_APPROVAL.equals(newState)) {
                String message = String.format("Initial state must be EDITING or READY_FOR_APPROVAL, not [%s]", newState.name());
                log.info(message);
                throw new EntityDataException(message);
            }
        } else if (TimeRecordStatus.WORKFLOW_TRANSITIONS[currentState.ordinal()][newState.ordinal()] == false) {
            String message = String.format("Inalid state transiton: from [%s] to [%s]", currentState.name(), newState.name());
            log.info(message);
            throw new EntityDataException(message);
        }
    }

    /**
     * Check if the referenced {@link Project} can be used for a booking:
     * <ul>
     * <li>the project is not locked</li>
     * <li>the {@link TimeRecord} is within the {@link Project} date range</li>
     * </ul>
     *
     * @param timeRecord The {@link TimeRecord} to check.
     * @throws EntityDataException
     */
    private void checkProjectCanBeUsedForBoooking(TimeRecord timeRecord) throws EntityDataException {
        // multi user environment: ensure latest availble project data
        timeRecord.setProject(em.find(Project.class, timeRecord.getProject().getProjectId()));

        if (timeRecord.getProject().isLocked()) {
            String message = "The project is locked, no TimeRecord actions allowed.";
            log.info(message);
            throw new EntityDataException(message);
        }

        if (timeRecord.getStartTime().before(timeRecord.getProject().getStartDate())
                || timeRecord.getEndTime().after(timeRecord.getProject().getEndDate())) {
            String message = String.format("The TimeRecord is not within the project's date range.");
            log.info(message);
            throw new EntityDataException(message);
        }
    }

    /**
     * Check if the {@link TimeRecord} values have changed. This check exclueds
     * the {@link TimeRecordStatus} which must change for state transitions.
     *
     * @param currentRecord The current {@link TimeRecord}.
     * @param newRecord The new {@link TimeRecord}.
     * @return
     */
    private boolean checkFieldsAreUnchanged(TimeRecord currentRecord, TimeRecord newRecord) {
        return (currentRecord.getOwner().equals(newRecord.getOwner())
                && currentRecord.getProject().equals(newRecord.getProject())
                && currentRecord.getStartTime().equals(newRecord.getStartTime())
                && currentRecord.getEndTime().equals(newRecord.getEndTime())
                && (currentRecord.getPauseMinutes() == newRecord.getPauseMinutes()));
    }
}
