package uk.gov.hmpo.dropwizard.smartLogging.bundle;

import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Environment;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class PrependLogBundleTest {
    private final String expectedHeaderName = "expected-header-name";
    private final SmartLogging smartLoggingConfig = new SmartLogging();
    private final PrependLogConfiguration prependLogConfig = () -> smartLoggingConfig;

    private Filter subject;

    private FilterChain mockFilterChain = mock(FilterChain.class);
    private HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    private ServletResponse mockResponse = mock(ServletResponse.class);
    private FilterRegistration.Dynamic dynamic = Mockito.mock(FilterRegistration.Dynamic.class);

    @Before
    public void setup() throws Exception {
        MDC.clear();

        smartLoggingConfig.setUseHeader(expectedHeaderName);

        Environment environment = Mockito.mock(Environment.class);
        ServletEnvironment servletEnvironment = Mockito.mock(ServletEnvironment.class);
        ArgumentCaptor<Filter> filterCaptor = ArgumentCaptor.forClass(Filter.class);

        doReturn(servletEnvironment).when(environment).servlets();
        doReturn(dynamic).when(servletEnvironment).addFilter(Mockito.anyString(), filterCaptor.capture());

        new PrependLogBundle().run(prependLogConfig, environment);

        subject = filterCaptor.getValue();
    }

    @After
    public void cleanup() {
        MDC.clear();
    }

    @Test
    public void run_registerForAllUrlPatterns() {
        verify(dynamic).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    @Test
    public void init_registerConfiguredExtraFields() throws ServletException {
        Map<String,String> extraFields = new HashMap<>();
        smartLoggingConfig.setExtraFields(extraFields);

        subject.init(null);

        assertThat(LogEntryHolder.getExtraFields(), sameInstance(extraFields));
    }

    @Test
    public void init_registerTheHeaderName() throws ServletException {
        subject.init(null);

        assertThat(LogEntryHolder.getUseHeader(), equalTo(expectedHeaderName));
    }

    @Test
    public void doFilter_addValueFromHeaderToMdcIfPresent() throws IOException, ServletException {
        String expectedValue = UUID.randomUUID().toString();
        doReturn(expectedValue).when(mockRequest).getHeader(expectedHeaderName);

        // Asserting that the MDC value has been set by the time the filter chain is invoked
        doAnswer(ignored -> {assertThat(MDC.get(expectedHeaderName), equalTo(expectedValue)); return null;})
                .when(mockFilterChain).doFilter(any(), any());

        subject.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void doFilter_addGeneratedValueToMdcWhenHeaderNotPresent() throws IOException, ServletException {
        doReturn(null).when(mockRequest).getHeader(expectedHeaderName);

        // Asserting that the MDC value has been set by the time the filter chain is invoked
        doAnswer(ignored -> {assertTrue(StringUtils.isNotEmpty(MDC.get(expectedHeaderName))); return null;})
                .when(mockFilterChain).doFilter(any(), any());

        subject.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }
}