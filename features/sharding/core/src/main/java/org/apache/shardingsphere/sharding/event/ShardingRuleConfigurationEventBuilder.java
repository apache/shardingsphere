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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;
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
import org.apache.shardingsphere.sharding.metadata.nodepath.ShardingNodePath;

import java.util.Optional;

/**
 * Sharding rule configuration event builder.
 */
public final class ShardingRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    private final RuleNodePath shardingRuleNodePath = ShardingNodePath.getInstance();
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!shardingRuleNodePath.getRoot().isValidatedPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> tableName = shardingRuleNodePath.getNamedItem(ShardingNodePath.TABLES).getNameByActiveVersion(event.getKey());
        if (tableName.isPresent()) {
            return Optional.of(createShardingTableConfigEvent(databaseName, tableName.get(), event));
        }
        Optional<String> autoTableName = shardingRuleNodePath.getNamedItem(ShardingNodePath.AUTO_TABLES).getNameByActiveVersion(event.getKey());
        if (autoTableName.isPresent()) {
            return Optional.of(createShardingAutoTableConfigEvent(databaseName, autoTableName.get(), event));
        }
        Optional<String> bindingTableName = shardingRuleNodePath.getNamedItem(ShardingNodePath.BINDING_TABLES).getNameByActiveVersion(event.getKey());
        if (bindingTableName.isPresent()) {
            return Optional.of(createShardingTableReferenceConfigEvent(databaseName, bindingTableName.get(), event));
        }
        if (shardingRuleNodePath.getUniqueItem(ShardingNodePath.DEFAULT_DATABASE_STRATEGY).isActiveVersionPath(event.getKey())) {
            return Optional.of(createDefaultDatabaseStrategyConfigEvent(databaseName, event));
        }
        if (shardingRuleNodePath.getUniqueItem(ShardingNodePath.DEFAULT_TABLE_STRATEGY).isActiveVersionPath(event.getKey())) {
            return Optional.of(createDefaultTableStrategyConfigEvent(databaseName, event));
        }
        if (shardingRuleNodePath.getUniqueItem(ShardingNodePath.DEFAULT_KEY_GENERATE_STRATEGY).isActiveVersionPath(event.getKey())) {
            return Optional.of(createDefaultKeyGenerateStrategyConfigEvent(databaseName, event));
        }
        if (shardingRuleNodePath.getUniqueItem(ShardingNodePath.DEFAULT_AUDIT_STRATEGY).isActiveVersionPath(event.getKey())) {
            return Optional.of(createDefaultShardingAuditorStrategyConfigEvent(databaseName, event));
        }
        if (shardingRuleNodePath.getUniqueItem(ShardingNodePath.DEFAULT_SHARDING_COLUMN).isActiveVersionPath(event.getKey())) {
            return Optional.of(createDefaultShardingColumnEvent(databaseName, event));
        }
        Optional<String> algorithmName = shardingRuleNodePath.getNamedItem(ShardingNodePath.ALGORITHMS).getNameByActiveVersion(event.getKey());
        if (algorithmName.isPresent()) {
            return Optional.of(createShardingAlgorithmEvent(databaseName, algorithmName.get(), event));
        }
        Optional<String> keyGeneratorName = shardingRuleNodePath.getNamedItem(ShardingNodePath.KEY_GENERATORS).getNameByActiveVersion(event.getKey());
        if (keyGeneratorName.isPresent()) {
            return Optional.of(createKeyGeneratorEvent(databaseName, keyGeneratorName.get(), event));
        }
        Optional<String> auditorName = shardingRuleNodePath.getNamedItem(ShardingNodePath.AUDITORS).getNameByActiveVersion(event.getKey());
        if (auditorName.isPresent()) {
            return Optional.of(createAuditorEvent(databaseName, auditorName.get(), event));
        }
        if (shardingRuleNodePath.getUniqueItem(ShardingNodePath.SHARDING_CACHE).isActiveVersionPath(event.getKey())) {
            return Optional.of(createShardingCacheEvent(databaseName, event));
        }
        return Optional.empty();
    }
    
    private GovernanceEvent createShardingTableConfigEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShardingTableConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShardingTableConfigurationEvent(databaseName, tableName, event.getKey(), event.getValue());
        }
        return new DeleteShardingTableConfigurationEvent(databaseName, tableName);
    }
    
    private GovernanceEvent createShardingAutoTableConfigEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShardingAutoTableConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShardingAutoTableConfigurationEvent(databaseName, tableName, event.getKey(), event.getValue());
        }
        return new DeleteShardingAutoTableConfigurationEvent(databaseName, tableName);
    }
    
    private GovernanceEvent createShardingTableReferenceConfigEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShardingTableReferenceConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShardingTableReferenceConfigurationEvent(databaseName, tableName, event.getKey(), event.getValue());
        }
        return new DeleteShardingTableReferenceConfigurationEvent(databaseName, tableName);
    }
    
    private GovernanceEvent createDefaultDatabaseStrategyConfigEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddDatabaseShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterDatabaseShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteDatabaseShardingStrategyConfigurationEvent(databaseName);
    }
    
    private GovernanceEvent createDefaultTableStrategyConfigEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddTableShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterTableShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteTableShardingStrategyConfigurationEvent(databaseName);
    }
    
    private GovernanceEvent createDefaultKeyGenerateStrategyConfigEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddKeyGenerateStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterKeyGenerateStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteKeyGenerateStrategyConfigurationEvent(databaseName);
    }
    
    private GovernanceEvent createDefaultShardingAuditorStrategyConfigEvent(final String databaseName, final DataChangedEvent event) {
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
    
    private GovernanceEvent createShardingCacheEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShardingCacheConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShardingCacheConfigurationEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteShardingCacheConfigurationEvent(databaseName);
    }
}
