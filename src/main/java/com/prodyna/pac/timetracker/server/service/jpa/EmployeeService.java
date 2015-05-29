/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */package com.prodyna.pac.timetracker.server.service.jpa;

import com.prodyna.pac.timetracker.Security;
import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.server.service.EmployeeServices;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;

/**
 * Implementation of the {@link EmployeeServices} {@code interface}.
 *
 * @author apatrikis
 */
@Local(value = EmployeeServices.class)
@Stateless
public class EmployeeService extends AbstractService implements EmployeeServices {

    @Inject
    private Logger log;

    @Override
    public void create(Employee employee) throws NoSuchAlgorithmException {
        if (employee.getPassword() != null) {
            employee.setPassword(Security.passwordHashSHA256(employee.getPassword()));
        }
        em.persist(employee);
    }

    @Override
    public Employee read(String email) {
        return em.find(Employee.class, email);
    }

    @Override
    public void update(Employee employee) {
        em.merge(employee);
    }

    @Override
    public Employee delete(String email) {
        Employee employee = read(email);
        if (employee != null) {
            em.remove(employee);
        }
        return employee;
    }

    @Override
    public List<Employee> find(String searchPattern) {
        List<Employee> allMember;
        allMember = em.createNamedQuery("Employee.findAll", Employee.class).getResultList();

        if ((searchPattern == null) || (searchPattern.length() == 0)) {
            return allMember;
        } else {
            searchPattern = searchPattern.toLowerCase();
            List<Employee> ret = new ArrayList<>();
            for (Employee member : allMember) {
                if (member.getFirstName().toLowerCase().contains(searchPattern)
                        || member.getLastName().toLowerCase().contains(searchPattern)
                        || member.getEmail().toLowerCase().contains(searchPattern)) {
                    ret.add(member);
                }
            }
            return ret;
        }
    }
}
