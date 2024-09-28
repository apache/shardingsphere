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

package org.apache.shardingsphere.logging.type.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.spi.ShardingSphereLogBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereLogbackBuilderTest {
    
    private final ShardingSphereLogbackBuilder builder = (ShardingSphereLogbackBuilder) TypedSPILoader.getService(ShardingSphereLogBuilder.class, LoggerContext.class);
    
    @Test
    void assertGetDefaultLoggers() {
        Logger logger1 = mockLogger("FOO", Level.INFO, "fooAppenderName", false);
        Logger logger2 = mockLogger("BAR", Level.INFO, null, false);
        Logger logger3 = mockLogger("EMPTY", null, null, false);
        Logger logger4 = mockLogger(Logger.ROOT_LOGGER_NAME, Level.INFO, null, false);
        LoggerContext loggerContext = mock(LoggerContext.class);
        when(loggerContext.getLoggerList()).thenReturn(Arrays.asList(logger1, logger2, logger3, logger4));
        List<ShardingSphereLogger> actual = new ArrayList<>(builder.getDefaultLoggers(loggerContext));
        assertThat(actual.size(), is(2));
        assertLogger1(actual.get(0));
        assertLogger2(actual.get(1));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Logger mockLogger(final String name, final Level level, final String appenderName, final boolean isFileAppender) {
        Logger result = mock(Logger.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn(name);
        when(result.getLevel()).thenReturn(level);
        if (null != appenderName) {
            when(result.iteratorForAppenders().hasNext()).thenReturn(true);
            when(result.iteratorForAppenders().next().getName()).thenReturn(appenderName);
            if (isFileAppender) {
                FileAppender appender = mock(FileAppender.class, RETURNS_DEEP_STUBS);
                when(appender.getName()).thenReturn(appenderName);
                when(appender.getFile()).thenReturn("foo_file");
                LayoutWrappingEncoder<?> encoder = mock(LayoutWrappingEncoder.class);
                PatternLayoutBase layout = mock(PatternLayoutBase.class);
                when(layout.getPattern()).thenReturn("foo_pattern");
                when(encoder.getLayout()).thenReturn(layout);
                when(appender.getEncoder()).thenReturn(encoder);
                when(result.iteratorForAppenders().next()).thenReturn(appender);
            }
        }
        return result;
    }
    
    private void assertLogger1(final ShardingSphereLogger actual) {
        assertThat(actual.getLoggerName(), is("FOO"));
        assertThat(actual.getLevel(), is("INFO"));
        assertFalse(actual.getAdditivity());
        assertThat(actual.getAppenderName(), is("fooAppenderName"));
    }
    
    private void assertLogger2(final ShardingSphereLogger actual) {
        assertThat(actual.getLoggerName(), is("BAR"));
        assertThat(actual.getLevel(), is("INFO"));
        assertFalse(actual.getAdditivity());
        assertNull(actual.getAppenderName());
    }
    
    @Test
    void assertGetDefaultAppenders() {
        Logger logger1 = mockLogger("FOO", Level.INFO, "fooAppenderName", true);
        Logger logger2 = mockLogger("BAR", Level.INFO, "barAppenderName", false);
        Logger logger3 = mockLogger("EMPTY_APPENDER", Level.INFO, null, false);
        LoggerContext loggerContext = mock(LoggerContext.class);
        when(loggerContext.getLoggerList()).thenReturn(Arrays.asList(logger1, logger2, logger3));
        List<ShardingSphereAppender> actual = new ArrayList<>(builder.getDefaultAppenders(loggerContext));
        assertThat(actual.size(), is(2));
        assertAppender1(actual.get(0));
        assertAppender2(actual.get(1));
    }
    
    private void assertAppender1(final ShardingSphereAppender actual) {
        assertThat(actual.getAppenderName(), is("barAppenderName"));
        assertThat(actual.getPattern(), is(""));
        assertNull(actual.getFile());
    }
    
    private void assertAppender2(final ShardingSphereAppender actual) {
        assertThat(actual.getAppenderName(), is("fooAppenderName"));
        assertThat(actual.getPattern(), is("foo_pattern"));
        assertThat(actual.getFile(), is("foo_file"));
    }
}
