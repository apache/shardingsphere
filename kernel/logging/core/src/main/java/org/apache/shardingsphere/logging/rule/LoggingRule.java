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

package org.apache.shardingsphere.logging.rule;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.util.OptionHelper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

/**
 * Logging rule.
 */
public final class LoggingRule implements GlobalRule {
    
    @Getter
    private final LoggingRuleConfiguration configuration;
    
    public LoggingRule(final LoggingRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        refreshLogger(ruleConfig);
    }
    
    private void refreshLogger(final LoggingRuleConfiguration ruleConfig) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        resetLoggers(loggerContext);
        configLoggers(ruleConfig, loggerContext);
        startRootLogger(loggerContext);
    }
    
    private void resetLoggers(final LoggerContext loggerContext) {
        loggerContext.getLoggerList().stream().filter(each -> Objects.nonNull(each.getLevel())).filter(each -> !Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(each.getName()))
                .forEach(each -> {
                    each.setLevel(null);
                    each.setAdditive(true);
                    each.detachAndStopAllAppenders();
                });
    }
    
    private void configLoggers(final LoggingRuleConfiguration ruleConfig, final LoggerContext loggerContext) {
        Collection<ShardingSphereLogger> loggers = ruleConfig.getLoggers();
        loggers.forEach(each -> {
            Logger logger = loggerContext.getLogger(each.getLoggerName());
            logger.setLevel(Level.valueOf(each.getLevel()));
            logger.setAdditive(each.getAdditivity());
            addAppender(logger, ruleConfig, each.getAppenderName());
        });
    }
    
    private void startRootLogger(final LoggerContext loggerContext) {
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        Iterator<Appender<ILoggingEvent>> appenderIterator = rootLogger.iteratorForAppenders();
        while (appenderIterator.hasNext()) {
            appenderIterator.next().start();
        }
    }
    
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void addAppender(final Logger logger, final LoggingRuleConfiguration ruleConfig, final String appenderName) {
        if (null == appenderName) {
            return;
        }
        Optional<ShardingSphereAppender> shardingSphereAppenderOptional = ruleConfig.getAppenders().stream().filter(each -> appenderName.equals(each.getAppenderName())).findFirst();
        if (shardingSphereAppenderOptional.isPresent()) {
            ShardingSphereAppender shardingSphereAppender = shardingSphereAppenderOptional.get();
            Appender<ILoggingEvent> appender = (Appender<ILoggingEvent>) OptionHelper.instantiateByClassName(shardingSphereAppender.getAppenderClass(), Appender.class, logger.getLoggerContext());
            appender.setContext(logger.getLoggerContext());
            appender.setName(appenderName);
            addEncoder(appender, shardingSphereAppender);
            appender.start();
            logger.addAppender(appender);
        }
    }
    
    private void addEncoder(final Appender<ILoggingEvent> appender, final ShardingSphereAppender shardingSphereAppender) {
        if (appender instanceof OutputStreamAppender) {
            OutputStreamAppender<ILoggingEvent> outputStreamAppender = (OutputStreamAppender<ILoggingEvent>) appender;
            PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
            patternLayoutEncoder.setContext(appender.getContext());
            patternLayoutEncoder.setPattern(shardingSphereAppender.getPattern());
            outputStreamAppender.setEncoder(patternLayoutEncoder);
            setFileOutput(outputStreamAppender, shardingSphereAppender);
            patternLayoutEncoder.start();
        }
    }
    
    private void setFileOutput(final OutputStreamAppender<ILoggingEvent> outputStreamAppender, final ShardingSphereAppender shardingSphereAppender) {
        if (outputStreamAppender instanceof FileAppender) {
            FileAppender<ILoggingEvent> fileAppender = (FileAppender<ILoggingEvent>) outputStreamAppender;
            fileAppender.setFile(shardingSphereAppender.getFile());
        }
    }
    
    @Override
    public String getType() {
        return LoggingRule.class.getSimpleName();
    }
}
