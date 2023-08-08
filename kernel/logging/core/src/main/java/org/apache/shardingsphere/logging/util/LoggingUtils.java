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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.rule.LoggingRule;

import java.util.Optional;
import java.util.Properties;

/**
 * Logging utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggingUtils {
    
    /**
     * Get ShardingSphere-SQL logger.
     *
     * @param globalRuleMetaData ShardingSphere global rule metaData
     * @return ShardingSphere-SQL logger
     */
    public static Optional<ShardingSphereLogger> getSQLLogger(final RuleMetaData globalRuleMetaData) {
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
}
