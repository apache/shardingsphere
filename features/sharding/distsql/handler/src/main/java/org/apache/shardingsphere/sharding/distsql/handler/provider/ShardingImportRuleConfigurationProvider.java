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
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.keygen.core.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharding import rule configuration provider.
 */
public final class ShardingImportRuleConfigurationProvider implements ImportRuleConfigurationProvider<ShardingRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ShardingRuleConfiguration ruleConfig) {
        checkLogicTables(databaseName, ruleConfig);
        checkShardingAlgorithms(ruleConfig.getShardingAlgorithms().values());
        checkKeyGeneratorAlgorithms(ruleConfig.getKeyGenerators().values());
    }
    
    private void checkLogicTables(final String databaseName, final ShardingRuleConfiguration ruleConfig) {
        Collection<String> logicTables = ruleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        logicTables.addAll(ruleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        Set<String> duplicatedLogicTables = logicTables.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedLogicTables.isEmpty(), () -> new DuplicateRuleException("sharding", databaseName, duplicatedLogicTables));
    }
    
    private void checkShardingAlgorithms(final Collection<AlgorithmConfiguration> algorithmConfigs) {
        algorithmConfigs.forEach(each -> TypedSPILoader.checkService(ShardingAlgorithm.class, each.getType(), each.getProps()));
    }
    
    private void checkKeyGeneratorAlgorithms(final Collection<AlgorithmConfiguration> algorithmConfigs) {
        algorithmConfigs.stream().filter(Objects::nonNull).forEach(each -> TypedSPILoader.checkService(KeyGenerateAlgorithm.class, each.getType(), each.getProps()));
    }
    
    @Override
    public Collection<String> getRequiredDataSourceNames(final ShardingRuleConfiguration ruleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        ruleConfig.getTables().forEach(each -> result.addAll(getDataSourceNames(each)));
        ruleConfig.getAutoTables().forEach(each -> result.addAll(getDataSourceNames(each)));
        return result;
    }
    
    private Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Collection<String> actualDataNodes = InlineExpressionParserFactory.newInstance(shardingTableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        return actualDataNodes.stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private Collection<String> getDataSourceNames(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        return new HashSet<>(InlineExpressionParserFactory.newInstance(shardingAutoTableRuleConfig.getActualDataSources()).splitAndEvaluate());
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getType() {
        return ShardingRuleConfiguration.class;
    }
}
