/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.fixture.log;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

import java.io.File;

/**
 * Logback configuration.
 */
public final class LogbackConfigurationFixture extends BasicConfigurator {
    
    public static final String DEFAULT_PATTERN = "[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %logger{36} - %msg%n";
    
    public static final String SHARDINGSPHERE_LOGGER_NAME = "org.apache.shardingsphere.agent";
    
    @Override
    public void configure(final LoggerContext loggerContext) {
        Appender<ILoggingEvent> appender = createFileAppender(loggerContext);
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(appender);
        Logger logger = loggerContext.getLogger(SHARDINGSPHERE_LOGGER_NAME);
        logger.setLevel(Level.INFO);
        logger.setAdditive(false);
        logger.addAppender(appender);
    }
    
    private FileAppender<ILoggingEvent> createFileAppender(final LoggerContext loggerContext) {
        FileAppender<ILoggingEvent> result = new FileAppender<>();
        result.setContext(loggerContext);
        result.setName("fileAppender");
        result.setFile(getLogFile());
        LayoutWrappingEncoder<ILoggingEvent> encoder = createEncoder(loggerContext);
        result.setEncoder(encoder);
        result.start();
        return result;
    }
    
    private String getLogFile() {
        return String.join(File.separator, "target", "logs", "stdout.log");
    }
    
    private LayoutWrappingEncoder<ILoggingEvent> createEncoder(final LoggerContext loggerContext) {
        LayoutWrappingEncoder<ILoggingEvent> result = new LayoutWrappingEncoder<>();
        result.setContext(loggerContext);
        PatternLayout layout = createPatternLayout(loggerContext);
        result.setLayout(layout);
        return result;
    }
    
    private PatternLayout createPatternLayout(final LoggerContext loggerContext) {
        PatternLayout result = new PatternLayout();
        result.setPattern(DEFAULT_PATTERN);
        result.setContext(loggerContext);
        result.start();
        return result;
    }
}
