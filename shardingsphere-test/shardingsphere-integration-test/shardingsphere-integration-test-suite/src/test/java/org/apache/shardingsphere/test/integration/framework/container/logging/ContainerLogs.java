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

package org.apache.shardingsphere.test.integration.framework.container.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.BaseConsumer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

/**
 * Container logs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContainerLogs {
    
    /**
     * Create new log consumer.
     *
     * @param serviceName service name
     * @return log consumer
     */
    public static BaseConsumer<?> newConsumer(final String serviceName) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(serviceName);
        logger.addAppender(createLogAppender(context));
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false);
        Slf4jLogConsumer result = new Slf4jLogConsumer(logger, true);
        result.withPrefix(serviceName);
        return result;
    }
    
    private static ConsoleAppender<ILoggingEvent> createLogAppender(final LoggerContext context) {
        ConsoleAppender<ILoggingEvent> result = new ConsoleAppender<>();
        result.setEncoder(createPatternLayoutEncoder(context));
        result.start();
        return result;
    }
    
    private static PatternLayoutEncoder createPatternLayoutEncoder(final LoggerContext context) {
        PatternLayoutEncoder result = new PatternLayoutEncoder();
        result.setContext(context);
        result.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %msg%n");
        result.start();
        return result;
    }
}
