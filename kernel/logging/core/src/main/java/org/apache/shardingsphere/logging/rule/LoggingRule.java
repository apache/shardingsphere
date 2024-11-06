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

import lombok.Getter;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.constant.LoggingOrder;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;

import java.util.Optional;

/**
 * Logging rule.
 */
@Getter
public final class LoggingRule implements GlobalRule {
    
    private final LoggingRuleConfiguration configuration;
    
    public LoggingRule(final LoggingRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
    }
    
    /**
     * Get SQL logger.
     *
     * @return SQL logger
     */
    public Optional<ShardingSphereLogger> getSQLLogger() {
        return configuration.getLoggers().stream().filter(each -> LoggingConstants.SQL_LOG_TOPIC.equalsIgnoreCase(each.getLoggerName())).findFirst();
    }
    
    @Override
    public int getOrder() {
        return LoggingOrder.ORDER;
    }
}
