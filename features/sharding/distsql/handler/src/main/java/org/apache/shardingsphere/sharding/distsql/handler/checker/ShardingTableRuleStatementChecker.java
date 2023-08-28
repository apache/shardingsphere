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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableRuleSegment;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmClassImplementationException;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingTableRuleNotFoundException;
import org.apache.shardingsphere.sharding.exception.strategy.InvalidShardingStrategyConfigurationException;
import org.apache.shardingsphere.sharding.rule.BindingTableCheckedConfiguration;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Sharding table rule checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingTableRuleStatementChecker {
    
    private static final String DELIMITER = ".";
    
    /**
     * Check create sharing table rule statement.
     *
     * @param database database
     * @param rules rules
     * @param ifNotExists if not exists
     * @param currentRuleConfig current rule configuration
     */
    public static void checkCreation(final ShardingSphereDatabase database, final Collection<AbstractTableRuleSegment> rules, final boolean ifNotExists,
                                     final ShardingRuleConfiguration currentRuleConfig) {
        check(database, rules, ifNotExists, currentRuleConfig, true);
    }
    
    /**
     * Check alter sharing table rule statement.
     *
     * @param database database
     * @param rules rules
     * @param currentRuleConfig current rule configuration
     */
    public static void checkAlteration(final ShardingSphereDatabase database, final Collection<AbstractTableRuleSegment> rules, final ShardingRuleConfiguration currentRuleConfig) {
        check(database, rules, false, currentRuleConfig, false);
    }
    
    /**
     * Judge whether binding table groups are valid.
     *
     * @param bindingTableGroups binding table groups
     * @param currentRuleConfig current rule configuration
     * @return binding table groups are valid or not
     */
    public static boolean isValidBindingTableGroups(final Collection<ShardingTableReferenceRuleConfiguration> bindingTableGroups, final ShardingRuleConfiguration currentRuleConfig) {
        ShardingRuleConfiguration toBeCheckedRuleConfig = createToBeCheckedShardingRuleConfiguration(currentRuleConfig);
        toBeCheckedRuleConfig.setBindingTableGroups(bindingTableGroups);
        Collection<String> dataSourceNames = getRequiredDataSources(toBeCheckedRuleConfig);
        dataSourceNames.addAll(getRequiredDataSources(currentRuleConfig));
        return check(toBeCheckedRuleConfig, dataSourceNames);
    }
    
    private static ShardingRuleConfiguration createToBeCheckedShardingRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setTables(new LinkedList<>(currentRuleConfig.getTables()));
        result.setAutoTables(new LinkedList<>(currentRuleConfig.getAutoTables()));
        result.setBindingTableGroups(new LinkedList<>(currentRuleConfig.getBindingTableGroups()));
        result.setDefaultTableShardingStrategy(currentRuleConfig.getDefaultTableShardingStrategy());
        result.setDefaultDatabaseShardingStrategy(currentRuleConfig.getDefaultDatabaseShardingStrategy());
        result.setDefaultKeyGenerateStrategy(currentRuleConfig.getDefaultKeyGenerateStrategy());
        result.setDefaultShardingColumn(currentRuleConfig.getDefaultShardingColumn());
        result.setShardingAlgorithms(new LinkedHashMap<>(currentRuleConfig.getShardingAlgorithms()));
        result.setKeyGenerators(new LinkedHashMap<>(currentRuleConfig.getKeyGenerators()));
        result.setAuditors(new LinkedHashMap<>(currentRuleConfig.getAuditors()));
        return result;
    }
    
    private static void check(final ShardingSphereDatabase database, final Collection<AbstractTableRuleSegment> rules, final boolean ifNotExists, final ShardingRuleConfiguration currentRuleConfig,
                              final boolean isCreated) {
        String databaseName = database.getName();
        checkTables(databaseName, rules, currentRuleConfig, isCreated, ifNotExists);
        checkDataSources(databaseName, rules, database);
        checkKeyGenerators(rules);
        checkAuditors(rules);
        checkAutoTableRule(rules.stream().filter(AutoTableRuleSegment.class::isInstance).map(AutoTableRuleSegment.class::cast).collect(Collectors.toList()));
        checkTableRule(databaseName, rules.stream().filter(TableRuleSegment.class::isInstance).map(TableRuleSegment.class::cast).collect(Collectors.toList()));
        if (!isCreated) {
            checkBindingTableRules(rules, currentRuleConfig);
        }
    }
    
    private static boolean check(final ShardingRuleConfiguration checkedConfig, final Collection<String> dataSourceNames) {
        Collection<String> allDataSourceNames = getDataSourceNames(checkedConfig.getTables(), checkedConfig.getAutoTables(), dataSourceNames);
        Map<String, ShardingAlgorithm> shardingAlgorithms = new HashMap<>(checkedConfig.getShardingAlgorithms().size(), 1F);
        Map<String, TableRule> tableRules = new HashMap<>();
        checkedConfig.getShardingAlgorithms().forEach((key, value) -> shardingAlgorithms.put(key, TypedSPILoader.getService(ShardingAlgorithm.class, value.getType(), value.getProps())));
        tableRules.putAll(createTableRules(checkedConfig.getTables(), checkedConfig.getDefaultKeyGenerateStrategy(), allDataSourceNames));
        tableRules.putAll(createAutoTableRules(checkedConfig.getAutoTables(), shardingAlgorithms, checkedConfig.getDefaultKeyGenerateStrategy(), allDataSourceNames));
        ShardingStrategyConfiguration defaultDatabaseShardingStrategyConfig = null == checkedConfig.getDefaultDatabaseShardingStrategy()
                ? new NoneShardingStrategyConfiguration()
                : checkedConfig.getDefaultDatabaseShardingStrategy();
        ShardingStrategyConfiguration defaultTableShardingStrategyConfig = null == checkedConfig.getDefaultTableShardingStrategy()
                ? new NoneShardingStrategyConfiguration()
                : checkedConfig.getDefaultTableShardingStrategy();
        return isValidBindingTableConfiguration(tableRules, new BindingTableCheckedConfiguration(allDataSourceNames, shardingAlgorithms, checkedConfig.getBindingTableGroups(),
                defaultDatabaseShardingStrategyConfig, defaultTableShardingStrategyConfig, checkedConfig.getDefaultShardingColumn()));
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
        autoTableRuleConfigs.forEach(each -> result.addAll(InlineExpressionParserFactory.newInstance().splitAndEvaluate(each.getActualDataSources())));
        return result;
    }
    
    private static Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        return InlineExpressionParserFactory.newInstance()
                .splitAndEvaluate(shardingTableRuleConfig.getActualDataNodes()).stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private static Collection<String> getDataSourceNames(final Collection<String> actualDataNodes) {
        Collection<String> result = new LinkedHashSet<>();
        for (String each : actualDataNodes) {
            result.add(isValidDataNode(each) ? new DataNode(each).getDataSourceName() : each);
        }
        return result;
    }
    
    private static boolean isValidDataNode(final String dataNodeStr) {
        return dataNodeStr.contains(DELIMITER) && 2 == Splitter.on(DELIMITER).omitEmptyStrings().splitToList(dataNodeStr).size();
    }
    
    private static Map<String, TableRule> createTableRules(final Collection<ShardingTableRuleConfiguration> tableRuleConfigs, final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig,
                                                           final Collection<String> dataSourceNames) {
        return tableRuleConfigs.stream().map(each -> new TableRule(each, dataSourceNames, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig)))
                .collect(Collectors.toMap(each -> each.getLogicTable().toLowerCase(), Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static String getDefaultGenerateKeyColumn(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return Optional.ofNullable(defaultKeyGenerateStrategyConfig).map(KeyGenerateStrategyConfiguration::getColumn).orElse(null);
    }
    
    private static Map<String, TableRule> createAutoTableRules(final Collection<ShardingAutoTableRuleConfiguration> autoTableRuleConfigs, final Map<String, ShardingAlgorithm> shardingAlgorithms,
                                                               final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig, final Collection<String> dataSourceNames) {
        return autoTableRuleConfigs.stream().map(each -> createAutoTableRule(defaultKeyGenerateStrategyConfig, each, shardingAlgorithms, dataSourceNames))
                .collect(Collectors.toMap(each -> each.getLogicTable().toLowerCase(), Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static TableRule createAutoTableRule(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig, final ShardingAutoTableRuleConfiguration autoTableRuleConfig,
                                                 final Map<String, ShardingAlgorithm> shardingAlgorithms, final Collection<String> dataSourceNames) {
        ShardingAlgorithm shardingAlgorithm = shardingAlgorithms.get(autoTableRuleConfig.getShardingStrategy().getShardingAlgorithmName());
        ShardingSpherePreconditions.checkState(shardingAlgorithm instanceof ShardingAutoTableAlgorithm,
                () -> new ShardingAlgorithmClassImplementationException(autoTableRuleConfig.getShardingStrategy().getShardingAlgorithmName(), ShardingAutoTableAlgorithm.class));
        return new TableRule(autoTableRuleConfig, dataSourceNames, (ShardingAutoTableAlgorithm) shardingAlgorithm, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig));
    }
    
    private static boolean isValidBindingTableConfiguration(final Map<String, TableRule> tableRules, final BindingTableCheckedConfiguration checkedConfig) {
        for (ShardingTableReferenceRuleConfiguration each : checkedConfig.getBindingTableGroups()) {
            Collection<String> bindingTables = Splitter.on(",").trimResults().splitToList(each.getReference().toLowerCase());
            if (bindingTables.size() <= 1) {
                return false;
            }
            Iterator<String> iterator = bindingTables.iterator();
            TableRule sampleTableRule = getTableRule(iterator.next(), tableRules);
            while (iterator.hasNext()) {
                TableRule tableRule = getTableRule(iterator.next(), tableRules);
                if (!isValidActualDataSourceName(sampleTableRule, tableRule) || !isValidActualTableName(sampleTableRule, tableRule)) {
                    return false;
                }
                if (isInvalidShardingAlgorithm(sampleTableRule, tableRule, true, checkedConfig) || isInvalidShardingAlgorithm(sampleTableRule, tableRule, false, checkedConfig)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static TableRule getTableRule(final String logicTableName, final Map<String, TableRule> tableRules) {
        TableRule result = tableRules.get(logicTableName);
        if (null != result) {
            return result;
        }
        throw new ShardingTableRuleNotFoundException(Collections.singleton(logicTableName));
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
    
    private static boolean isInvalidShardingAlgorithm(final TableRule sampleTableRule, final TableRule tableRule, final boolean databaseAlgorithm,
                                                      final BindingTableCheckedConfiguration checkedConfig) {
        return !getAlgorithmExpression(sampleTableRule, databaseAlgorithm, checkedConfig).equals(getAlgorithmExpression(tableRule, databaseAlgorithm, checkedConfig));
    }
    
    private static Optional<String> getAlgorithmExpression(final TableRule tableRule, final boolean databaseAlgorithm, final BindingTableCheckedConfiguration checkedConfig) {
        ShardingStrategyConfiguration shardingStrategyConfig = databaseAlgorithm
                ? getDatabaseShardingStrategyConfiguration(tableRule, checkedConfig)
                : getTableShardingStrategyConfiguration(tableRule, checkedConfig);
        ShardingAlgorithm shardingAlgorithm = checkedConfig.getShardingAlgorithms().get(shardingStrategyConfig.getShardingAlgorithmName());
        String dataNodePrefix = databaseAlgorithm ? tableRule.getDataSourceDataNode().getPrefix() : tableRule.getTableDataNode().getPrefix();
        String shardingColumn = getShardingColumn(shardingStrategyConfig, checkedConfig.getDefaultShardingColumn());
        return null == shardingAlgorithm ? Optional.empty() : shardingAlgorithm.getAlgorithmStructure(dataNodePrefix, shardingColumn);
    }
    
    private static ShardingStrategyConfiguration getDatabaseShardingStrategyConfiguration(final TableRule tableRule, final BindingTableCheckedConfiguration checkedConfig) {
        return null == tableRule.getDatabaseShardingStrategyConfig() ? checkedConfig.getDefaultDatabaseShardingStrategyConfig() : tableRule.getDatabaseShardingStrategyConfig();
    }
    
    private static ShardingStrategyConfiguration getTableShardingStrategyConfiguration(final TableRule tableRule, final BindingTableCheckedConfiguration checkedConfig) {
        return null == tableRule.getTableShardingStrategyConfig() ? checkedConfig.getDefaultTableShardingStrategyConfig() : tableRule.getTableShardingStrategyConfig();
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
    
    private static void checkDataSources(final String databaseName, final Collection<AbstractTableRuleSegment> rules, final ShardingSphereDatabase database) {
        Collection<String> requiredDataSource = getRequiredDataSources(rules);
        Collection<String> notExistedDataSources = database.getResourceMetaData().getNotExistedDataSources(requiredDataSource);
        Collection<String> logicDataSources = getLogicDataSources(database);
        notExistedDataSources.removeIf(logicDataSources::contains);
        ShardingSpherePreconditions.checkState(notExistedDataSources.isEmpty(), () -> new MissingRequiredStorageUnitsException(databaseName, notExistedDataSources));
    }
    
    private static Collection<String> getRequiredDataSources(final ShardingRuleConfiguration config) {
        Collection<String> result = new LinkedHashSet<>();
        result.addAll(config.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getActualDataSources)
                .map(each -> Splitter.on(",").trimResults().splitToList(each)).flatMap(Collection::stream).collect(Collectors.toSet()));
        result.addAll(config.getTables().stream().map(each -> InlineExpressionParserFactory.newInstance().splitAndEvaluate(each.getActualDataNodes()))
                .flatMap(Collection::stream).distinct().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toSet()));
        return result;
    }
    
    private static <T extends AbstractTableRuleSegment> Collection<String> getRequiredDataSources(final Collection<T> rules) {
        return rules.stream().map(AbstractTableRuleSegment::getDataSourceNodes).flatMap(Collection::stream)
                .map(ShardingTableRuleStatementChecker::parseDateSource).map(ShardingTableRuleStatementChecker::getDataSourceNames).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private static Collection<String> parseDateSource(final String dateSource) {
        return InlineExpressionParserFactory.newInstance().splitAndEvaluate(dateSource);
    }
    
    private static Collection<String> getLogicDataSources(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findRules(DataSourceContainedRule.class).stream()
                .map(each -> each.getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private static void checkTables(final String databaseName, final Collection<AbstractTableRuleSegment> rules, final ShardingRuleConfiguration currentRuleConfig, final boolean isCreate,
                                    final boolean ifNotExists) {
        Collection<String> requiredTables = rules.stream().map(AbstractTableRuleSegment::getLogicTable).collect(Collectors.toList());
        Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(requiredTables);
        ShardingSpherePreconditions.checkState(duplicatedRuleNames.isEmpty(), () -> new DuplicateRuleException("sharding", databaseName, duplicatedRuleNames));
        Collection<String> currentShardingTables = null == currentRuleConfig ? Collections.emptyList() : getCurrentShardingTables(currentRuleConfig);
        if (isCreate) {
            if (!ifNotExists) {
                duplicatedRuleNames.addAll(getDuplicatedRuleNames(requiredTables, currentShardingTables));
                ShardingSpherePreconditions.checkState(duplicatedRuleNames.isEmpty(), () -> new DuplicateRuleException("sharding", databaseName, duplicatedRuleNames));
            }
        } else {
            Collection<String> notExistsRules = getNotExistsRules(requiredTables, currentShardingTables);
            ShardingSpherePreconditions.checkState(notExistsRules.isEmpty(), () -> new MissingRequiredRuleException("sharding", databaseName, notExistsRules));
        }
    }
    
    private static Collection<String> getDuplicatedRuleNames(final Collection<String> collection) {
        Collection<String> duplicate = collection.stream().collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
        return collection.stream().filter(each -> containsIgnoreCase(duplicate, each)).collect(Collectors.toSet());
    }
    
    private static Collection<String> getDuplicatedRuleNames(final Collection<String> require, final Collection<String> current) {
        return require.stream().filter(each -> containsIgnoreCase(current, each)).collect(Collectors.toSet());
    }
    
    private static Set<String> getNotExistsRules(final Collection<String> require, final Collection<String> current) {
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
    
    private static void checkKeyGenerators(final Collection<AbstractTableRuleSegment> rules) {
        rules.stream().map(AbstractTableRuleSegment::getKeyGenerateStrategySegment).filter(Objects::nonNull)
                .map(KeyGenerateStrategySegment::getKeyGenerateAlgorithmSegment)
                .forEach(each -> TypedSPILoader.checkService(KeyGenerateAlgorithm.class, each.getName(), each.getProps()));
    }
    
    private static void checkAuditors(final Collection<AbstractTableRuleSegment> rules) {
        Collection<AuditStrategySegment> auditStrategySegments = rules.stream().map(AbstractTableRuleSegment::getAuditStrategySegment).filter(Objects::nonNull).collect(Collectors.toList());
        Collection<AlgorithmSegment> requiredAuditors = new LinkedHashSet<>();
        for (AuditStrategySegment each : auditStrategySegments) {
            requiredAuditors.addAll(each.getAuditorSegments().stream().map(ShardingAuditorSegment::getAlgorithmSegment).collect(Collectors.toList()));
        }
        requiredAuditors.forEach(each -> TypedSPILoader.checkService(ShardingAuditAlgorithm.class, each.getName(), each.getProps()));
    }
    
    private static void checkAutoTableRule(final Collection<AutoTableRuleSegment> autoTableRules) {
        checkAutoTableShardingAlgorithms(autoTableRules);
    }
    
    private static void checkAutoTableShardingAlgorithms(final Collection<AutoTableRuleSegment> autoTableRules) {
        autoTableRules.forEach(each -> {
            ShardingSpherePreconditions.checkState(TypedSPILoader.findService(
                    ShardingAlgorithm.class, each.getShardingAlgorithmSegment().getName(), each.getShardingAlgorithmSegment().getProps()).isPresent(),
                    () -> new InvalidAlgorithmConfigurationException("sharding", each.getShardingAlgorithmSegment().getName()));
            ShardingAlgorithm shardingAlgorithm = TypedSPILoader.getService(ShardingAlgorithm.class, each.getShardingAlgorithmSegment().getName(), each.getShardingAlgorithmSegment().getProps());
            ShardingSpherePreconditions.checkState(shardingAlgorithm instanceof ShardingAutoTableAlgorithm, () -> new InvalidAlgorithmConfigurationException("sharding", shardingAlgorithm.getType(),
                    String.format("auto sharding algorithm is required for rule `%s`", each.getLogicTable())));
        });
    }
    
    private static void checkTableRule(final String databaseName, final Collection<TableRuleSegment> tableRules) {
        checkStrategy(databaseName, tableRules);
    }
    
    private static void checkStrategy(final String databaseName, final Collection<TableRuleSegment> rules) {
        for (TableRuleSegment each : rules) {
            Optional<ShardingStrategySegment> databaseStrategySegment = Optional.ofNullable(each.getDatabaseStrategySegment());
            if (databaseStrategySegment.isPresent()) {
                if ("none".equalsIgnoreCase(databaseStrategySegment.get().getType())) {
                    Collection<String> requiredDataSources = getRequiredDataSources(rules);
                    ShardingSpherePreconditions.checkState(1 == requiredDataSources.size(),
                            () -> new InvalidShardingStrategyConfigurationException("database", databaseStrategySegment.get().getType(), "strategy does not match data nodes"));
                } else {
                    AlgorithmSegment databaseShardingAlgorithm = databaseStrategySegment.get().getShardingAlgorithm();
                    checkDatabaseShardingAlgorithm(databaseName, each, databaseShardingAlgorithm);
                }
            }
            Optional<ShardingStrategySegment> tableStrategySegment = Optional.ofNullable(each.getTableStrategySegment());
            if (tableStrategySegment.isPresent()) {
                if ("none".equalsIgnoreCase(tableStrategySegment.get().getType())) {
                    Collection<String> requiredTables = getRequiredTables(rules);
                    ShardingSpherePreconditions.checkState(1 == requiredTables.size(),
                            () -> new InvalidShardingStrategyConfigurationException("table", tableStrategySegment.get().getType(), "strategy does not match data nodes"));
                } else {
                    AlgorithmSegment tableShardingAlgorithm = tableStrategySegment.get().getShardingAlgorithm();
                    checkTableShardingAlgorithm(databaseName, each, tableShardingAlgorithm);
                }
            }
        }
    }
    
    private static void checkDatabaseShardingAlgorithm(final String databaseName, final TableRuleSegment each, final AlgorithmSegment databaseShardingAlgorithm) {
        if (null != databaseShardingAlgorithm) {
            ShardingAlgorithm shardingAlgorithm = TypedSPILoader.getService(ShardingAlgorithm.class, databaseShardingAlgorithm.getName(), databaseShardingAlgorithm.getProps());
            ShardingSpherePreconditions.checkState(!(shardingAlgorithm instanceof ShardingAutoTableAlgorithm),
                    () -> new InvalidAlgorithmConfigurationException("sharding", shardingAlgorithm.getType(),
                            String.format("auto sharding algorithm cannot be used to create a table in Table mode `%s`", each.getLogicTable())));
        }
        ShardingSpherePreconditions.checkState(isValidStrategy(each.getDatabaseStrategySegment()),
                () -> new InvalidAlgorithmConfigurationException(databaseName, null == databaseShardingAlgorithm ? null : databaseShardingAlgorithm.getName()));
    }
    
    private static void checkTableShardingAlgorithm(final String databaseName, final TableRuleSegment each, final AlgorithmSegment tableShardingAlgorithm) {
        if (null != tableShardingAlgorithm) {
            ShardingAlgorithm shardingAlgorithm = TypedSPILoader.getService(ShardingAlgorithm.class, tableShardingAlgorithm.getName(), tableShardingAlgorithm.getProps());
            ShardingSpherePreconditions.checkState(!(shardingAlgorithm instanceof ShardingAutoTableAlgorithm),
                    () -> new InvalidAlgorithmConfigurationException("sharding", shardingAlgorithm.getType(),
                            String.format("auto sharding algorithm cannot be used to create a table in Table mode `%s`", each.getLogicTable())));
        }
        ShardingSpherePreconditions.checkState(isValidStrategy(each.getTableStrategySegment()),
                () -> new InvalidAlgorithmConfigurationException(databaseName, null == tableShardingAlgorithm ? null : tableShardingAlgorithm.getName()));
    }
    
    private static boolean isValidStrategy(final ShardingStrategySegment shardingStrategySegment) {
        return ShardingStrategyType.getValueOf(shardingStrategySegment.getType()).isValid(shardingStrategySegment.getShardingColumn()) && null != shardingStrategySegment.getShardingAlgorithm();
    }
    
    private static Collection<String> getRequiredTables(final Collection<TableRuleSegment> rules) {
        return rules.stream().map(TableRuleSegment::getDataSourceNodes).flatMap(Collection::stream)
                .map(ShardingTableRuleStatementChecker::parseDateSource).map(ShardingTableRuleStatementChecker::getTableNames).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private static Collection<String> getTableNames(final Collection<String> actualDataNodes) {
        Collection<String> result = new HashSet<>();
        for (String each : actualDataNodes) {
            result.add(isValidDataNode(each) ? new DataNode(each).getTableName() : each);
        }
        return result;
    }
    
    private static void checkBindingTableRules(final Collection<AbstractTableRuleSegment> rules, final ShardingRuleConfiguration currentRuleConfig) {
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
        ShardingRuleConfiguration toBeCheckedRuleConfig = createToBeCheckedShardingRuleConfiguration(currentRuleConfig);
        removeRuleConfiguration(toBeCheckedRuleConfig, toBeAlteredRuleConfig);
        addRuleConfiguration(toBeCheckedRuleConfig, toBeAlteredRuleConfig);
        Collection<String> dataSourceNames = getRequiredDataSources(toBeCheckedRuleConfig);
        dataSourceNames.addAll(getRequiredDataSources(toBeAlteredRuleConfig));
        ShardingSpherePreconditions.checkState(check(toBeCheckedRuleConfig, dataSourceNames),
                () -> new InvalidRuleConfigurationException("sharding table", toBeAlteredLogicTableNames, Collections.singleton("invalid binding table configuration.")));
    }
    
    private static Collection<String> getCurrentBindingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        currentRuleConfig.getBindingTableGroups().forEach(each -> result.addAll(Splitter.on(",").trimResults().splitToList(each.getReference())));
        return result;
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
}
