package com.scaledrop.sddownload.utilities

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory

class LoggerWatcher {

  static ListAppender<ILoggingEvent> logsAppenderFor(Class<?> clazz) {
    Logger logger = (Logger) LoggerFactory.getLogger(clazz)

    def appender = new ListAppender<ILoggingEvent>()
    appender.start()

    logger.addAppender(appender)
    logger.setAdditive(false)

    return appender
  }

  static void detach(Class<?> clazz, ListAppender<?> appender) {
    Logger logger = (Logger) LoggerFactory.getLogger(clazz)
    logger.detachAppender(appender)
  }
}
