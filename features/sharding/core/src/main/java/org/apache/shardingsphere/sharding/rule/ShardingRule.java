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

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContextAware;
import org.apache.shardingsphere.infra.metadata.database.resource.PhysicalDataSourceAggregator;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.aggregate.AggregatedDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.cache.ShardingCache;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingTableRuleNotFoundException;
import org.apache.shardingsphere.sharding.rule.attribute.ShardingDataNodeRuleAttribute;
import org.apache.shardingsphere.sharding.rule.attribute.ShardingTableNamesRuleAttribute;
import org.apache.shardingsphere.sharding.rule.checker.ShardingRuleChecker;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Sharding rule.
 */
@Getter
public final class ShardingRule implements DatabaseRule {
    
    private final ShardingRuleConfiguration configuration;
    
    private final Collection<String> dataSourceNames;
    
    private final Map<String, ShardingAlgorithm> shardingAlgorithms = new CaseInsensitiveMap<>();
    
    private final Map<String, KeyGenerateAlgorithm> keyGenerators = new CaseInsensitiveMap<>();
    
    private final Map<String, ShardingAuditAlgorithm> auditors = new CaseInsensitiveMap<>();
    
    private final Map<String, ShardingTable> shardingTables = new CaseInsensitiveMap<>();
    
    private final Map<String, BindingTableRule> bindingTableRules = new CaseInsensitiveMap<>();
    
    private final ShardingStrategyConfiguration defaultDatabaseShardingStrategyConfig;
    
    private final ShardingStrategyConfiguration defaultTableShardingStrategyConfig;
    
    private final ShardingAuditStrategyConfiguration defaultAuditStrategy;
    
    private final KeyGenerateAlgorithm defaultKeyGenerateAlgorithm;
    
    private final String defaultShardingColumn;
    
    private final ShardingCache shardingCache;
    
    private final RuleAttributes attributes;
    
    private final ShardingRuleChecker shardingRuleChecker = new ShardingRuleChecker(this);
    
    public ShardingRule(final ShardingRuleConfiguration ruleConfig, final Map<String, DataSource> dataSources, final ComputeNodeInstanceContext computeNodeInstanceContext,
                        final Collection<ShardingSphereRule> builtRules) {
        configuration = ruleConfig;
        dataSourceNames = getDataSourceNames(ruleConfig.getTables(), ruleConfig.getAutoTables(), dataSources.keySet());
        ruleConfig.getShardingAlgorithms().forEach((key, value) -> shardingAlgorithms.put(key, TypedSPILoader.getService(ShardingAlgorithm.class, value.getType(), value.getProps())));
        ruleConfig.getKeyGenerators().forEach((key, value) -> keyGenerators.put(key, TypedSPILoader.getService(KeyGenerateAlgorithm.class, value.getType(), value.getProps())));
        ruleConfig.getAuditors().forEach((key, value) -> auditors.put(key, TypedSPILoader.getService(ShardingAuditAlgorithm.class, value.getType(), value.getProps())));
        shardingTables.putAll(createShardingTables(ruleConfig.getTables(), ruleConfig.getDefaultKeyGenerateStrategy()));
        shardingTables.putAll(createShardingAutoTables(ruleConfig.getAutoTables(), ruleConfig.getDefaultKeyGenerateStrategy()));
        bindingTableRules.putAll(createBindingTableRules(ruleConfig.getBindingTableGroups()));
        defaultDatabaseShardingStrategyConfig = createDefaultDatabaseShardingStrategyConfiguration(ruleConfig);
        defaultTableShardingStrategyConfig = createDefaultTableShardingStrategyConfiguration(ruleConfig);
        defaultAuditStrategy = null == ruleConfig.getDefaultAuditStrategy() ? new ShardingAuditStrategyConfiguration(Collections.emptyList(), true) : ruleConfig.getDefaultAuditStrategy();
        defaultKeyGenerateAlgorithm = null == ruleConfig.getDefaultKeyGenerateStrategy()
                ? TypedSPILoader.getService(KeyGenerateAlgorithm.class, null)
                : keyGenerators.get(ruleConfig.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
        defaultShardingColumn = ruleConfig.getDefaultShardingColumn();
        keyGenerators.values().stream().filter(ComputeNodeInstanceContextAware.class::isInstance)
                .forEach(each -> ((ComputeNodeInstanceContextAware) each).setComputeNodeInstanceContext(computeNodeInstanceContext));
        if (defaultKeyGenerateAlgorithm instanceof ComputeNodeInstanceContextAware && -1 == computeNodeInstanceContext.getWorkerId()) {
            ((ComputeNodeInstanceContextAware) defaultKeyGenerateAlgorithm).setComputeNodeInstanceContext(computeNodeInstanceContext);
        }
        shardingCache = null == ruleConfig.getShardingCache() ? null : new ShardingCache(ruleConfig.getShardingCache(), this);
        // TODO check sharding rule configuration according to aggregated data sources
        Map<String, DataSource> aggregatedDataSources = new RuleMetaData(builtRules).findAttribute(AggregatedDataSourceRuleAttribute.class)
                .map(AggregatedDataSourceRuleAttribute::getAggregatedDataSources).orElseGet(() -> PhysicalDataSourceAggregator.getAggregatedDataSources(dataSources, builtRules));
        attributes = new RuleAttributes(new ShardingDataNodeRuleAttribute(shardingTables), new ShardingTableNamesRuleAttribute(shardingTables.values()),
                new AggregatedDataSourceRuleAttribute(aggregatedDataSources));
        shardingRuleChecker.check(ruleConfig);
    }
    
    private ShardingStrategyConfiguration createDefaultDatabaseShardingStrategyConfiguration(final ShardingRuleConfiguration ruleConfig) {
        Optional.ofNullable(ruleConfig.getDefaultDatabaseShardingStrategy()).ifPresent(optional -> checkManualShardingAlgorithm(optional.getShardingAlgorithmName(), "default"));
        return null == ruleConfig.getDefaultDatabaseShardingStrategy() ? new NoneShardingStrategyConfiguration() : ruleConfig.getDefaultDatabaseShardingStrategy();
    }
    
    private ShardingStrategyConfiguration createDefaultTableShardingStrategyConfiguration(final ShardingRuleConfiguration ruleConfig) {
        Optional.ofNullable(ruleConfig.getDefaultTableShardingStrategy()).ifPresent(optional -> checkManualShardingAlgorithm(optional.getShardingAlgorithmName(), "default"));
        return null == ruleConfig.getDefaultTableShardingStrategy() ? new NoneShardingStrategyConfiguration() : ruleConfig.getDefaultTableShardingStrategy();
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
        List<String> actualDataSources = InlineExpressionParserFactory.newInstance(shardingAutoTableRuleConfig.getActualDataSources()).splitAndEvaluate();
        return new HashSet<>(actualDataSources);
    }
    
    private Collection<String> getDataSourceNames(final ShardingTableRuleConfiguration shardingTableRuleConfig) {
        List<String> actualDataNodes = InlineExpressionParserFactory.newInstance(shardingTableRuleConfig.getActualDataNodes()).splitAndEvaluate();
        return actualDataNodes.stream().map(each -> new DataNode(each).getDataSourceName()).collect(Collectors.toList());
    }
    
    private Map<String, ShardingTable> createShardingTables(final Collection<ShardingTableRuleConfiguration> tableRuleConfigs,
                                                            final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return tableRuleConfigs.stream().map(each -> createShardingTable(each, defaultKeyGenerateStrategyConfig))
                .collect(Collectors.toMap(ShardingTable::getLogicTable, Function.identity(), (oldValue, currentValue) -> oldValue, CaseInsensitiveMap::new));
    }
    
    private ShardingTable createShardingTable(final ShardingTableRuleConfiguration tableRuleConfig, final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        Optional.ofNullable(tableRuleConfig.getDatabaseShardingStrategy()).ifPresent(optional -> checkManualShardingAlgorithm(optional.getShardingAlgorithmName(), tableRuleConfig.getLogicTable()));
        Optional.ofNullable(tableRuleConfig.getTableShardingStrategy()).ifPresent(optional -> checkManualShardingAlgorithm(optional.getShardingAlgorithmName(), tableRuleConfig.getLogicTable()));
        return new ShardingTable(tableRuleConfig, dataSourceNames, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig));
    }
    
    private void checkManualShardingAlgorithm(final String shardingAlgorithmName, final String logicTable) {
        ShardingAlgorithm shardingAlgorithm = shardingAlgorithms.get(shardingAlgorithmName);
        ShardingSpherePreconditions.checkState(!(shardingAlgorithm instanceof ShardingAutoTableAlgorithm),
                () -> new AlgorithmInitializationException(shardingAlgorithm, "`%s` tables sharding configuration can not use auto sharding algorithm.", logicTable));
    }
    
    private Map<String, ShardingTable> createShardingAutoTables(final Collection<ShardingAutoTableRuleConfiguration> autoTableRuleConfigs,
                                                                final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return autoTableRuleConfigs.stream().map(each -> createShardingAutoTable(defaultKeyGenerateStrategyConfig, each))
                .collect(Collectors.toMap(ShardingTable::getLogicTable, Function.identity(), (oldValue, currentValue) -> oldValue, CaseInsensitiveMap::new));
    }
    
    private ShardingTable createShardingAutoTable(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig, final ShardingAutoTableRuleConfiguration autoTableRuleConfig) {
        checkAutoShardingAlgorithm(autoTableRuleConfig.getShardingStrategy().getShardingAlgorithmName(), autoTableRuleConfig.getLogicTable());
        ShardingAlgorithm shardingAlgorithm = shardingAlgorithms.get(autoTableRuleConfig.getShardingStrategy().getShardingAlgorithmName());
        return new ShardingTable(autoTableRuleConfig, dataSourceNames, (ShardingAutoTableAlgorithm) shardingAlgorithm, getDefaultGenerateKeyColumn(defaultKeyGenerateStrategyConfig));
    }
    
    private void checkAutoShardingAlgorithm(final String shardingAlgorithmName, final String logicTable) {
        ShardingAlgorithm shardingAlgorithm = shardingAlgorithms.get(shardingAlgorithmName);
        ShardingSpherePreconditions.checkState(shardingAlgorithm instanceof ShardingAutoTableAlgorithm,
                () -> new AlgorithmInitializationException(shardingAlgorithm, "`%s` autoTables sharding configuration must use auto sharding algorithm.", logicTable));
    }
    
    private String getDefaultGenerateKeyColumn(final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategyConfig) {
        return Optional.ofNullable(defaultKeyGenerateStrategyConfig).map(KeyGenerateStrategyConfiguration::getColumn).orElse(null);
    }
    
    private Map<String, BindingTableRule> createBindingTableRules(final Collection<ShardingTableReferenceRuleConfiguration> bindingTableGroups) {
        Map<String, BindingTableRule> result = new LinkedHashMap<>();
        for (ShardingTableReferenceRuleConfiguration each : bindingTableGroups) {
            BindingTableRule bindingTableRule = createBindingTableRule(each.getReference());
            for (String logicTable : bindingTableRule.getAllLogicTables()) {
                result.put(logicTable, bindingTableRule);
            }
        }
        return result;
    }
    
    private BindingTableRule createBindingTableRule(final String bindingTableGroup) {
        Map<String, ShardingTable> shardingTables = Splitter.on(",").trimResults().splitToList(bindingTableGroup).stream()
                .map(this::getShardingTable).collect(Collectors.toMap(ShardingTable::getLogicTable, Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        BindingTableRule result = new BindingTableRule();
        result.getShardingTables().putAll(shardingTables);
        return result;
    }
    
    /**
     * Get database sharding strategy configuration.
     *
     * @param shardingTable sharding table
     * @return database sharding strategy configuration
     */
    public ShardingStrategyConfiguration getDatabaseShardingStrategyConfiguration(final ShardingTable shardingTable) {
        return getDatabaseShardingStrategyConfiguration(shardingTable, defaultDatabaseShardingStrategyConfig);
    }
    
    private ShardingStrategyConfiguration getDatabaseShardingStrategyConfiguration(final ShardingTable shardingTable, final ShardingStrategyConfiguration defaultDatabaseShardingStrategyConfig) {
        return null == shardingTable.getDatabaseShardingStrategyConfig() ? defaultDatabaseShardingStrategyConfig : shardingTable.getDatabaseShardingStrategyConfig();
    }
    
    /**
     * Get table sharding strategy configuration.
     *
     * @param shardingTable sharding table
     * @return table sharding strategy configuration
     */
    public ShardingStrategyConfiguration getTableShardingStrategyConfiguration(final ShardingTable shardingTable) {
        return getTableShardingStrategyConfiguration(shardingTable, defaultTableShardingStrategyConfig);
    }
    
    private ShardingStrategyConfiguration getTableShardingStrategyConfiguration(final ShardingTable shardingTable, final ShardingStrategyConfiguration defaultTableShardingStrategyConfig) {
        return null == shardingTable.getTableShardingStrategyConfig() ? defaultTableShardingStrategyConfig : shardingTable.getTableShardingStrategyConfig();
    }
    
    /**
     * Get audit strategy configuration.
     *
     * @param shardingTable sharding table
     * @return audit strategy configuration
     */
    public ShardingAuditStrategyConfiguration getAuditStrategyConfiguration(final ShardingTable shardingTable) {
        return null == shardingTable.getAuditStrategyConfig() ? defaultAuditStrategy : shardingTable.getAuditStrategyConfig();
    }
    
    /**
     * Find sharding table.
     *
     * @param logicTableName logic table name
     * @return sharding table
     */
    public Optional<ShardingTable> findShardingTable(final String logicTableName) {
        if (Strings.isNullOrEmpty(logicTableName) || !shardingTables.containsKey(logicTableName)) {
            return Optional.empty();
        }
        return Optional.of(shardingTables.get(logicTableName));
    }
    
    /**
     * Find sharding table via actual table name.
     *
     * @param actualTableName actual table name
     * @return sharding table
     */
    public Optional<ShardingTable> findShardingTableByActualTable(final String actualTableName) {
        for (ShardingTable each : shardingTables.values()) {
            if (each.isExisted(actualTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get sharding table.
     *
     * @param logicTableName logic table name
     * @return sharding table
     * @throws ShardingTableRuleNotFoundException sharding table rule not found exception
     */
    public ShardingTable getShardingTable(final String logicTableName) {
        return findShardingTable(logicTableName).orElseThrow(() -> new ShardingTableRuleNotFoundException(Collections.singleton(logicTableName)));
    }
    
    /**
     * Judge whether logic table is all config binding tables or not.
     *
     * @param logicTableNames logic table names
     * @return whether logic table is all config binding tables or not
     */
    public boolean isAllConfigBindingTables(final Collection<String> logicTableNames) {
        if (logicTableNames.isEmpty()) {
            return false;
        }
        Optional<BindingTableRule> bindingTableRule = findBindingTableRule(logicTableNames);
        if (!bindingTableRule.isPresent()) {
            return false;
        }
        Collection<String> result = new CaseInsensitiveSet<>(bindingTableRule.get().getAllLogicTables());
        return !result.isEmpty() && result.containsAll(logicTableNames);
    }
    
    /**
     * Judge whether logic table is all config binding tables and use sharding columns join.
     *
     * @param sqlStatementContext sqlStatementContext
     * @param logicTableNames logic table names
     * @return whether logic table is all config binding tables and use sharding columns join
     */
    public boolean isBindingTablesUseShardingColumnsJoin(final SQLStatementContext sqlStatementContext, final Collection<String> logicTableNames) {
        if (!(sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsJoinQuery())) {
            return isAllConfigBindingTables(logicTableNames);
        }
        if (!isAllConfigBindingTables(logicTableNames)) {
            return false;
        }
        SelectStatementContext select = (SelectStatementContext) sqlStatementContext;
        return isJoinConditionContainsShardingColumns(logicTableNames, select.getWhereSegments());
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
        return Optional.ofNullable(bindingTableRules.get(logicTableName));
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
        return shardingTables.containsKey(logicTableName);
    }
    
    /**
     * Judge whether all tables are in same data source or not.
     *
     * @param logicTableNames logic table names
     * @return whether all tables are in same data source or not
     */
    public boolean isAllTablesInSameDataSource(final Collection<String> logicTableNames) {
        Collection<String> dataSourceNames = new HashSet<>();
        for (String each : logicTableNames) {
            ShardingTable shardingTable = shardingTables.get(each);
            if (null == shardingTable) {
                continue;
            }
            dataSourceNames.addAll(shardingTable.getActualDataSourceNames());
            if (dataSourceNames.size() > 1) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Judge whether contains sharding table or not.
     *
     * @param logicTableNames logic table names
     * @return whether contains sharding table or not
     */
    public boolean containsShardingTable(final Collection<String> logicTableNames) {
        for (String each : logicTableNames) {
            if (isShardingTable(each)) {
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
        return Optional.ofNullable(shardingTables.get(tableName)).flatMap(optional -> findShardingColumn(optional, columnName));
    }
    
    private Optional<String> findShardingColumn(final ShardingTable shardingTable, final String columnName) {
        Optional<String> databaseShardingColumn = findShardingColumn(getDatabaseShardingStrategyConfiguration(shardingTable), columnName);
        if (databaseShardingColumn.isPresent()) {
            return databaseShardingColumn;
        }
        return findShardingColumn(getTableShardingStrategyConfiguration(shardingTable), columnName);
    }
    
    private Optional<String> findShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfig, final String columnName) {
        if (shardingStrategyConfig instanceof StandardShardingStrategyConfiguration) {
            String shardingColumn = null == ((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn()
                    ? defaultShardingColumn
                    : ((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn();
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
     * Judge whether given logic table column is key generated column or not.
     *
     * @param columnName column name
     * @param tableName table name
     * @return whether given logic table column is key generated column or not
     */
    public boolean isGenerateKeyColumn(final String columnName, final String tableName) {
        return Optional.ofNullable(shardingTables.get(tableName)).filter(each -> isGenerateKeyColumn(each, columnName)).isPresent();
    }
    
    private boolean isGenerateKeyColumn(final ShardingTable shardingTable, final String columnName) {
        Optional<String> generateKeyColumn = shardingTable.getGenerateKeyColumn();
        return generateKeyColumn.isPresent() && generateKeyColumn.get().equalsIgnoreCase(columnName);
    }
    
    /**
     * Find column name of generated key.
     *
     * @param logicTableName logic table name
     * @return column name of generated key
     */
    public Optional<String> findGenerateKeyColumnName(final String logicTableName) {
        return Optional.ofNullable(shardingTables.get(logicTableName)).filter(each -> each.getGenerateKeyColumn().isPresent()).flatMap(ShardingTable::getGenerateKeyColumn);
    }
    
    /**
     * Find the generated keys of logic table.
     *
     * @param algorithmSQLContext key generate context 
     * @param keyGenerateCount key generate count
     * @return generated keys
     */
    public Collection<? extends Comparable<?>> generateKeys(final AlgorithmSQLContext algorithmSQLContext, final int keyGenerateCount) {
        return getKeyGenerateAlgorithm(algorithmSQLContext.getTableName()).generateKeys(algorithmSQLContext, keyGenerateCount);
    }
    
    /**
     * Judge whether support auto increment or not.
     *
     * @param logicTableName logic table name
     * @return whether support auto increment or not
     */
    public boolean isSupportAutoIncrement(final String logicTableName) {
        return getKeyGenerateAlgorithm(logicTableName).isSupportAutoIncrement();
    }
    
    private KeyGenerateAlgorithm getKeyGenerateAlgorithm(final String logicTableName) {
        ShardingTable shardingTable = getShardingTable(logicTableName);
        return null == shardingTable.getKeyGeneratorName() ? defaultKeyGenerateAlgorithm : keyGenerators.get(shardingTable.getKeyGeneratorName());
    }
    
    /**
     * Find data node by logic table name.
     *
     * @param logicTableName logic table name
     * @return data node
     */
    public DataNode getDataNode(final String logicTableName) {
        ShardingTable shardingTable = getShardingTable(logicTableName);
        return shardingTable.getActualDataNodes().get(0);
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
        return findBindingTableRule(logicTable).map(optional -> optional.getLogicAndActualTables(dataSourceName, logicTable, actualTable, availableLogicBindingTables))
                .orElseGet(Collections::emptyMap);
    }
    
    /**
     * Is sharding cache enabled.
     *
     * @return is sharding cache enabled
     */
    public boolean isShardingCacheEnabled() {
        return null != shardingCache;
    }
    
    private boolean isJoinConditionContainsShardingColumns(final Collection<String> tableNames, final Collection<WhereSegment> whereSegments) {
        Collection<String> databaseJoinConditionTables = new CaseInsensitiveSet<>(tableNames.size(), 1F);
        Collection<String> tableJoinConditionTables = new CaseInsensitiveSet<>(tableNames.size(), 1F);
        for (WhereSegment each : whereSegments) {
            Collection<AndPredicate> andPredicates = ExpressionExtractor.extractAndPredicates(each.getExpr());
            if (andPredicates.size() > 1) {
                return false;
            }
            for (AndPredicate andPredicate : andPredicates) {
                databaseJoinConditionTables.addAll(getJoinConditionTables(andPredicate.getPredicates(), true));
                tableJoinConditionTables.addAll(getJoinConditionTables(andPredicate.getPredicates(), false));
            }
        }
        ShardingTable shardingTable = getShardingTable(tableNames.iterator().next());
        boolean containsDatabaseShardingColumns = !(getDatabaseShardingStrategyConfiguration(shardingTable) instanceof StandardShardingStrategyConfiguration)
                || databaseJoinConditionTables.containsAll(tableNames);
        boolean containsTableShardingColumns =
                !(getTableShardingStrategyConfiguration(shardingTable) instanceof StandardShardingStrategyConfiguration) || tableJoinConditionTables.containsAll(tableNames);
        return containsDatabaseShardingColumns && containsTableShardingColumns;
    }
    
    private Collection<String> getJoinConditionTables(final Collection<ExpressionSegment> predicates, final boolean isDatabaseJoinCondition) {
        Collection<String> result = new LinkedList<>();
        for (ExpressionSegment each : predicates) {
            if (!isJoinConditionExpression(each)) {
                continue;
            }
            ColumnSegment leftColumn = (ColumnSegment) ((BinaryOperationExpression) each).getLeft();
            ColumnSegment rightColumn = (ColumnSegment) ((BinaryOperationExpression) each).getRight();
            Optional<ShardingTable> leftShardingTable = findShardingTable(leftColumn.getColumnBoundInfo().getOriginalTable().getValue());
            Optional<ShardingTable> rightShardingTable = findShardingTable(rightColumn.getColumnBoundInfo().getOriginalTable().getValue());
            if (!leftShardingTable.isPresent() || !rightShardingTable.isPresent()) {
                continue;
            }
            ShardingStrategyConfiguration leftConfig =
                    isDatabaseJoinCondition ? getDatabaseShardingStrategyConfiguration(leftShardingTable.get()) : getTableShardingStrategyConfiguration(leftShardingTable.get());
            ShardingStrategyConfiguration rightConfig =
                    isDatabaseJoinCondition ? getDatabaseShardingStrategyConfiguration(rightShardingTable.get()) : getTableShardingStrategyConfiguration(rightShardingTable.get());
            if (findShardingColumn(leftConfig, leftColumn.getIdentifier().getValue()).isPresent() && findShardingColumn(rightConfig, rightColumn.getIdentifier().getValue()).isPresent()) {
                result.add(leftColumn.getColumnBoundInfo().getOriginalTable().getValue());
                result.add(rightColumn.getColumnBoundInfo().getOriginalTable().getValue());
            }
        }
        return result;
    }
    
    private boolean isJoinConditionExpression(final ExpressionSegment expression) {
        if (!(expression instanceof BinaryOperationExpression)) {
            return false;
        }
        BinaryOperationExpression binaryExpression = (BinaryOperationExpression) expression;
        return binaryExpression.getLeft() instanceof ColumnSegment && binaryExpression.getRight() instanceof ColumnSegment && "=".equals(binaryExpression.getOperator());
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
}
