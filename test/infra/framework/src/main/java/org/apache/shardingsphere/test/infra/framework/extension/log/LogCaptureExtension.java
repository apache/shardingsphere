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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.LoggerFactory;

/**
 * Log capture extension.
 */
public final class LogCaptureExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
    
    private ListAppender<ILoggingEvent> listAppender;
    
    private Logger logger;
    
    @Override
    public void beforeEach(final ExtensionContext context) {
        String logTopic = context.getRequiredTestClass().getName().substring(0, context.getRequiredTestClass().getName().lastIndexOf("Test"));
        logger = (Logger) LoggerFactory.getLogger(logTopic);
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
        return parameterContext.getParameter().getType() == LogCaptureAssertion.class;
    }
    
    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return new LogCaptureAssertion(listAppender.list);
    }
}
