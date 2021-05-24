package io.bootique.tools.release.service.logger;

import java.lang.reflect.Constructor;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.BootiqueException;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.logback.filter.FilterFactory;

@JsonTypeName("custom")
public class CustomAppenderFilterFactory extends FilterFactory {

    String className;

    @BQConfigProperty
    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public Filter<ILoggingEvent> createFilter() {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Filter<ILoggingEvent>> filterClass
                    = (Class<? extends Filter<ILoggingEvent>>)Class.forName(className);
            Constructor<? extends Filter<ILoggingEvent>> filterConstructor
                    = filterClass.getConstructor();
            Filter<ILoggingEvent> filter = filterConstructor.newInstance();
            filter.setName("custom-filter-" + filterClass.getSimpleName());
            filter.start();
            return filter;
        } catch (Exception e) {
            throw new BootiqueException(-1, "Unable to initialize logback filter", e);
        }
    }
}
