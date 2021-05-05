package io.bootique.tools.release.service.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class FileLoggerFilter extends Filter<ILoggingEvent> {

    private static final String threadToLog = "ForkJoinPool";

    public FileLoggerFilter() {
    }

    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        if (iLoggingEvent.getThreadName().contains(threadToLog)) {
            return FilterReply.ACCEPT;
        } else {
            return FilterReply.DENY;
        }
    }
}
