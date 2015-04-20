/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.server.interceptor;

import com.prodyna.pac.timetracker.StopWatch;
import com.prodyna.pac.timetracker.server.rest.AbstractREST;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import org.slf4j.Logger;

/**
 * An {@code Interceptor} that is used for logging method calls:
 * <ul>
 * <li>log informations about the call, like class, method and parameters</li>
 * <li>log an successful or failed execution</li>
 * <li>log the elapsed time of an successful execution</li>
 * </ul>
 * The interceptor may be used for any class by using a {@code Annotation} at
 * class level:
 * <code>@Interceptors(InterfaceUsageLoggingInterceptor.class) public class MyClass ...</code>
 *
 * @author apatrikis
 */
public class InterfaceUsageLoggingInterceptor {

    @Resource
    private SessionContext sCtx;

    @Inject
    private Logger log;

    /**
     * {@code Intercept} any method call for a given class.
     *
     * @param iCtx The context information of the method for which the
     * interception is taking place.
     * @return The {@code Object} which was returned by the execution of the
     * intercepted method.
     * @throws Exception The exception which was generated during execution of
     * the intercepted method.
     */
    @AroundInvoke
    public Object intercept(final InvocationContext iCtx) throws Exception {
        // log method call
        log.info(createCallInfo(sCtx, iCtx));

        try {
            // exceute
            StopWatch sw = new StopWatch().start();
            Object retVal = iCtx.proceed();
            sw.stop();

            // log and monitor success
            addSuccessfulCall(iCtx, sw.getElapsedTime());
            return retVal;
        }
        catch (Exception e) {
            addFailedCall(iCtx, e);
            throw e;
        }
    }

    /**
     * Create a {@link String} that contains some interesting values to be
     * logged.
     *
     * @param sCtx The {@link SessionContext} information.
     * @param iCtx The {@link InvocationContext} information.
     * @return The {@link String} with values to log.
     */
    private String createCallInfo(final SessionContext sCtx, final InvocationContext iCtx) {
        StringBuilder sb = new StringBuilder();
        sb.append("user [").append(sCtx.getCallerPrincipal().getName())
                .append("] invoked [").append(iCtx.getTarget().toString())
                .append("] with method [").append(iCtx.getMethod().getName())
                .append("] and parameters: [");

        // build pareameters string
        if (iCtx.getParameters() == null) {
            sb.append("<null>");
        } else {
            for (Object obj : iCtx.getParameters()) {
                sb.append(obj.toString()).append(", ");
            }
            int delPos = sb.length();
            sb.delete(delPos - 2, delPos);
        }

        return sb.append("]").toString();
    }

    /**
     * Log a call that was executed successful. In case the intercepted class is
     * an instance of {@link AbstractREST}, the matching
     * {@link BusinessServiceMXBean} will be invoked, too.
     *
     * @param iCtx The {@link InvocationContext} information.
     * @param time The execution time of the intercepted method.
     */
    private void addSuccessfulCall(final InvocationContext iCtx, long time) {
        log.info("Exceution time for {} : {}ms", iCtx.getMethod().getName(), time);
        if (iCtx.getTarget() instanceof AbstractREST) {
            AbstractREST restCallObject = (AbstractREST) iCtx.getTarget();
            if (restCallObject.getMonitorBean() != null) {
                restCallObject.getMonitorBean().addSuccessfulCall(iCtx.getTarget().getClass().getSimpleName(), iCtx.getMethod().getName(), time);
            }
        }
    }

    /**
     * Log a call that was executed with an exception. In case the intercepted
     * class is an instance of {@link AbstractREST}, the matching
     * {@link BusinessServiceMXBean} will be invoked, too.
     *
     * @param iCtx The {@link InvocationContext} information.
     * @param e The exception of the intercepted method.
     */
    private void addFailedCall(final InvocationContext iCtx, Exception e) {
        log.error("Exception for {} : {}", iCtx.getMethod().getName(), e.getMessage());
        if (iCtx.getTarget() instanceof AbstractREST) {
            AbstractREST restCallObject = (AbstractREST) iCtx.getTarget();
            if (restCallObject.getMonitorBean() != null) {
                restCallObject.getMonitorBean().addFailedCall();
            }
        }
    }
}
