/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service.jpa;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Project;
import com.prodyna.pac.timetracker.server.service.ProjectServices;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import org.slf4j.Logger;

/**
 * Implementation of the {@link ProjectServices} {@code interface}.
 *
 * @author apatrikis
 */
@Local(value = ProjectServices.class)
@Stateless
public class ProjectService extends AbstractService implements ProjectServices {

    @Inject
    private Logger log;

    @Override
    public void create(Project project) {
        em.persist(project);
    }

    @Override
    public Project read(String projectID) {
        return em.find(Project.class, projectID);
    }

    @Override
    public void update(Project project) {
        em.merge(project);
    }

    @Override
    public Project delete(String projectID) {
        Project project = read(projectID);
        if (project != null) {
            em.remove(project);
        }
        return project;
    }

    @Override
    public List<Project> find(String searchPattern) {
        List<Project> allProjects;
        allProjects = em.createNamedQuery("Project.findAll", Project.class).getResultList();

        if ((searchPattern == null) || (searchPattern.length() == 0)) {
            return allProjects;
        } else {
            searchPattern = searchPattern.toLowerCase();
            List<Project> ret = new ArrayList<>();
            for (Project project : allProjects) {
                if (project.getProjectId().toLowerCase().contains(searchPattern)
                        || project.getTitle().toLowerCase().contains(searchPattern)
                        || project.getDescription().toLowerCase().contains(searchPattern)) {
                    ret.add(project);
                }
            }
            return ret;
        }
    }

    @Override
    public List<Project> findByManager(Employee manager) {
        Query q = em.createNamedQuery("Project.findByOwner", Project.class);
        q.setParameter("employee", manager);
        return q.getResultList();
    }
}
