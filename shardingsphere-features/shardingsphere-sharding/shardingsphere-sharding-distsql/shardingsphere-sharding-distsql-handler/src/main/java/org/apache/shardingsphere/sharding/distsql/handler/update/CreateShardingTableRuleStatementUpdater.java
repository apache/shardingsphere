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

import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create sharding table rule statement updater.
 */
public final class CreateShardingTableRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingTableRuleStatement, ShardingRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final CreateShardingTableRuleStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        Collection<String> extraResources = getResourcesFromDataSourceContainedRules(shardingSphereMetaData.getRuleMetaData().getRules());
        checkToBeCreatedResource(schemaName, sqlStatement, shardingSphereMetaData.getResource(), extraResources);
        checkDuplicateTables(schemaName, sqlStatement, currentRuleConfig);
        checkShardingAlgorithmsCompleteness(sqlStatement);
        checkToBeCreatedShardingAlgorithms(sqlStatement);
        checkToBeCreatedKeyGenerators(sqlStatement);
    }
    
    private void checkShardingAlgorithmsCompleteness(final CreateShardingTableRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        for (TableRuleSegment each : sqlStatement.getRules()) {
            if (null == each.getTableStrategy() || null == each.getTableStrategyColumn()) {
                throw new InvalidAlgorithmConfigurationException("sharding");
            }
        }
    }
    
    private Collection<String> getResourcesFromDataSourceContainedRules(final Collection<ShardingSphereRule> rules) {
        if (rules.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<>();
        rules.stream().filter(each -> each instanceof DataSourceContainedRule).forEach(each -> result.addAll(((DataSourceContainedRule) each).getDataSourceMapper().keySet()));
        return result;
    }
    
    private void checkToBeCreatedResource(final String schemaName, final CreateShardingTableRuleStatement sqlStatement, final ShardingSphereResource resource, 
                                          final Collection<String> extraResources) throws RequiredResourceMissedException {
        Collection<String> notExistedResources = resource.getNotExistedResources(getToBeCreatedResources(sqlStatement));
        notExistedResources.removeIf(each -> extraResources.contains(each));
        if (!notExistedResources.isEmpty()) {
            throw new RequiredResourceMissedException(schemaName, notExistedResources);
        }
    }
    
    private Collection<String> getToBeCreatedResources(final CreateShardingTableRuleStatement sqlStatement) {
        Collection<String> result = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> result.addAll(each.getDataSources()));
        return result;
    }
    
    private void checkDuplicateTables(final String schemaName, final CreateShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DuplicateRuleException {
        Collection<String> shardingTableNames = null == currentRuleConfig ? Collections.emptyList() : getShardingTables(currentRuleConfig);
        Set<String> duplicateTableNames = sqlStatement.getRules().stream().collect(Collectors.toMap(TableRuleSegment::getLogicTable, each -> 1, Integer::sum))
                .entrySet().stream().filter(entry -> entry.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
        duplicateTableNames.addAll(sqlStatement.getRules().stream().map(TableRuleSegment::getLogicTable).filter(shardingTableNames::contains).collect(Collectors.toSet()));
        if (!duplicateTableNames.isEmpty()) {
            throw new DuplicateRuleException("sharding", schemaName, duplicateTableNames);
        }
    }
    
    private Collection<String> getShardingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    private void checkToBeCreatedShardingAlgorithms(final CreateShardingTableRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> notExistedShardingAlgorithms = sqlStatement.getRules().stream().map(each -> each.getTableStrategy().getName()).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ShardingAlgorithm.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        if (!notExistedShardingAlgorithms.isEmpty()) {
            throw new InvalidAlgorithmConfigurationException("sharding", notExistedShardingAlgorithms);
        }
    }
    
    private void checkToBeCreatedKeyGenerators(final CreateShardingTableRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> invalidKeyGenerators = getToBeCreatedKeyGenerators(sqlStatement).stream().distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(KeyGenerateAlgorithm.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        if (!invalidKeyGenerators.isEmpty()) {
            throw new InvalidAlgorithmConfigurationException("key generator", invalidKeyGenerators);
        }
    }
    
    private Collection<String> getToBeCreatedKeyGenerators(final CreateShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().filter(each -> Objects.nonNull(each.getKeyGenerateStrategy())).map(each -> each.getKeyGenerateStrategy().getName()).collect(Collectors.toSet());
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingTableRuleStatement sqlStatement) {
        return ShardingRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getAutoTables().addAll(toBeCreatedRuleConfig.getAutoTables());
            currentRuleConfig.getShardingAlgorithms().putAll(toBeCreatedRuleConfig.getShardingAlgorithms());
            currentRuleConfig.getKeyGenerators().putAll(toBeCreatedRuleConfig.getKeyGenerators());
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShardingTableRuleStatement.class.getCanonicalName();
    }
}
