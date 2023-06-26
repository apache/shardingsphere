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
import org.apache.shardingsphere.sharding.event.table.sharding.AddShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.AlterShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.DeleteShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.metadata.converter.ShardingNodeConverter;

import java.util.Optional;

/**
 * Sharding rule configuration event builder.
 */
public final class ShardingRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!ShardingNodeConverter.getRuleRootNodeConverter().isRulePath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> tableName = ShardingNodeConverter.getTableNodeConverter().getNameByActiveVersionPath(event.getKey());
        if (tableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createShardingTableConfigEvent(databaseName, tableName.get(), event);
        }
        Optional<String> autoTableName = ShardingNodeConverter.getAutoTableNodeConverter().getNameByActiveVersionPath(event.getKey());
        if (autoTableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createShardingAutoTableConfigEvent(databaseName, autoTableName.get(), event);
        }
        Optional<String> bindingTableName = ShardingNodeConverter.getBindingTableNodeConverter().getNameByActiveVersionPath(event.getKey());
        if (bindingTableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createShardingTableReferenceConfigEvent(databaseName, bindingTableName.get(), event);
        }
        if (ShardingNodeConverter.isDefaultDatabaseStrategyWithActiveVersionPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            return createDefaultDatabaseStrategyConfigEvent(databaseName, event);
        }
        if (ShardingNodeConverter.isDefaultTableStrategyWithActiveVersionPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            return createDefaultTableStrategyConfigEvent(databaseName, event);
        }
        if (ShardingNodeConverter.isDefaultKeyGenerateStrategyWithActiveVersionPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            return createDefaultKeyGenerateStrategyConfigEvent(databaseName, event);
        }
        if (ShardingNodeConverter.isDefaultAuditStrategyWithActiveVersionPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            return createDefaultShardingAuditorStrategyConfigEvent(databaseName, event);
        }
        if (ShardingNodeConverter.isDefaultShardingColumnWithActiveVersionPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            return createDefaultShardingColumnEvent(databaseName, event);
        }
        Optional<String> algorithmName = ShardingNodeConverter.getShardingCacheNodeConverter().getNameByActiveVersionPath(event.getKey());
        if (algorithmName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createShardingAlgorithmEvent(databaseName, algorithmName.get(), event);
        }
        Optional<String> keyGeneratorName = ShardingNodeConverter.getKeyGeneratorNodeConverter().getNameByActiveVersionPath(event.getKey());
        if (keyGeneratorName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createKeyGeneratorEvent(databaseName, keyGeneratorName.get(), event);
        }
        Optional<String> auditorName = ShardingNodeConverter.getAuditorNodeConverter().getNameByActiveVersionPath(event.getKey());
        if (auditorName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createAuditorEvent(databaseName, auditorName.get(), event);
        }
        if (ShardingNodeConverter.isShardingCacheWithActiveVersionPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            return createShardingCacheEvent(databaseName, event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createShardingTableConfigEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShardingTableConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingTableConfigurationEvent(databaseName, tableName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteShardingTableConfigurationEvent(databaseName, tableName));
    }
    
    private Optional<GovernanceEvent> createShardingAutoTableConfigEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShardingAutoTableConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingAutoTableConfigurationEvent(databaseName, tableName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteShardingAutoTableConfigurationEvent(databaseName, tableName));
    }
    
    private Optional<GovernanceEvent> createShardingTableReferenceConfigEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShardingAutoTableConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingAutoTableConfigurationEvent(databaseName, tableName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteShardingAutoTableConfigurationEvent(databaseName, tableName));
    }
    
    private Optional<GovernanceEvent> createDefaultDatabaseStrategyConfigEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddDatabaseShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterDatabaseShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteDatabaseShardingStrategyConfigurationEvent(databaseName));
    }
    
    private Optional<GovernanceEvent> createDefaultTableStrategyConfigEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddTableShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterTableShardingStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteTableShardingStrategyConfigurationEvent(databaseName));
    }
    
    private Optional<GovernanceEvent> createDefaultKeyGenerateStrategyConfigEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddKeyGenerateStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterKeyGenerateStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteKeyGenerateStrategyConfigurationEvent(databaseName));
    }
    
    private Optional<GovernanceEvent> createDefaultShardingAuditorStrategyConfigEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShardingAuditorStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingAuditorStrategyConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteShardingAuditorStrategyConfigurationEvent(databaseName));
    }
    
    private Optional<GovernanceEvent> createDefaultShardingColumnEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddDefaultShardingColumnEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterDefaultShardingColumnEvent(databaseName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteDefaultShardingColumnEvent(databaseName));
    }
    
    private Optional<GovernanceEvent> createShardingAlgorithmEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingAlgorithmEvent(databaseName, algorithmName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteShardingAlgorithmEvent(databaseName, algorithmName));
    }
    
    private Optional<GovernanceEvent> createKeyGeneratorEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterKeyGeneratorEvent(databaseName, algorithmName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteKeyGeneratorEvent(databaseName, algorithmName));
    }
    
    private Optional<GovernanceEvent> createAuditorEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterAuditorEvent(databaseName, algorithmName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteAuditorEvent(databaseName, algorithmName));
    }
    
    private Optional<GovernanceEvent> createShardingCacheEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShardingCacheConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingCacheConfigurationEvent(databaseName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteShardingCacheConfigurationEvent(databaseName));
    }
}
