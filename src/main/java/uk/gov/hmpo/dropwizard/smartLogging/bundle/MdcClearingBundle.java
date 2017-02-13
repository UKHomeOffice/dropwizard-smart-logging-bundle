package uk.gov.hmpo.dropwizard.smartLogging.bundle;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Dropwizard filter to log all requests
 */
public class MdcClearingBundle implements Bundle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(Environment environment) {

        environment.servlets().addFilter("MDC clearing filter", new Filter() {

            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
            }

            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain
                    filterChain) throws IOException, ServletException {

                MDC.clear();
                logger.trace("Cleared MDC");

                filterChain.doFilter(servletRequest, servletResponse);

            }

            @Override
            public void destroy() {
            }
        }).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }
}
