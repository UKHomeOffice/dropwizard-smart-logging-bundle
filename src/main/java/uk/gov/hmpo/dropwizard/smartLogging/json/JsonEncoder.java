package uk.gov.hmpo.dropwizard.smartLogging.json;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmpo.dropwizard.smartLogging.bundle.LogEntryHolder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonEncoder extends LayoutWrappingEncoder<ILoggingEvent> {

    private Logger logger = LoggerFactory.getLogger(JsonEncoder.class);

    private static final byte[] RETURN_BYTES = "\n".getBytes(StandardCharsets.UTF_8);

    private String apphostname, appname, appenvironment, apptype, appsecurityzone;

    public JsonEncoder(String apphostname, String appname, String appenvironment, String apptype, String appsecurityzone) {
        this.apphostname = apphostname;
        this.appname = appname;
        this.appenvironment = appenvironment;
        this.apptype = apptype;
        this.appsecurityzone = appsecurityzone;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        try {
            return convertToBytes(event, event.getFormattedMessage());
        } catch (JsonProcessingException e) {
            logger.error("Enable to process JSON: " + e);
            return new byte[0];
        }
    }

    private byte[] convertToBytes(ILoggingEvent event, String message) throws JsonProcessingException {
        HashMap<String, Object> jsonContent = new HashMap<>(LogEntryHolder.getExtraFields());
        jsonContent.put("timestamp", new DateTime().withZone(DateTimeZone.UTC).toString(ISODateTimeFormat.dateTime()));
        jsonContent.put("apphostname", apphostname);
        jsonContent.put("appname", appname);
        jsonContent.put("appenvironment", appenvironment);
        jsonContent.put("apptype", apptype);
        jsonContent.put("app_securityzone", appsecurityzone);

        Map<String, Object> messageObj = new HashMap<>();
        messageObj.put("log", message);
        jsonContent.put("message_obj", messageObj);

        messageObj.put("level", event.getLevel().toString());
        messageObj.put("logger", event.getLoggerName());
        messageObj.put("thread", event.getThreadName());

        Map<String, Object> extra = new HashMap<>();

        addRequestId(event, jsonContent);
        addExtraKeys(event, extra);

        if (!extra.isEmpty()) {
            messageObj.put("extra", extra);
        }

        addExceptionMessage(event, messageObj);

        ObjectMapper mapper = new ObjectMapper();
        String outputString = mapper.writeValueAsString(jsonContent);
        StringBuilder sb = new StringBuilder(outputString);
        return sb.append(System.lineSeparator()).toString().getBytes(StandardCharsets.UTF_8);
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
        String useHeader = LogEntryHolder.getUseHeader();

        Optional.ofNullable(event.getMDCPropertyMap())
                .filter(m -> m.containsKey(useHeader))
                .map(m -> m.get(useHeader))
                .map(header ->
                        jsonContent.put("request_header_x_unique_id", header));
    }

    private void addExceptionMessage(ILoggingEvent event, Map<String, Object> jsonContent) {
        Optional.ofNullable(event.getThrowableProxy())
                .map(t -> {
                    Map<String, Object> exceptionMessage = new HashMap<>();
                    jsonContent.put("exceptionMessage", exceptionMessage);

                    exceptionMessage.put("message", t.getMessage());

                    if (ThrowableProxy.class.isAssignableFrom(t.getClass())) {
                        ThrowableProxy tp = (ThrowableProxy) t;
                        exceptionMessage.put("stacktrace",
                                Arrays.stream(ExceptionUtils.getRootCauseStackTrace(tp.getThrowable()))
                                        .map(s -> s.replaceAll("\\tat ", ""))
                                        .collect(Collectors.toList())
                        );
                    } else {
                        exceptionMessage.put("stacktrace",
                                Arrays.stream(t.getStackTraceElementProxyArray())
                                        .map(s -> s.getSTEAsString())
                                        .map(s -> s.replaceAll("\\tat ", ""))
                                        .collect(Collectors.toList())
                        );
                    }

                    return jsonContent;
                });
    }
}