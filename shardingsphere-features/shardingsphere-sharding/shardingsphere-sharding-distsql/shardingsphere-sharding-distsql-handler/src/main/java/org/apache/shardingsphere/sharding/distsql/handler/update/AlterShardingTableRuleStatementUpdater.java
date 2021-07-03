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
import org.apache.shardingsphere.infra.distsql.update.RDLAlterUpdater;
import org.apache.shardingsphere.infra.exception.rule.ResourceNotExistedException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.handler.exception.DuplicateTablesException;
import org.apache.shardingsphere.sharding.distsql.handler.exception.InvalidKeyGeneratorsException;
import org.apache.shardingsphere.sharding.distsql.handler.exception.InvalidShardingAlgorithmsException;
import org.apache.shardingsphere.sharding.distsql.handler.exception.ShardingTableRuleNotExistedException;
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
public final class AlterShardingTableRuleStatementUpdater implements RDLAlterUpdater<AlterShardingTableRuleStatement, ShardingRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    @Override
    public void checkSQLStatement(final String schemaName, final AlterShardingTableRuleStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        checkCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig);
        checkToBeAlteredResources(schemaName, sqlStatement, resource);
        checkToBeAlteredShardingTables(schemaName, sqlStatement, currentRuleConfig);
        checkToBeAlteredShardingAlgorithm(sqlStatement);
        checkToBeAlteredKeyGenerators(sqlStatement);
        checkToBeAlteredDuplicateShardingTables(sqlStatement);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final AlterShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            throw new ShardingTableRuleNotExistedException(schemaName, getToBeAlteredTableNames(sqlStatement));
        }
    }
    
    private Collection<String> getToBeAlteredTableNames(final AlterShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(TableRuleSegment::getLogicTable).collect(Collectors.toList());
    }
    
    private void checkToBeAlteredResources(final String schemaName, final AlterShardingTableRuleStatement sqlStatement, final ShardingSphereResource resource) {
        Collection<String> notExistResources = resource.getNotExistedResources(getToBeAlteredResources(sqlStatement));
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistResources);
        }
    }
    
    private Collection<String> getToBeAlteredResources(final AlterShardingTableRuleStatement sqlStatement) {
        Collection<String> result = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> result.addAll(each.getDataSources()));
        return result;
    }
    
    private void checkToBeAlteredShardingTables(final String schemaName, final AlterShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> currentShardingTables = getCurrentShardingTables(currentRuleConfig);
        Collection<String> notExistedShardingTables = getToBeAlteredTableNames(sqlStatement).stream().filter(each -> !currentShardingTables.contains(each)).collect(Collectors.toList());
        if (!notExistedShardingTables.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(schemaName, notExistedShardingTables);
        }
    }
    
    private Collection<String> getCurrentShardingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    private void checkToBeAlteredShardingAlgorithm(final AlterShardingTableRuleStatement sqlStatement) {
        Collection<String> notExistedShardingAlgorithms = sqlStatement.getRules().stream().map(each -> each.getTableStrategy().getName()).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ShardingAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!notExistedShardingAlgorithms.isEmpty()) {
            throw new InvalidShardingAlgorithmsException(notExistedShardingAlgorithms);
        }
    }
    
    private void checkToBeAlteredKeyGenerators(final AlterShardingTableRuleStatement sqlStatement) {
        Collection<String> notExistedKeyGenerators = getToBeAlteredKeyGenerators(sqlStatement).stream().distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(KeyGenerateAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!notExistedKeyGenerators.isEmpty()) {
            throw new InvalidKeyGeneratorsException(notExistedKeyGenerators);
        }
    }
    
    private Collection<String> getToBeAlteredKeyGenerators(final AlterShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().filter(each -> null != each.getKeyGenerateStrategy()).map(each -> each.getKeyGenerateStrategy().getName()).collect(Collectors.toSet());
    }
    
    private void checkToBeAlteredDuplicateShardingTables(final AlterShardingTableRuleStatement sqlStatement) {
        Collection<String> duplicateTables = getDuplicateTables(sqlStatement);
        if (!duplicateTables.isEmpty()) {
            throw new DuplicateTablesException(duplicateTables);
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
    public void updateCurrentRuleConfiguration(final String schemaName, final AlterShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        dropRuleConfiguration(sqlStatement, currentRuleConfig);
        addRuleConfiguration(sqlStatement, currentRuleConfig);
    }
    
    private void dropRuleConfiguration(final AlterShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        for (String each : getToBeAlteredTableNames(sqlStatement)) {
            Optional<ShardingAutoTableRuleConfiguration> shardingAutoTableRuleConfig = currentRuleConfig.getAutoTables().stream().filter(tableRule -> each.equals(tableRule.getLogicTable())).findAny();
            Preconditions.checkState(shardingAutoTableRuleConfig.isPresent());
            currentRuleConfig.getAutoTables().remove(shardingAutoTableRuleConfig.get());
            currentRuleConfig.getShardingAlgorithms().remove(shardingAutoTableRuleConfig.get().getShardingStrategy().getShardingAlgorithmName());
            if (null != shardingAutoTableRuleConfig.get().getKeyGenerateStrategy()) {
                currentRuleConfig.getKeyGenerators().remove(shardingAutoTableRuleConfig.get().getKeyGenerateStrategy().getKeyGeneratorName());
            }
        }
    }
    
    private void addRuleConfiguration(final AlterShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        ShardingRuleConfiguration toBeAlteredRuleConfig = ShardingRuleStatementConverter.convert(sqlStatement.getRules());
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
