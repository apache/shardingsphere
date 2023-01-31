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
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import lombok.Getter;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;

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
        Collection<ShardingSphereLogger> loggers = ruleConfig.getLoggers();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLoggerList().stream().filter(each -> Objects.nonNull(each.getLevel())).filter(each -> !Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(each.getName()))
                .forEach(each -> each.setLevel(null));
        loggers.forEach(each -> loggerContext.getLogger(each.getLoggerName()).setLevel(Level.valueOf(each.getLevel())));
    }
    
    @Override
    public String getType() {
        return LoggingRule.class.getSimpleName();
    }
}
