/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.service.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Base class to be used for all classes which provide services unsing a
 * {@link EntityManager}.
 *
 * @author apatrikis
 */
public abstract class AbstractService {

    /**
     * The {@link EntityManager} to use.
     */
    @PersistenceContext
    protected EntityManager em;
}
