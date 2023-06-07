package uk.gov.hmpo.dropwizard.smartLogging.json;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.common.FileAppenderFactory;
import io.dropwizard.logging.common.async.AsyncAppenderFactory;
import io.dropwizard.logging.common.filter.LevelFilterFactory;
import io.dropwizard.logging.common.layout.LayoutFactory;
import io.dropwizard.validation.ValidationMethod;

import java.util.Objects;

@JsonTypeName("json")
public class JsonFileAppenderFactory extends FileAppenderFactory<ILoggingEvent> {
    @JsonProperty
    private String appname, apphostname, appenvironment, apptype, appsecurityzone;

    @JsonIgnore
    @ValidationMethod(message = "must have appname")
    public boolean isValidAppname() {
        return Objects.nonNull(appname);
    }

    @JsonIgnore
    @ValidationMethod(message = "must have apphostname")
    public boolean isValidApphostname() {
        return Objects.nonNull(apphostname);
    }


    @JsonIgnore
    @ValidationMethod(message = "must have appenvironment")
    public boolean isValidAppenvironment() {
        return Objects.nonNull(appenvironment);
    }

    @JsonIgnore
    @ValidationMethod(message = "must have apptype")
    public boolean isValidApptype() {
        return Objects.nonNull(apptype);
    }

    @JsonIgnore
    @ValidationMethod(message = "must have appsecurityzone")
    public boolean isValidAppsecurityzone() {
        return Objects.nonNull(appsecurityzone);
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext loggerContext,
                          String applicationName,
                          LayoutFactory<ILoggingEvent> layoutFactory,
                          LevelFilterFactory<ILoggingEvent> levelFilterFactory,
                          AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {


        FileAppender<ILoggingEvent> appender = buildAppender(loggerContext);
        appender.setName("file-appender");
        appender.setAppend(true);
        appender.setContext(loggerContext);

        JsonEncoder jsonEncoder = new JsonEncoder(
                apphostname,
                appname,
                appenvironment,
                apptype,
                appsecurityzone
        );

        jsonEncoder.setLayout(buildLayout(loggerContext, layoutFactory));
        jsonEncoder.setContext(loggerContext);
        jsonEncoder.start();

        appender.setEncoder(jsonEncoder);
        appender.setPrudent(false);
        appender.addFilter(levelFilterFactory.build(threshold));

        getFilterFactories().stream()
                .forEach(f -> appender.addFilter(f.build()));

        appender.start();

        return wrapAsync(appender, asyncAppenderFactory);
    }

}
