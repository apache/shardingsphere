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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.checker;

import org.apache.shardingsphere.infra.config.TypedSPIConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.support.InlineExpressionParser;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
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
     * @param shardingSphereMetaData ShardingSphere meta data
     * @param currentRuleConfig current rule configuration
     * @throws DistSQLException definition violation exception
     */
    public void check(final ShardingSphereMetaData shardingSphereMetaData, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (null == shardingSphereMetaData || null == currentRuleConfig) {
            return;
        }
        String schemaName = shardingSphereMetaData.getName();
        checkLogicTables(schemaName, currentRuleConfig);
        checkResources(schemaName, shardingSphereMetaData, currentRuleConfig);
        checkKeyGenerators(currentRuleConfig);
        checkShardingAlgorithms(currentRuleConfig);
    }
    
    private void checkLogicTables(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> tablesLogicTables = currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toCollection(LinkedList::new));
        Collection<String> autoTablesLogicTables = currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toCollection(LinkedList::new));
        Collection<String> allLogicTables = new LinkedList<>();
        allLogicTables.addAll(tablesLogicTables);
        allLogicTables.addAll(autoTablesLogicTables);
        Set<String> duplicatedLogicTables = allLogicTables.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        DistSQLException.predictionThrow(duplicatedLogicTables.isEmpty(), () -> new DuplicateRuleException(SHARDING, schemaName, duplicatedLogicTables));
    }
    
    private void checkShardingAlgorithms(final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        checkInvalidAlgorithms(SHARDING, currentRuleConfig.getShardingAlgorithms().values());
    }
    
    private void checkKeyGenerators(final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        checkInvalidAlgorithms(KEY_GENERATOR, currentRuleConfig.getKeyGenerators().values());
    }
    
    private void checkInvalidAlgorithms(final String algorithmType, final Collection<ShardingSphereAlgorithmConfiguration> algorithmConfigurations) throws DistSQLException {
        Collection<String> invalidAlgorithms = algorithmConfigurations.stream()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ALGORITHM_TYPE_MAP.get(algorithmType), each.getType(), new Properties()).isPresent())
                .map(TypedSPIConfiguration::getType).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException(algorithmType, invalidAlgorithms));
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
    
    private void checkResources(final String schemaName, final ShardingSphereMetaData shardingSphereMetaData, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> requiredResource = getRequiredResources(currentRuleConfig);
        Collection<String> notExistedResources = shardingSphereMetaData.getResource().getNotExistedResources(requiredResource);
        Collection<String> logicResources = getLogicResources(shardingSphereMetaData);
        notExistedResources.removeIf(logicResources::contains);
        DistSQLException.predictionThrow(notExistedResources.isEmpty(), () -> new RequiredResourceMissedException(schemaName, notExistedResources));
    }
    
    private Collection<String> getLogicResources(final ShardingSphereMetaData shardingSphereMetaData) {
        return shardingSphereMetaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataSourceContainedRule)
                .map(each -> ((DataSourceContainedRule) each).getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
