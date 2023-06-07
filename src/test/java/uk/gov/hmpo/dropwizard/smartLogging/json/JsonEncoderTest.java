package uk.gov.hmpo.dropwizard.smartLogging.json;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmpo.dropwizard.smartLogging.bundle.LogEntryHolder;

import java.util.HashMap;

/**
 * Created by rgallet on 09/02/17.
 */
@ExtendWith(MockitoExtension.class)
public class JsonEncoderTest {

    ObjectMapper om = new ObjectMapper();

    @Test
    void testJsonEncoder() throws Exception {
//        DateTimeUtils.setCurrentMillisFixed(0);

        LogEntryHolder.setExtraFields(new HashMap<String, String>() {
            {
                put("ExtraKey", "ExtraValue");
            }
        });

        LogEntryHolder.setUseHeader("X-Unique-ID");

        JsonEncoder encoder = new JsonEncoder("host", "appname", "appenvironment", "apptype", "appsecurityzone");

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

        Assertions.assertEquals(node.findValue("ExtraKey").asText(), "ExtraValue");

        Assertions.assertEquals(node.findValue("timestamp").asText(), "1970-01-01T00:00:00.000Z");
        Assertions.assertEquals(node.findValue("apphostname").asText(), "host");
        Assertions.assertEquals(node.findValue("appname").asText(), "appname");
        Assertions.assertEquals(node.findValue("appenvironment").asText(), "appenvironment");
        Assertions.assertEquals(node.findValue("apptype").asText(), "apptype");
        Assertions.assertEquals(node.findValue("app_securityzone").asText(), "appsecurityzone");

        Assertions.assertEquals(node.findValue("message_obj").findValue("log").asText(), "Message");
        Assertions.assertEquals(node.findValue("message_obj").findValue("extra").findValue("ExtraMDCKey").asText(), "ExtraMDCValue");

        Assertions.assertEquals(node.findValue("request_header_x_unique_id").asText(), "ROMAIN");
        Assertions.assertEquals(node.findValue("level").asText(), "ERROR");
        Assertions.assertEquals(node.findValue("logger").asText(), "LoggerName");
        Assertions.assertEquals(node.findValue("message_obj").findValue("exceptionMessage").findValue("message").asText(), "Boom!");
        Assertions.assertEquals(node.findValue("message_obj").findValue("exceptionMessage").findValue("stacktrace").get(0).asText(), "java.lang.RuntimeException: Boom!");

//        DateTimeUtils.setCurrentMillisSystem();
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

        Assertions.assertEquals(node.findValue("message_obj").findValue("log").asText(), "Message with Placeholder");
    }

}
