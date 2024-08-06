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

package org.apache.shardingsphere.logging.rule.builder;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import org.apache.shardingsphere.infra.rule.builder.global.DefaultGlobalRuleConfigurationBuilder;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.constant.LoggingOrder;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Default logging rule configuration builder.
 */
public final class DefaultLoggingRuleConfigurationBuilder implements DefaultGlobalRuleConfigurationBuilder<LoggingRuleConfiguration, LoggingRuleBuilder> {
    
    @Override
    public LoggingRuleConfiguration build() {
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        if ("ch.qos.logback.classic.LoggerContext".equals(iLoggerFactory.getClass().getName())) {
            LoggerContext loggerContext = (LoggerContext) iLoggerFactory;
            return new LoggingRuleConfiguration(getDefaultLoggers(loggerContext), getDefaultAppenders(loggerContext));
        }
        return new LoggingRuleConfiguration(Collections.emptyList(), Collections.emptySet());
    }
    
    private Collection<ShardingSphereLogger> getDefaultLoggers(final LoggerContext loggerContext) {
        return loggerContext.getLoggerList().stream().filter(each -> null != each.getLevel()).filter(each -> !Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(each.getName()))
                .map(each -> new ShardingSphereLogger(each.getName(), each.getLevel().levelStr, each.isAdditive(),
                        each.iteratorForAppenders().hasNext() ? each.iteratorForAppenders().next().getName() : null))
                .collect(Collectors.toList());
    }
    
    private Collection<ShardingSphereAppender> getDefaultAppenders(final LoggerContext loggerContext) {
        return loggerContext.getLoggerList().stream().filter(each -> null != each.getLevel()).filter(each -> !Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(each.getName())).map(each -> {
            if (each.iteratorForAppenders().hasNext()) {
                Appender<?> appender = each.iteratorForAppenders().next();
                ShardingSphereAppender shardingSphereAppender = new ShardingSphereAppender(appender.getName(), appender.getClass().getName(), getAppenderPattern(appender));
                getFileOutput(appender, shardingSphereAppender);
                return shardingSphereAppender;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ShardingSphereAppender::getAppenderName))));
    }
    
    private String getAppenderPattern(final Appender<?> appender) {
        if (appender instanceof OutputStreamAppender) {
            OutputStreamAppender<?> consoleAppender = (OutputStreamAppender<?>) appender;
            LayoutWrappingEncoder<?> encoder = (LayoutWrappingEncoder<?>) consoleAppender.getEncoder();
            PatternLayoutBase<?> layout = (PatternLayoutBase<?>) encoder.getLayout();
            return layout.getPattern();
        }
        return "";
    }
    
    private void getFileOutput(final Appender<?> appender, final ShardingSphereAppender shardingSphereAppender) {
        if (appender instanceof FileAppender) {
            shardingSphereAppender.setFile(((FileAppender<?>) appender).getFile());
        }
    }
    
    @Override
    public int getOrder() {
        return LoggingOrder.ORDER;
    }
    
    @Override
    public Class<LoggingRuleBuilder> getTypeClass() {
        return LoggingRuleBuilder.class;
    }
}
