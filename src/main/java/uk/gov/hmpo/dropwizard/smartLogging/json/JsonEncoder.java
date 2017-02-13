package uk.gov.hmpo.dropwizard.smartLogging.json;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import uk.gov.hmpo.dropwizard.smartLogging.bundle.LogEntryHolder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JsonEncoder extends PatternLayoutEncoder {

    private static final byte[] RETURN_BYTES = "\n".getBytes();

    @Override
    public void doEncode(ILoggingEvent event) throws IOException {
        outputStream.write(convertToBytes(event, event.getMessage()));
        outputStream.write(RETURN_BYTES);
        outputStream.flush();
    }

    private byte[] convertToBytes(ILoggingEvent event, String message) throws JsonProcessingException {
        HashMap<String, Object> jsonContent = new HashMap<>(LogEntryHolder.getExtraFields());
        String useHeader = LogEntryHolder.getUseHeader();
        jsonContent.put("timestamp", new DateTime().withZone(DateTimeZone.UTC).toString(ISODateTimeFormat.dateTime()));
        jsonContent.put("level", event.getLevel().toString());
        jsonContent.put("logger", event.getLoggerName());
        jsonContent.put("thread", event.getThreadName());

        Optional.ofNullable(event.getMDCPropertyMap())
                .filter(m -> m.containsKey(useHeader))
                .map(m -> m.get(useHeader))
                .map(header ->
                        jsonContent.put("request_header_x_unique_id", header));

        Map<String, Object> messageObj = new HashMap<>();
        messageObj.put("log", message);
        jsonContent.put("message_obj", messageObj);

        Map<String, Object> extra = new HashMap<>();

        addRequestId(event, jsonContent);
        addExtraKeys(event, extra);

        if (!extra.isEmpty()) {
            messageObj.put("extra", extra);
        }

        addExceptionMessage(event, messageObj);

        return new ObjectMapper().writeValueAsBytes(jsonContent);
    }

    private void addExtraKeys(ILoggingEvent event, Map<String, Object> extra) {
        Map<String, String> mdc = event.getMDCPropertyMap();

        mdc.entrySet()
                .stream()
                .filter((e) -> !e.getKey().equals(LogEntryHolder.getUseHeader()))
                .forEach((entry) ->
                        extra.put(entry.getKey(), entry.getValue())
                );
    }

    private void addRequestId(ILoggingEvent event, Map<String, Object> jsonContent) {
        Map<String, String> mdc = event.getMDCPropertyMap();

        Optional<String> os1 = Optional.ofNullable(mdc.get("X-REQ-ID"));
        Optional<String> os2 = Optional.ofNullable(mdc.get("X-Unique-ID"));
        Optional<String> sessionId = os1.map(Optional::of).orElse(os2);

        sessionId.map((s) -> jsonContent.put("sessionID", s));
    }

    private void addExceptionMessage(ILoggingEvent event, Map<String, Object> jsonContent) {
        if (event.getThrowableProxy() != null) {
            jsonContent.put("exceptionMessage", ThrowableProxyUtil.asString(event.getThrowableProxy()));
        }
    }
}