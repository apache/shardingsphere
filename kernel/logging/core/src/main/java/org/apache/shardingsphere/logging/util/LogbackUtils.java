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

package org.apache.shardingsphere.logging.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.util.DynamicClassLoadingException;
import ch.qos.logback.core.util.IncompatibleClassException;
import ch.qos.logback.core.util.OptionHelper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

/**
 * Logback utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogbackUtils {
    
    /**
     * Refresh logger context with logging rule.
     *
     * @param loggingRuleConfiguration logging global rule configuration
     */
    public static void refreshLogger(final LoggingRuleConfiguration loggingRuleConfiguration) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        LogbackUtils.configLoggers(loggingRuleConfiguration, loggerContext);
        LogbackUtils.startRootLogger(loggerContext);
    }
    
    private static void configLoggers(final LoggingRuleConfiguration loggingRuleConfiguration, final LoggerContext loggerContext) {
        Collection<ShardingSphereLogger> loggers = loggingRuleConfiguration.getLoggers();
        loggers.forEach(each -> {
            Logger logger = loggerContext.getLogger(each.getLoggerName());
            logger.setLevel(Level.valueOf(each.getLevel()));
            logger.setAdditive(each.getAdditivity());
            LogbackUtils.addAppender(logger, loggingRuleConfiguration, each.getAppenderName());
        });
    }
    
    @SneakyThrows({IncompatibleClassException.class, DynamicClassLoadingException.class})
    @SuppressWarnings("unchecked")
    private static void addAppender(final Logger logger, final LoggingRuleConfiguration loggingRuleConfiguration, final String appenderName) {
        if (null == appenderName) {
            return;
        }
        Optional<ShardingSphereAppender> shardingSphereAppenderOptional = loggingRuleConfiguration.getAppenders().stream().filter(each -> appenderName.equals(each.getAppenderName())).findFirst();
        if (shardingSphereAppenderOptional.isPresent()) {
            ShardingSphereAppender shardingSphereAppender = shardingSphereAppenderOptional.get();
            Appender<ILoggingEvent> appender = (Appender<ILoggingEvent>) OptionHelper.instantiateByClassName(shardingSphereAppender.getAppenderClass(), Appender.class, logger.getLoggerContext());
            appender.setContext(logger.getLoggerContext());
            appender.setName(appenderName);
            LogbackUtils.addEncoder(appender, shardingSphereAppender);
            appender.start();
            logger.detachAndStopAllAppenders();
            logger.addAppender(appender);
        }
    }
    
    private static void addEncoder(final Appender<ILoggingEvent> appender, final ShardingSphereAppender shardingSphereAppender) {
        if (appender instanceof OutputStreamAppender) {
            OutputStreamAppender<ILoggingEvent> outputStreamAppender = (OutputStreamAppender<ILoggingEvent>) appender;
            PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
            patternLayoutEncoder.setContext(appender.getContext());
            patternLayoutEncoder.setPattern(shardingSphereAppender.getPattern());
            outputStreamAppender.setEncoder(patternLayoutEncoder);
            LogbackUtils.setFileOutput(outputStreamAppender, shardingSphereAppender);
            patternLayoutEncoder.start();
        }
    }
    
    private static void setFileOutput(final OutputStreamAppender<ILoggingEvent> outputStreamAppender, final ShardingSphereAppender shardingSphereAppender) {
        if (outputStreamAppender instanceof FileAppender) {
            FileAppender<ILoggingEvent> fileAppender = (FileAppender<ILoggingEvent>) outputStreamAppender;
            fileAppender.setFile(shardingSphereAppender.getFile());
        }
    }
    
    private static void startRootLogger(final LoggerContext loggerContext) {
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        Iterator<Appender<ILoggingEvent>> appenderIterator = rootLogger.iteratorForAppenders();
        while (appenderIterator.hasNext()) {
            appenderIterator.next().start();
        }
    }
}
