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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
public final class LogbackTest {
    
    public static final String DEFAULT_PATTERN = "[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %logger{36} - %msg%n";
    
    @Test
    public void assertLogConfiguration() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("ROOT");
        assertThat(rootLogger.getLevel(), is(Level.INFO));
        assertTrue(rootLogger.getAppender("console") instanceof ConsoleAppender);
        ConsoleAppender<ILoggingEvent> consoleAppender = (ConsoleAppender<ILoggingEvent>) rootLogger.getAppender("console");
        assertTrue(consoleAppender.getEncoder() instanceof LayoutWrappingEncoder);
        LayoutWrappingEncoder<ILoggingEvent> encoder = (LayoutWrappingEncoder<ILoggingEvent>) consoleAppender.getEncoder();
        assertTrue(encoder.getLayout() instanceof PatternLayout);
        PatternLayout layout = (PatternLayout) encoder.getLayout();
        assertThat(layout.getPattern(), is(DEFAULT_PATTERN));
        assertThat(loggerContext.getLogger("org.apache.shardingsphere").getLevel(), is(Level.INFO));
        assertThat(loggerContext.getLogger("com.zaxxer.hikari").getLevel(), is(Level.ERROR));
        assertThat(loggerContext.getLogger("com.atomikos").getLevel(), is(Level.ERROR));
        assertThat(loggerContext.getLogger("io.netty").getLevel(), is(Level.ERROR));
    }
}
