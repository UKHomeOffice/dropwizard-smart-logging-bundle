package uk.gov.hmpo.dropwizard.smartLogging.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.EnumSet;


/**
 * Dropwizard filter to prepend header to all log messages
 */
public class PrependLogBundle implements ConfiguredBundle<PrependLogConfiguration> {

    @Override
    public void run(PrependLogConfiguration prependLogConfiguration, Environment environment) throws Exception {

        environment.servlets().addFilter("Identity logger", new Filter() {

            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
            }

            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

                HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

                MDC.put(prependLogConfiguration.getSmartLogging().useHeader,
                        httpRequest.getHeader(prependLogConfiguration.getSmartLogging().useHeader));

                filterChain.doFilter(httpRequest, servletResponse);
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
