package uk.gov.hmpo.dropwizard.smartLogging.json;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmpo.dropwizard.smartLogging.bundle.LogEntryHolder;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JsonEncoder extends PatternLayoutEncoder {

    private static final byte[] RETURN_BYTES = "\n".getBytes();

    @Override
    public void doEncode(ILoggingEvent event) throws IOException {
        outputStream.write(convertToBytes(event, event.getMessage()));
        outputStream.write(RETURN_BYTES);
        outputStream.flush();
    }

    private byte[] convertToBytes(ILoggingEvent event, String message) throws JsonProcessingException {
        Map<String, String> mdc = event.getMDCPropertyMap();

        String sessionId = mdc.get("X-REQ-ID");

        HashMap<String, Object> jsonContent = new HashMap<>(LogEntryHolder.getExtraFields());
        jsonContent.put("timestamp", new Date());
        jsonContent.put("message", message);
        jsonContent.put("level", event.getLevel().toString());
        jsonContent.put("sessionID", sessionId);
        jsonContent.put("logger", event.getLoggerName());
        jsonContent.put("thread", event.getThreadName());
        addExceptionMessage(event, jsonContent);

        return new ObjectMapper().writeValueAsBytes(jsonContent);
    }

    private void addExceptionMessage(ILoggingEvent event, Map<String, Object> jsonContent) {
        if (event.getThrowableProxy() != null) {
            jsonContent.put("exceptionMessage", ThrowableProxyUtil.asString(event.getThrowableProxy()));
        }
    }
}