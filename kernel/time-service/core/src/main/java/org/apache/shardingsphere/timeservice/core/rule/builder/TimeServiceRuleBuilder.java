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

package org.apache.shardingsphere.timeservice.core.rule.builder;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRuleBuilder;
import org.apache.shardingsphere.timeservice.api.config.TimeServiceRuleConfiguration;
import org.apache.shardingsphere.timeservice.core.rule.TimeServiceRule;
import org.apache.shardingsphere.timeservice.core.rule.constant.TimeServiceOrder;

import java.util.Map;

/**
 * Time service rule builder.
 */
public final class TimeServiceRuleBuilder implements GlobalRuleBuilder<TimeServiceRuleConfiguration> {
    
    @Override
    public TimeServiceRule build(final TimeServiceRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases, final ConfigurationProperties props) {
        return new TimeServiceRule(ruleConfig);
    }
    
    @Override
    public int getOrder() {
        return TimeServiceOrder.ORDER;
    }
    
    @Override
    public Class<TimeServiceRuleConfiguration> getTypeClass() {
        return TimeServiceRuleConfiguration.class;
    }
}
