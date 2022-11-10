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

package org.apache.shardingsphere.traffic.rule.builder;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRuleBuilder;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.constant.TrafficOrder;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import java.util.Map;

/**
 * Traffic rule builder.
 */
public final class TrafficRuleBuilder implements GlobalRuleBuilder<TrafficRuleConfiguration> {
    
    @Override
    public GlobalRule build(final TrafficRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext,
                            final ConfigurationProperties props) {
        return new TrafficRule(ruleConfig);
    }
    
    @Override
    public int getOrder() {
        return TrafficOrder.ORDER;
    }
    
    @Override
    public Class<TrafficRuleConfiguration> getTypeClass() {
        return TrafficRuleConfiguration.class;
    }
}
