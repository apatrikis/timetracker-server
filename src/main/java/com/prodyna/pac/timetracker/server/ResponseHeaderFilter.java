/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * A {@link Provider} for intercepting an {@code Response}. This is used for
 * setting important response headers.
 *
 * @author apatrikis
 */
@Provider
public class ResponseHeaderFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext response) {
        setCORSHeader(requestContext, response);
    }

    /**
     * Set {@code CORS} header to enable client/server communication.
     *
     * @see
     * <a href=http://de.wikipedia.org/wiki/Cross-Origin_Resource_Sharing">CORS</a>
     */
    private void setCORSHeader(ContainerRequestContext requestContext, ContainerResponseContext response) {
        // for access by the Angular JS frontend:
        // http://stackoverflow.com/questions/10143093/origin-is-not-allowed-by-access-control-allow-origin
        response.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        response.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.getHeaders().putSingle("Access-Control-Allow-Headers", "Authorization, Content-Type");
    }
}
