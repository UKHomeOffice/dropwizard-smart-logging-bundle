package uk.gov.hmpo.dropwizard.smartLogging.json;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.validation.ValidationMethod;

import java.util.Objects;

@JsonTypeName("json")
public class JsonFileAppenderFactory extends FileAppenderFactory {
    @JsonProperty
    private String apphostname, appenvironment, apptype, appsecurityzone;

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
    public Appender build(LoggerContext loggerContext,
                          String applicationName,
                          LayoutFactory layoutFactory,
                          LevelFilterFactory levelFilterFactory,
                          AsyncAppenderFactory asyncAppenderFactory) {

        FileAppender appender = this.buildAppender(loggerContext);
        appender.setName("file-appender");
        appender.setAppend(true);
        appender.setContext(loggerContext);

        JsonEncoder jsonEncoder = new JsonEncoder(
                apphostname,
                applicationName,
                appenvironment,
                apptype,
                appsecurityzone
        );

        appender.setEncoder(jsonEncoder);

        jsonEncoder.setContext(loggerContext);
        jsonEncoder.start();

        appender.setPrudent(false);
        appender.addFilter(levelFilterFactory.build(this.threshold));

        this.getFilterFactories().stream()
                .forEach((f) -> appender.addFilter(((io.dropwizard.logging.filter.FilterFactory) f).build()));

        appender.start();

        return this.wrapAsync(appender, asyncAppenderFactory);
    }

}
