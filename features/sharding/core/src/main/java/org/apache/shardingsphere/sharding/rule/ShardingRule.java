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
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmNotFoundOnTableException;
import org.apache.shardingsphere.infra.algorithm.keygen.core.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.InstanceContextAware;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
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
import org.apache.shardingsphere.sharding.exception.metadata.DuplicateSharingActualDataNodeException;
import org.apache.shardingsphere.sharding.exception.metadata.InvalidBindingTablesException;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingTableRuleNotFoundException;
import org.apache.shardingsphere.sharding.rule.attribute.ShardingDataNodeRuleAttribute;
import org.apache.shardingsphere.sharding.rule.attribute.ShardingTableMapperRuleAttribute;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Sharding rule.
 */
@Getter
public final class ShardingRule implements DatabaseRule {
    
    private static final String ALGORITHM_EXPRESSION_KEY = "algorithm-expression";
    
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
    
    public ShardingRule(final ShardingRuleConfiguration ruleConfig, final Map<String, DataSource> dataSources, final InstanceContext instanceContext) {
        configuration = ruleConfig;
        this.dataSourceNames = getDataSourceNames(ruleConfig.getTables(), ruleConfig.getAutoTables(), dataSources.keySet());
        ruleConfig.getShardingAlgorithms().forEach((key, value) -> shardingAlgorithms.put(key, TypedSPILoader.getService(ShardingAlgorithm.class, value.getType(), value.getProps())));
        ruleConfig.getKeyGenerators().forEach((key, value) -> keyGenerators.put(key, TypedSPILoader.getService(KeyGenerateAlgorithm.class, value.getType(), value.getProps())));
        ruleConfig.getAuditors().forEach((key, value) -> auditors.put(key, TypedSPILoader.getService(ShardingAuditAlgorithm.class, value.getType(), value.getProps())));
        shardingTables.putAll(createShardingTables(ruleConfig.getTables(), ruleConfig.getDefaultKeyGenerateStrategy()));
        shardingTables.putAll(createShardingAutoTables(ruleConfig.getAutoTables(), ruleConfig.getDefaultKeyGenerateStrategy()));
        validateUniqueActualDataNodesInTableRules();
        bindingTableRules.putAll(createBindingTableRules(ruleConfig.getBindingTableGroups()));
        defaultDatabaseShardingStrategyConfig = createDefaultDatabaseShardingStrategyConfig(ruleConfig);
        defaultTableShardingStrategyConfig = createDefaultTableShardingStrategyConfig(ruleConfig);
        defaultAuditStrategy = null == ruleConfig.getDefaultAuditStrategy() ? new ShardingAuditStrategyConfiguration(Collections.emptyList(), true) : ruleConfig.getDefaultAuditStrategy();
        defaultKeyGenerateAlgorithm = null == ruleConfig.getDefaultKeyGenerateStrategy()
                ? TypedSPILoader.getService(KeyGenerateAlgorithm.class, null)
                : keyGenerators.get(ruleConfig.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
        defaultShardingColumn = ruleConfig.getDefaultShardingColumn();
        ShardingSpherePreconditions.checkState(isValidBindingTableConfiguration(shardingTables, new BindingTableCheckedConfiguration(this.dataSourceNames, shardingAlgorithms,
                ruleConfig.getBindingTableGroups(), defaultDatabaseShardingStrategyConfig, defaultTableShardingStrategyConfig, defaultShardingColumn)),
                InvalidBindingTablesException::new);
        keyGenerators.values().stream().filter(InstanceContextAware.class::isInstance).forEach(each -> ((InstanceContextAware) each).setInstanceContext(instanceContext));
        if (defaultKeyGenerateAlgorithm instanceof InstanceContextAware && -1 == instanceContext.getWorkerId()) {
            ((InstanceContextAware) defaultKeyGenerateAlgorithm).setInstanceContext(instanceContext);
        }
        shardingCache = null == ruleConfig.getShardingCache() ? null : new ShardingCache(ruleConfig.getShardingCache(), this);
        attributes = new RuleAttributes(new ShardingDataNodeRuleAttribute(shardingTables), new ShardingTableMapperRuleAttribute(shardingTables.values()));
    }
    
    private void validateUniqueActualDataNodesInTableRules() {
        Set<DataNode> uniqueActualDataNodes = new HashSet<>(shardingTables.size(), 1L);
        shardingTables.forEach((key, value) -> {
            DataNode sampleActualDataNode = value.getActualDataNodes().iterator().next();
            ShardingSpherePreconditions.checkState(!uniqueActualDataNodes.contains(sampleActualDataNode),
                    () -> new DuplicateSharingActualDataNodeException(key, sampleActualDataNode.getDataSourceName(), sampleActualDataNode.getTableName()));
            uniqueActualDataNodes.add(sampleActualDataNode);
        });
    }
    
    private ShardingStrategyConfiguration createDefaultDatabaseShardingStrategyConfig(final ShardingRuleConfiguration ruleConfig) {
        Optional.ofNullable(ruleConfig.getDefaultDatabaseShardingStrategy()).ifPresent(optional -> checkManualShardingAlgorithm(optional.getShardingAlgorithmName(), "default"));
        return null == ruleConfig.getDefaultDatabaseShardingStrategy() ? new NoneShardingStrategyConfiguration() : ruleConfig.getDefaultDatabaseShardingStrategy();
    }
    
    private ShardingStrategyConfiguration createDefaultTableShardingStrategyConfig(final ShardingRuleConfiguration ruleConfig) {
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
    
    private boolean isValidBindingTableConfiguration(final Map<String, ShardingTable> shardingTables, final BindingTableCheckedConfiguration checkedConfig) {
        for (ShardingTableReferenceRuleConfiguration each : checkedConfig.getBindingTableGroups()) {
            Collection<String> bindingTables = Splitter.on(",").trimResults().splitToList(each.getReference());
            if (bindingTables.size() <= 1) {
                continue;
            }
            Iterator<String> iterator = bindingTables.iterator();
            ShardingTable sampleShardingTable = getShardingTable(iterator.next(), shardingTables);
            while (iterator.hasNext()) {
                ShardingTable shardingTable = getShardingTable(iterator.next(), shardingTables);
                if (!isValidActualDataSourceName(sampleShardingTable, shardingTable) || !isValidActualTableName(sampleShardingTable, shardingTable)) {
                    return false;
                }
                if (!isBindingShardingAlgorithm(sampleShardingTable, shardingTable, true, checkedConfig) || !isBindingShardingAlgorithm(sampleShardingTable, shardingTable, false, checkedConfig)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isValidActualDataSourceName(final ShardingTable sampleShardingTable, final ShardingTable shardingTable) {
        return sampleShardingTable.getActualDataSourceNames().equals(shardingTable.getActualDataSourceNames());
    }
    
    private boolean isValidActualTableName(final ShardingTable sampleShardingTable, final ShardingTable shardingTable) {
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
    
    private boolean isBindingShardingAlgorithm(final ShardingTable sampleShardingTable, final ShardingTable shardingTable, final boolean databaseAlgorithm,
                                               final BindingTableCheckedConfiguration checkedConfig) {
        return getAlgorithmExpression(sampleShardingTable, databaseAlgorithm, checkedConfig).equals(getAlgorithmExpression(shardingTable, databaseAlgorithm, checkedConfig));
    }
    
    private Optional<String> getAlgorithmExpression(final ShardingTable shardingTable, final boolean databaseAlgorithm, final BindingTableCheckedConfiguration checkedConfig) {
        ShardingStrategyConfiguration shardingStrategyConfig = databaseAlgorithm
                ? getDatabaseShardingStrategyConfiguration(shardingTable, checkedConfig.getDefaultDatabaseShardingStrategyConfig())
                : getTableShardingStrategyConfiguration(shardingTable, checkedConfig.getDefaultTableShardingStrategyConfig());
        ShardingAlgorithm shardingAlgorithm = checkedConfig.getShardingAlgorithms().get(shardingStrategyConfig.getShardingAlgorithmName());
        String dataNodePrefix = databaseAlgorithm ? shardingTable.getDataSourceDataNode().getPrefix() : shardingTable.getTableDataNode().getPrefix();
        String shardingColumn = getShardingColumn(shardingStrategyConfig, checkedConfig.getDefaultShardingColumn());
        return null == shardingAlgorithm ? Optional.empty() : shardingAlgorithm.getAlgorithmStructure(dataNodePrefix, shardingColumn);
    }
    
    private String getShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfig, final String defaultShardingColumn) {
        String shardingColumn = defaultShardingColumn;
        if (shardingStrategyConfig instanceof ComplexShardingStrategyConfiguration) {
            shardingColumn = ((ComplexShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumns();
        }
        if (shardingStrategyConfig instanceof StandardShardingStrategyConfiguration) {
            shardingColumn = ((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn();
        }
        return null == shardingColumn ? "" : shardingColumn;
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
        Optional<ShardingTable> shardingTable = findShardingTable(logicTableName);
        if (shardingTable.isPresent()) {
            return shardingTable.get();
        }
        throw new ShardingTableRuleNotFoundException(Collections.singleton(logicTableName));
    }
    
    private ShardingTable getShardingTable(final String logicTableName, final Map<String, ShardingTable> shardingTables) {
        ShardingTable result = shardingTables.get(logicTableName);
        if (null != result) {
            return result;
        }
        throw new ShardingTableRuleNotFoundException(Collections.singleton(logicTableName));
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
     * @param database database
     * @param sqlStatementContext sqlStatementContext
     * @param logicTableNames logic table names
     * @return whether logic table is all binding tables
     */
    public boolean isAllBindingTables(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext, final Collection<String> logicTableNames) {
        if (!(sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsJoinQuery())) {
            return isAllBindingTables(logicTableNames);
        }
        if (!isAllBindingTables(logicTableNames)) {
            return false;
        }
        String defaultSchemaName = new DatabaseTypeRegistry(sqlStatementContext.getDatabaseType()).getDefaultSchemaName(database.getName());
        ShardingSphereSchema schema = sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
        SelectStatementContext select = (SelectStatementContext) sqlStatementContext;
        return isJoinConditionContainsShardingColumns(schema, select, logicTableNames, select.getWhereSegments());
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
        Collection<String> dataSourceNames = logicTableNames.stream().map(shardingTables::get)
                .filter(Objects::nonNull).flatMap(each -> each.getActualDataSourceNames().stream()).collect(Collectors.toSet());
        return 1 == dataSourceNames.size();
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
     * Judge whether given logic table column is generate key column or not.
     *
     * @param columnName column name
     * @param tableName table name
     * @return whether given logic table column is generate key column or not
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
        return getKeyGenerateAlgorithm(algorithmSQLContext.getDatabaseName(), algorithmSQLContext.getTableName()).generateKeys(algorithmSQLContext, keyGenerateCount);
    }
    
    private KeyGenerateAlgorithm getKeyGenerateAlgorithm(final String databaseName, final String logicTableName) {
        Optional<ShardingTable> shardingTable = findShardingTable(logicTableName);
        ShardingSpherePreconditions.checkState(shardingTable.isPresent(), () -> new AlgorithmNotFoundOnTableException("key generator", databaseName, logicTableName));
        return null == shardingTable.get().getKeyGeneratorName() ? defaultKeyGenerateAlgorithm : keyGenerators.get(shardingTable.get().getKeyGeneratorName());
    }
    
    /**
     * Judge whether support auto increment or not.
     * 
     * @param databaseName database name
     * @param logicTableName logic table name
     * @return whether support auto increment or not
     */
    public boolean isSupportAutoIncrement(final String databaseName, final String logicTableName) {
        return getKeyGenerateAlgorithm(databaseName, logicTableName).isSupportAutoIncrement();
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
     * Get sharding rule table names.
     *
     * @param logicTableNames logic table names
     * @return sharding rule table names
     */
    public Collection<String> getShardingRuleTableNames(final Collection<String> logicTableNames) {
        return logicTableNames.stream().filter(this::isShardingTable).collect(Collectors.toList());
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
    
    private boolean isJoinConditionContainsShardingColumns(final ShardingSphereSchema schema, final SelectStatementContext select,
                                                           final Collection<String> tableNames, final Collection<WhereSegment> whereSegments) {
        Collection<String> databaseJoinConditionTables = new HashSet<>(tableNames.size(), 1F);
        Collection<String> tableJoinConditionTables = new HashSet<>(tableNames.size(), 1F);
        for (WhereSegment each : whereSegments) {
            Collection<AndPredicate> andPredicates = ExpressionExtractUtils.getAndPredicates(each.getExpr());
            if (andPredicates.size() > 1) {
                return false;
            }
            for (AndPredicate andPredicate : andPredicates) {
                databaseJoinConditionTables.addAll(getJoinConditionTables(schema, select, andPredicate.getPredicates(), true));
                tableJoinConditionTables.addAll(getJoinConditionTables(schema, select, andPredicate.getPredicates(), false));
            }
        }
        ShardingTable shardingTable = getShardingTable(tableNames.iterator().next());
        boolean containsDatabaseShardingColumns = !(getDatabaseShardingStrategyConfiguration(shardingTable) instanceof StandardShardingStrategyConfiguration)
                || databaseJoinConditionTables.containsAll(tableNames);
        boolean containsTableShardingColumns =
                !(getTableShardingStrategyConfiguration(shardingTable) instanceof StandardShardingStrategyConfiguration) || tableJoinConditionTables.containsAll(tableNames);
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
            Optional<ShardingTable> leftShardingTable = findShardingTable(columnExpressionTableNames.get(leftColumn.getExpression()));
            Optional<ShardingTable> rightShardingTable = findShardingTable(columnExpressionTableNames.get(rightColumn.getExpression()));
            if (!leftShardingTable.isPresent() || !rightShardingTable.isPresent()) {
                continue;
            }
            ShardingStrategyConfiguration leftConfig = isDatabaseJoinCondition
                    ? getDatabaseShardingStrategyConfiguration(leftShardingTable.get())
                    : getTableShardingStrategyConfiguration(leftShardingTable.get());
            ShardingStrategyConfiguration rightConfig = isDatabaseJoinCondition
                    ? getDatabaseShardingStrategyConfiguration(rightShardingTable.get())
                    : getTableShardingStrategyConfiguration(rightShardingTable.get());
            if (findShardingColumn(leftConfig, leftColumn.getIdentifier().getValue()).isPresent()
                    && findShardingColumn(rightConfig, rightColumn.getIdentifier().getValue()).isPresent()) {
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
        return binaryExpression.getLeft() instanceof ColumnSegment && binaryExpression.getRight() instanceof ColumnSegment && "=".equals(binaryExpression.getOperator());
    }
}
