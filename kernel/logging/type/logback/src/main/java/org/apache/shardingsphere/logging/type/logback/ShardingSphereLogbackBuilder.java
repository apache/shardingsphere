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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.spi.ShardingSphereLogBuilder;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * ShardingSphere log builder for logback.
 */
public final class ShardingSphereLogbackBuilder implements ShardingSphereLogBuilder<LoggerContext> {
    
    @Override
    public Collection<ShardingSphereLogger> getDefaultLoggers(final LoggerContext loggerContext) {
        return loggerContext.getLoggerList().stream().filter(this::isQualifiedLogger).map(each -> new ShardingSphereLogger(
                each.getName(), each.getLevel().levelStr, each.isAdditive(), each.iteratorForAppenders().hasNext() ? each.iteratorForAppenders().next().getName() : null))
                .collect(Collectors.toList());
    }
    
    private boolean isQualifiedLogger(final Logger logger) {
        return null != logger.getLevel() && !Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(logger.getName());
    }
    
    @Override
    public Collection<ShardingSphereAppender> getDefaultAppenders(final LoggerContext loggerContext) {
        return loggerContext.getLoggerList().stream().filter(this::isQualifiedLogger)
                .map(each -> each.iteratorForAppenders().hasNext() ? getShardingSphereAppender(each.iteratorForAppenders().next()) : null).filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ShardingSphereAppender::getAppenderName))));
    }
    
    private ShardingSphereAppender getShardingSphereAppender(final Appender<?> appender) {
        String file = appender instanceof FileAppender ? ((FileAppender<?>) appender).getFile() : null;
        return new ShardingSphereAppender(appender.getName(), appender.getClass().getName(), getAppenderPattern(appender), file);
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
    
    @Override
    public Class<LoggerContext> getType() {
        return LoggerContext.class;
    }
}
