package org.worldcubeassociation.dbsanitycheck.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LogUtil {

    private static final String LIST_APPENDER_NAME = "LIST_APPENDER";

    /**
     * Default log. Holds all messages during execution.
     *
     * @return Logger
     */
    public static Logger getDefaultLogger(Class<?> classType) {
        Logger log = (Logger) LoggerFactory.getLogger(classType);
        log.detachAndStopAllAppenders(); // Clear logs between calls
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.setName(LIST_APPENDER_NAME);
        listAppender.start();
        log.addAppender(listAppender);
        return log;
    }

    public static List<String> getLogs(Logger log) {
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) log.getAppender(LIST_APPENDER_NAME);
        List<ILoggingEvent> logList = listAppender.list;
        return logList.stream().map(l -> l.getMessage()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<String> getLogsContaining(Logger log, String mensagem) {
        return getLogs(log).stream().filter(l -> l.contains(mensagem))
                .collect(Collectors.toList());
    }

    public static int countLogsContaining(Logger log, String mensagem) {
        return getLogsContaining(log, mensagem).size();
    }

}