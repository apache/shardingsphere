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

package org.apache.shardingsphere.proxy.config;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

public class LogbackConfiguration extends BasicConfigurator {
    
    public static final String DEFAULT_PATTERN = "[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %logger{36} - %msg%n";
    
    public static final String SHARDINGSPHERE_LOGGER_NAME = "org.apache.shardingsphere";
    
    public static final String HIKARI_LOGGER_NAME = "com.zaxxer.hikari";
    
    public static final String ATOMIKOS_LOGGER_NAME = "com.atomikos";
    
    public static final String NETTY_LOGGER_NAME = "io.netty";
    
    @Override
    public void configure(final LoggerContext loggerContext) {
        ConsoleAppender<ILoggingEvent> consoleAppender = createConsoleAppender(loggerContext);
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(consoleAppender);
        Logger shardingsphereLogger = loggerContext.getLogger(SHARDINGSPHERE_LOGGER_NAME);
        shardingsphereLogger.setLevel(Level.INFO);
        shardingsphereLogger.setAdditive(false);
        shardingsphereLogger.addAppender(consoleAppender);
        initBasicLogger(loggerContext);
    }
    
    private ConsoleAppender<ILoggingEvent> createConsoleAppender(final LoggerContext loggerContext) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName("console");
        LayoutWrappingEncoder<ILoggingEvent> encoder = createEncoder(loggerContext);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        return consoleAppender;
    }
    
    private LayoutWrappingEncoder<ILoggingEvent> createEncoder(final LoggerContext loggerContext) {
        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(loggerContext);
        PatternLayout layout = createConsolePatternLayout(loggerContext);
        encoder.setLayout(layout);
        return encoder;
    }
    
    private PatternLayout createConsolePatternLayout(final LoggerContext loggerContext) {
        PatternLayout layout = new PatternLayout();
        layout.setPattern(DEFAULT_PATTERN);
        layout.setContext(loggerContext);
        layout.start();
        return layout;
    }
    
    private void initBasicLogger(final LoggerContext loggerContext) {
        loggerContext.getLogger(HIKARI_LOGGER_NAME).setLevel(Level.ERROR);
        loggerContext.getLogger(ATOMIKOS_LOGGER_NAME).setLevel(Level.ERROR);
        loggerContext.getLogger(NETTY_LOGGER_NAME).setLevel(Level.ERROR);
    }
}
