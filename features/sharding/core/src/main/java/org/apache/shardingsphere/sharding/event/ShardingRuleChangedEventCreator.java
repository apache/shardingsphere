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
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleChangedEventCreator;
import org.apache.shardingsphere.sharding.event.algorithm.auditor.AlterAuditorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.auditor.DeleteAuditorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.keygenerator.AlterKeyGeneratorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.keygenerator.DeleteKeyGeneratorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.sharding.AlterShardingAlgorithmEvent;
import org.apache.shardingsphere.sharding.event.algorithm.sharding.DeleteShardingAlgorithmEvent;
import org.apache.shardingsphere.sharding.event.cache.AddShardingCacheConfigurationEvent;
import org.apache.shardingsphere.sharding.event.cache.AlterShardingCacheConfigurationEvent;
import org.apache.shardingsphere.sharding.event.cache.DeleteShardingCacheConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.AddShardingAuditorStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.AlterShardingAuditorStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.DeleteShardingAuditorStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.AddDatabaseShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.AlterDatabaseShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.DeleteDatabaseShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AddKeyGenerateStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AlterKeyGenerateStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.DeleteKeyGenerateStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.AddDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.AlterDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.DeleteDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.AddTableShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.AlterTableShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.DeleteTableShardingStrategyConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.auto.AddShardingAutoTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.auto.AlterShardingAutoTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.auto.DeleteShardingAutoTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.binding.AddShardingTableReferenceConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.binding.AlterShardingTableReferenceConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.binding.DeleteShardingTableReferenceConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.AddShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.AlterShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.DeleteShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.metadata.nodepath.ShardingRuleNodePathProvider;

/**
 * Sharding rule changed event creator.
 */
public final class ShardingRuleChangedEventCreator implements RuleChangedEventCreator {
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType, final String itemName) {
        switch (itemType) {
            case ShardingRuleNodePathProvider.TABLES:
                return createShardingTableEvent(databaseName, itemName, event);
            case ShardingRuleNodePathProvider.AUTO_TABLES:
                return createShardingAutoTableEvent(databaseName, itemName, event);
            case ShardingRuleNodePathProvider.BINDING_TABLES:
                return createShardingTableReferenceEvent(databaseName, itemName, event);
            case ShardingRuleNodePathProvider.ALGORITHMS:
                return createShardingAlgorithmEvent(databaseName, itemName, event);
            case ShardingRuleNodePathProvider.KEY_GENERATORS:
                return createKeyGeneratorEvent(databaseName, itemName, event);
            case ShardingRuleNodePathProvider.AUDITORS:
                return createAuditorEvent(databaseName, itemName, event);
            default:
                throw new UnsupportedOperationException(itemType);
        }
    }
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType) {
        switch (itemType) {
            case ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY:
                return createDefaultDatabaseStrategyEvent(databaseName, event);
            case ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY:
                return createDefaultTableStrategyEvent(databaseName, event);
            case ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY:
                return createDefaultKeyGenerateStrategyEvent(databaseName, event);
            case ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY:
                return createDefaultShardingAuditorStrategyEvent(databaseName, event);
            case ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN:
                return createDefaultShardingColumnEvent(databaseName, event);
            case ShardingRuleNodePathProvider.SHARDING_CACHE:
                return createShardingCacheEvent(databaseName, event);
            default:
                throw new UnsupportedOperationException(itemType);
        }
    }
    
    private GovernanceEvent createShardingTableEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShardingTableConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShardingTableConfigurationEvent(databaseName, tableName, event.getKey(), event.getValue());
        }
        return new DeleteShardingTableConfigurationEvent(databaseName, tableName);
    }
    
    private GovernanceEvent createShardingAutoTableEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShardingAutoTableConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShardingAutoTableConfigurationEvent(databaseName, tableName, event.getKey(), event.getValue());
        }
        return new DeleteShardingAutoTableConfigurationEvent(databaseName, tableName);
    }
    
    private GovernanceEvent createShardingTableReferenceEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShardingTableReferenceConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShardingTableReferenceConfigurationEvent(databaseName, tableName, event.getKey(), event.getValue());
        }
        return new DeleteShardingTableReferenceConfigurationEvent(databaseName, tableName);
    }
    
    private GovernanceEvent createShardingAlgorithmEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return new AlterShardingAlgorithmEvent(databaseName, algorithmName, event.getKey(), event.getValue());
        }
        return new DeleteShardingAlgorithmEvent(databaseName, algorithmName);
    }
    
    private GovernanceEvent createKeyGeneratorEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return new AlterKeyGeneratorEvent(databaseName, algorithmName, event.getKey(), event.getValue());
        }
        return new DeleteKeyGeneratorEvent(databaseName, algorithmName);
    }
    
    private GovernanceEvent createAuditorEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return new AlterAuditorEvent(databaseName, algorithmName, event.getKey(), event.getValue());
        }
        return new DeleteAuditorEvent(databaseName, algorithmName);
    }
    
    private GovernanceEvent createDefaultDatabaseStrategyEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddDatabaseShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterDatabaseShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteDatabaseShardingStrategyConfigurationEvent(databaseName);
    }
    
    private GovernanceEvent createDefaultTableStrategyEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddTableShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterTableShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteTableShardingStrategyConfigurationEvent(databaseName);
    }
    
    private GovernanceEvent createDefaultKeyGenerateStrategyEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddKeyGenerateStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterKeyGenerateStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteKeyGenerateStrategyConfigurationEvent(databaseName);
    }
    
    private GovernanceEvent createDefaultShardingAuditorStrategyEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShardingAuditorStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShardingAuditorStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteShardingAuditorStrategyConfigurationEvent(databaseName);
    }
    
    private GovernanceEvent createDefaultShardingColumnEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddDefaultShardingColumnEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterDefaultShardingColumnEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteDefaultShardingColumnEvent(databaseName);
    }
    
    private GovernanceEvent createShardingCacheEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShardingCacheConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShardingCacheConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteShardingCacheConfigurationEvent(databaseName);
    }
    
    @Override
    public String getType() {
        return "sharding";
    }
}
