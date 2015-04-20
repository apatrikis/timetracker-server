/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Producer} of a {@link Logger}. This may be used in any class for
 * injecting a {@link Logger}. <code>@Inject private Logger log;</code>
 *
 * @author apatrikis
 */
@Dependent
public class LoggerProducer {

    /**
     * Crate a {@link Logger} for the {@code class} provided in the
     * {@link InjectionPoint}.
     *
     * @param ip The {@link InjectionPoint} containing the class that requests a
     * {@link Logger}.
     * @return The cretaed {@link Logger}.
     */
    @Produces
    public Logger createLogger(InjectionPoint ip) {
        return LoggerFactory.getLogger(ip.getMember().getDeclaringClass());
    }
}
