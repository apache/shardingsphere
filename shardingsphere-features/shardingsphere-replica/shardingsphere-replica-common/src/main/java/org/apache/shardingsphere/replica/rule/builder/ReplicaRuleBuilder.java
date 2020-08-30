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

package org.apache.shardingsphere.replica.rule.builder;

import org.apache.shardingsphere.infra.rule.ShardingSphereRuleBuilder;
import org.apache.shardingsphere.replica.api.config.ReplicaRuleConfiguration;
import org.apache.shardingsphere.replica.constant.ReplicaOrder;
import org.apache.shardingsphere.replica.rule.ReplicaRule;

import java.util.Collection;

/**
 * Replica rule builder.
 */
public final class ReplicaRuleBuilder implements ShardingSphereRuleBuilder<ReplicaRule, ReplicaRuleConfiguration> {
    
    @Override
    public ReplicaRule build(final ReplicaRuleConfiguration ruleConfiguration, final Collection<String> dataSourceNames) {
        return new ReplicaRule(ruleConfiguration);
    }
    
    @Override
    public int getOrder() {
        return ReplicaOrder.ORDER;
    }
    
    @Override
    public Class<ReplicaRuleConfiguration> getTypeClass() {
        return ReplicaRuleConfiguration.class;
    }
}
