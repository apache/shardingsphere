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
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.expr.InlineExpressionParser;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.CheckableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.factory.KeyGenerateAlgorithmFactory;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
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
     * @param database database
     * @param rules rules
     * @param currentRuleConfig current rule configuration
     * @throws DistSQLException definition violation exception
     */
    public static void checkCreation(final ShardingSphereDatabase database,
                                     final Collection<AbstractTableRuleSegment> rules, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        check(database, rules, currentRuleConfig, true);
    }
    
    /**
     * Check alter sharing table rule statement.
     *
     * @param database database
     * @param rules rules
     * @param currentRuleConfig current rule configuration
     * @throws DistSQLException definition violation exception
     */
    public static void checkAlteration(final ShardingSphereDatabase database,
                                       final Collection<AbstractTableRuleSegment> rules, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        check(database, rules, currentRuleConfig, false);
    }
    
    private static void check(final ShardingSphereDatabase database,
                              final Collection<AbstractTableRuleSegment> rules, final ShardingRuleConfiguration currentRuleConfig, final boolean isCreate) throws DistSQLException {
        String databaseName = database.getName();
        checkShardingTables(databaseName, rules, currentRuleConfig, isCreate);
        checkResources(databaseName, rules, database);
        checkKeyGenerators(rules, currentRuleConfig);
        Map<String, List<AbstractTableRuleSegment>> groupedTableRule = groupingByClassType(rules);
        checkAutoTableRule(groupedTableRule.getOrDefault(AutoTableRuleSegment.class.getSimpleName(), Collections.emptyList()));
        checkTableRule(databaseName, currentRuleConfig, groupedTableRule.getOrDefault(TableRuleSegment.class.getSimpleName(), Collections.emptyList()));
    }
    
    private static void checkResources(final String databaseName, final Collection<AbstractTableRuleSegment> rules, final ShardingSphereDatabase database) throws DistSQLException {
        Collection<String> requiredResource = getRequiredResources(rules);
        Collection<String> notExistedResources = database.getResource().getNotExistedResources(requiredResource);
        Collection<String> logicResources = getLogicResources(database);
        notExistedResources.removeIf(logicResources::contains);
        DistSQLException.predictionThrow(notExistedResources.isEmpty(), () -> new RequiredResourceMissedException(databaseName, notExistedResources));
    }
    
    private static Collection<String> getLogicResources(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataSourceContainedRule)
                .map(each -> ((DataSourceContainedRule) each).getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private static Collection<String> getRequiredResources(final Collection<AbstractTableRuleSegment> rules) {
        return rules.stream().map(AbstractTableRuleSegment::getDataSourceNodes).flatMap(Collection::stream)
                .map(ShardingTableRuleStatementChecker::parseDateSource).map(ShardingTableRuleStatementChecker::getDataSourceNames).flatMap(Collection::stream).collect(Collectors.toList());
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
    
    private static void checkShardingTables(final String databaseName, final Collection<AbstractTableRuleSegment> rules,
                                            final ShardingRuleConfiguration currentRuleConfig, final boolean isCreate) throws DistSQLException {
        Collection<String> requiredShardingTables = rules.stream().map(AbstractTableRuleSegment::getLogicTable).collect(Collectors.toList());
        Collection<String> duplicatedShardingTables = getDuplicate(requiredShardingTables);
        DistSQLException.predictionThrow(duplicatedShardingTables.isEmpty(), () -> new DuplicateRuleException("sharding", databaseName, duplicatedShardingTables));
        Collection<String> currentShardingTables = null == currentRuleConfig ? Collections.emptyList() : getCurrentShardingTables(currentRuleConfig);
        if (isCreate) {
            Set<String> identical = getIdentical(requiredShardingTables, currentShardingTables);
            DistSQLException.predictionThrow(identical.isEmpty(), () -> new DuplicateRuleException("sharding", databaseName, identical));
        } else {
            Set<String> different = getDifferent(requiredShardingTables, currentShardingTables);
            DistSQLException.predictionThrow(different.isEmpty(), () -> new RequiredRuleMissedException("sharding", databaseName, different));
        }
    }
    
    private static Set<String> getDuplicate(final Collection<String> collection) {
        Collection<String> duplicate = collection.stream().collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
        return collection.stream().filter(each -> containsIgnoreCase(duplicate, each)).collect(Collectors.toSet());
    }
    
    private static Set<String> getIdentical(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(each -> containsIgnoreCase(current, each)).collect(Collectors.toSet());
    }
    
    private static Set<String> getDifferent(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(each -> !containsIgnoreCase(current, each)).collect(Collectors.toSet());
    }
    
    private static boolean containsIgnoreCase(final Collection<String> collection, final String str) {
        return collection.stream().anyMatch(each -> each.equalsIgnoreCase(str));
    }
    
    private static Collection<String> getCurrentShardingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    private static void checkKeyGenerators(final Collection<AbstractTableRuleSegment> rules, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Set<String> notExistKeyGenerator = new LinkedHashSet<>(rules.size());
        Set<String> requiredKeyGenerators = new LinkedHashSet<>(rules.size());
        rules.stream().map(AbstractTableRuleSegment::getKeyGenerateStrategySegment).filter(Objects::nonNull)
                .peek(each -> each.getKeyGenerateAlgorithmName()
                        .filter(optional -> null == currentRuleConfig || !currentRuleConfig.getKeyGenerators().containsKey(optional)).ifPresent(notExistKeyGenerator::add))
                .filter(each -> !each.getKeyGenerateAlgorithmName().isPresent()).forEach(each -> requiredKeyGenerators.add(each.getKeyGenerateAlgorithmSegment().getName()));
        DistSQLException.predictionThrow(notExistKeyGenerator.isEmpty(), () -> new RequiredAlgorithmMissedException("key generator", notExistKeyGenerator));
        Collection<String> invalidKeyGenerators = requiredKeyGenerators.stream().distinct().filter(each -> !KeyGenerateAlgorithmFactory.contains(each)).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidKeyGenerators.isEmpty(), () -> new InvalidAlgorithmConfigurationException("key generator", invalidKeyGenerators));
    }
    
    private static void checkAutoTableRule(final Collection<AbstractTableRuleSegment> rules) throws DistSQLException {
        Collection<AutoTableRuleSegment> autoTableRules = rules.stream().map(each -> (AutoTableRuleSegment) each).collect(Collectors.toList());
        Optional<AlgorithmSegment> anyAutoTableRule = autoTableRules.stream().map(AutoTableRuleSegment::getShardingAlgorithmSegment).filter(Objects::nonNull).findAny();
        if (anyAutoTableRule.isPresent()) {
            checkShardingAlgorithms(autoTableRules);
        }
    }
    
    private static void checkShardingAlgorithms(final Collection<AutoTableRuleSegment> rules) throws DistSQLException {
        Collection<AutoTableRuleSegment> incompleteShardingRules = rules.stream().filter(each -> !each.isCompleteShardingAlgorithm()).collect(Collectors.toList());
        DistSQLException.predictionThrow(incompleteShardingRules.isEmpty(), () -> new InvalidAlgorithmConfigurationException("sharding"));
        Collection<String> invalidShardingAlgorithms = rules.stream().map(each -> each.getShardingAlgorithmSegment().getName()).distinct()
                .filter(each -> !ShardingAlgorithmFactory.contains(each)).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidShardingAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException("sharding", invalidShardingAlgorithms));
    }
    
    private static void checkTableRule(final String databaseName, final ShardingRuleConfiguration currentRuleConfig, final Collection<AbstractTableRuleSegment> rules) throws DistSQLException {
        Collection<TableRuleSegment> tableRules = rules.stream().map(each -> (TableRuleSegment) each).collect(Collectors.toList());
        Optional<ShardingStrategySegment> anyTableRule = tableRules.stream().map(each -> Arrays.asList(each.getDatabaseStrategySegment(), each.getTableStrategySegment()))
                .flatMap(Collection::stream).filter(Objects::nonNull).findAny();
        if (anyTableRule.isPresent()) {
            checkStrategy(databaseName, currentRuleConfig, tableRules);
        }
    }
    
    private static void checkStrategy(final String databaseName, final ShardingRuleConfiguration currentRuleConfig, final Collection<TableRuleSegment> rules) throws DistSQLException {
        Collection<String> currentAlgorithms = null == currentRuleConfig ? Collections.emptySet() : currentRuleConfig.getShardingAlgorithms().keySet();
        Collection<String> invalidAlgorithms = rules.stream().map(each -> Arrays.asList(each.getDatabaseStrategySegment(), each.getTableStrategySegment()))
                .flatMap(Collection::stream).filter(Objects::nonNull).filter(each -> isInvalidStrategy(currentAlgorithms, each))
                .map(ShardingStrategySegment::getShardingAlgorithmName).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException(databaseName, invalidAlgorithms));
    }
    
    private static boolean isInvalidStrategy(final Collection<String> currentAlgorithms, final ShardingStrategySegment shardingStrategySegment) {
        return !ShardingStrategyType.contain(shardingStrategySegment.getType())
                || !ShardingStrategyType.getValueOf(shardingStrategySegment.getType()).isValid(shardingStrategySegment.getShardingColumn())
                || !isAlgorithmExists(currentAlgorithms, shardingStrategySegment);
    }
    
    private static boolean isAlgorithmExists(final Collection<String> currentAlgorithms, final ShardingStrategySegment shardingStrategySegment) {
        if (null == shardingStrategySegment.getShardingAlgorithmName() && null != shardingStrategySegment.getAlgorithmSegment()) {
            return true;
        }
        return currentAlgorithms.contains(shardingStrategySegment.getShardingAlgorithmName());
    }
    
    private static Map<String, List<AbstractTableRuleSegment>> groupingByClassType(final Collection<AbstractTableRuleSegment> rules) {
        return rules.stream().collect(Collectors.groupingBy(each -> each.getClass().getSimpleName()));
    }
    
    /**
     * Check binding table rules.
     *
     * @param database database
     * @param currentRuleConfig current rule configuration
     * @param toBeAlteredRuleConfig to be altered rule configuration
     * @throws DistSQLException definition violation exception
     */
    public static void checkBindingTableRules(final ShardingSphereDatabase database, final ShardingRuleConfiguration currentRuleConfig,
                                              final ShardingRuleConfiguration toBeAlteredRuleConfig) throws DistSQLException {
        if (null == currentRuleConfig || currentRuleConfig.getBindingTableGroups().isEmpty()) {
            return;
        }
        Collection<String> bindingTables = getCurrentBindingTables(currentRuleConfig);
        if (bindingTables.size() <= 1) {
            return;
        }
        Collection<String> toBeAlteredLogicTableNames = getAlteredLogicalTableNames(toBeAlteredRuleConfig);
        Collection<String> toBeAlteredBindingTableNames = toBeAlteredLogicTableNames.stream().filter(each -> bindingTables.contains(each)).collect(Collectors.toSet());
        if (toBeAlteredBindingTableNames.isEmpty()) {
            return;
        }
        ShardingRuleConfiguration toBeCheckedRuleConfig = new ShardingRuleConfiguration();
        toBeCheckedRuleConfig.setTables(currentRuleConfig.getTables());
        toBeCheckedRuleConfig.setAutoTables(currentRuleConfig.getAutoTables());
        toBeCheckedRuleConfig.setBindingTableGroups(currentRuleConfig.getBindingTableGroups());
        toBeCheckedRuleConfig.setBroadcastTables(currentRuleConfig.getBroadcastTables());
        toBeCheckedRuleConfig.setDefaultTableShardingStrategy(currentRuleConfig.getDefaultTableShardingStrategy());
        toBeCheckedRuleConfig.setDefaultDatabaseShardingStrategy(currentRuleConfig.getDefaultDatabaseShardingStrategy());
        toBeCheckedRuleConfig.setDefaultKeyGenerateStrategy(currentRuleConfig.getDefaultKeyGenerateStrategy());
        toBeCheckedRuleConfig.setDefaultShardingColumn(currentRuleConfig.getDefaultShardingColumn());
        toBeCheckedRuleConfig.setShardingAlgorithms(currentRuleConfig.getShardingAlgorithms());
        toBeCheckedRuleConfig.setKeyGenerators(currentRuleConfig.getKeyGenerators());
        toBeCheckedRuleConfig.setScalingName(currentRuleConfig.getScalingName());
        toBeCheckedRuleConfig.setScaling(currentRuleConfig.getScaling());
        removeRuleConfiguration(toBeCheckedRuleConfig, toBeAlteredRuleConfig);
        addRuleConfiguration(toBeCheckedRuleConfig, toBeAlteredRuleConfig);
        Collection<String> dataSourceNames = toBeCheckedRuleConfig.getRequiredResource();
        dataSourceNames.addAll(toBeAlteredRuleConfig.getRequiredResource());
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof CheckableRule) {
                DistSQLException.predictionThrow(((CheckableRule) each).check(toBeCheckedRuleConfig, dataSourceNames),
                        () -> new InvalidRuleConfigurationException("sharding table", toBeAlteredLogicTableNames, Collections.singleton("invalid binding table configuration")));
            }
        }
    }
    
    private static void removeRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        Collection<String> toBeAlteredLogicTableNames = getAlteredLogicalTableNames(toBeAlteredRuleConfig);
        toBeAlteredLogicTableNames.forEach(each -> {
            currentRuleConfig.getTables().removeIf(table -> table.getLogicTable().equalsIgnoreCase(each));
            currentRuleConfig.getAutoTables().removeIf(table -> table.getLogicTable().equalsIgnoreCase(each));
        });
    }
    
    private static void addRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getTables().addAll(toBeAlteredRuleConfig.getTables());
        currentRuleConfig.getAutoTables().addAll(toBeAlteredRuleConfig.getAutoTables());
        currentRuleConfig.getShardingAlgorithms().putAll(toBeAlteredRuleConfig.getShardingAlgorithms());
        currentRuleConfig.getKeyGenerators().putAll(toBeAlteredRuleConfig.getKeyGenerators());
    }
    
    private static Collection<String> getAlteredLogicalTableNames(final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        Collection<String> result = toBeAlteredRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList());
        result.addAll(toBeAlteredRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    private static Collection<String> getCurrentBindingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        currentRuleConfig.getBindingTableGroups().forEach(each -> result.addAll(Splitter.on(",").trimResults().splitToList(each)));
        return result;
    }
}
