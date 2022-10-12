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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.expr.InlineExpressionParser;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharding rule configuration import checker.
 */
public final class ShardingRuleConfigurationImportChecker {
    
    private static final String SHARDING = "sharding";
    
    private static final String KEY_GENERATOR = "key generator";
    
    private static final Map<String, Class<? extends ShardingSphereAlgorithm>> ALGORITHM_TYPE_MAP = new HashMap<>(2, 1);
    
    static {
        ALGORITHM_TYPE_MAP.put(SHARDING, ShardingAlgorithm.class);
        ALGORITHM_TYPE_MAP.put(KEY_GENERATOR, KeyGenerateAlgorithm.class);
    }
    
    /**
     * Check sharding rule configuration.
     *
     * @param database database
     * @param currentRuleConfig current rule configuration
     */
    public void check(final ShardingSphereDatabase database, final ShardingRuleConfiguration currentRuleConfig) {
        if (null == database || null == currentRuleConfig) {
            return;
        }
        String databaseName = database.getName();
        checkLogicTables(databaseName, currentRuleConfig);
        checkResources(databaseName, database, currentRuleConfig);
        checkKeyGenerators(currentRuleConfig);
        checkShardingAlgorithms(currentRuleConfig);
    }
    
    private void checkLogicTables(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> tablesLogicTables = currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        Collection<String> autoTablesLogicTables = currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        Collection<String> allLogicTables = new LinkedList<>();
        allLogicTables.addAll(tablesLogicTables);
        allLogicTables.addAll(autoTablesLogicTables);
        Set<String> duplicatedLogicTables = allLogicTables.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedLogicTables.isEmpty(), () -> new DuplicateRuleException(SHARDING, databaseName, duplicatedLogicTables));
    }
    
    private void checkShardingAlgorithms(final ShardingRuleConfiguration currentRuleConfig) {
        checkInvalidAlgorithms(SHARDING, currentRuleConfig.getShardingAlgorithms().values());
    }
    
    private void checkKeyGenerators(final ShardingRuleConfiguration currentRuleConfig) {
        checkInvalidAlgorithms(KEY_GENERATOR, currentRuleConfig.getKeyGenerators().values());
    }
    
    private void checkInvalidAlgorithms(final String algorithmType, final Collection<AlgorithmConfiguration> algorithmConfigs) {
        Collection<String> invalidAlgorithms = algorithmConfigs.stream()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ALGORITHM_TYPE_MAP.get(algorithmType), each.getType(), each.getProps()).isPresent())
                .map(AlgorithmConfiguration::getType).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException(algorithmType, invalidAlgorithms));
    }
    
    private Collection<String> getRequiredResources(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        currentRuleConfig.getTables().forEach(each -> result.addAll(getDataSourceNames(each)));
        currentRuleConfig.getAutoTables().forEach(each -> result.addAll(getDataSourceNames(each)));
        return result;
    }
    
    private Collection<String> getDataSourceNames(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        Collection<String> actualDataSources = new InlineExpressionParser(shardingAutoTableRuleConfig.getActualDataSources()).splitAndEvaluate();
        return new HashSet<>(actualDataSources);
    }
    
    private Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Collection<String> actualDataNodes = new InlineExpressionParser(shardingTableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        return actualDataNodes.stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private void checkResources(final String databaseName, final ShardingSphereDatabase database, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> requiredResource = getRequiredResources(currentRuleConfig);
        Collection<String> notExistedResources = database.getResourceMetaData().getNotExistedResources(requiredResource);
        Collection<String> logicResources = getLogicResources(database);
        notExistedResources.removeIf(logicResources::contains);
        ShardingSpherePreconditions.checkState(notExistedResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistedResources));
    }
    
    private Collection<String> getLogicResources(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataSourceContainedRule)
                .map(each -> ((DataSourceContainedRule) each).getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
