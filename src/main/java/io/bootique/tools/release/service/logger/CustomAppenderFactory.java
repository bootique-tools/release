package io.bootique.tools.release.service.logger;

import java.lang.reflect.Constructor;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.BootiqueException;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.logback.appender.AppenderFactory;

@JsonTypeName("custom")
public class CustomAppenderFactory extends AppenderFactory {

    String className;

    @BQConfigProperty
    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public Appender<ILoggingEvent> createAppender(LoggerContext context, String defaultLogFormat) {
        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setLayout(createLayout(context, defaultLogFormat));

        Appender<ILoggingEvent> appender;
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Appender<ILoggingEvent>> appenderClass
                    = (Class<? extends Appender<ILoggingEvent>>)Class.forName(className);
            Constructor<? extends Appender<ILoggingEvent>> appenderConstructor
                    = appenderClass.getConstructor();
            appender = appenderConstructor.newInstance();
        } catch (Exception e) {
            throw new BootiqueException(-1, "Unable to initialize logback appender", e);
        }
        appender.setName(getName());
        appender.setContext(context);
        if (filters != null) {
            filters.forEach(filter -> appender.addFilter(filter.createFilter()));
        }
        appender.start();
        return appender;
    }
}
