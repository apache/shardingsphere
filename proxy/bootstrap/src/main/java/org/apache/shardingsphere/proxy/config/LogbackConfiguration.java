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
    
    public static final String SHARDINGSPHERE_SQL_LOGGER_NAME = "ShardingSphere-SQL";
    
    public static final String HIKARI_LOGGER_NAME = "com.zaxxer.hikari";
    
    public static final String ATOMIKOS_LOGGER_NAME = "com.atomikos";
    
    public static final String NETTY_LOGGER_NAME = "io.netty";
    
    @Override
    public void configure(final LoggerContext loggerContext) {
        ConsoleAppender<ILoggingEvent> consoleAppender = createConsoleAppender(loggerContext);
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(consoleAppender);
        initBasicLogger(loggerContext);
    }
    
    private ConsoleAppender<ILoggingEvent> createConsoleAppender(final LoggerContext loggerContext) {
        ConsoleAppender<ILoggingEvent> result = new ConsoleAppender<>();
        result.setContext(loggerContext);
        result.setName("console");
        LayoutWrappingEncoder<ILoggingEvent> encoder = createEncoder(loggerContext);
        result.setEncoder(encoder);
        result.start();
        return result;
    }
    
    private LayoutWrappingEncoder<ILoggingEvent> createEncoder(final LoggerContext loggerContext) {
        LayoutWrappingEncoder<ILoggingEvent> result = new LayoutWrappingEncoder<>();
        result.setContext(loggerContext);
        PatternLayout layout = createConsolePatternLayout(loggerContext);
        result.setLayout(layout);
        return result;
    }
    
    private PatternLayout createConsolePatternLayout(final LoggerContext loggerContext) {
        PatternLayout result = new PatternLayout();
        result.setPattern(DEFAULT_PATTERN);
        result.setContext(loggerContext);
        result.start();
        return result;
    }
    
    private void initBasicLogger(final LoggerContext loggerContext) {
        loggerContext.getLogger(SHARDINGSPHERE_SQL_LOGGER_NAME).setLevel(Level.INFO);
        loggerContext.getLogger(HIKARI_LOGGER_NAME).setLevel(Level.ERROR);
        loggerContext.getLogger(ATOMIKOS_LOGGER_NAME).setLevel(Level.ERROR);
        loggerContext.getLogger(NETTY_LOGGER_NAME).setLevel(Level.ERROR);
    }
}
