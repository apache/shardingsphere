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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRuleBuilder;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.constant.LoggingOrder;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.rule.LoggingRule;

import java.util.Collection;
import java.util.Optional;
import java.util.Properties;

/**
 * Logging rule builder.
 */
public final class LoggingRuleBuilder implements GlobalRuleBuilder<LoggingRuleConfiguration> {
    
    @Override
    public LoggingRule build(final LoggingRuleConfiguration ruleConfig, final Collection<ShardingSphereDatabase> databases, final ConfigurationProperties props) {
        syncLoggingRuleConfiguration(ruleConfig, props);
        return new LoggingRule(ruleConfig);
    }
    
    private void syncLoggingRuleConfiguration(final LoggingRuleConfiguration ruleConfig, final ConfigurationProperties props) {
        getSQLLogger(ruleConfig).ifPresent(optional -> {
            syncPropertiesToRule(optional.getProps(), props);
            syncRuleToProperties(optional.getProps(), props);
        });
    }
    
    private Optional<ShardingSphereLogger> getSQLLogger(final LoggingRuleConfiguration ruleConfig) {
        return ruleConfig.getLoggers().stream().filter(each -> LoggingConstants.SQL_LOG_TOPIC.equalsIgnoreCase(each.getLoggerName())).findFirst();
    }
    
    private void syncPropertiesToRule(final Properties loggerProps, final ConfigurationProperties props) {
        if (!loggerProps.containsKey(LoggingConstants.SQL_LOG_ENABLE) && props.getProps().containsKey(LoggingConstants.SQL_SHOW)) {
            loggerProps.setProperty(LoggingConstants.SQL_LOG_ENABLE, props.getProps().get(LoggingConstants.SQL_SHOW).toString());
        }
        if (!loggerProps.containsKey(LoggingConstants.SQL_LOG_SIMPLE) && props.getProps().containsKey(LoggingConstants.SQL_SIMPLE)) {
            loggerProps.setProperty(LoggingConstants.SQL_LOG_SIMPLE, props.getProps().get(LoggingConstants.SQL_SIMPLE).toString());
        }
    }
    
    private void syncRuleToProperties(final Properties loggerProps, final ConfigurationProperties props) {
        if (loggerProps.containsKey(LoggingConstants.SQL_LOG_ENABLE)) {
            props.getProps().setProperty(LoggingConstants.SQL_SHOW, loggerProps.get(LoggingConstants.SQL_LOG_ENABLE).toString());
        }
        if (loggerProps.containsKey(LoggingConstants.SQL_LOG_SIMPLE)) {
            props.getProps().setProperty(LoggingConstants.SQL_SIMPLE, loggerProps.get(LoggingConstants.SQL_LOG_SIMPLE).toString());
        }
    }
    
    @Override
    public int getOrder() {
        return LoggingOrder.ORDER;
    }
    
    @Override
    public Class<LoggingRuleConfiguration> getTypeClass() {
        return LoggingRuleConfiguration.class;
    }
}
