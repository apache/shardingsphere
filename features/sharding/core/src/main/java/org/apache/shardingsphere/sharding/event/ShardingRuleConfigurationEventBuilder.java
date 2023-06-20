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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
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
import org.apache.shardingsphere.sharding.event.table.broadcast.AddBroadcastTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.broadcast.AlterBroadcastTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.broadcast.DeleteBroadcastTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.AddShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.AlterShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.DeleteShardingTableConfigurationEvent;
import org.apache.shardingsphere.sharding.metadata.converter.ShardingNodeConverter;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.cache.YamlShardingCacheConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingAutoTableRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingTableReferenceRuleConfigurationConverter;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingTableRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlKeyGenerateStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingAuditStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingStrategyConfigurationSwapper;

import java.util.Collection;
import java.util.Optional;

/**
 * Sharding rule configuration event builder.
 */
public final class ShardingRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!ShardingNodeConverter.isShardingPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> tableName = ShardingNodeConverter.getTableName(event.getKey());
        if (tableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> tableNameVersion = ShardingNodeConverter.getTableNameVersion(event.getKey());
            if (tableNameVersion.isPresent()) {
                return createShardingTableConfigEvent(databaseName, tableName.get(), Integer.parseInt(tableNameVersion.get()), event);
            }
        }
        Optional<String> autoTableName = ShardingNodeConverter.getAutoTableName(event.getKey());
        if (autoTableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> autoTableNameVersion = ShardingNodeConverter.getAutoTableNameVersion(event.getKey());
            if (autoTableNameVersion.isPresent()) {
                return createShardingAutoTableConfigEvent(databaseName, autoTableName.get(), Integer.parseInt(autoTableNameVersion.get()), event);
            }
        }
        Optional<String> bindingTableName = ShardingNodeConverter.getBindingTableName(event.getKey());
        if (bindingTableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> bingingTableNameVersion = ShardingNodeConverter.getBindingTableNameVersion(event.getKey());
            if (bingingTableNameVersion.isPresent()) {
                return createShardingTableReferenceConfigEvent(databaseName, bindingTableName.get(), Integer.parseInt(bingingTableNameVersion.get()), event);
            }
        }
        if (ShardingNodeConverter.isDefaultDatabaseStrategyPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> defaultDatabaseStrategyVersion = ShardingNodeConverter.getDefaultDatabaseStrategyVersion(event.getKey());
            if (defaultDatabaseStrategyVersion.isPresent()) {
                return createDefaultDatabaseStrategyConfigEvent(databaseName, Integer.parseInt(defaultDatabaseStrategyVersion.get()), event);
            }
        }
        if (ShardingNodeConverter.isDefaultTableStrategyPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> defaultTableStrategyVersion = ShardingNodeConverter.getDefaultTableStrategyVersion(event.getKey());
            if (defaultTableStrategyVersion.isPresent()) {
                return createDefaultTableStrategyConfigEvent(databaseName, Integer.parseInt(defaultTableStrategyVersion.get()), event);
            }
        }
        if (ShardingNodeConverter.isDefaultKeyGenerateStrategyPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> defaultKeyGenerateStrategyVersion = ShardingNodeConverter.getDefaultKeyGenerateStrategyVersion(event.getKey());
            if (defaultKeyGenerateStrategyVersion.isPresent()) {
                return createDefaultKeyGenerateStrategyConfigEvent(databaseName, Integer.parseInt(defaultKeyGenerateStrategyVersion.get()), event);
            }
        }
        if (ShardingNodeConverter.isDefaultAuditStrategyPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> defaultAuditStrategyVersion = ShardingNodeConverter.getDefaultAuditStrategyVersion(event.getKey());
            if (defaultAuditStrategyVersion.isPresent()) {
                return createDefaultShardingAuditorStrategyConfigEvent(databaseName, Integer.parseInt(defaultAuditStrategyVersion.get()), event);
            }
        }
        if (ShardingNodeConverter.isDefaultShardingColumnPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> defaultShardingColumnVersion = ShardingNodeConverter.getDefaultShardingColumnVersion(event.getKey());
            if (defaultShardingColumnVersion.isPresent()) {
                return createDefaultShardingColumnEvent(databaseName, Integer.parseInt(defaultShardingColumnVersion.get()), event);
            }
        }
        Optional<String> algorithmName = ShardingNodeConverter.getShardingAlgorithmName(event.getKey());
        if (algorithmName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> algorithmVersion = ShardingNodeConverter.getShardingAlgorithmVersion(event.getKey());
            if (algorithmVersion.isPresent()) {
                return createShardingAlgorithmEvent(databaseName, algorithmName.get(), Integer.parseInt(algorithmVersion.get()), event);
            }
        }
        Optional<String> keyGeneratorName = ShardingNodeConverter.getKeyGeneratorName(event.getKey());
        if (keyGeneratorName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> keyGeneratorVersion = ShardingNodeConverter.getKeyGeneratorVersion(event.getKey());
            if (keyGeneratorVersion.isPresent()) {
                return createKeyGeneratorEvent(databaseName, keyGeneratorName.get(), Integer.parseInt(keyGeneratorVersion.get()), event);
            }
        }
        Optional<String> auditorName = ShardingNodeConverter.getAuditorName(event.getKey());
        if (auditorName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> auditorVersion = ShardingNodeConverter.getAuditorVersion(event.getKey());
            if (auditorVersion.isPresent()) {
                return createAuditorEvent(databaseName, auditorName.get(), Integer.parseInt(auditorVersion.get()), event);
            }
        }
        if (ShardingNodeConverter.isShardingCachePath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> shardingCacheVersion = ShardingNodeConverter.getShardingCacheVersion(event.getKey());
            if (shardingCacheVersion.isPresent()) {
                return createShardingCacheEvent(databaseName, Integer.parseInt(shardingCacheVersion.get()), event);
            }
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createShardingTableConfigEvent(final String databaseName, final String tableName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShardingTableConfigurationEvent<>(databaseName, swapShardingTableRuleConfig(event.getValue()), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingTableConfigurationEvent<>(databaseName, tableName, swapShardingTableRuleConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteShardingTableConfigurationEvent(databaseName, tableName, event.getKey(), version));
    }
    
    private ShardingTableRuleConfiguration swapShardingTableRuleConfig(final String yamlContext) {
        return new YamlShardingTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlTableRuleConfiguration.class));
    }
    
    private Optional<GovernanceEvent> createShardingAutoTableConfigEvent(final String databaseName, final String tableName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShardingAutoTableConfigurationEvent<>(databaseName, swapShardingAutoTableRuleConfig(event.getValue()), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingAutoTableConfigurationEvent<>(databaseName, tableName, swapShardingAutoTableRuleConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteShardingAutoTableConfigurationEvent(databaseName, tableName, event.getKey(), version));
    }
    
    private ShardingAutoTableRuleConfiguration swapShardingAutoTableRuleConfig(final String yamlContext) {
        return new YamlShardingAutoTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShardingAutoTableRuleConfiguration.class));
    }
    
    private Optional<GovernanceEvent> createShardingTableReferenceConfigEvent(final String databaseName, final String tableName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShardingAutoTableConfigurationEvent<>(databaseName, swapShardingTableReferenceRuleConfig(event.getValue()), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingAutoTableConfigurationEvent<>(databaseName, tableName, swapShardingTableReferenceRuleConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteShardingAutoTableConfigurationEvent(databaseName, tableName, event.getKey(), version));
    }
    
    private ShardingTableReferenceRuleConfiguration swapShardingTableReferenceRuleConfig(final String yamlContext) {
        return YamlShardingTableReferenceRuleConfigurationConverter.convertToObject(yamlContext);
    }
    
    @SuppressWarnings("unchecked")
    private Optional<GovernanceEvent> createBroadcastTableConfigEvent(final String databaseName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddBroadcastTableConfigurationEvent(databaseName, YamlEngine.unmarshal(event.getValue(), Collection.class), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterBroadcastTableConfigurationEvent(databaseName, YamlEngine.unmarshal(event.getValue(), Collection.class), event.getKey(), version));
        }
        return Optional.of(new DeleteBroadcastTableConfigurationEvent(databaseName, event.getKey(), version));
    }
    
    private Optional<GovernanceEvent> createDefaultDatabaseStrategyConfigEvent(final String databaseName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddDatabaseShardingStrategyConfigurationEvent(databaseName, swapShardingStrategyConfig(event.getValue()), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterDatabaseShardingStrategyConfigurationEvent(databaseName, swapShardingStrategyConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteDatabaseShardingStrategyConfigurationEvent(databaseName, event.getKey(), version));
    }
    
    private Optional<GovernanceEvent> createDefaultTableStrategyConfigEvent(final String databaseName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddTableShardingStrategyConfigurationEvent(databaseName, swapShardingStrategyConfig(event.getValue()), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterTableShardingStrategyConfigurationEvent(databaseName, swapShardingStrategyConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteTableShardingStrategyConfigurationEvent(databaseName, event.getKey(), version));
    }
    
    private ShardingStrategyConfiguration swapShardingStrategyConfig(final String yamlContext) {
        return new YamlShardingStrategyConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShardingStrategyConfiguration.class));
    }
    
    private Optional<GovernanceEvent> createDefaultKeyGenerateStrategyConfigEvent(final String databaseName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddKeyGenerateStrategyConfigurationEvent(databaseName, swapKeyGenerateStrategyConfig(event.getValue()), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterKeyGenerateStrategyConfigurationEvent(databaseName, swapKeyGenerateStrategyConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteKeyGenerateStrategyConfigurationEvent(databaseName, event.getKey(), version));
    }
    
    private KeyGenerateStrategyConfiguration swapKeyGenerateStrategyConfig(final String yamlContext) {
        return new YamlKeyGenerateStrategyConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlKeyGenerateStrategyConfiguration.class));
    }
    
    private Optional<GovernanceEvent> createDefaultShardingAuditorStrategyConfigEvent(final String databaseName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShardingAuditorStrategyConfigurationEvent(databaseName, swapShardingAuditorStrategyConfig(event.getValue()), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingAuditorStrategyConfigurationEvent(databaseName, swapShardingAuditorStrategyConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteShardingAuditorStrategyConfigurationEvent(databaseName, event.getKey(), version));
    }
    
    private ShardingAuditStrategyConfiguration swapShardingAuditorStrategyConfig(final String yamlContext) {
        return new YamlShardingAuditStrategyConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShardingAuditStrategyConfiguration.class));
    }
    
    private Optional<GovernanceEvent> createDefaultShardingColumnEvent(final String databaseName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddDefaultShardingColumnEvent(databaseName, event.getValue(), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterDefaultShardingColumnEvent(databaseName, event.getValue(), event.getKey(), version));
        }
        return Optional.of(new DeleteDefaultShardingColumnEvent(databaseName, event.getKey(), version));
    }
    
    private Optional<GovernanceEvent> createShardingAlgorithmEvent(final String databaseName, final String algorithmName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingAlgorithmEvent(databaseName, algorithmName, swapToAlgorithmConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteShardingAlgorithmEvent(databaseName, algorithmName, event.getKey(), version));
    }
    
    private Optional<GovernanceEvent> createKeyGeneratorEvent(final String databaseName, final String algorithmName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterKeyGeneratorEvent(databaseName, algorithmName, swapToAlgorithmConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteKeyGeneratorEvent(databaseName, algorithmName, event.getKey(), version));
    }
    
    private Optional<GovernanceEvent> createAuditorEvent(final String databaseName, final String algorithmName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterAuditorEvent(databaseName, algorithmName, swapToAlgorithmConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteAuditorEvent(databaseName, algorithmName, event.getKey(), version));
    }
    
    private AlgorithmConfiguration swapToAlgorithmConfig(final String yamlContext) {
        return new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlAlgorithmConfiguration.class));
    }
    
    private Optional<GovernanceEvent> createShardingCacheEvent(final String databaseName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShardingCacheConfigurationEvent(databaseName, swapToShardingCacheConfig(event.getValue()), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShardingCacheConfigurationEvent(databaseName, swapToShardingCacheConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteShardingCacheConfigurationEvent(databaseName, event.getKey(), version));
    }
    
    private ShardingCacheConfiguration swapToShardingCacheConfig(final String yamlContext) {
        return new YamlShardingCacheConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShardingCacheConfiguration.class));
    }
}
