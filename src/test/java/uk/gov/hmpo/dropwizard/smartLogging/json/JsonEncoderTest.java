package uk.gov.hmpo.dropwizard.smartLogging.json;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTimeUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import uk.gov.hmpo.dropwizard.smartLogging.bundle.LogEntryHolder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by rgallet on 09/02/17.
 */
@RunWith(JUnit4.class)
public class JsonEncoderTest {

    ObjectMapper om = new ObjectMapper();

    @Test
    public void testJsonEncoder() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(0);

        OutputStream os = new ByteArrayOutputStream();

        LogEntryHolder.setExtraFields(new HashMap<String, String>(){
            {
                put("apphostname", "host");
                put("appname", "appname");
                put("appenvironment", "appenvironment");
                put("apptype", "apptype");
                put("app_securityzone", "appsecurityzone");
            }
        });

        JsonEncoder encoder = new JsonEncoder();
        encoder.init(os);

        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        Mockito.doReturn(ch.qos.logback.classic.Level.ERROR).when(event).getLevel();
        Mockito.doReturn("LoggerName").when(event).getLoggerName();
        Mockito.doReturn("ThreadName").when(event).getThreadName();
        Mockito.doReturn("Message").when(event).getMessage();
        Mockito.doReturn(new ThrowableProxy(new RuntimeException("Boom!"))).when(event).getThrowableProxy();
        Mockito.doReturn(new HashMap<String, String>() {
            {
                put("X-Unique-ID", "ROMAIN");
                put("ExtraKey", "ExtraValue");
            }
        }).when(event).getMDCPropertyMap();

        encoder.doEncode(event);

        JsonNode node = om.readTree(os.toString());

        Assert.assertEquals(node.findValue("timestamp").asText(), "1970-01-01T00:00:00.000Z");
        Assert.assertEquals(node.findValue("apphostname").asText(), "host");
        Assert.assertEquals(node.findValue("appname").asText(), "appname");
        Assert.assertEquals(node.findValue("appenvironment").asText(), "appenvironment");
        Assert.assertEquals(node.findValue("apptype").asText(), "apptype");
        Assert.assertEquals(node.findValue("app_securityzone").asText(), "appsecurityzone");

        Assert.assertEquals(node.findValue("message_obj").findValue("log").asText(), "Message");
        Assert.assertEquals(node.findValue("message_obj").findValue("extra").findValue("ExtraKey").asText(), "ExtraValue");

        Assert.assertEquals(node.findValue("level").asText(), "ERROR");
        Assert.assertEquals(node.findValue("logger").asText(), "LoggerName");
        Assert.assertEquals(node.findValue("sessionID").asText(), "ROMAIN");
        Assert.assertTrue(node.findValue("exceptionMessage").asText().contains("java.lang.RuntimeException: Boom!"));

        DateTimeUtils.setCurrentMillisSystem();
    }
}
