/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * A {@link Provider} for intercepting {@code Request} and it's
 * {@code Response}. This is used for:
 * <ul>
 * <li>setting up the {@link MDC} for identifying the {@code Request} in all
 * created log outputs</li>
 * <li>creating log statements before and after the processing of the
 * request</li>
 * </ul>
 *
 * @author apatrikis
 * @see
 * <a href="http://www.oracle.com/technetwork/articles/java/jaxrs20-1929352.html">JEE7
 * and filter</a>
 */
@Provider
public class RequestResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // Glassfisch app server exceptions on startup when injected
    private final Logger log = LoggerFactory.getLogger(RequestResponseFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // declare UUID for identyfying subsequent calls in logs
        MDC.put("UUID", UUID.randomUUID().toString());
        log.debug("*** start request: {}", MDC.get("UUID"));

        log(requestContext);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // help identifying the processed request on the client side
        responseContext.getHeaders().add("X-UUID", MDC.get("UUID"));
        log(responseContext);

        // remove UUID info
        log.debug("*** end request: {}", MDC.get("UUID"));
        MDC.remove("UUID");
    }

    /**
     * Cretae a log output for the incomming {@code Request}.
     *
     * @param requestContext The {@code Request} context to extract log
     * informations.
     */
    private void log(ContainerRequestContext requestContext) {
        SecurityContext securityContext = requestContext.getSecurityContext();
        String authentication = securityContext.getAuthenticationScheme();
        Principal userPrincipal = securityContext.getUserPrincipal();
        UriInfo uriInfo = requestContext.getUriInfo();
        String method = requestContext.getMethod();

        log.debug("AuthenticationScheme: {}, UserPrincipal: {}, UriInfo: {}, Method: {}",
                authentication, userPrincipal, uriInfo.getMatchedResources(), method);
    }

    /**
     * Cretae a log output for the outgoing {@code Response}.
     *
     * @param responseContext The {@code Response} context to extract log
     * informations.
     */
    private void log(ContainerResponseContext responseContext) {
        MultivaluedMap<String, String> stringHeaders = responseContext.getStringHeaders();
        Object entity = responseContext.getEntity();

        log.debug("StringHeaders: {}, Entity: {}", stringHeaders, entity);
    }
}
