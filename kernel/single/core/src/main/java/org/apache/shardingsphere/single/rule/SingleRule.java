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

package org.apache.shardingsphere.single.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableNamesMapper;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Single rule.
 */
public final class SingleRule implements DatabaseRule, DataNodeContainedRule, TableContainedRule, MutableDataNodeRule, ExportableRule {
    
    @Getter
    private final SingleRuleConfiguration configuration;
    
    private final String defaultDataSource;
    
    @Getter
    private final Collection<String> dataSourceNames;
    
    @Getter
    private final Map<String, Collection<DataNode>> singleTableDataNodes;
    
    private final TableNamesMapper tableNamesMapper = new TableNamesMapper();
    
    private final DatabaseType databaseType;
    
    public SingleRule(final SingleRuleConfiguration ruleConfig, final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        configuration = ruleConfig;
        defaultDataSource = ruleConfig.getDefaultDataSource().orElse(null);
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSourceMap(databaseName, dataSourceMap);
        Map<String, DataSource> aggregateDataSourceMap = SingleTableLoadUtils.getAggregatedDataSourceMap(enabledDataSources, builtRules);
        dataSourceNames = aggregateDataSourceMap.keySet();
        databaseType = DatabaseTypeEngine.getStorageType(enabledDataSources.values());
        singleTableDataNodes = SingleTableDataNodeLoader.load(databaseName, databaseType, aggregateDataSourceMap, builtRules, configuration.getTables());
        singleTableDataNodes.forEach((key, value) -> tableNamesMapper.put(value.iterator().next().getTableName()));
    }
    
    /**
     * Assign new data source name.
     *
     * @return assigned data source name
     */
    public String assignNewDataSourceName() {
        return null == defaultDataSource ? new ArrayList<>(dataSourceNames).get(ThreadLocalRandom.current().nextInt(dataSourceNames.size())) : defaultDataSource;
    }
    
    /**
     * Judge whether single tables are in same data source or not.
     *
     * @param singleTableNames single table names
     * @return whether single tables are in same data source or not
     */
    public boolean isSingleTablesInSameDataSource(final Collection<QualifiedTable> singleTableNames) {
        String firstFoundDataSourceName = null;
        for (QualifiedTable each : singleTableNames) {
            Optional<DataNode> dataNode = findTableDataNode(each.getSchemaName(), each.getTableName());
            if (!dataNode.isPresent()) {
                continue;
            }
            if (null == firstFoundDataSourceName) {
                firstFoundDataSourceName = dataNode.get().getDataSourceName();
                continue;
            }
            if (!firstFoundDataSourceName.equals(dataNode.get().getDataSourceName())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Judge whether all tables are in same data source or not.
     * 
     * @param routeContext route context
     * @param singleTableNames single table names
     * @return whether all tables are in same data source or not
     */
    public boolean isAllTablesInSameDataSource(final RouteContext routeContext, final Collection<QualifiedTable> singleTableNames) {
        if (!isSingleTablesInSameDataSource(singleTableNames)) {
            return false;
        }
        QualifiedTable sampleTable = singleTableNames.iterator().next();
        Optional<DataNode> dataNode = findTableDataNode(sampleTable.getSchemaName(), sampleTable.getTableName());
        if (dataNode.isPresent()) {
            for (RouteUnit each : routeContext.getRouteUnits()) {
                if (!each.getDataSourceMapper().getLogicName().equals(dataNode.get().getDataSourceName())) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Get single table names.
     *
     * @param qualifiedTables qualified tables
     * @return single table names
     */
    public Collection<QualifiedTable> getSingleTableNames(final Collection<QualifiedTable> qualifiedTables) {
        Collection<QualifiedTable> result = new LinkedList<>();
        for (QualifiedTable each : qualifiedTables) {
            Collection<DataNode> dataNodes = singleTableDataNodes.getOrDefault(each.getTableName().toLowerCase(), new LinkedList<>());
            if (!dataNodes.isEmpty() && containsDataNode(each, dataNodes)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean containsDataNode(final QualifiedTable qualifiedTable, final Collection<DataNode> dataNodes) {
        for (DataNode each : dataNodes) {
            if (qualifiedTable.getSchemaName().equalsIgnoreCase(each.getSchemaName())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void put(final String dataSourceName, final String schemaName, final String tableName) {
        if (dataSourceNames.contains(dataSourceName)) {
            Collection<DataNode> dataNodes = singleTableDataNodes.computeIfAbsent(tableName.toLowerCase(), key -> new LinkedHashSet<>());
            DataNode dataNode = new DataNode(dataSourceName, tableName);
            dataNode.setSchemaName(schemaName);
            dataNodes.add(dataNode);
            tableNamesMapper.put(tableName);
            addTableConfiguration(dataSourceName, schemaName, tableName);
        }
    }
    
    private void addTableConfiguration(final String dataSourceName, final String schemaName, final String tableName) {
        Collection<String> splitTables = SingleTableLoadUtils.splitTableLines(configuration.getTables());
        if (splitTables.contains(SingleTableLoadUtils.getAllTablesNodeStr(databaseType))
                || splitTables.contains(SingleTableLoadUtils.getAllTablesNodeStrFromDataSource(databaseType, dataSourceName, schemaName))) {
            return;
        }
        String dataNodeString = SingleTableLoadUtils.getDataNodeString(databaseType, dataSourceName, schemaName, tableName);
        if (!configuration.getTables().contains(dataNodeString)) {
            configuration.getTables().add(dataNodeString);
        }
    }
    
    @Override
    public void remove(final String schemaName, final String tableName) {
        remove(Collections.singleton(schemaName.toLowerCase()), tableName);
    }
    
    @Override
    public void remove(final Collection<String> schemaNames, final String tableName) {
        if (!singleTableDataNodes.containsKey(tableName.toLowerCase())) {
            return;
        }
        Collection<DataNode> dataNodes = singleTableDataNodes.get(tableName.toLowerCase());
        Iterator<DataNode> iterator = dataNodes.iterator();
        while (iterator.hasNext()) {
            DataNode each = iterator.next();
            if (schemaNames.contains(each.getSchemaName().toLowerCase())) {
                iterator.remove();
                configuration.getTables().remove(SingleTableLoadUtils.getDataNodeString(databaseType, each.getDataSourceName(), each.getSchemaName(), tableName));
            }
        }
        if (dataNodes.isEmpty()) {
            singleTableDataNodes.remove(tableName.toLowerCase());
            tableNamesMapper.remove(tableName);
        }
    }
    
    @Override
    public Optional<DataNode> findTableDataNode(final String schemaName, final String tableName) {
        Collection<DataNode> dataNodes = singleTableDataNodes.getOrDefault(tableName.toLowerCase(), new LinkedHashSet<>());
        for (DataNode each : dataNodes) {
            if (schemaName.equalsIgnoreCase(each.getSchemaName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public ShardingSphereRule reloadRule(final RuleConfiguration config, final String databaseName, final Map<String, DataSource> dataSourceMap,
                                         final Collection<ShardingSphereRule> builtRules) {
        return new SingleRule((SingleRuleConfiguration) config, databaseName, dataSourceMap, builtRules);
    }
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        return singleTableDataNodes;
    }
    
    @Override
    public Collection<DataNode> getDataNodesByTableName(final String tableName) {
        return singleTableDataNodes.getOrDefault(tableName.toLowerCase(), Collections.emptyList());
    }
    
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return Optional.empty();
    }
    
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return false;
    }
    
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return Optional.empty();
    }
    
    @Override
    public Optional<String> findActualTableByCatalog(final String catalog, final String logicTable) {
        return Optional.empty();
    }
    
    @Override
    public TableNamesMapper getLogicTableMapper() {
        return tableNamesMapper;
    }
    
    @Override
    public TableNamesMapper getActualTableMapper() {
        return new TableNamesMapper();
    }
    
    @Override
    public TableNamesMapper getDistributedTableMapper() {
        return new TableNamesMapper();
    }
    
    @Override
    public TableNamesMapper getEnhancedTableMapper() {
        return new TableNamesMapper();
    }
    
    @Override
    public Map<String, Object> getExportData() {
        return Collections.singletonMap(ExportableConstants.EXPORT_SINGLE_TABLES, tableNamesMapper.getTableNames());
    }
    
    @Override
    public String getType() {
        return SingleRule.class.getSimpleName();
    }
}
