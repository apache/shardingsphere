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

package org.apache.shardingsphere.singletable.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.datanode.SingleTableDataNodeLoader;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Single table rule.
 */
public final class SingleTableRule implements DatabaseRule, DataNodeContainedRule, TableContainedRule, MutableDataNodeRule, ExportableRule {
    
    @Getter
    private final SingleTableRuleConfiguration configuration;
    
    private final String defaultDataSource;
    
    @Getter
    private final Collection<String> dataSourceNames;
    
    @Getter
    private final Map<String, Collection<DataNode>> singleTableDataNodes;
    
    private final Map<String, String> tableNames;
    
    public SingleTableRule(final SingleTableRuleConfiguration ruleConfig, final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        configuration = ruleConfig;
        defaultDataSource = ruleConfig.getDefaultDataSource().orElse(null);
        Map<String, DataSource> aggregateDataSourceMap = getAggregateDataSourceMap(dataSourceMap, builtRules);
        dataSourceNames = aggregateDataSourceMap.keySet();
        singleTableDataNodes = SingleTableDataNodeLoader.load(databaseName, DatabaseTypeEngine.getDatabaseType(dataSourceMap.values()), aggregateDataSourceMap, getLoadedTables(builtRules));
        tableNames = singleTableDataNodes.entrySet().stream().collect(Collectors.toConcurrentMap(Entry::getKey, entry -> entry.getValue().iterator().next().getTableName()));
    }
    
    private Map<String, DataSource> getAggregateDataSourceMap(final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceMap);
        for (ShardingSphereRule each : builtRules) {
            if (each instanceof DataSourceContainedRule) {
                result = getAggregateDataSourceMap(result, (DataSourceContainedRule) each);
            }
        }
        return result;
    }
    
    private Map<String, DataSource> getAggregateDataSourceMap(final Map<String, DataSource> dataSourceMap, final DataSourceContainedRule builtRule) {
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (Entry<String, Collection<String>> entry : builtRule.getDataSourceMapper().entrySet()) {
            for (String each : entry.getValue()) {
                if (dataSourceMap.containsKey(each)) {
                    result.putIfAbsent(entry.getKey(), dataSourceMap.remove(each));
                }
            }
        }
        result.putAll(dataSourceMap);
        return result;
    }
    
    private Collection<String> getLoadedTables(final Collection<ShardingSphereRule> builtRules) {
        return builtRules.stream().filter(each -> each instanceof DataNodeContainedRule)
                .flatMap(each -> ((DataNodeContainedRule) each).getAllTables().stream()).collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));
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
            Optional<DataNode> dataNode = findSingleTableDataNode(each.getSchemaName(), each.getTableName());
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
        Optional<DataNode> dataNode = findSingleTableDataNode(sampleTable.getSchemaName(), sampleTable.getTableName());
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
            tableNames.put(tableName.toLowerCase(), tableName);
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
        dataNodes.removeIf(each -> schemaNames.contains(each.getSchemaName().toLowerCase()));
        if (dataNodes.isEmpty()) {
            singleTableDataNodes.remove(tableName.toLowerCase());
            tableNames.remove(tableName.toLowerCase());
        }
    }
    
    @Override
    public Optional<DataNode> findSingleTableDataNode(final String schemaName, final String tableName) {
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
        return new SingleTableRule((SingleTableRuleConfiguration) config, databaseName, dataSourceMap, builtRules);
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
    public Collection<String> getAllTables() {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(tableNames.values());
        return result;
    }
    
    @Override
    public Collection<String> getTables() {
        return new HashSet<>(tableNames.values());
    }
    
    @Override
    public Map<String, Object> getExportData() {
        return Collections.singletonMap("single_tables", tableNames.keySet());
    }
    
    @Override
    public String getType() {
        return SingleTableRule.class.getSimpleName();
    }
}
