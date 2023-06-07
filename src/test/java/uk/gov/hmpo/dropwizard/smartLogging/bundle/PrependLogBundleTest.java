package uk.gov.hmpo.dropwizard.smartLogging.bundle;

import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.core.setup.Environment;
import org.apache.commons.lang3.StringUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.MDC;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.*;

class PrependLogBundleTest {
    private final String expectedHeaderName = "expected-header-name";
    private final SmartLogging smartLoggingConfig = new SmartLogging();
    private final PrependLogConfiguration prependLogConfig = () -> smartLoggingConfig;

    private Filter subject;

    private final FilterChain mockFilterChain = mock(FilterChain.class);
    private final HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    private final ServletResponse mockResponse = mock(ServletResponse.class);
    private final FilterRegistration.Dynamic dynamic = Mockito.mock(FilterRegistration.Dynamic.class);

    @BeforeEach
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

    @AfterEach
    public void cleanup() {
        MDC.clear();
    }

    @Test
    void run_registerForAllUrlPatterns() {
        verify(dynamic).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    @Test
    void init_registerConfiguredExtraFields() throws ServletException {
        Map<String,String> extraFields = new HashMap<>();
        smartLoggingConfig.setExtraFields(extraFields);

        subject.init(null);

        assertThat(LogEntryHolder.getExtraFields(), sameInstance(extraFields));
    }

    @Test
    void init_registerTheHeaderName() throws ServletException {
        subject.init(null);

        assertThat(LogEntryHolder.getUseHeader(), equalTo(expectedHeaderName));
    }

    @Test
    void doFilter_addValueFromHeaderToMdcIfPresent() throws IOException, ServletException {
        String expectedValue = UUID.randomUUID().toString();
        doReturn(expectedValue).when(mockRequest).getHeader(expectedHeaderName);

        // Asserting that the MDC value has been set by the time the filter chain is invoked
        doAnswer(ignored -> {assertThat(MDC.get(expectedHeaderName), equalTo(expectedValue)); return null;})
                .when(mockFilterChain).doFilter(any(), any());

        subject.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    void doFilter_addGeneratedValueToMdcWhenHeaderNotPresent() throws IOException, ServletException {
        doReturn(null).when(mockRequest).getHeader(expectedHeaderName);

        // Asserting that the MDC value has been set by the time the filter chain is invoked
        doAnswer(ignored -> {assertTrue(StringUtils.isNotEmpty(MDC.get(expectedHeaderName))); return null;})
                .when(mockFilterChain).doFilter(any(), any());

        subject.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }
}
