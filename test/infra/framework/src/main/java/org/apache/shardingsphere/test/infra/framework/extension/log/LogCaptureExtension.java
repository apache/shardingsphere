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

package org.apache.shardingsphere.test.infra.framework.extension.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.is;

/**
 * Log capture extension.
 */
public final class LogCaptureExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private ListAppender<ILoggingEvent> listAppender;
    
    private Logger logger;

    @Override
    public void beforeEach(final ExtensionContext context) {
        String targetClassName = context.getRequiredTestClass().getName().substring(0, context.getRequiredTestClass().getName().lastIndexOf("Test"));
        logger = (Logger) LoggerFactory.getLogger(targetClassName);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        if (null != logger && null != listAppender) {
            logger.detachAppender(listAppender);
        }
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == LogCaptureExtension.class;
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return this;
    }
    
    /**
     * Assert the number of captured logs.
     * 
     * @param expectedCount expected log count
     */
    public void assertLogCount(final int expectedCount) {
        MatcherAssert.assertThat(listAppender.list.size(), is(expectedCount));
    }
    
    /**
     * Assert the number of captured logs with specific level and message.
     * 
     * @param actualLogEventIndex actual log event index
     * @param expectedLevel expected log level
     * @param expectedMessage expected log message
     */
    public void assertLogContent(final int actualLogEventIndex, final Level expectedLevel, final String expectedMessage) {
        MatcherAssert.assertThat(listAppender.list.get(actualLogEventIndex).getLevel(), is(expectedLevel));
        MatcherAssert.assertThat(listAppender.list.get(actualLogEventIndex).getFormattedMessage(), is(expectedMessage));
    }
}
