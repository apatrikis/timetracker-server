package com.prodyna.pac.timetracker.server.rest;

import com.prodyna.pac.timetracker.server.interceptor.InterfaceUsageLoggingInterceptor;
import com.prodyna.pac.timetracker.server.monitoring.BusinessServiceMXBean;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import org.slf4j.Logger;

/**
 * Base class to be used for all classes which provide a {@code REST Interface}.
 * This base class defines the use of the
 * {@link InterfaceUsageLoggingInterceptor}, which means all metod calls will be
 * intercepted. In conjuction with {@link #getMonitorBean()} this can be used to
 * pass values into {@code JMX Monitoring}.
 *
 * @author apatrikis
 */
@Interceptors(InterfaceUsageLoggingInterceptor.class)
public abstract class AbstractREST {

    /**
     * The {@link Logger} to use.
     */
    @Inject
    private Logger log;

    /**
     * Use a {@link URLEncoder} for creating {@link URI} paths that contain
     * special characters, like " " (space).
     *
     * @param stringToEncode The string which may contain sepcial characters.
     * @return The {@link URLEncoder} string.
     */
    protected String getURLEncodedString(String stringToEncode) {
        String encodedString = "";

        try {
            encodedString = URLEncoder.encode(stringToEncode, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            log.error(stringToEncode, ex.getMessage());
        }

        return encodedString;
    }

    /**
     * Provide access to the matching {@link BusinessServiceMXBean} which will
     * can be used for {@code JMX} monitoring.
     *
     * @return The matching {@link BusinessServiceMXBean} instance or
     * {@code null} if no such object exists.
     */
    public abstract BusinessServiceMXBean getMonitorBean();
}
