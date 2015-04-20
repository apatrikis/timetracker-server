/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service.jpa;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Project;
import com.prodyna.pac.timetracker.entity.Project2Employee;
import com.prodyna.pac.timetracker.server.exception.PrimaryKeyException;
import com.prodyna.pac.timetracker.server.service.Project2EmployeeServices;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import org.slf4j.Logger;

/**
 * Implementation of the {@link Project2EmployeeServices} {@code interface}.
 *
 * @author apatrikis
 */
@Local(value = Project2EmployeeServices.class)
@Stateless
public class Project2EmployeeService extends AbstractService implements Project2EmployeeServices {

    @Inject
    private Logger log;

    @Override
    public void create(Project2Employee projectEmployee) throws PrimaryKeyException {
        checkAllreadyAssigned(projectEmployee);
        em.persist(projectEmployee);
    }

    @Override
    public Project2Employee read(String id) {
        return em.find(Project2Employee.class, id);
    }

    @Override
    public void update(Project2Employee projectEmployee) {
        em.merge(projectEmployee);
    }

    @Override
    public Project2Employee delete(String id) {
        Project2Employee timeRecord = read(id);
        if (timeRecord != null) {
            em.remove(timeRecord);
        }
        return timeRecord;
    }

    @Override
    public List<Project> findProjects(Employee employee) {
        List<Project> projects;

        projects = em.createQuery("select pe.project from Project2Employee pe where pe.employee = :employee")
                .setParameter("employee", employee).getResultList();
        return projects;
    }

    @Override
    public List<Employee> findEmployees(Project project) {
        List<Employee> employees;

        employees = em.createQuery("select pe.employee from Project2Employee pe where pe.project = :project")
                .setParameter("project", project).getResultList();
        return employees;
    }

    @Override
    public List<Project2Employee> find(Project project, Employee employee) {
        List<Project2Employee> allTimeRecords;

        if ((project == null) && (employee == null)) {
            allTimeRecords = em.createQuery("from Project2Employee pe").getResultList();
        } else {
            ArrayList<String> whereClause = new ArrayList<>(3);
            HashMap<String, Object> parameters = new HashMap<>(3);
            if (project != null) {
                whereClause.add("pe.project= :project");
                parameters.put("project", project);
            }
            if (employee != null) {
                whereClause.add("pe.employee = :employee");
                parameters.put("employee", employee);
            }

            Query q = em.createQuery("from Project2Employee pe where " + String.join(" and ", whereClause));
            for (Map.Entry<String, Object> e : parameters.entrySet()) {
                q.setParameter(e.getKey(), e.getValue());
            }

            allTimeRecords = q.getResultList();
        }
        return allTimeRecords;
    }

    /**
     * Check if the assignment already exists.
     *
     * @param projectEmployee The entity to be created and which must not
     * already exist.
     * @throws PrimaryKeyException if the {@link Employee} is already assigned
     * to the {@link Project}.
     */
    private void checkAllreadyAssigned(Project2Employee projectEmployee) throws PrimaryKeyException {
        // check if allready assigned
        List<Project2Employee> assignments = find(projectEmployee.getProject(), projectEmployee.getEmployee());
        if ((assignments != null) && !assignments.isEmpty()) {
            throw new PrimaryKeyException(String.format("Assigment Project [%s] to Employee [%s] already exists",
                    projectEmployee.getProject().toString(),
                    projectEmployee.getEmployee().toString()));
        }
    }
}
