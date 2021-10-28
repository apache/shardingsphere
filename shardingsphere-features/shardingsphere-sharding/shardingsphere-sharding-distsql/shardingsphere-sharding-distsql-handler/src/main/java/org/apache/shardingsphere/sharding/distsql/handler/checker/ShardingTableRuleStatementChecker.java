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

package org.apache.shardingsphere.sharding.distsql.handler.checker;

import com.google.common.base.Splitter;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.support.InlineExpressionParser;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharding table rule checker.
 */
public final class ShardingTableRuleStatementChecker {
    
    private static final String DELIMITER = ".";
    
    /**
     * Check create sharing table rule statement.
     *
     * @param shardingSphereMetaData ShardingSphere meta data
     * @param rules rules
     * @param currentRuleConfig current rule configuration
     * @throws DistSQLException definition violation exception
     */
    public static void checkCreation(final ShardingSphereMetaData shardingSphereMetaData, final Collection<AbstractTableRuleSegment> rules,
                                     final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        check(shardingSphereMetaData, rules, currentRuleConfig, true);
    }
    
    /**
     * Check alter sharing table rule statement.
     *
     * @param shardingSphereMetaData ShardingSphere meta data
     * @param rules rules
     * @param currentRuleConfig current rule configuration
     * @throws DistSQLException definition violation exception
     */
    public static void checkAlteration(final ShardingSphereMetaData shardingSphereMetaData, final Collection<AbstractTableRuleSegment> rules,
                                       final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        check(shardingSphereMetaData, rules, currentRuleConfig, false);
    }
    
    private static void check(final ShardingSphereMetaData shardingSphereMetaData, final Collection<AbstractTableRuleSegment> rules,
                              final ShardingRuleConfiguration currentRuleConfig, final boolean isCreate) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkShardingTables(schemaName, rules, currentRuleConfig, isCreate);
        checkResources(schemaName, rules, shardingSphereMetaData);
        checkKeyGenerators(rules);
        Map<String, List<AbstractTableRuleSegment>> groupedTableRule = groupingByClassType(rules);
        checkAutoTableRule(groupedTableRule.getOrDefault(AutoTableRuleSegment.class.getSimpleName(), Collections.emptyList()));
        checkTableRule(schemaName, currentRuleConfig, groupedTableRule.getOrDefault(TableRuleSegment.class.getSimpleName(), Collections.emptyList()));
    }
    
    private static void checkResources(final String schemaName, final Collection<AbstractTableRuleSegment> rules, final ShardingSphereMetaData shardingSphereMetaData) throws DistSQLException {
        Collection<String> requiredResource = getRequiredResources(rules);
        Collection<String> notExistedResources = shardingSphereMetaData.getResource().getNotExistedResources(requiredResource);
        Collection<String> logicResources = getLogicResources(shardingSphereMetaData);
        notExistedResources.removeIf(logicResources::contains);
        DistSQLException.predictionThrow(notExistedResources.isEmpty(), new RequiredResourceMissedException(schemaName, notExistedResources));
    }
    
    private static Collection<String> getLogicResources(final ShardingSphereMetaData shardingSphereMetaData) {
        return shardingSphereMetaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataSourceContainedRule)
                .map(each -> ((DataSourceContainedRule) each).getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private static Collection<String> getRequiredResources(final Collection<AbstractTableRuleSegment> rules) {
        return rules.stream().map(AbstractTableRuleSegment::getDataSourceNodes).flatMap(Collection::stream)
                .map(ShardingTableRuleStatementChecker::parseDateSource).map(ShardingTableRuleStatementChecker::getDataSourceNames)
                .flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private static Collection<String> parseDateSource(final String dateSource) {
        return InlineExpressionParser.isInlineExpression(dateSource) ? new InlineExpressionParser(dateSource).splitAndEvaluate() : Collections.singletonList(dateSource);
    }
    
    private static Collection<String> getDataSourceNames(final Collection<String> actualDataNodes) {
        return actualDataNodes.stream().map(each -> {
            if (isValidDataNode(each)) {
                return actualDataNodes.stream().map(each1 -> new DataNode(each1).getDataSourceName()).collect(Collectors.toList());
            }
            return Collections.singletonList(each);
        }).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private static boolean isValidDataNode(final String dataNodeStr) {
        return dataNodeStr.contains(DELIMITER) && 2 == Splitter.on(DELIMITER).omitEmptyStrings().splitToList(dataNodeStr).size();
    }
    
    private static void checkShardingTables(final String schemaName, final Collection<AbstractTableRuleSegment> rules,
                                            final ShardingRuleConfiguration currentRuleConfig, final boolean isCreate) throws DistSQLException {
        LinkedList<String> requiredShardingTables = rules.stream().map(AbstractTableRuleSegment::getLogicTable).collect(Collectors.toCollection(LinkedList::new));
        Set<String> duplicatedShardingTables = getDuplicate(requiredShardingTables);
        DistSQLException.predictionThrow(duplicatedShardingTables.isEmpty(), new DuplicateRuleException("sharding", schemaName, duplicatedShardingTables));
        Collection<String> currentShardingTables = null == currentRuleConfig ? Collections.emptyList() : getCurrentShardingTables(currentRuleConfig);
        if (isCreate) {
            Set<String> identical = getIdentical(requiredShardingTables, currentShardingTables);
            DistSQLException.predictionThrow(identical.isEmpty(), new DuplicateRuleException("sharding", schemaName, identical));
        } else {
            Set<String> different = getDifferent(requiredShardingTables, currentShardingTables);
            DistSQLException.predictionThrow(different.isEmpty(), new RequiredRuleMissedException("sharding", schemaName, different));
        }
    }
    
    private static Set<String> getDuplicate(final Collection<String> collection) {
        return collection.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
    }
    
    private static Set<String> getIdentical(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(current::contains).collect(Collectors.toSet());
    }
    
    private static Set<String> getDifferent(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(each -> !current.contains(each)).collect(Collectors.toSet());
    }
    
    private static Collection<String> getCurrentShardingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    private static void checkKeyGenerators(final Collection<AbstractTableRuleSegment> rules) throws DistSQLException {
        Set<String> requiredKeyGenerators = rules.stream().filter(each -> Objects.nonNull(each.getKeyGenerateSegment()))
                .map(each -> each.getKeyGenerateSegment().getKeyGenerateAlgorithmSegment().getName()).collect(Collectors.toSet());
        Collection<String> invalidKeyGenerators = requiredKeyGenerators.stream().distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(KeyGenerateAlgorithm.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidKeyGenerators.isEmpty(), new InvalidAlgorithmConfigurationException("key generator", invalidKeyGenerators));
    }
    
    private static void checkAutoTableRule(final Collection<AbstractTableRuleSegment> rules) throws DistSQLException {
        Collection<AutoTableRuleSegment> autoTableRules = rules.stream().map(each -> (AutoTableRuleSegment) each).collect(Collectors.toCollection(LinkedList::new));
        Optional<AlgorithmSegment> anyAutoTableRule = autoTableRules.stream().map(AutoTableRuleSegment::getShardingAlgorithmSegment).filter(Objects::nonNull).findAny();
        if (anyAutoTableRule.isPresent()) {
            checkShardingAlgorithms(autoTableRules);
        }
    }
    
    private static void checkShardingAlgorithms(final Collection<AutoTableRuleSegment> rules) throws DistSQLException {
        LinkedList<AutoTableRuleSegment> incompleteShardingRules = rules.stream().filter(each -> !each.isCompleteShardingAlgorithm()).collect(Collectors.toCollection(LinkedList::new));
        DistSQLException.predictionThrow(incompleteShardingRules.isEmpty(), new InvalidAlgorithmConfigurationException("sharding"));
        Collection<String> invalidShardingAlgorithms = rules.stream().map(each -> each.getShardingAlgorithmSegment().getName()).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ShardingAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidShardingAlgorithms.isEmpty(), new InvalidAlgorithmConfigurationException("sharding", invalidShardingAlgorithms));
    }
    
    private static void checkTableRule(final String schemaName, final ShardingRuleConfiguration currentRuleConfig, final Collection<AbstractTableRuleSegment> rules) throws DistSQLException {
        Collection<TableRuleSegment> tableRules = rules.stream().map(each -> (TableRuleSegment) each).collect(Collectors.toCollection(LinkedList::new));
        Optional<ShardingStrategySegment> anyTableRule = tableRules.stream().map(each -> Arrays.asList(each.getTableStrategySegment(), each.getTableStrategySegment()))
                .flatMap(Collection::stream).filter(Objects::nonNull).findAny();
        if (anyTableRule.isPresent()) {
            checkStrategy(schemaName, currentRuleConfig, tableRules);
        }
    }
    
    private static void checkStrategy(final String schemaName, final ShardingRuleConfiguration currentRuleConfig, final Collection<TableRuleSegment> rules) throws DistSQLException {
        Set<String> algorithms = currentRuleConfig.getShardingAlgorithms().keySet();
        LinkedList<String> invalidAlgorithms = rules.stream().map(each -> Arrays.asList(each.getDatabaseStrategySegment(), each.getTableStrategySegment()))
                .flatMap(Collection::stream).filter(Objects::nonNull).filter(each -> !ShardingStrategyType.contain(each.getType()) || !algorithms.contains(each.getShardingAlgorithmName()))
                .map(ShardingStrategySegment::getShardingAlgorithmName).collect(Collectors.toCollection(LinkedList::new));
        DistSQLException.predictionThrow(invalidAlgorithms.isEmpty(), new InvalidAlgorithmConfigurationException(schemaName, invalidAlgorithms));
    }
    
    private static Map<String, List<AbstractTableRuleSegment>> groupingByClassType(final Collection<AbstractTableRuleSegment> rules) {
        return rules.stream().collect(Collectors.groupingBy(each -> each.getClass().getSimpleName()));
    }
}
