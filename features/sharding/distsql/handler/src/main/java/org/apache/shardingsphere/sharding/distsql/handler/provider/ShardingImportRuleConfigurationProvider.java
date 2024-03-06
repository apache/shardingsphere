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

package org.apache.shardingsphere.sharding.distsql.handler.provider;

import org.apache.shardingsphere.distsql.handler.engine.update.ral.rule.spi.database.ImportRuleConfigurationProvider;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.keygen.core.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.datasource.DataSourceMapperRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharding import rule configuration provider.
 */
public final class ShardingImportRuleConfigurationProvider implements ImportRuleConfigurationProvider {
    
    @Override
    public void check(final ShardingSphereDatabase database, final RuleConfiguration ruleConfig) {
        if (null == database || null == ruleConfig) {
            return;
        }
        ShardingRuleConfiguration shardingRuleConfig = (ShardingRuleConfiguration) ruleConfig;
        String databaseName = database.getName();
        checkLogicTables(databaseName, shardingRuleConfig);
        checkResources(databaseName, database, shardingRuleConfig);
        checkShardingAlgorithms(shardingRuleConfig.getShardingAlgorithms().values());
        checkKeyGeneratorAlgorithms(shardingRuleConfig.getKeyGenerators().values());
    }
    
    @Override
    public DatabaseRule build(final ShardingSphereDatabase database, final RuleConfiguration ruleConfig, final InstanceContext instanceContext) {
        ShardingRuleConfiguration shardingRuleConfig = (ShardingRuleConfiguration) ruleConfig;
        Map<String, DataSource> dataSources = database.getResourceMetaData().getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, storageUnit -> storageUnit.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return new ShardingRule(shardingRuleConfig, dataSources, instanceContext);
    }
    
    private void checkLogicTables(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> tablesLogicTables = currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        Collection<String> autoTablesLogicTables = currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        Collection<String> allLogicTables = new LinkedList<>();
        allLogicTables.addAll(tablesLogicTables);
        allLogicTables.addAll(autoTablesLogicTables);
        Set<String> duplicatedLogicTables = allLogicTables.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedLogicTables.isEmpty(), () -> new DuplicateRuleException("sharding", databaseName, duplicatedLogicTables));
    }
    
    private void checkResources(final String databaseName, final ShardingSphereDatabase database, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> requiredResource = getRequiredResources(currentRuleConfig);
        Collection<String> notExistedResources = database.getResourceMetaData().getNotExistedDataSources(requiredResource);
        Collection<String> logicResources = getLogicResources(database);
        notExistedResources.removeIf(logicResources::contains);
        ShardingSpherePreconditions.checkState(notExistedResources.isEmpty(), () -> new MissingRequiredStorageUnitsException(databaseName, notExistedResources));
    }
    
    private Collection<String> getRequiredResources(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        currentRuleConfig.getTables().forEach(each -> result.addAll(getDataSourceNames(each)));
        currentRuleConfig.getAutoTables().forEach(each -> result.addAll(getDataSourceNames(each)));
        return result;
    }
    
    private Collection<String> getDataSourceNames(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        Collection<String> actualDataSources = InlineExpressionParserFactory.newInstance(shardingAutoTableRuleConfig.getActualDataSources()).splitAndEvaluate();
        return new HashSet<>(actualDataSources);
    }
    
    private Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Collection<String> actualDataNodes = InlineExpressionParserFactory.newInstance(shardingTableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        return actualDataNodes.stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private Collection<String> getLogicResources(final ShardingSphereDatabase database) {
        Collection<String> result = new LinkedHashSet<>();
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            each.getRuleIdentifiers().findIdentifier(DataSourceMapperRule.class).ifPresent(optional -> result.addAll(optional.getDataSourceMapper().keySet()));
        }
        return result;
    }
    
    private void checkShardingAlgorithms(final Collection<AlgorithmConfiguration> algorithmConfigs) {
        algorithmConfigs.forEach(each -> TypedSPILoader.checkService(ShardingAlgorithm.class, each.getType(), each.getProps()));
    }
    
    private void checkKeyGeneratorAlgorithms(final Collection<AlgorithmConfiguration> algorithmConfigs) {
        algorithmConfigs.stream().filter(Objects::nonNull).forEach(each -> TypedSPILoader.checkService(KeyGenerateAlgorithm.class, each.getType(), each.getProps()));
    }
    
    @Override
    public Class<? extends RuleConfiguration> getType() {
        return ShardingRuleConfiguration.class;
    }
}
