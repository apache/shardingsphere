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

package org.apache.shardingsphere.sharding.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereInstanceRequiredAlgorithm;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.InstanceAwareRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.support.InlineExpressionParser;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.required.RequiredSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;
import org.apache.shardingsphere.sql.parser.sql.common.util.WhereExtractUtil;

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
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Sharding rule.
 */
@Getter
public final class ShardingRule implements SchemaRule, DataNodeContainedRule, TableContainedRule, InstanceAwareRule {
    
    private static final String EQUAL = "=";
    
    static {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    private final Collection<String> dataSourceNames;
    
    private final Map<String, ShardingAlgorithm> shardingAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, KeyGenerateAlgorithm> keyGenerators = new LinkedHashMap<>();
    
    private final Map<String, TableRule> tableRules = new LinkedHashMap<>();
    
    private final Map<String, BindingTableRule> bindingTableRules = new LinkedHashMap<>();
    
    private final Collection<String> broadcastTables;
    
    private final ShardingStrategyConfiguration defaultDatabaseShardingStrategyConfig;
    
    private final ShardingStrategyConfiguration defaultTableShardingStrategyConfig;
    
    private final KeyGenerateAlgorithm defaultKeyGenerateAlgorithm;

    private final String defaultShardingColumn;
    
    private final Map<String, Collection<DataNode>> shardingTableDataNodes;
    
    public ShardingRule(final ShardingRuleConfiguration config, final Collection<String> dataSourceNames) {
        this.dataSourceNames = getDataSourceNames(config.getTables(), config.getAutoTables(), dataSourceNames);
        config.getShardingAlgorithms().forEach((key, value) -> shardingAlgorithms.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, ShardingAlgorithm.class)));
        config.getKeyGenerators().forEach((key, value) -> keyGenerators.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, KeyGenerateAlgorithm.class)));
        tableRules.putAll(createTableRules(config.getTables(), config.getDefaultKeyGenerateStrategy()));
        tableRules.putAll(createAutoTableRules(config.getAutoTables(), config.getDefaultKeyGenerateStrategy()));
        bindingTableRules.putAll(createBindingTableRules(config.getBindingTableGroups()));
        broadcastTables = createBroadcastTables(config.getBroadcastTables());
        defaultDatabaseShardingStrategyConfig = null == config.getDefaultDatabaseShardingStrategy() ? new NoneShardingStrategyConfiguration() : config.getDefaultDatabaseShardingStrategy();
        defaultTableShardingStrategyConfig = null == config.getDefaultTableShardingStrategy() ? new NoneShardingStrategyConfiguration() : config.getDefaultTableShardingStrategy();
        defaultKeyGenerateAlgorithm = null == config.getDefaultKeyGenerateStrategy()
                ? RequiredSPIRegistry.getRegisteredService(KeyGenerateAlgorithm.class) : keyGenerators.get(config.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
        defaultShardingColumn = config.getDefaultShardingColumn();
        shardingTableDataNodes = createShardingTableDataNodes(tableRules);
        Preconditions.checkArgument(isValidBindingTableConfiguration(config.getBindingTableGroups()), "Invalid binding table configuration in ShardingRuleConfiguration.");
    }
    
    public ShardingRule(final AlgorithmProvidedShardingRuleConfiguration config, final Collection<String> dataSourceNames) {
        this.dataSourceNames = getDataSourceNames(config.getTables(), config.getAutoTables(), dataSourceNames);
        shardingAlgorithms.putAll(config.getShardingAlgorithms());
        keyGenerators.putAll(config.getKeyGenerators());
        tableRules.putAll(createTableRules(config.getTables(), config.getDefaultKeyGenerateStrategy()));
        tableRules.putAll(createAutoTableRules(config.getAutoTables(), config.getDefaultKeyGenerateStrategy()));
        bindingTableRules.putAll(createBindingTableRules(config.getBindingTableGroups()));
        broadcastTables = createBroadcastTables(config.getBroadcastTables());
        defaultDatabaseShardingStrategyConfig = null == config.getDefaultDatabaseShardingStrategy() ? new NoneShardingStrategyConfiguration() : config.getDefaultDatabaseShardingStrategy();
        defaultTableShardingStrategyConfig = null == config.getDefaultTableShardingStrategy() ? new NoneShardingStrategyConfiguration() : config.getDefaultTableShardingStrategy();
        defaultKeyGenerateAlgorithm = null == config.getDefaultKeyGenerateStrategy()
                ? RequiredSPIRegistry.getRegisteredService(KeyGenerateAlgorithm.class) : keyGenerators.get(config.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
        defaultShardingColumn = config.getDefaultShardingColumn();
        shardingTableDataNodes = createShardingTableDataNodes(tableRules);
        Preconditions.checkArgument(isValidBindingTableConfiguration(config.getBindingTableGroups()), "Invalid binding table configuration in ShardingRuleConfiguration.");
    }
    
    private Map<String, Collection<DataNode>> createShardingTableDataNodes(final Map<String, TableRule> tableRules) {
        Map<String, Collection<DataNode>> result = new HashMap<>(tableRules.size(), 1);
        for (TableRule each : tableRules.values()) {
            result.put(each.getLogicTable().toLowerCase(), each.getActualDataNodes());
        }
        return result;
    }
    
    private Collection<String> getDataSourceNames(final Collection<ShardingTableRuleConfiguration> tableRuleConfigs, 
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
    
    private Collection<String> getDataSourceNames(final ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfig) {
        List<String> actualDataSources = new InlineExpressionParser(shardingAutoTableRuleConfig.getActualDataSources()).splitAndEvaluate();
        return new HashSet<>(actualDataSources);
    }
    
    private Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        List<String> actualDataNodes = new InlineExpressionParser(shardingTableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        return actualDataNodes.stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private Map<String, TableRule> createTableRules(final Collection<ShardingTableRuleConfiguration> tableRuleConfigs, final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return tableRuleConfigs.stream().map(each -> new TableRule(each, dataSourceNames, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig)))
                .collect(Collectors.toMap(each -> each.getLogicTable().toLowerCase(), Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, TableRule> createAutoTableRules(final Collection<ShardingAutoTableRuleConfiguration> autoTableRuleConfigs, 
                                                       final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return autoTableRuleConfigs.stream().map(each -> createAutoTableRule(defaultKeyGenerateStrategyConfig, each))
                .collect(Collectors.toMap(each -> each.getLogicTable().toLowerCase(), Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private TableRule createAutoTableRule(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig, final ShardingAutoTableRuleConfiguration autoTableRuleConfig) {
        ShardingAlgorithm shardingAlgorithm = null == autoTableRuleConfig.getShardingStrategy() ? null : shardingAlgorithms.get(autoTableRuleConfig.getShardingStrategy().getShardingAlgorithmName());
        Preconditions.checkState(shardingAlgorithm instanceof ShardingAutoTableAlgorithm, "Sharding auto table rule configuration must match sharding auto table algorithm.");
        return new TableRule(autoTableRuleConfig, dataSourceNames, (ShardingAutoTableAlgorithm) shardingAlgorithm, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig));
    }
    
    private String getDefaultGenerateKeyColumn(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return Optional.ofNullable(defaultKeyGenerateStrategyConfig).map(KeyGenerateStrategyConfiguration::getColumn).orElse(null);
    }
    
    private Collection<String> createBroadcastTables(final Collection<String> broadcastTables) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(broadcastTables);
        return result;
    }
    
    private Map<String, BindingTableRule> createBindingTableRules(final Collection<String> bindingTableGroups) {
        Map<String, BindingTableRule> result = new LinkedHashMap<>();
        for (String each : bindingTableGroups) {
            BindingTableRule bindingTableRule = createBindingTableRule(each);
            for (String logicTable : bindingTableRule.getAllLogicTables()) {
                result.put(logicTable.toLowerCase(), bindingTableRule);
            }
        }
        return result;
    }
    
    private BindingTableRule createBindingTableRule(final String bindingTableGroup) {
        Map<String, TableRule> tableRules = Splitter.on(",").trimResults().splitToList(bindingTableGroup).stream()
                .map(this::getTableRule).collect(Collectors.toMap(each -> each.getLogicTable().toLowerCase(), Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        BindingTableRule result = new BindingTableRule();
        result.getTableRules().putAll(tableRules);
        return result;
    }
    
    private boolean isValidBindingTableConfiguration(final Collection<String> bindingTableGroups) {
        for (String each : bindingTableGroups) {
            Collection<String> bindingTables = Splitter.on(",").trimResults().splitToList(each.toLowerCase());
            if (bindingTables.size() <= 1) {
                continue;
            }
            Iterator<String> iterator = bindingTables.iterator();
            TableRule sampleTableRule = getTableRule(iterator.next());
            while (iterator.hasNext()) {
                TableRule tableRule = getTableRule(iterator.next());
                if (!isValidActualDatasourceName(sampleTableRule, tableRule) || !isValidActualTableName(sampleTableRule, tableRule)) {
                    return false;
                }
                if (!isValidShardingAlgorithm(sampleTableRule, tableRule, true) 
                        || !isValidShardingAlgorithm(sampleTableRule, tableRule, false)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isValidActualDatasourceName(final TableRule sampleTableRule, final TableRule tableRule) {
        return sampleTableRule.getActualDatasourceNames().equals(tableRule.getActualDatasourceNames());
    }
    
    private boolean isValidActualTableName(final TableRule sampleTableRule, final TableRule tableRule) {
        for (String each : sampleTableRule.getActualDatasourceNames()) {
            Collection<String> sampleActualTableNames = sampleTableRule.getActualTableNames(each).stream().map(actualTableName 
                -> actualTableName.replace(sampleTableRule.getTableDataNode().getPrefix(), "")).collect(Collectors.toSet());
            Collection<String> actualTableNames = tableRule.getActualTableNames(each).stream().map(actualTableName 
                -> actualTableName.replace(tableRule.getTableDataNode().getPrefix(), "")).collect(Collectors.toSet());
            if (!sampleActualTableNames.equals(actualTableNames)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isValidShardingAlgorithm(final TableRule sampleTableRule, final TableRule tableRule, final boolean databaseAlgorithm) {
        String sampleAlgorithmExpression = getAlgorithmExpression(sampleTableRule, databaseAlgorithm);
        String algorithmExpression = getAlgorithmExpression(tableRule, databaseAlgorithm);
        return sampleAlgorithmExpression.equalsIgnoreCase(algorithmExpression);
    }
    
    private String getAlgorithmExpression(final TableRule tableRule, final boolean databaseAlgorithm) {
        ShardingStrategyConfiguration shardingStrategyConfig = databaseAlgorithm
                ? null == tableRule.getDatabaseShardingStrategyConfig() ? defaultDatabaseShardingStrategyConfig : tableRule.getDatabaseShardingStrategyConfig()
                : null == tableRule.getTableShardingStrategyConfig() ? defaultTableShardingStrategyConfig : tableRule.getTableShardingStrategyConfig();
        ShardingAlgorithm shardingAlgorithm = shardingAlgorithms.get(shardingStrategyConfig.getShardingAlgorithmName());
        String originAlgorithmExpression = null == shardingAlgorithm ? "" : StringUtils.defaultString(shardingAlgorithm.getProps().getProperty("algorithm-expression"), "");
        String sampleDataNodePrefix = databaseAlgorithm ? tableRule.getDataSourceDataNode().getPrefix() : tableRule.getTableDataNode().getPrefix();
        String shardingColumn = getShardingColumn(shardingStrategyConfig);
        return originAlgorithmExpression.replace(sampleDataNodePrefix, "").replace(shardingColumn, "");
    }
    
    private String getShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfig) {
        if (shardingStrategyConfig instanceof ComplexShardingStrategyConfiguration) {
            return ((ComplexShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumns();
        }
        if (shardingStrategyConfig instanceof StandardShardingStrategyConfiguration) {
            return ((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn();
        }
        return StringUtils.defaultString(defaultShardingColumn, "");
    }
    
    @Override
    public Collection<String> getAllTables() {
        Collection<String> result = new HashSet<>(getTables());
        result.addAll(getAllActualTables());
        return result;
    }
    
    /**
     * Get database sharding strategy configuration.
     * 
     * @param tableRule table rule
     * @return database sharding strategy configuration
     */
    public ShardingStrategyConfiguration getDatabaseShardingStrategyConfiguration(final TableRule tableRule) {
        return null == tableRule.getDatabaseShardingStrategyConfig() ? defaultDatabaseShardingStrategyConfig : tableRule.getDatabaseShardingStrategyConfig();
    }
    
    /**
     * Get table sharding strategy configuration.
     *
     * @param tableRule table rule
     * @return table sharding strategy configuration
     */
    public ShardingStrategyConfiguration getTableShardingStrategyConfiguration(final TableRule tableRule) {
        return null == tableRule.getTableShardingStrategyConfig() ? defaultTableShardingStrategyConfig : tableRule.getTableShardingStrategyConfig();
    }
    
    /**
     * Find table rule.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public Optional<TableRule> findTableRule(final String logicTableName) {
        return Optional.ofNullable(tableRules.get(logicTableName.toLowerCase()));
    }
    
    /**
     * Find table rule via actual table name.
     *
     * @param actualTableName actual table name
     * @return table rule
     */
    public Optional<TableRule> findTableRuleByActualTable(final String actualTableName) {
        for (TableRule each : tableRules.values()) {
            if (each.isExisted(actualTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get table rule.
     *
     * @param logicTableName logic table name
     * @return table rule
     */
    public TableRule getTableRule(final String logicTableName) {
        Optional<TableRule> tableRule = findTableRule(logicTableName);
        if (tableRule.isPresent()) {
            return tableRule.get();
        }
        if (isBroadcastTable(logicTableName)) {
            return new TableRule(dataSourceNames, logicTableName);
        }
        throw new ShardingSphereConfigurationException("Cannot find table rule with logic table: '%s'", logicTableName);
    }
    
    /**
     * Judge whether logic table is all binding tables or not.
     *
     * @param logicTableNames logic table names
     * @return whether logic table is all binding tables or not
     */
    public boolean isAllBindingTables(final Collection<String> logicTableNames) {
        if (logicTableNames.isEmpty()) {
            return false;
        }
        Optional<BindingTableRule> bindingTableRule = findBindingTableRule(logicTableNames);
        if (!bindingTableRule.isPresent()) {
            return false;
        }
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(bindingTableRule.get().getAllLogicTables());
        return !result.isEmpty() && result.containsAll(logicTableNames);
    }
    
    /**
     * Judge whether logic table is all binding tables.
     *
     * @param schema schema
     * @param sqlStatementContext sqlStatementContext
     * @param logicTableNames logic table names
     * @return whether logic table is all binding tables
     */
    public boolean isAllBindingTables(final ShardingSphereSchema schema, final SQLStatementContext<?> sqlStatementContext, final Collection<String> logicTableNames) {
        if (!(sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsJoinQuery())) {
            return isAllBindingTables(logicTableNames);
        }
        if (!isAllBindingTables(logicTableNames)) {
            return false;
        }
        SelectStatementContext select = (SelectStatementContext) sqlStatementContext;
        Collection<WhereSegment> joinSegments = WhereExtractUtil.getJoinWhereSegments(select.getSqlStatement());
        return isJoinConditionContainsShardingColumns(schema, select, logicTableNames, joinSegments) 
                || isJoinConditionContainsShardingColumns(schema, select, logicTableNames, select.getWhereSegments());
    }
    
    private Optional<BindingTableRule> findBindingTableRule(final Collection<String> logicTableNames) {
        for (String each : logicTableNames) {
            Optional<BindingTableRule> result = findBindingTableRule(each);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find binding table rule via logic table name.
     *
     * @param logicTableName logic table name
     * @return binding table rule
     */
    public Optional<BindingTableRule> findBindingTableRule(final String logicTableName) {
        return Optional.ofNullable(bindingTableRules.get(logicTableName.toLowerCase()));
    }
    
    /**
     * Judge whether logic table is all broadcast tables or not.
     *
     * @param logicTableNames logic table names
     * @return whether logic table is all broadcast tables or not
     */
    public boolean isAllBroadcastTables(final Collection<String> logicTableNames) {
        return !logicTableNames.isEmpty() && broadcastTables.containsAll(logicTableNames);
    }
    
    /**
     * Judge whether logic table is all sharding table or not.
     *
     * @param logicTableNames logic table names
     * @return whether logic table is all sharding table or not
     */
    public boolean isAllShardingTables(final Collection<String> logicTableNames) {
        if (logicTableNames.isEmpty()) {
            return false;
        }
        for (String each : logicTableNames) {
            if (!isShardingTable(each)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Judge whether logic table is sharding table or not.
     *
     * @param logicTableName logic table name
     * @return whether logic table is sharding table or not
     */
    public boolean isShardingTable(final String logicTableName) {
        return tableRules.containsKey(logicTableName.toLowerCase());
    }
    
    /**
     * Judge whether logic table is broadcast table or not.
     *
     * @param logicTableName logic table name
     * @return whether logic table is broadcast table or not
     */
    public boolean isBroadcastTable(final String logicTableName) {
        return broadcastTables.contains(logicTableName);
    }
    
    /**
     * Judge whether all tables are in same data source or not.
     * 
     * @param logicTableNames logic table names
     * @return whether all tables are in same data source or not
     */
    public boolean isAllTablesInSameDataSource(final Collection<String> logicTableNames) {
        Collection<String> dataSourceNames = logicTableNames.stream().map(each -> tableRules.get(each.toLowerCase()))
                .filter(Objects::nonNull).flatMap(each -> each.getActualDatasourceNames().stream()).collect(Collectors.toSet());
        return 1 == dataSourceNames.size();
    }
    
    /**
     * Judge whether a table rule exists for logic tables.
     *
     * @param logicTableNames logic table names
     * @return whether a table rule exists for logic tables
     */
    public boolean tableRuleExists(final Collection<String> logicTableNames) {
        for (String each : logicTableNames) {
            if (isShardingTable(each) || isBroadcastTable(each)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Find sharding column.
     *
     * @param columnName column name
     * @param tableName table name
     * @return sharding column
     */
    public Optional<String> findShardingColumn(final String columnName, final String tableName) {
        return Optional.ofNullable(tableRules.get(tableName.toLowerCase())).flatMap(optional -> findShardingColumn(optional, columnName));
    }
    
    private Optional<String> findShardingColumn(final TableRule tableRule, final String columnName) {
        Optional<String> databaseShardingColumn = findShardingColumn(getDatabaseShardingStrategyConfiguration(tableRule), columnName);
        if (databaseShardingColumn.isPresent()) {
            return databaseShardingColumn;
        }
        return findShardingColumn(getTableShardingStrategyConfiguration(tableRule), columnName);
    }
    
    private Optional<String> findShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfig, final String columnName) {
        if (shardingStrategyConfig instanceof StandardShardingStrategyConfiguration) {
            String shardingColumn = null == ((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn()
                    ? defaultShardingColumn : ((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn();
            return shardingColumn.equalsIgnoreCase(columnName) ? Optional.of(shardingColumn) : Optional.empty();
        }
        if (shardingStrategyConfig instanceof ComplexShardingStrategyConfiguration) {
            List<String> shardingColumns = Splitter.on(",").trimResults().splitToList(((ComplexShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumns());
            for (String each : shardingColumns) {
                if (each.equalsIgnoreCase(columnName)) {
                    return Optional.of(each);
                }
            }
        }
        return Optional.empty();
    } 
    
    /**
     * Judge whether given logic table column is generate key column or not.
     *
     * @param columnName column name
     * @param tableName table name
     * @return whether given logic table column is generate key column or not
     */
    public boolean isGenerateKeyColumn(final String columnName, final String tableName) {
        return Optional.ofNullable(tableRules.get(tableName.toLowerCase())).filter(each -> isGenerateKeyColumn(each, columnName)).isPresent();
    }
    
    private boolean isGenerateKeyColumn(final TableRule tableRule, final String columnName) {
        Optional<String> generateKeyColumn = tableRule.getGenerateKeyColumn();
        return generateKeyColumn.isPresent() && generateKeyColumn.get().equalsIgnoreCase(columnName);
    }
    
    /**
     * Find column name of generated key.
     *
     * @param logicTableName logic table name
     * @return column name of generated key
     */
    public Optional<String> findGenerateKeyColumnName(final String logicTableName) {
        return Optional.ofNullable(tableRules.get(logicTableName.toLowerCase())).filter(each -> each.getGenerateKeyColumn().isPresent()).flatMap(TableRule::getGenerateKeyColumn);
    }
    
    /**
     * Find the Generated key of logic table.
     *
     * @param logicTableName logic table name
     * @return generated key
     */
    public Comparable<?> generateKey(final String logicTableName) {
        Optional<TableRule> tableRule = findTableRule(logicTableName);
        if (!tableRule.isPresent()) {
            throw new ShardingSphereConfigurationException("Cannot find strategy for generate keys.");
        }
        KeyGenerateAlgorithm keyGenerator = null != tableRule.get().getKeyGeneratorName() ? keyGenerators.get(tableRule.get().getKeyGeneratorName()) : defaultKeyGenerateAlgorithm;
        return keyGenerator.generateKey();
    }
    
    /**
     * Find data node by logic table name.
     *
     * @param logicTableName logic table name
     * @return data node
     */
    public DataNode getDataNode(final String logicTableName) {
        TableRule tableRule = getTableRule(logicTableName);
        return tableRule.getActualDataNodes().get(0);
    }
    
    /**
     * Get sharding logic table names.
     *
     * @param logicTableNames logic table names
     * @return sharding logic table names
     */
    public Collection<String> getShardingLogicTableNames(final Collection<String> logicTableNames) {
        Collection<String> result = new LinkedList<>();
        for (String each : logicTableNames) {
            if (isShardingTable(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Get sharding rule table names.
     *
     * @param logicTableNames logic table names
     * @return sharding rule table names
     */
    public Collection<String> getShardingRuleTableNames(final Collection<String> logicTableNames) {
        return logicTableNames.stream().filter(each -> isShardingTable(each) || isBroadcastTable(each)).collect(Collectors.toCollection(LinkedList::new));
    }
    
    /**
     * Get logic and actual binding tables.
     *
     * @param dataSourceName data source name
     * @param logicTable logic table name
     * @param actualTable actual table name
     * @param availableLogicBindingTables available logic binding table names
     * @return logic and actual binding tables
     */
    public Map<String, String> getLogicAndActualTablesFromBindingTable(final String dataSourceName,
                                                                       final String logicTable, final String actualTable, final Collection<String> availableLogicBindingTables) {
        return findBindingTableRule(logicTable).map(optional 
            -> optional.getLogicAndActualTables(dataSourceName, logicTable, actualTable, availableLogicBindingTables)).orElseGet(Collections::emptyMap);
    }

    /**
     * Get logic tables via actual table name.
     *
     * @param actualTable actual table name
     * @return logic tables
     */
    public Collection<String> getLogicTablesByActualTable(final String actualTable) {
        return tableRules.values().stream().filter(each -> each.isExisted(actualTable)).map(TableRule::getLogicTable).collect(Collectors.toSet());
    }
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        return shardingTableDataNodes;
    }
    
    @Override
    public Collection<DataNode> getDataNodesByTableName(final String tableName) {
        return shardingTableDataNodes.getOrDefault(tableName.toLowerCase(), Collections.emptyList());
    }
    
    private Collection<String> getAllActualTables() {
        return tableRules.values().stream().flatMap(each -> each.getActualDataNodes().stream().map(DataNode::getTableName)).collect(Collectors.toSet());
    }
    
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return findTableRule(logicTable).map(tableRule -> tableRule.getActualDataNodes().get(0).getTableName());
    }
    
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return !isAllBroadcastTables(tables);
    }
    
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return findTableRuleByActualTable(actualTable).map(TableRule::getLogicTable);
    }
    
    @Override
    public Collection<String> getTables() {
        Collection<String> result = tableRules.values().stream().map(TableRule::getLogicTable).collect(Collectors.toSet());
        result.addAll(broadcastTables);
        return result;
    }
    
    @Override
    public Optional<String> findActualTableByCatalog(final String catalog, final String logicTable) {
        return findTableRule(logicTable).flatMap(tableRule -> findActualTableFromActualDataNode(catalog, tableRule.getActualDataNodes()));
    }
    
    private Optional<String> findActualTableFromActualDataNode(final String catalog, final List<DataNode> actualDataNodes) {
        return actualDataNodes.stream().filter(each -> each.getDataSourceName().equalsIgnoreCase(catalog)).findFirst().map(DataNode::getTableName);
    }
    
    @Override
    public String getType() {
        return ShardingRule.class.getSimpleName();
    }
    
    private boolean isJoinConditionContainsShardingColumns(final ShardingSphereSchema schema, final SelectStatementContext select, 
                                                           final Collection<String> tableNames, final Collection<WhereSegment> whereSegments) {
        Collection<String> databaseJoinConditionTables = new HashSet<>(tableNames.size());
        Collection<String> tableJoinConditionTables = new HashSet<>(tableNames.size());
        for (WhereSegment each : whereSegments) {
            Collection<AndPredicate> andPredicates = ExpressionExtractUtil.getAndPredicates(each.getExpr());
            if (andPredicates.size() > 1) {
                return false;
            }
            for (AndPredicate andPredicate : andPredicates) {
                databaseJoinConditionTables.addAll(getJoinConditionTables(schema, select, andPredicate.getPredicates(), true));
                tableJoinConditionTables.addAll(getJoinConditionTables(schema, select, andPredicate.getPredicates(), false));
            }
        }
        TableRule tableRule = getTableRule(tableNames.iterator().next());
        boolean containsDatabaseShardingColumns = !(getDatabaseShardingStrategyConfiguration(tableRule) instanceof StandardShardingStrategyConfiguration) 
                || databaseJoinConditionTables.containsAll(tableNames);
        boolean containsTableShardingColumns = !(getTableShardingStrategyConfiguration(tableRule) instanceof StandardShardingStrategyConfiguration) || tableJoinConditionTables.containsAll(tableNames);
        return containsDatabaseShardingColumns && containsTableShardingColumns;
    }
    
    private Collection<String> getJoinConditionTables(final ShardingSphereSchema schema, final SelectStatementContext select,
                                                      final Collection<ExpressionSegment> predicates, final boolean isDatabaseJoinCondition) {
        Collection<String> result = new LinkedList<>();
        for (ExpressionSegment each : predicates) {
            if (!isJoinConditionExpression(each)) {
                continue;
            }
            ColumnSegment leftColumn = (ColumnSegment) ((BinaryOperationExpression) each).getLeft();
            ColumnSegment rightColumn = (ColumnSegment) ((BinaryOperationExpression) each).getRight();
            Map<String, String> columnExpressionTableNames = select.getTablesContext().findTableNamesByColumnSegment(Arrays.asList(leftColumn, rightColumn), schema);
            Optional<TableRule> leftTableRule = findTableRule(columnExpressionTableNames.get(leftColumn.getExpression()));
            Optional<TableRule> rightTableRule = findTableRule(columnExpressionTableNames.get(rightColumn.getExpression()));
            if (!leftTableRule.isPresent() || !rightTableRule.isPresent()) {
                continue;
            }
            ShardingStrategyConfiguration leftConfiguration = isDatabaseJoinCondition
                    ? getDatabaseShardingStrategyConfiguration(leftTableRule.get()) : getTableShardingStrategyConfiguration(leftTableRule.get());
            ShardingStrategyConfiguration rightConfiguration = isDatabaseJoinCondition
                    ? getDatabaseShardingStrategyConfiguration(rightTableRule.get()) : getTableShardingStrategyConfiguration(rightTableRule.get());
            if (findShardingColumn(leftConfiguration, leftColumn.getIdentifier().getValue()).isPresent() 
                    && findShardingColumn(rightConfiguration, rightColumn.getIdentifier().getValue()).isPresent()) {
                result.add(columnExpressionTableNames.get(leftColumn.getExpression()));
                result.add(columnExpressionTableNames.get(rightColumn.getExpression()));
            }
        }
        return result;
    }
    
    private boolean isJoinConditionExpression(final ExpressionSegment expression) {
        if (!(expression instanceof BinaryOperationExpression)) {
            return false;
        }
        BinaryOperationExpression binaryExpression = (BinaryOperationExpression) expression;
        return binaryExpression.getLeft() instanceof ColumnSegment && binaryExpression.getRight() instanceof ColumnSegment && EQUAL.equals(binaryExpression.getOperator());
    }
    
    @Override
    public void setInstanceContext(final InstanceContext instanceContext) {
        keyGenerators.values().stream().filter(each -> each instanceof ShardingSphereInstanceRequiredAlgorithm)
                .forEach(each -> ((ShardingSphereInstanceRequiredAlgorithm) each).setInstanceContext(instanceContext));
        if (defaultKeyGenerateAlgorithm instanceof ShardingSphereInstanceRequiredAlgorithm) {
            ((ShardingSphereInstanceRequiredAlgorithm) defaultKeyGenerateAlgorithm).setInstanceContext(instanceContext);
        }
    }
}
