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

package org.apache.shardingsphere.broadcast.rule.builder;

import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.constant.BroadcastOrder;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRuleBuilder;

import java.util.Collection;

/**
 * Broadcast rule builder.
 */
public final class BroadcastRuleBuilder implements DatabaseRuleBuilder<BroadcastRuleConfiguration> {
    
    @Override
    public BroadcastRule build(final BroadcastRuleConfiguration ruleConfig, final String databaseName, final DatabaseType protocolType,
                               final ResourceMetaData resourceMetaData, final Collection<ShardingSphereRule> builtRules, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        return new BroadcastRule(ruleConfig, resourceMetaData.getDataSourceMap(), builtRules);
    }
    
    @Override
    public int getOrder() {
        return BroadcastOrder.ORDER;
    }
    
    @Override
    public Class<BroadcastRuleConfiguration> getTypeClass() {
        return BroadcastRuleConfiguration.class;
    }
}
