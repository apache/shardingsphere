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

package org.apache.shardingsphere.sharding.event;

import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.NamedRuleItemChangedEventCreator;
import org.apache.shardingsphere.mode.event.UniqueRuleItemChangedEventCreator;
import org.apache.shardingsphere.mode.spi.RuleChangedEventCreator;
import org.apache.shardingsphere.sharding.event.algorithm.auditor.creator.AuditShardingEventCreator;
import org.apache.shardingsphere.sharding.event.algorithm.keygenerator.creator.KeyGeneratorEventCreator;
import org.apache.shardingsphere.sharding.event.algorithm.sharding.creator.ShardingAlgorithmEventCreator;
import org.apache.shardingsphere.sharding.event.cache.creator.ShardingCacheEventCreator;
import org.apache.shardingsphere.sharding.event.strategy.audit.creator.ShardingAuditorStrategyEventCreator;
import org.apache.shardingsphere.sharding.event.strategy.database.creator.DefaultDatabaseShardingStrategyEventCreator;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.creator.DefaultKeyGenerateStrategyEventCreator;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.creator.DefaultShardingColumnEventCreator;
import org.apache.shardingsphere.sharding.event.strategy.table.creator.DefaultTableShardingStrategyEventCreator;
import org.apache.shardingsphere.sharding.event.table.auto.creator.ShardingAutoTableEventCreator;
import org.apache.shardingsphere.sharding.event.table.binding.creator.ShardingTableReferenceEventCreator;
import org.apache.shardingsphere.sharding.event.table.sharding.creator.ShardingTableEventCreator;
import org.apache.shardingsphere.sharding.metadata.nodepath.ShardingRuleNodePathProvider;

/**
 * Sharding rule changed event creator.
 */
public final class ShardingRuleChangedEventCreator implements RuleChangedEventCreator {
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType, final String itemName) {
        return getNamedRuleItemChangedEventCreator(itemType).create(databaseName, itemName, event);
    }
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType) {
        return getUniqueRuleItemChangedEventCreator(itemType).create(databaseName, event);
    }
    
    private NamedRuleItemChangedEventCreator getNamedRuleItemChangedEventCreator(final String itemType) {
        switch (itemType) {
            case ShardingRuleNodePathProvider.TABLES:
                return new ShardingTableEventCreator();
            case ShardingRuleNodePathProvider.AUTO_TABLES:
                return new ShardingAutoTableEventCreator();
            case ShardingRuleNodePathProvider.BINDING_TABLES:
                return new ShardingTableReferenceEventCreator();
            case ShardingRuleNodePathProvider.ALGORITHMS:
                return new ShardingAlgorithmEventCreator();
            case ShardingRuleNodePathProvider.KEY_GENERATORS:
                return new KeyGeneratorEventCreator();
            case ShardingRuleNodePathProvider.AUDITORS:
                return new AuditShardingEventCreator();
            default:
                throw new UnsupportedOperationException(itemType);
        }
    }
    
    private UniqueRuleItemChangedEventCreator getUniqueRuleItemChangedEventCreator(final String itemType) {
        switch (itemType) {
            case ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY:
                return new DefaultDatabaseShardingStrategyEventCreator();
            case ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY:
                return new DefaultTableShardingStrategyEventCreator();
            case ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY:
                return new DefaultKeyGenerateStrategyEventCreator();
            case ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY:
                return new ShardingAuditorStrategyEventCreator();
            case ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN:
                return new DefaultShardingColumnEventCreator();
            case ShardingRuleNodePathProvider.SHARDING_CACHE:
                return new ShardingCacheEventCreator();
            default:
                throw new UnsupportedOperationException(itemType);
        }
    }
    
    @Override
    public String getType() {
        return "sharding";
    }
}
