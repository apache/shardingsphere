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

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.expr.InlineExpressionParser;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingRuleNotFoundException;
import org.apache.shardingsphere.sharding.factory.KeyGenerateAlgorithmFactory;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;
import org.apache.shardingsphere.sharding.factory.ShardingAuditAlgorithmFactory;
import org.apache.shardingsphere.sharding.rule.BindingTableCheckedConfiguration;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
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
        checkTables(databaseName, rules, currentRuleConfig, isCreate);
        checkResources(databaseName, rules, database);
        checkKeyGenerators(rules, currentRuleConfig);
        checkAuditors(rules, currentRuleConfig);
        Map<String, List<AbstractTableRuleSegment>> groupedTableRule = groupingByClassType(rules);
        checkAutoTableRule(groupedTableRule.getOrDefault(AutoTableRuleSegment.class.getSimpleName(), Collections.emptyList()));
        checkTableRule(databaseName, currentRuleConfig, groupedTableRule.getOrDefault(TableRuleSegment.class.getSimpleName(), Collections.emptyList()));
        if (!isCreate) {
            checkBindingTableRules(rules, currentRuleConfig);
        }
    }
    
    private static boolean check(final ShardingRuleConfiguration checkedConfig, final Collection<String> dataSourceNames) {
        Collection<String> allDataSourceNames = getDataSourceNames(checkedConfig.getTables(), checkedConfig.getAutoTables(), dataSourceNames);
        Map<String, ShardingAlgorithm> shardingAlgorithms = new HashMap<>(checkedConfig.getShardingAlgorithms().size(), 1);
        Map<String, TableRule> tableRules = new HashMap<>();
        checkedConfig.getShardingAlgorithms().forEach((key, value) -> shardingAlgorithms.put(key, ShardingAlgorithmFactory.newInstance(value)));
        tableRules.putAll(createTableRules(checkedConfig.getTables(), checkedConfig.getDefaultKeyGenerateStrategy(), allDataSourceNames));
        tableRules.putAll(createAutoTableRules(checkedConfig.getAutoTables(), shardingAlgorithms, checkedConfig.getDefaultKeyGenerateStrategy(), allDataSourceNames));
        Collection<String> broadcastTables = createBroadcastTables(checkedConfig.getBroadcastTables());
        ShardingStrategyConfiguration defaultDatabaseShardingStrategyConfig = null == checkedConfig.getDefaultDatabaseShardingStrategy()
                ? new NoneShardingStrategyConfiguration()
                : checkedConfig.getDefaultDatabaseShardingStrategy();
        ShardingStrategyConfiguration defaultTableShardingStrategyConfig = null == checkedConfig.getDefaultTableShardingStrategy()
                ? new NoneShardingStrategyConfiguration()
                : checkedConfig.getDefaultTableShardingStrategy();
        return isValidBindingTableConfiguration(tableRules, new BindingTableCheckedConfiguration(allDataSourceNames, shardingAlgorithms, checkedConfig.getBindingTableGroups(), broadcastTables,
                defaultDatabaseShardingStrategyConfig, defaultTableShardingStrategyConfig, checkedConfig.getDefaultShardingColumn()));
    }
    
    private static void checkResources(final String databaseName, final Collection<AbstractTableRuleSegment> rules, final ShardingSphereDatabase database) throws DistSQLException {
        Collection<String> requiredResource = getRequiredResources(rules);
        Collection<String> notExistedResources = database.getResource().getNotExistedResources(requiredResource);
        Collection<String> logicResources = getLogicResources(database);
        notExistedResources.removeIf(logicResources::contains);
        ShardingSpherePreconditions.checkState(notExistedResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistedResources));
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
        return new InlineExpressionParser(dateSource).splitAndEvaluate();
    }
    
    private static Collection<String> getDataSourceNames(final Collection<String> actualDataNodes) {
        Collection<String> result = new HashSet<>();
        for (String each : actualDataNodes) {
            result.add(isValidDataNode(each) ? new DataNode(each).getDataSourceName() : each);
        }
        return result;
    }
    
    private static Collection<String> getDataSourceNames(final Collection<ShardingTableRuleConfiguration> tableRuleConfigs,
                                                         final Collection<ShardingAutoTableRuleConfiguration> autoTableRuleConfigs, final Collection<String> dataSourceNames) {
        if (tableRuleConfigs.isEmpty() && autoTableRuleConfigs.isEmpty()) {
            return dataSourceNames;
        }
        if (tableRuleConfigs.stream().map(ShardingTableRuleConfiguration::getActualDataNodes).anyMatch(each -> null == each || each.isEmpty())) {
            return dataSourceNames;
        }
        Collection<String> result = new LinkedHashSet<>();
        tableRuleConfigs.forEach(each -> result.addAll(getDataSourceNames(each)));
        autoTableRuleConfigs.forEach(each -> result.addAll(getDataSourceNames(each)));
        return result;
    }
    
    private static Collection<String> getDataSourceNames(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        Collection<String> actualDataSources = new InlineExpressionParser(shardingAutoTableRuleConfig.getActualDataSources()).splitAndEvaluate();
        return new HashSet<>(actualDataSources);
    }
    
    private static Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        Collection<String> actualDataNodes = new InlineExpressionParser(shardingTableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        return actualDataNodes.stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private static boolean isValidDataNode(final String dataNodeStr) {
        return dataNodeStr.contains(DELIMITER) && 2 == Splitter.on(DELIMITER).omitEmptyStrings().splitToList(dataNodeStr).size();
    }
    
    private static void checkTables(final String databaseName, final Collection<AbstractTableRuleSegment> rules,
                                    final ShardingRuleConfiguration currentRuleConfig, final boolean isCreate) throws DistSQLException {
        Collection<String> requiredTables = rules.stream().map(AbstractTableRuleSegment::getLogicTable).collect(Collectors.toList());
        Collection<String> duplicatedTables = getDuplicate(requiredTables);
        ShardingSpherePreconditions.checkState(duplicatedTables.isEmpty(), () -> new DuplicateRuleException("sharding", databaseName, duplicatedTables));
        Collection<String> currentShardingTables = null == currentRuleConfig ? Collections.emptyList() : getCurrentShardingTables(currentRuleConfig);
        if (isCreate) {
            Collection<String> identical = getIdentical(requiredTables, currentShardingTables);
            ShardingSpherePreconditions.checkState(identical.isEmpty(), () -> new DuplicateRuleException("sharding", databaseName, identical));
        } else {
            Collection<String> different = getDifferent(requiredTables, currentShardingTables);
            ShardingSpherePreconditions.checkState(different.isEmpty(), () -> new MissingRequiredRuleException("sharding", databaseName, different));
        }
    }
    
    private static Collection<String> getDuplicate(final Collection<String> collection) {
        Collection<String> duplicate = collection.stream().collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
        return collection.stream().filter(each -> containsIgnoreCase(duplicate, each)).collect(Collectors.toSet());
    }
    
    private static Collection<String> getIdentical(final Collection<String> require, final Collection<String> current) {
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
        ShardingSpherePreconditions.checkState(notExistKeyGenerator.isEmpty(), () -> new MissingRequiredAlgorithmException("key generator", notExistKeyGenerator));
        Collection<String> invalidKeyGenerators = requiredKeyGenerators.stream().distinct().filter(each -> !KeyGenerateAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidKeyGenerators.isEmpty(), () -> new InvalidAlgorithmConfigurationException("key generator", invalidKeyGenerators));
    }
    
    private static void checkAuditors(final Collection<AbstractTableRuleSegment> rules, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Set<String> notExistAuditors = new LinkedHashSet<>();
        Set<String> requiredAuditors = new LinkedHashSet<>();
        Collection<AuditStrategySegment> auditStrategySegments = rules.stream().map(AbstractTableRuleSegment::getAuditStrategySegment).filter(Objects::nonNull).collect(Collectors.toList());
        Collection<String> auditorNames = new LinkedList<>();
        for (AuditStrategySegment each : auditStrategySegments) {
            auditorNames.addAll(each.getAuditorNames());
        }
        auditorNames.stream().filter(each -> null == currentRuleConfig || !currentRuleConfig.getAuditors().containsKey(each)).collect(Collectors.toList()).forEach(notExistAuditors::add);
        ShardingSpherePreconditions.checkState(notExistAuditors.isEmpty(), () -> new MissingRequiredAlgorithmException("auditor", notExistAuditors));
        for (AuditStrategySegment each : auditStrategySegments) {
            requiredAuditors.addAll(each.getShardingAuditorSegments().stream().map(ShardingAuditorSegment::getAlgorithmSegment).collect(Collectors.toList())
                    .stream().map(AlgorithmSegment::getName).collect(Collectors.toList()));
        }
        Collection<String> invalidAuditors = requiredAuditors.stream().distinct().filter(each -> !ShardingAuditAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidAuditors.isEmpty(), () -> new InvalidAlgorithmConfigurationException("auditor", invalidAuditors));
    }
    
    private static void checkAutoTableRule(final Collection<AbstractTableRuleSegment> rules) throws DistSQLException {
        Collection<AutoTableRuleSegment> autoTableRules = rules.stream().map(each -> (AutoTableRuleSegment) each).collect(Collectors.toList());
        Optional<AlgorithmSegment> anyAutoTableRule = autoTableRules.stream().map(AutoTableRuleSegment::getShardingAlgorithmSegment).filter(Objects::nonNull).findAny();
        if (anyAutoTableRule.isPresent()) {
            checkAutoTableShardingAlgorithms(autoTableRules);
        }
    }
    
    private static void checkAutoTableShardingAlgorithms(final Collection<AutoTableRuleSegment> rules) throws DistSQLException {
        Collection<AutoTableRuleSegment> incompleteShardingRules = rules.stream().filter(each -> !each.isCompleteShardingAlgorithm()).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(incompleteShardingRules.isEmpty(), () -> new InvalidAlgorithmConfigurationException("sharding"));
        Collection<String> invalidShardingAlgorithms = rules.stream().map(each -> each.getShardingAlgorithmSegment().getName()).distinct()
                .filter(each -> !ShardingAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidShardingAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException("sharding", invalidShardingAlgorithms));
        invalidShardingAlgorithms.addAll(rules.stream().map(AutoTableRuleSegment::getShardingAlgorithmSegment).map(each -> new AlgorithmConfiguration(each.getName(), each.getProps()))
                .map(ShardingAlgorithmFactory::newInstance).filter(each -> !(each instanceof ShardingAutoTableAlgorithm)).map(TypedSPI::getType).collect(Collectors.toList()));
        ShardingSpherePreconditions.checkState(invalidShardingAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException("sharding", invalidShardingAlgorithms));
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
        ShardingSpherePreconditions.checkState(invalidAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException(databaseName, invalidAlgorithms));
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
    
    private static void checkBindingTableRules(final Collection<AbstractTableRuleSegment> rules, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (null == currentRuleConfig || currentRuleConfig.getBindingTableGroups().isEmpty()) {
            return;
        }
        Collection<String> bindingTables = getCurrentBindingTables(currentRuleConfig);
        if (bindingTables.size() <= 1) {
            return;
        }
        ShardingRuleConfiguration toBeAlteredRuleConfig = ShardingTableRuleStatementConverter.convert(rules);
        Collection<String> toBeAlteredLogicTableNames = getAlteredLogicalTableNames(toBeAlteredRuleConfig);
        Collection<String> toBeAlteredBindingTableNames = toBeAlteredLogicTableNames.stream().filter(bindingTables::contains).collect(Collectors.toSet());
        if (toBeAlteredBindingTableNames.isEmpty()) {
            return;
        }
        ShardingRuleConfiguration toBeCheckedRuleConfig = new ShardingRuleConfiguration();
        toBeCheckedRuleConfig.setTables(new LinkedList<>(currentRuleConfig.getTables()));
        toBeCheckedRuleConfig.setAutoTables(new LinkedList<>(currentRuleConfig.getAutoTables()));
        toBeCheckedRuleConfig.setBindingTableGroups(new LinkedList<>(currentRuleConfig.getBindingTableGroups()));
        toBeCheckedRuleConfig.setBroadcastTables(new LinkedList<>(currentRuleConfig.getBroadcastTables()));
        toBeCheckedRuleConfig.setDefaultTableShardingStrategy(currentRuleConfig.getDefaultTableShardingStrategy());
        toBeCheckedRuleConfig.setDefaultDatabaseShardingStrategy(currentRuleConfig.getDefaultDatabaseShardingStrategy());
        toBeCheckedRuleConfig.setDefaultKeyGenerateStrategy(currentRuleConfig.getDefaultKeyGenerateStrategy());
        toBeCheckedRuleConfig.setDefaultShardingColumn(currentRuleConfig.getDefaultShardingColumn());
        toBeCheckedRuleConfig.setShardingAlgorithms(new LinkedHashMap<>(currentRuleConfig.getShardingAlgorithms()));
        toBeCheckedRuleConfig.setKeyGenerators(new LinkedHashMap<>(currentRuleConfig.getKeyGenerators()));
        toBeCheckedRuleConfig.setAuditors(new LinkedHashMap<>(currentRuleConfig.getAuditors()));
        removeRuleConfiguration(toBeCheckedRuleConfig, toBeAlteredRuleConfig);
        addRuleConfiguration(toBeCheckedRuleConfig, toBeAlteredRuleConfig);
        Collection<String> dataSourceNames = getRequiredResource(toBeCheckedRuleConfig);
        dataSourceNames.addAll(getRequiredResource(toBeAlteredRuleConfig));
        ShardingSpherePreconditions.checkState(check(toBeCheckedRuleConfig, dataSourceNames),
                () -> new InvalidRuleConfigurationException("sharding table", toBeAlteredLogicTableNames, Collections.singleton("invalid binding table configuration")));
    }
    
    private static Collection<String> getRequiredResource(final ShardingRuleConfiguration config) {
        Collection<String> result = new LinkedHashSet<>();
        result.addAll(config.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getActualDataSources)
                .map(each -> Splitter.on(",").trimResults().splitToList(each)).flatMap(Collection::stream).collect(Collectors.toSet()));
        result.addAll(config.getTables().stream().map(each -> new InlineExpressionParser(each.getActualDataNodes()).splitAndEvaluate())
                .flatMap(Collection::stream).distinct().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toSet()));
        return result;
    }
    
    private static boolean isValidBindingTableConfiguration(final Map<String, TableRule> tableRules, final BindingTableCheckedConfiguration checkedConfig) {
        for (String each : checkedConfig.getBindingTableGroups()) {
            Collection<String> bindingTables = Splitter.on(",").trimResults().splitToList(each.toLowerCase());
            if (bindingTables.size() <= 1) {
                continue;
            }
            Iterator<String> iterator = bindingTables.iterator();
            TableRule sampleTableRule = getTableRule(iterator.next(), checkedConfig.getDataSourceNames(), tableRules, checkedConfig.getBroadcastTables());
            while (iterator.hasNext()) {
                TableRule tableRule = getTableRule(iterator.next(), checkedConfig.getDataSourceNames(), tableRules, checkedConfig.getBroadcastTables());
                if (!isValidActualDataSourceName(sampleTableRule, tableRule) || !isValidActualTableName(sampleTableRule, tableRule)) {
                    return false;
                }
                if (!isValidShardingAlgorithm(sampleTableRule, tableRule, true, checkedConfig) || !isValidShardingAlgorithm(sampleTableRule, tableRule, false, checkedConfig)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean isValidActualDataSourceName(final TableRule sampleTableRule, final TableRule tableRule) {
        return sampleTableRule.getActualDataSourceNames().equals(tableRule.getActualDataSourceNames());
    }
    
    private static boolean isValidActualTableName(final TableRule sampleTableRule, final TableRule tableRule) {
        for (String each : sampleTableRule.getActualDataSourceNames()) {
            Collection<String> sampleActualTableNames =
                    sampleTableRule.getActualTableNames(each).stream().map(actualTableName -> actualTableName.replace(sampleTableRule.getTableDataNode().getPrefix(), "")).collect(Collectors.toSet());
            Collection<String> actualTableNames =
                    tableRule.getActualTableNames(each).stream().map(optional -> optional.replace(tableRule.getTableDataNode().getPrefix(), "")).collect(Collectors.toSet());
            if (!sampleActualTableNames.equals(actualTableNames)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isValidShardingAlgorithm(final TableRule sampleTableRule, final TableRule tableRule, final boolean databaseAlgorithm, final BindingTableCheckedConfiguration checkedConfig) {
        String sampleAlgorithmExpression = getAlgorithmExpression(sampleTableRule, databaseAlgorithm, checkedConfig);
        String algorithmExpression = getAlgorithmExpression(tableRule, databaseAlgorithm, checkedConfig);
        return sampleAlgorithmExpression.equalsIgnoreCase(algorithmExpression);
    }
    
    private static String getAlgorithmExpression(final TableRule tableRule, final boolean databaseAlgorithm, final BindingTableCheckedConfiguration checkedConfig) {
        ShardingStrategyConfiguration shardingStrategyConfig = databaseAlgorithm
                ? null == tableRule.getDatabaseShardingStrategyConfig() ? checkedConfig.getDefaultDatabaseShardingStrategyConfig() : tableRule.getDatabaseShardingStrategyConfig()
                : null == tableRule.getTableShardingStrategyConfig() ? checkedConfig.getDefaultTableShardingStrategyConfig() : tableRule.getTableShardingStrategyConfig();
        ShardingAlgorithm shardingAlgorithm = checkedConfig.getShardingAlgorithms().get(shardingStrategyConfig.getShardingAlgorithmName());
        String originAlgorithmExpression = null == shardingAlgorithm ? "" : shardingAlgorithm.getProps().getProperty("algorithm-expression", "");
        String sampleDataNodePrefix = databaseAlgorithm ? tableRule.getDataSourceDataNode().getPrefix() : tableRule.getTableDataNode().getPrefix();
        String shardingColumn = getShardingColumn(shardingStrategyConfig, checkedConfig.getDefaultShardingColumn());
        return originAlgorithmExpression.replaceFirst(sampleDataNodePrefix, "").replaceFirst(shardingColumn, "").replaceAll(" ", "");
    }
    
    private static String getShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfig, final String defaultShardingColumn) {
        String shardingColumn = defaultShardingColumn;
        if (shardingStrategyConfig instanceof ComplexShardingStrategyConfiguration) {
            shardingColumn = ((ComplexShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumns();
        }
        if (shardingStrategyConfig instanceof StandardShardingStrategyConfiguration) {
            shardingColumn = ((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn();
        }
        return null == shardingColumn ? "" : shardingColumn;
    }
    
    private static TableRule getTableRule(final String logicTableName, final Collection<String> dataSourceNames, final Map<String, TableRule> tableRules, final Collection<String> broadcastTables) {
        TableRule result = tableRules.get(logicTableName);
        if (null != result) {
            return result;
        }
        if (broadcastTables.contains(logicTableName)) {
            return new TableRule(dataSourceNames, logicTableName);
        }
        throw new ShardingRuleNotFoundException(Collections.singleton(logicTableName));
    }
    
    private static Collection<String> createBroadcastTables(final Collection<String> broadcastTables) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(broadcastTables);
        return result;
    }
    
    private static Map<String, TableRule> createAutoTableRules(final Collection<ShardingAutoTableRuleConfiguration> autoTableRuleConfigs, final Map<String, ShardingAlgorithm> shardingAlgorithms,
                                                               final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig, final Collection<String> dataSourceNames) {
        return autoTableRuleConfigs.stream().map(each -> createAutoTableRule(defaultKeyGenerateStrategyConfig, each, shardingAlgorithms, dataSourceNames))
                .collect(Collectors.toMap(each -> each.getLogicTable().toLowerCase(), Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static TableRule createAutoTableRule(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig, final ShardingAutoTableRuleConfiguration autoTableRuleConfig,
                                                 final Map<String, ShardingAlgorithm> shardingAlgorithms, final Collection<String> dataSourceNames) {
        ShardingAlgorithm shardingAlgorithm = null == autoTableRuleConfig.getShardingStrategy() ? null : shardingAlgorithms.get(autoTableRuleConfig.getShardingStrategy().getShardingAlgorithmName());
        Preconditions.checkState(shardingAlgorithm instanceof ShardingAutoTableAlgorithm, "Sharding auto table rule configuration must match sharding auto table algorithm.");
        return new TableRule(autoTableRuleConfig, dataSourceNames, (ShardingAutoTableAlgorithm) shardingAlgorithm, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig));
    }
    
    private static Map<String, TableRule> createTableRules(final Collection<ShardingTableRuleConfiguration> tableRuleConfigs, final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig,
                                                           final Collection<String> dataSourceNames) {
        return tableRuleConfigs.stream().map(each -> new TableRule(each, dataSourceNames, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig)))
                .collect(Collectors.toMap(each -> each.getLogicTable().toLowerCase(), Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static String getDefaultGenerateKeyColumn(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return Optional.ofNullable(defaultKeyGenerateStrategyConfig).map(KeyGenerateStrategyConfiguration::getColumn).orElse(null);
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
