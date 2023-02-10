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

package org.apache.shardingsphere.logging.utils;

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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.rule.LoggingRule;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;

/**
 * Logging utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggingUtils {
    
    /**
     * Get ShardingSphere-SQL logger.
     *
     * @param globalRuleMetaData ShardingSphere global rule metaData
     * @return ShardingSphere-SQL logger
     */
    public static Optional<ShardingSphereLogger> getSQLLogger(final ShardingSphereRuleMetaData globalRuleMetaData) {
        return globalRuleMetaData.findSingleRule(LoggingRule.class).isPresent() ? getSQLLogger(globalRuleMetaData.getSingleRule(LoggingRule.class).getConfiguration()) : Optional.empty();
    }
    
    /**
     * Get ShardingSphere-SQL logger.
     *
     * @param loggingRuleConfiguration logging global rule configuration
     * @return ShardingSphere-SQL logger
     */
    public static Optional<ShardingSphereLogger> getSQLLogger(final LoggingRuleConfiguration loggingRuleConfiguration) {
        return loggingRuleConfiguration.getLoggers().stream()
                .filter(each -> LoggingConstants.SQL_LOG_TOPIC.equalsIgnoreCase(each.getLoggerName())).findFirst();
    }
    
    /**
     * Synchronize the log-related configuration in logging rule and props.
     * Use the configuration in the logging rule first.
     *
     * @param loggingRuleConfiguration logging global rule configuration
     * @param props configuration properties
     */
    public static void syncLoggingConfig(final LoggingRuleConfiguration loggingRuleConfiguration, final ConfigurationProperties props) {
        LoggingUtils.getSQLLogger(loggingRuleConfiguration).ifPresent(option -> {
            Properties loggerProperties = option.getProps();
            syncPropsToLoggingRule(loggerProperties, props);
            syncLoggingRuleToProps(loggerProperties, props);
        });
    }
    
    private static void syncPropsToLoggingRule(final Properties loggerProperties, final ConfigurationProperties props) {
        if (!loggerProperties.containsKey(LoggingConstants.SQL_LOG_ENABLE) && props.getProps().containsKey(LoggingConstants.SQL_SHOW)) {
            loggerProperties.setProperty(LoggingConstants.SQL_LOG_ENABLE, props.getProps().get(LoggingConstants.SQL_SHOW).toString());
        }
        if (!loggerProperties.containsKey(LoggingConstants.SQL_LOG_SIMPLE) && props.getProps().containsKey(LoggingConstants.SQL_SIMPLE)) {
            loggerProperties.setProperty(LoggingConstants.SQL_LOG_SIMPLE, props.getProps().get(LoggingConstants.SQL_SIMPLE).toString());
        }
    }
    
    private static void syncLoggingRuleToProps(final Properties loggerProperties, final ConfigurationProperties props) {
        if (loggerProperties.containsKey(LoggingConstants.SQL_LOG_ENABLE)) {
            props.getProps().setProperty(LoggingConstants.SQL_SHOW, loggerProperties.get(LoggingConstants.SQL_LOG_ENABLE).toString());
        }
        if (loggerProperties.containsKey(LoggingConstants.SQL_LOG_SIMPLE)) {
            props.getProps().setProperty(LoggingConstants.SQL_SIMPLE, loggerProperties.get(LoggingConstants.SQL_LOG_SIMPLE).toString());
        }
    }
    
    /**
     * Refresh logger context with logging rule.
     *
     * @param loggingRuleConfiguration logging global rule configuration
     */
    public static void refreshLogger(final LoggingRuleConfiguration loggingRuleConfiguration) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        configLoggers(loggingRuleConfiguration, loggerContext);
        startRootLogger(loggerContext);
    }
    
    private static void configLoggers(final LoggingRuleConfiguration loggingRuleConfiguration, final LoggerContext loggerContext) {
        Collection<ShardingSphereLogger> loggers = loggingRuleConfiguration.getLoggers();
        loggers.forEach(each -> {
            Logger logger = loggerContext.getLogger(each.getLoggerName());
            logger.setLevel(Level.valueOf(each.getLevel()));
            logger.setAdditive(each.getAdditivity());
            addAppender(logger, loggingRuleConfiguration, each.getAppenderName());
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
            addEncoder(appender, shardingSphereAppender);
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
            setFileOutput(outputStreamAppender, shardingSphereAppender);
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
