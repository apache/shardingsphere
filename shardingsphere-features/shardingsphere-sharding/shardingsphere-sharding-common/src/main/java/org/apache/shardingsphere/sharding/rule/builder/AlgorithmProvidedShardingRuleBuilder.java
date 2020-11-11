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

package org.apache.shardingsphere.sharding.rule.builder;

import lombok.Setter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.aware.ResourceAware;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Algorithm provided sharding rule builder.
 */
@Setter
public final class AlgorithmProvidedShardingRuleBuilder implements ShardingSphereRuleBuilder<ShardingRule, AlgorithmProvidedShardingRuleConfiguration>, ResourceAware {
    
    private DatabaseType databaseType;
    
    private Map<String, DataSource> dataSourceMap;
    
    @Override
    public ShardingRule build(final AlgorithmProvidedShardingRuleConfiguration ruleConfig) {
        return new ShardingRule(ruleConfig, dataSourceMap.keySet());
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER + 1;
    }
    
    @Override
    public Class<AlgorithmProvidedShardingRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedShardingRuleConfiguration.class;
    }
}
