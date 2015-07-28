package com.entero.websocket

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.Context
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import ratpack.server.Service
import ratpack.server.StartEvent
import ratpack.server.StopEvent
import ratpack.stream.internal.SimpleNonConcurrentPushPublisher

public class LogFilePublisher extends SimpleNonConcurrentPushPublisher<String> implements Service {
    private PatternLayout layout
    private AppenderBase<ILoggingEvent> appender
    private ObjectMapper objectMapper

    public LogFilePublisher() {
        this.layout = new PatternLayout()
        this.layout.setContext((Context) LoggerFactory.getILoggerFactory())
        this.layout.setPattern("%5p %d [%t] %F:%L - %m%n")
        this.layout.start()
        objectMapper = new ObjectMapper()
    }

    @Override
    public void onStart(StartEvent event) throws Exception {
        println "LogFilePublisher.onStart"
        appender = new AppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                def data = [
                        timestamp: eventObject.timeStamp,
                        level: eventObject.level.levelStr,
                        message: layout.doLayout(eventObject)
                ]
                getStream().push(objectMapper.writeValueAsString(data))
            }
        }

        LoggerContext loggerContext = ((LoggerContext) LoggerFactory.getILoggerFactory())
        appender.setContext(loggerContext)
        appender.start()
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).addAppender(appender)

    }

    @Override
    public void onStop(StopEvent event) throws Exception {
        appender.stop()
    }
}