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
import org.apache.shardingsphere.logging.constant.LoggingOrder;
import org.apache.shardingsphere.logging.rule.LoggingRule;
import org.apache.shardingsphere.logging.util.LoggingUtils;

import java.util.Map;

/**
 * Logging rule builder.
 */
public final class LoggingRuleBuilder implements GlobalRuleBuilder<LoggingRuleConfiguration> {
    
    @Override
    public LoggingRule build(final LoggingRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases, final ConfigurationProperties props) {
        LoggingUtils.syncLoggingConfig(ruleConfig, props);
        return new LoggingRule(ruleConfig);
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
