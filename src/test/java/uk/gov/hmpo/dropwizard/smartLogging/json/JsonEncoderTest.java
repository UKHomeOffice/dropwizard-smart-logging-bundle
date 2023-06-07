package uk.gov.hmpo.dropwizard.smartLogging.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmpo.dropwizard.smartLogging.bundle.LogEntryHolder;

import java.util.HashMap;

/**
 * Created by rgallet on 09/02/17.
 */
class JsonEncoderTest {

    ObjectMapper om = new ObjectMapper();

    private MockedStatic<Clock> clockMock;

    @BeforeEach
    public void setup() {
        Clock spyClock = spy(Clock.class);
        clockMock = mockStatic(Clock.class);
        clockMock.when(Clock::systemUTC).thenReturn(spyClock);
        when(spyClock.instant()).thenReturn(Instant.ofEpochMilli(1));
        when(spyClock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @AfterEach
    public void destroy() {
        clockMock.close();
    }


    @Test
    void testJsonEncoder() throws Exception {

        LogEntryHolder.setUseHeader("X-Unique-ID");

        JsonEncoder encoder = new JsonEncoder("host", "appname", "appenvironment", "apptype", "appsecurityzone");

        LogEntryHolder.setExtraFields(new HashMap<>() {
            {
                put("ExtraKey", "ExtraValue");
            }
        });
        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        Mockito.doReturn(ch.qos.logback.classic.Level.ERROR).when(event).getLevel();
        Mockito.doReturn("LoggerName").when(event).getLoggerName();
        Mockito.doReturn("ThreadName").when(event).getThreadName();
        Mockito.doReturn("Message").when(event).getMessage();
        Mockito.doReturn("Message").when(event).getFormattedMessage();
        Mockito.doReturn(new ThrowableProxy(new RuntimeException("Boom!"))).when(event).getThrowableProxy();
        Mockito.doReturn(new HashMap<String, String>() {
            {
                put("X-Unique-ID", "ROMAIN");
                put("ExtraMDCKey", "ExtraMDCValue");
            }
        }).when(event).getMDCPropertyMap();
        String output = new String(encoder.encode(event));
        JsonNode node = om.readTree(output);

        assertEquals("ExtraValue", node.findValue("ExtraKey").asText());

        assertEquals("1970-01-01T00:00:00.001", node.findValue("timestamp").asText());
        assertEquals("host", node.findValue("apphostname").asText());
        assertEquals("appname", node.findValue("appname").asText());
        assertEquals("appenvironment", node.findValue("appenvironment").asText());
        assertEquals("apptype", node.findValue("apptype").asText());
        assertEquals("appsecurityzone", node.findValue("app_securityzone").asText());

        assertEquals("Message", node.findValue("message_obj").findValue("log").asText());
        assertEquals("ExtraMDCValue", node.findValue("message_obj").findValue("extra").findValue("ExtraMDCKey").asText());

        assertEquals("ROMAIN", node.findValue("request_header_x_unique_id").asText());
        assertEquals("ERROR", node.findValue("level").asText());
        assertEquals("LoggerName", node.findValue("logger").asText());
        assertEquals("Boom!", node.findValue("message_obj").findValue("exceptionMessage").findValue("message").asText());
        assertEquals("java.lang.RuntimeException: Boom!", node.findValue("message_obj").findValue("exceptionMessage").findValue("stacktrace").get(0).asText());
    }

    @Test
    void testMessageWithPlaceholderSyntax() throws Exception {
        JsonEncoder encoder = new JsonEncoder("host", "appname", "appenvironment", "apptype", "appsecurityzone");

        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        Mockito.doReturn(ch.qos.logback.classic.Level.ERROR).when(event).getLevel();
        Mockito.doReturn("Message with {}").when(event).getMessage();
        Mockito.doReturn("Message with Placeholder").when(event).getFormattedMessage();

        String output = new String(encoder.encode(event));

        JsonNode node = om.readTree(output);

        assertEquals("Message with Placeholder", node.findValue("message_obj").findValue("log").asText());
    }

}
