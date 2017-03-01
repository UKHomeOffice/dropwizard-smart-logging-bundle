package uk.gov.hmpo.dropwizard.smartLogging.bundle;

import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by rgallet on 01/03/17.
 */
public class RequestLogBundleTest {
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

    @Before
    public void prepare() {
        Mockito.reset(environment, servletEnvironment, chain);

        Mockito.doReturn(servletEnvironment).when(environment).servlets();
    }

    @Test
    public void testRegularUrl() throws Exception {
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
    public void testGETHealthCheckUrl() throws Exception {
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
    public void testNonGETHealthCheckUrl() throws Exception {
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
