package uk.gov.hmpo.dropwizard.smartLogging.bundle;

import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.slf4j.MDC;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;


/**
 * Dropwizard filter to prepend header to all log messages
 */
public class PrependLogBundle implements ConfiguredBundle<PrependLogConfiguration> {

    @Override
    public void run(PrependLogConfiguration prependLogConfiguration, Environment environment) throws Exception {

        environment.servlets().addFilter("Identity logger", new Filter() {

            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
                LogEntryHolder.setExtraFields(prependLogConfiguration.getSmartLogging().extraFields);
                LogEntryHolder.setUseHeader(prependLogConfiguration.getSmartLogging().useHeader);
            }

            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain
                    filterChain) throws IOException, ServletException {

                HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

                String useHeader = prependLogConfiguration.getSmartLogging().useHeader;

                Optional<String> headerValue = Optional.ofNullable(httpRequest.getHeader(useHeader));

                MDC.put(useHeader, headerValue.orElseGet(() -> UUID.randomUUID().toString()));

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
