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

import com.cedarsoftware.util.CaseInsensitiveSet;
import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.core.exception.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
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
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.constant.ShardingTableConstants;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableRuleSegment;
import org.apache.shardingsphere.sharding.exception.algorithm.ShardingAlgorithmClassImplementationException;
import org.apache.shardingsphere.sharding.exception.metadata.DuplicateShardingActualDataNodeException;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingTableRuleNotFoundException;
import org.apache.shardingsphere.sharding.exception.strategy.InvalidShardingStrategyConfigurationException;
import org.apache.shardingsphere.sharding.rule.BindingTableCheckedConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
     * Check create sharding table rule statement.
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
     * Check alter sharding table rule statement.
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
        checkTableRule(rules.stream().filter(TableRuleSegment.class::isInstance).map(TableRuleSegment.class::cast).collect(Collectors.toList()));
        if (!isCreated) {
            checkBindingTableRules(rules, currentRuleConfig);
        }
    }
    
    private static boolean check(final ShardingRuleConfiguration checkedConfig, final Collection<String> dataSourceNames) {
        Collection<String> allDataSourceNames = getDataSourceNames(checkedConfig.getTables(), checkedConfig.getAutoTables(), dataSourceNames);
        Map<String, ShardingAlgorithm> shardingAlgorithms = new HashMap<>(checkedConfig.getShardingAlgorithms().size(), 1F);
        Map<String, ShardingTable> shardingTables = new HashMap<>();
        checkedConfig.getShardingAlgorithms().forEach((key, value) -> shardingAlgorithms.put(key, TypedSPILoader.getService(ShardingAlgorithm.class, value.getType(), value.getProps())));
        shardingTables.putAll(createShardingTables(checkedConfig.getTables(), checkedConfig.getDefaultKeyGenerateStrategy(), allDataSourceNames));
        shardingTables.putAll(createShardingAutoTables(checkedConfig.getAutoTables(), shardingAlgorithms, checkedConfig.getDefaultKeyGenerateStrategy(), allDataSourceNames));
        ShardingStrategyConfiguration defaultDatabaseShardingStrategyConfig = null == checkedConfig.getDefaultDatabaseShardingStrategy()
                ? new NoneShardingStrategyConfiguration()
                : checkedConfig.getDefaultDatabaseShardingStrategy();
        ShardingStrategyConfiguration defaultTableShardingStrategyConfig = null == checkedConfig.getDefaultTableShardingStrategy()
                ? new NoneShardingStrategyConfiguration()
                : checkedConfig.getDefaultTableShardingStrategy();
        return isValidBindingTableConfiguration(shardingTables, new BindingTableCheckedConfiguration(allDataSourceNames, shardingAlgorithms, checkedConfig.getBindingTableGroups(),
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
        autoTableRuleConfigs.forEach(each -> result.addAll(InlineExpressionParserFactory.newInstance(each.getActualDataSources()).splitAndEvaluate()));
        return result;
    }
    
    private static Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        return InlineExpressionParserFactory.newInstance(shardingTableRuleConfig.getActualDataNodes())
                .splitAndEvaluate().stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private static Collection<String> getDataSourceNames(final Collection<String> actualDataNodes) {
        Collection<String> result = new LinkedHashSet<>(actualDataNodes.size(), 1F);
        for (String each : actualDataNodes) {
            result.add(isValidDataNode(each) ? new DataNode(each).getDataSourceName() : each);
        }
        return result;
    }
    
    private static boolean isValidDataNode(final String dataNodeStr) {
        return dataNodeStr.contains(DELIMITER) && 2 == Splitter.on(DELIMITER).omitEmptyStrings().splitToList(dataNodeStr).size();
    }
    
    private static Map<String, ShardingTable> createShardingTables(final Collection<ShardingTableRuleConfiguration> tableRuleConfigs,
                                                                   final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig,
                                                                   final Collection<String> dataSourceNames) {
        return tableRuleConfigs.stream().map(each -> new ShardingTable(each, dataSourceNames, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig)))
                .collect(Collectors.toMap(each -> each.getLogicTable().toLowerCase(), Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static String getDefaultGenerateKeyColumn(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return Optional.ofNullable(defaultKeyGenerateStrategyConfig).map(KeyGenerateStrategyConfiguration::getColumn).orElse(null);
    }
    
    private static Map<String, ShardingTable> createShardingAutoTables(final Collection<ShardingAutoTableRuleConfiguration> autoTableRuleConfigs,
                                                                       final Map<String, ShardingAlgorithm> shardingAlgorithms,
                                                                       final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig, final Collection<String> dataSourceNames) {
        return autoTableRuleConfigs.stream().map(each -> createShardingAutoTable(defaultKeyGenerateStrategyConfig, each, shardingAlgorithms, dataSourceNames))
                .collect(Collectors.toMap(each -> each.getLogicTable().toLowerCase(), Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static ShardingTable createShardingAutoTable(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig, final ShardingAutoTableRuleConfiguration autoTableRuleConfig,
                                                         final Map<String, ShardingAlgorithm> shardingAlgorithms, final Collection<String> dataSourceNames) {
        ShardingAlgorithm shardingAlgorithm = shardingAlgorithms.get(autoTableRuleConfig.getShardingStrategy().getShardingAlgorithmName());
        ShardingSpherePreconditions.checkState(shardingAlgorithm instanceof ShardingAutoTableAlgorithm,
                () -> new ShardingAlgorithmClassImplementationException(autoTableRuleConfig.getShardingStrategy().getShardingAlgorithmName(), ShardingAutoTableAlgorithm.class));
        return new ShardingTable(autoTableRuleConfig, dataSourceNames, (ShardingAutoTableAlgorithm) shardingAlgorithm, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig));
    }
    
    private static boolean isValidBindingTableConfiguration(final Map<String, ShardingTable> shardingTables, final BindingTableCheckedConfiguration checkedConfig) {
        for (ShardingTableReferenceRuleConfiguration each : checkedConfig.getBindingTableGroups()) {
            Collection<String> bindingTables = Splitter.on(",").trimResults().splitToList(each.getReference().toLowerCase());
            if (bindingTables.size() <= 1) {
                return false;
            }
            Iterator<String> iterator = bindingTables.iterator();
            ShardingTable sampleShardingTable = getShardingTable(iterator.next(), shardingTables);
            while (iterator.hasNext()) {
                ShardingTable shardingTable = getShardingTable(iterator.next(), shardingTables);
                if (!isValidActualDataSourceName(sampleShardingTable, shardingTable) || !isValidActualTableName(sampleShardingTable, shardingTable)) {
                    return false;
                }
                if (isInvalidShardingAlgorithm(sampleShardingTable, shardingTable, true, checkedConfig) || isInvalidShardingAlgorithm(sampleShardingTable, shardingTable, false, checkedConfig)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static ShardingTable getShardingTable(final String logicTableName, final Map<String, ShardingTable> shardingTables) {
        ShardingTable result = shardingTables.get(logicTableName);
        if (null != result) {
            return result;
        }
        throw new ShardingTableRuleNotFoundException(Collections.singleton(logicTableName));
    }
    
    private static boolean isValidActualDataSourceName(final ShardingTable sampleShardingTable, final ShardingTable shardingTable) {
        return sampleShardingTable.getActualDataSourceNames().equals(shardingTable.getActualDataSourceNames());
    }
    
    private static boolean isValidActualTableName(final ShardingTable sampleShardingTable, final ShardingTable shardingTable) {
        for (String each : sampleShardingTable.getActualDataSourceNames()) {
            Collection<String> sampleActualTableNames =
                    sampleShardingTable.getActualTableNames(each).stream().map(actualTableName -> actualTableName.replace(sampleShardingTable.getTableDataNode().getPrefix(), ""))
                            .collect(Collectors.toSet());
            Collection<String> actualTableNames =
                    shardingTable.getActualTableNames(each).stream().map(optional -> optional.replace(shardingTable.getTableDataNode().getPrefix(), "")).collect(Collectors.toSet());
            if (!sampleActualTableNames.equals(actualTableNames)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isInvalidShardingAlgorithm(final ShardingTable sampleShardingTable, final ShardingTable shardingTable, final boolean databaseAlgorithm,
                                                      final BindingTableCheckedConfiguration checkedConfig) {
        return !getAlgorithmExpression(sampleShardingTable, databaseAlgorithm, checkedConfig).equals(getAlgorithmExpression(shardingTable, databaseAlgorithm, checkedConfig));
    }
    
    private static Optional<String> getAlgorithmExpression(final ShardingTable shardingTable, final boolean databaseAlgorithm, final BindingTableCheckedConfiguration checkedConfig) {
        ShardingStrategyConfiguration shardingStrategyConfig = databaseAlgorithm
                ? getDatabaseShardingStrategyConfiguration(shardingTable, checkedConfig)
                : getTableShardingStrategyConfiguration(shardingTable, checkedConfig);
        ShardingAlgorithm shardingAlgorithm = checkedConfig.getShardingAlgorithms().get(shardingStrategyConfig.getShardingAlgorithmName());
        String dataNodePrefix = databaseAlgorithm ? shardingTable.getDataSourceDataNode().getPrefix() : shardingTable.getTableDataNode().getPrefix();
        String shardingColumn = getShardingColumn(shardingStrategyConfig, checkedConfig.getDefaultShardingColumn());
        return null == shardingAlgorithm ? Optional.empty() : shardingAlgorithm.getAlgorithmStructure(dataNodePrefix, shardingColumn);
    }
    
    private static ShardingStrategyConfiguration getDatabaseShardingStrategyConfiguration(final ShardingTable shardingTable, final BindingTableCheckedConfiguration checkedConfig) {
        return null == shardingTable.getDatabaseShardingStrategyConfig() ? checkedConfig.getDefaultDatabaseShardingStrategyConfig() : shardingTable.getDatabaseShardingStrategyConfig();
    }
    
    private static ShardingStrategyConfiguration getTableShardingStrategyConfiguration(final ShardingTable shardingTable, final BindingTableCheckedConfiguration checkedConfig) {
        return null == shardingTable.getTableShardingStrategyConfig() ? checkedConfig.getDefaultTableShardingStrategyConfig() : shardingTable.getTableShardingStrategyConfig();
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
        ShardingSpherePreconditions.checkMustEmpty(notExistedDataSources, () -> new MissingRequiredStorageUnitsException(databaseName, notExistedDataSources));
    }
    
    private static Collection<String> getRequiredDataSources(final ShardingRuleConfiguration config) {
        Collection<String> result = new LinkedHashSet<>();
        result.addAll(config.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getActualDataSources)
                .map(each -> Splitter.on(",").trimResults().splitToList(each)).flatMap(Collection::stream).collect(Collectors.toSet()));
        result.addAll(config.getTables().stream().map(each -> InlineExpressionParserFactory.newInstance(each.getActualDataNodes()).splitAndEvaluate())
                .flatMap(Collection::stream).distinct().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toSet()));
        return result;
    }
    
    private static <T extends AbstractTableRuleSegment> Collection<String> getRequiredDataSources(final Collection<T> rules) {
        return rules.stream().map(AbstractTableRuleSegment::getDataSourceNodes).flatMap(Collection::stream)
                .map(ShardingTableRuleStatementChecker::parseDateSource).map(ShardingTableRuleStatementChecker::getDataSourceNames).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private static Collection<String> parseDateSource(final String dateSource) {
        return InlineExpressionParserFactory.newInstance(dateSource).splitAndEvaluate();
    }
    
    private static Collection<String> getLogicDataSources(final ShardingSphereDatabase database) {
        Collection<String> result = new LinkedHashSet<>();
        for (DataSourceMapperRuleAttribute each : database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)) {
            result.addAll(each.getDataSourceMapper().keySet());
        }
        return result;
    }
    
    private static void checkTables(final String databaseName, final Collection<AbstractTableRuleSegment> rules, final ShardingRuleConfiguration currentRuleConfig, final boolean isCreate,
                                    final boolean ifNotExists) {
        Collection<String> requiredTables = rules.stream().map(AbstractTableRuleSegment::getLogicTable).collect(Collectors.toList());
        Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(requiredTables);
        ShardingSpherePreconditions.checkMustEmpty(duplicatedRuleNames, () -> new DuplicateRuleException("sharding", databaseName, duplicatedRuleNames));
        Collection<String> currentShardingTables = null == currentRuleConfig ? Collections.emptyList() : getCurrentShardingTables(currentRuleConfig);
        if (isCreate) {
            if (!ifNotExists) {
                duplicatedRuleNames.addAll(getDuplicatedRuleNames(requiredTables, currentShardingTables));
                ShardingSpherePreconditions.checkMustEmpty(duplicatedRuleNames, () -> new DuplicateRuleException("sharding", databaseName, duplicatedRuleNames));
            }
        } else {
            Collection<String> notExistedRules = getNotExistedRules(requiredTables, currentShardingTables);
            ShardingSpherePreconditions.checkMustEmpty(notExistedRules, () -> new MissingRequiredRuleException("sharding", databaseName, notExistedRules));
        }
    }
    
    private static Collection<String> getDuplicatedRuleNames(final Collection<String> collection) {
        Collection<String> duplicatedNames = collection.stream().collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1L).map(Entry::getKey).collect(Collectors.toCollection(CaseInsensitiveSet::new));
        return collection.stream().filter(duplicatedNames::contains).collect(Collectors.toSet());
    }
    
    private static Collection<String> getDuplicatedRuleNames(final Collection<String> requiredRuleNames, final Collection<String> currentRuleNames) {
        return requiredRuleNames.stream().filter(currentRuleNames::contains).collect(Collectors.toSet());
    }
    
    private static Set<String> getNotExistedRules(final Collection<String> requiredRuleNames, final Collection<String> currentRuleNames) {
        return requiredRuleNames.stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
    }
    
    private static Collection<String> getCurrentShardingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new CaseInsensitiveSet<>();
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
            ShardingAlgorithm shardingAlgorithm = TypedSPILoader.getService(ShardingAlgorithm.class, each.getShardingAlgorithmSegment().getName(), each.getShardingAlgorithmSegment().getProps());
            ShardingSpherePreconditions.checkState(shardingAlgorithm instanceof ShardingAutoTableAlgorithm,
                    () -> new AlgorithmInitializationException(shardingAlgorithm, "Auto sharding algorithm is required for table '%s'", each.getLogicTable()));
        });
    }
    
    private static void checkTableRule(final Collection<TableRuleSegment> tableRules) {
        checkStrategy(tableRules);
    }
    
    private static void checkStrategy(final Collection<TableRuleSegment> rules) {
        for (TableRuleSegment each : rules) {
            Optional<ShardingStrategySegment> databaseStrategySegment = Optional.ofNullable(each.getDatabaseStrategySegment());
            if (databaseStrategySegment.isPresent()) {
                if ("none".equalsIgnoreCase(databaseStrategySegment.get().getType())) {
                    Collection<String> requiredDataSources = getRequiredDataSources(rules);
                    ShardingSpherePreconditions.checkState(1 == requiredDataSources.size(),
                            () -> new InvalidShardingStrategyConfigurationException("database", databaseStrategySegment.get().getType()));
                } else {
                    checkDatabaseShardingAlgorithm(each, databaseStrategySegment.get());
                }
            }
            Optional<ShardingStrategySegment> tableStrategySegment = Optional.ofNullable(each.getTableStrategySegment());
            if (tableStrategySegment.isPresent()) {
                if ("none".equalsIgnoreCase(tableStrategySegment.get().getType())) {
                    Collection<String> requiredTables = getRequiredTables(rules);
                    ShardingSpherePreconditions.checkState(1 == requiredTables.size(),
                            () -> new InvalidShardingStrategyConfigurationException("table", tableStrategySegment.get().getType()));
                } else {
                    checkTableShardingAlgorithm(each, tableStrategySegment.get());
                }
            }
        }
    }
    
    private static void checkDatabaseShardingAlgorithm(final TableRuleSegment tableRuleSegment, final ShardingStrategySegment databaseStrategySegment) {
        AlgorithmSegment databaseShardingAlgorithm = databaseStrategySegment.getShardingAlgorithm();
        if (null != databaseShardingAlgorithm) {
            ShardingAlgorithm shardingAlgorithm = TypedSPILoader.getService(ShardingAlgorithm.class, databaseShardingAlgorithm.getName(), databaseShardingAlgorithm.getProps());
            ShardingSpherePreconditions.checkState(!(shardingAlgorithm instanceof ShardingAutoTableAlgorithm),
                    () -> new AlgorithmInitializationException(shardingAlgorithm, "Auto sharding algorithm can not be used to create a table in table '%s'", tableRuleSegment.getLogicTable()));
            if (shardingAlgorithm instanceof InlineShardingAlgorithm) {
                DataNodeInfo dataSourceDataNode = createDataSourceDataNode(ShardingTableRuleStatementConverter.getActualDataNodes(tableRuleSegment));
                checkInlineExpression(tableRuleSegment.getLogicTable(), databaseStrategySegment.getShardingColumn(), (InlineShardingAlgorithm) shardingAlgorithm, dataSourceDataNode);
            }
        }
        ShardingSpherePreconditions.checkState(isValidStrategy(tableRuleSegment.getDatabaseStrategySegment()),
                () -> new InvalidAlgorithmConfigurationException("sharding", null == databaseShardingAlgorithm ? null : databaseShardingAlgorithm.getName()));
    }
    
    private static void checkTableShardingAlgorithm(final TableRuleSegment tableRuleSegment, final ShardingStrategySegment tableStrategySegment) {
        AlgorithmSegment tableShardingAlgorithm = tableStrategySegment.getShardingAlgorithm();
        if (null != tableShardingAlgorithm) {
            ShardingAlgorithm shardingAlgorithm = TypedSPILoader.getService(ShardingAlgorithm.class, tableShardingAlgorithm.getName(), tableShardingAlgorithm.getProps());
            ShardingSpherePreconditions.checkState(!(shardingAlgorithm instanceof ShardingAutoTableAlgorithm),
                    () -> new AlgorithmInitializationException(shardingAlgorithm, "Auto sharding algorithm can not be used to create a table in table '%s'", tableRuleSegment.getLogicTable()));
            if (shardingAlgorithm instanceof InlineShardingAlgorithm) {
                DataNodeInfo tableDataNode = createTableDataNode(tableRuleSegment.getLogicTable(), ShardingTableRuleStatementConverter.getActualDataNodes(tableRuleSegment));
                checkInlineExpression(tableRuleSegment.getLogicTable(), tableStrategySegment.getShardingColumn(), (InlineShardingAlgorithm) shardingAlgorithm, tableDataNode);
            }
        }
        ShardingSpherePreconditions.checkState(isValidStrategy(tableRuleSegment.getTableStrategySegment()),
                () -> new InvalidAlgorithmConfigurationException("sharding", null == tableShardingAlgorithm ? null : tableShardingAlgorithm.getName()));
    }
    
    private static boolean isValidStrategy(final ShardingStrategySegment shardingStrategySegment) {
        return ShardingStrategyType.getValueOf(shardingStrategySegment.getType()).isValid(shardingStrategySegment.getShardingColumn()) && null != shardingStrategySegment.getShardingAlgorithm();
    }
    
    private static Collection<String> getRequiredTables(final Collection<TableRuleSegment> rules) {
        return rules.stream().map(TableRuleSegment::getDataSourceNodes).flatMap(Collection::stream)
                .map(ShardingTableRuleStatementChecker::parseDateSource).map(ShardingTableRuleStatementChecker::getTableNames).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private static Collection<String> getTableNames(final Collection<String> actualDataNodes) {
        Collection<String> result = new HashSet<>(actualDataNodes.size(), 1F);
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
    
    /**
     * Check to be added data nodes.
     *
     * @param toBeAddedDataNodes to be added data nodes
     */
    public static void checkToBeAddedDataNodes(final Map<String, Collection<DataNode>> toBeAddedDataNodes) {
        Collection<DataNode> uniqueActualDataNodes = new HashSet<>(toBeAddedDataNodes.size(), 1F);
        toBeAddedDataNodes.forEach((key, value) -> {
            DataNode sampleActualDataNode = value.iterator().next();
            ShardingSpherePreconditions.checkNotContains(uniqueActualDataNodes, sampleActualDataNode,
                    () -> new DuplicateShardingActualDataNodeException(key, sampleActualDataNode.getDataSourceName(), sampleActualDataNode.getTableName()));
            uniqueActualDataNodes.add(sampleActualDataNode);
        });
    }
    
    private static DataNodeInfo createDataSourceDataNode(final Collection<DataNode> actualDataNodes) {
        String prefix = ShardingTableConstants.DATA_NODE_SUFFIX_PATTERN.matcher(actualDataNodes.iterator().next().getDataSourceName()).replaceAll("");
        int suffixMinLength = actualDataNodes.stream().map(each -> each.getDataSourceName().length() - prefix.length()).min(Comparator.comparing(Integer::intValue)).orElse(1);
        return new DataNodeInfo(prefix, suffixMinLength, ShardingTableConstants.DEFAULT_PADDING_CHAR);
    }
    
    private static DataNodeInfo createTableDataNode(final String logicTable, final Collection<DataNode> actualDataNodes) {
        String tableName = actualDataNodes.iterator().next().getTableName();
        String prefix = tableName.startsWith(logicTable) ? logicTable + ShardingTableConstants.DATA_NODE_SUFFIX_PATTERN.matcher(tableName.substring(logicTable.length())).replaceAll("")
                : ShardingTableConstants.DATA_NODE_SUFFIX_PATTERN.matcher(tableName).replaceAll("");
        int suffixMinLength = actualDataNodes.stream().map(each -> each.getTableName().length() - prefix.length()).min(Comparator.comparing(Integer::intValue)).orElse(1);
        return new DataNodeInfo(prefix, suffixMinLength, ShardingTableConstants.DEFAULT_PADDING_CHAR);
    }
    
    /**
     * Check inline sharding algorithms.
     *
     * @param logicTable logic table
     * @param shardingColumn sharding column
     * @param inlineShardingAlgorithm inline sharding algorithm
     * @param dataNodeInfo data node info
     */
    public static void checkInlineExpression(final String logicTable, final String shardingColumn, final InlineShardingAlgorithm inlineShardingAlgorithm, final DataNodeInfo dataNodeInfo) {
        String result = null;
        try {
            result = inlineShardingAlgorithm.doSharding(Collections.emptySet(), new PreciseShardingValue<>(logicTable, shardingColumn, dataNodeInfo, 1));
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
        }
        ShardingSpherePreconditions.checkState(null == result || result.startsWith(dataNodeInfo.getPrefix()),
                () -> new AlgorithmInitializationException(inlineShardingAlgorithm, "inline expression of rule `%s` does not match the actual data nodes", logicTable));
    }
}
