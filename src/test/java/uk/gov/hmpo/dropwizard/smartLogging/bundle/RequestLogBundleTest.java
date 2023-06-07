package uk.gov.hmpo.dropwizard.smartLogging.bundle;

import io.dropwizard.core.setup.Environment;
import io.dropwizard.jetty.setup.ServletEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterRegistration.Dynamic;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Created by rgallet on 01/03/17.
 */
class RequestLogBundleTest {
    PrependLogConfiguration prependLogConfiguration = () ->
            new SmartLogging() {
                {
                    requestLoggingFilter = new RequestLoggingFilter() {
                        {
                            excluded = new String[]{"^/healthcheck$"};
                        }
                    };
                }
            };

    Environment environment = Mockito.mock(Environment.class);
    ServletEnvironment servletEnvironment = Mockito.mock(ServletEnvironment.class);
    FilterChain chain = Mockito.mock(FilterChain.class);

    @BeforeEach
    void prepare() {
        Mockito.reset(environment, servletEnvironment, chain);

        Mockito.doReturn(servletEnvironment).when(environment).servlets();
    }

    @Test
    void testRegularUrl() throws Exception {
        Dynamic dynamic = Mockito.mock(Dynamic.class);
        ArgumentCaptor<Filter> argument = ArgumentCaptor.forClass(Filter.class);
        Mockito.doReturn(dynamic).when(servletEnvironment).addFilter(Mockito.anyString(), argument.capture());

        RequestLogBundle bundle = new RequestLogBundle();

        bundle.run(prependLogConfiguration, environment);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn("/some/url").when(request).getRequestURI();
        Mockito.doReturn("GET").when(request).getMethod();

        Filter requestFilter = argument.getValue();

        requestFilter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(Mockito.any(ServletRequest.class), Mockito.any(ServletResponse.class));
        Mockito.verify(response).getStatus();
    }

    @Test
    void testGETHealthCheckUrl() throws Exception {
        Dynamic dynamic = Mockito.mock(Dynamic.class);
        ArgumentCaptor<Filter> argument = ArgumentCaptor.forClass(Filter.class);
        Mockito.doReturn(dynamic).when(servletEnvironment).addFilter(Mockito.anyString(), argument.capture());

        RequestLogBundle bundle = new RequestLogBundle();

        bundle.run(prependLogConfiguration, environment);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn("/healthcheck").when(request).getRequestURI();
        Mockito.doReturn("GET").when(request).getMethod();

        Filter requestFilter = argument.getValue();

        requestFilter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(Mockito.any(ServletRequest.class), Mockito.any(ServletResponse.class));
        Mockito.verify(response, Mockito.never()).getStatus();
    }

    @Test
    void testNonGETHealthCheckUrl() throws Exception {
        Dynamic dynamic = Mockito.mock(Dynamic.class);
        ArgumentCaptor<Filter> argument = ArgumentCaptor.forClass(Filter.class);
        Mockito.doReturn(dynamic).when(servletEnvironment).addFilter(Mockito.anyString(), argument.capture());

        RequestLogBundle bundle = new RequestLogBundle();

        bundle.run(prependLogConfiguration, environment);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn("/healthcheck").when(request).getRequestURI();
        Mockito.doReturn("POST").when(request).getMethod();

        Filter requestFilter = argument.getValue();

        requestFilter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(Mockito.any(ServletRequest.class), Mockito.any(ServletResponse.class));
        Mockito.verify(response).getStatus();
    }
}
