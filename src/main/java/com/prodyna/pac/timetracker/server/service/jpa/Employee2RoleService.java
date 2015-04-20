/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service.jpa;

import com.prodyna.pac.timetracker.entity.Employee;
import com.prodyna.pac.timetracker.entity.Employee2Role;
import com.prodyna.pac.timetracker.entity.EmployeeRole;
import com.prodyna.pac.timetracker.server.exception.PrimaryKeyException;
import com.prodyna.pac.timetracker.server.service.Employee2RoleServices;
import java.util.Arrays;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;

@Local(value = Employee2RoleServices.class)
@Stateless
public class Employee2RoleService extends AbstractService implements Employee2RoleServices {

    @Inject
    private Logger log;

    @Override
    public void create(Employee2Role employeeRole) throws PrimaryKeyException {
        List<Employee2Role> roles = find(employeeRole.getEmployee());
        for (Employee2Role role : roles) {
            if (role.getRoleName().equals(employeeRole.getRoleName())) {
                throw new PrimaryKeyException(String.format("Assigment Employee [%S] to Role [%s] already exists",
                        employeeRole.getEmployee().toString(),
                        employeeRole.getRoleName()));
            }
        }

        em.persist(employeeRole);
    }

    @Override
    public Employee2Role read(String id) {
        return em.find(Employee2Role.class, id);
    }

    @Override
    public Employee2Role delete(String id) {
        Employee2Role employeeRole = read(id);
        if (employeeRole != null) {
            em.remove(employeeRole);
        }
        return employeeRole;
    }

    @Override
    public List<Employee2Role> find(Employee employee) {
        List<Employee2Role> employeeRoles;

        employeeRoles = em.createQuery("from Employee2Role er where er.employee = :employee")
                .setParameter("employee", employee).getResultList();
        return employeeRoles;
    }

    @Override
    public List<Employee2Role> find(EmployeeRole role) {
        List<Employee2Role> employeeRoles;

        employeeRoles = em.createQuery("from Employee2Role er where er.roleName = :role")
                .setParameter("role", role.toString()).getResultList();
        return employeeRoles;
    }

    @Override
    public List<EmployeeRole> getAllRoles() {
        return Arrays.asList(EmployeeRole.values());
    }
}
