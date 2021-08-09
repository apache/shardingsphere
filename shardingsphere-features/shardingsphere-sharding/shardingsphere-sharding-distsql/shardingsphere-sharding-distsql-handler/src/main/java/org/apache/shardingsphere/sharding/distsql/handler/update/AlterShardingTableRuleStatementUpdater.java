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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Alter sharding table rule statement updater.
 */
public final class AlterShardingTableRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterShardingTableRuleStatement, ShardingRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final AlterShardingTableRuleStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeAlteredResources(schemaName, sqlStatement, shardingSphereMetaData.getResource());
        checkToBeAlteredShardingTables(schemaName, sqlStatement, currentRuleConfig);
        checkToBeAlteredShardingAlgorithm(sqlStatement);
        checkToBeAlteredKeyGenerators(sqlStatement);
        checkToBeAlteredDuplicateShardingTables(schemaName, sqlStatement);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Sharding", schemaName);
        }
    }
    
    private Collection<String> getToBeAlteredTableNames(final AlterShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(TableRuleSegment::getLogicTable).collect(Collectors.toList());
    }
    
    private void checkToBeAlteredResources(final String schemaName, final AlterShardingTableRuleStatement sqlStatement, final ShardingSphereResource resource) throws RequiredResourceMissedException {
        Collection<String> notExistResources = resource.getNotExistedResources(getToBeAlteredResources(sqlStatement));
        if (!notExistResources.isEmpty()) {
            throw new RequiredResourceMissedException(schemaName, notExistResources);
        }
    }
    
    private Collection<String> getToBeAlteredResources(final AlterShardingTableRuleStatement sqlStatement) {
        Collection<String> result = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> result.addAll(each.getDataSources()));
        return result;
    }
    
    private void checkToBeAlteredShardingTables(final String schemaName, final AlterShardingTableRuleStatement sqlStatement, 
                                                final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        Collection<String> currentShardingTables = getCurrentShardingTables(currentRuleConfig);
        Collection<String> notExistedShardingTables = getToBeAlteredTableNames(sqlStatement).stream().filter(each -> !currentShardingTables.contains(each)).collect(Collectors.toList());
        if (!notExistedShardingTables.isEmpty()) {
            throw new RequiredRuleMissedException("Sharding", schemaName, notExistedShardingTables);
        }
    }
    
    private Collection<String> getCurrentShardingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    private void checkToBeAlteredShardingAlgorithm(final AlterShardingTableRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> notExistedShardingAlgorithms = sqlStatement.getRules().stream().map(each -> each.getTableStrategy().getName()).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ShardingAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!notExistedShardingAlgorithms.isEmpty()) {
            throw new InvalidAlgorithmConfigurationException("sharding", notExistedShardingAlgorithms);
        }
    }
    
    private void checkToBeAlteredKeyGenerators(final AlterShardingTableRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> notExistedKeyGenerators = getToBeAlteredKeyGenerators(sqlStatement).stream().distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(KeyGenerateAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!notExistedKeyGenerators.isEmpty()) {
            throw new InvalidAlgorithmConfigurationException("key generator", notExistedKeyGenerators);
        }
    }
    
    private Collection<String> getToBeAlteredKeyGenerators(final AlterShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().filter(each -> null != each.getKeyGenerateStrategy()).map(each -> each.getKeyGenerateStrategy().getName()).collect(Collectors.toSet());
    }
    
    private void checkToBeAlteredDuplicateShardingTables(final String schemaName, final AlterShardingTableRuleStatement sqlStatement) throws DuplicateRuleException {
        Collection<String> duplicateTables = getDuplicateTables(sqlStatement);
        if (!duplicateTables.isEmpty()) {
            throw new DuplicateRuleException("sharding", schemaName, duplicateTables);
        }
    }
    
    private Collection<String> getDuplicateTables(final AlterShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream()
                .collect(Collectors.toMap(TableRuleSegment::getLogicTable, entry -> 1, Integer::sum))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShardingTableRuleStatement sqlStatement) {
        return ShardingRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        dropRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        addRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
    }
    
    private void dropRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        for (ShardingAutoTableRuleConfiguration each : toBeAlteredRuleConfig.getAutoTables()) {
            Optional<ShardingAutoTableRuleConfiguration> shardingAutoTableRuleConfig
                    = currentRuleConfig.getAutoTables().stream().filter(tableRule -> each.getLogicTable().equals(tableRule.getLogicTable())).findAny();
            Preconditions.checkState(shardingAutoTableRuleConfig.isPresent());
            currentRuleConfig.getAutoTables().remove(shardingAutoTableRuleConfig.get());
            currentRuleConfig.getShardingAlgorithms().remove(shardingAutoTableRuleConfig.get().getShardingStrategy().getShardingAlgorithmName());
            if (null != shardingAutoTableRuleConfig.get().getKeyGenerateStrategy()) {
                currentRuleConfig.getKeyGenerators().remove(shardingAutoTableRuleConfig.get().getKeyGenerateStrategy().getKeyGeneratorName());
            }
        }
    }
    
    private void addRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getAutoTables().addAll(toBeAlteredRuleConfig.getAutoTables());
        currentRuleConfig.getShardingAlgorithms().putAll(toBeAlteredRuleConfig.getShardingAlgorithms());
        currentRuleConfig.getKeyGenerators().putAll(toBeAlteredRuleConfig.getKeyGenerators());
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterShardingTableRuleStatement.class.getCanonicalName();
    }
}
