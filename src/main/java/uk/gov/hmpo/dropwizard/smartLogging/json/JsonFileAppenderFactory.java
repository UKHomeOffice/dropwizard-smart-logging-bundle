package uk.gov.hmpo.dropwizard.smartLogging.json;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;

@JsonTypeName("json")
public class JsonFileAppenderFactory extends FileAppenderFactory {

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

        JsonEncoder jsonEncoder = new JsonEncoder();

        appender.setEncoder(jsonEncoder);
        jsonEncoder.start();

        appender.setPrudent(false);
        appender.addFilter(levelFilterFactory.build(this.threshold));
        this.getFilterFactories().stream().forEach((f) -> {
            appender.addFilter(((io.dropwizard.logging.filter.FilterFactory) f).build());
        });
        appender.start();
        return this.wrapAsync(appender, asyncAppenderFactory);
    }

}
