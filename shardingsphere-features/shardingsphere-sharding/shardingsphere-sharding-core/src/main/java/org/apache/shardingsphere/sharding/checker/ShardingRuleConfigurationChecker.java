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

package org.apache.shardingsphere.sharding.checker;

import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;

import java.util.Collection;

/**
 * Sharding rule configuration checker.
 */
public final class ShardingRuleConfigurationChecker extends AbstractShardingRuleConfigurationChecker<ShardingRuleConfiguration> {
    
    @Override
    protected void checkShardingRuleConfiguration(final String databaseName, final ShardingRuleConfiguration config) {
        Collection<String> keyGenerators = config.getKeyGenerators().keySet();
        Collection<String> auditors = config.getAuditors().keySet();
        Collection<String> shardingAlgorithms = config.getShardingAlgorithms().keySet();
        checkTableConfiguration(config.getTables(), config.getAutoTables(), keyGenerators, auditors, shardingAlgorithms, databaseName);
        checkKeyGenerateStrategy(config.getDefaultKeyGenerateStrategy(), keyGenerators, databaseName);
        checkAuditStrategy(config.getDefaultAuditStrategy(), auditors, databaseName);
        checkShardingStrategy(config.getDefaultDatabaseShardingStrategy(), shardingAlgorithms, databaseName);
        checkShardingStrategy(config.getDefaultTableShardingStrategy(), shardingAlgorithms, databaseName);
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getTypeClass() {
        return ShardingRuleConfiguration.class;
    }
}
