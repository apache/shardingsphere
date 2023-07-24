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

package org.apache.shardingsphere.broadcast.rule;

import lombok.Getter;
import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableNamesMapper;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Broadcast rule.
 */
@Getter
public final class BroadcastRule implements DatabaseRule, DataNodeContainedRule, TableContainedRule {
    
    private final BroadcastRuleConfiguration configuration;
    
    private final String databaseName;
    
    private final Collection<String> tables;
    
    private final Collection<String> dataSourceNames;
    
    private final Map<String, Collection<DataNode>> tableDataNodes;
    
    private final TableNamesMapper logicalTableMapper;
    
    public BroadcastRule(final BroadcastRuleConfiguration configuration, final String databaseName, final Map<String, DataSource> dataSources) {
        this.configuration = configuration;
        this.databaseName = databaseName;
        dataSourceNames = getDataSourceNames(dataSources);
        tables = createBroadcastTables(configuration.getTables());
        logicalTableMapper = createTableMapper();
        tableDataNodes = createShardingTableDataNodes(dataSourceNames, tables);
    }
    
    private Collection<String> getDataSourceNames(final Map<String, DataSource> dataSources) {
        return new LinkedList<>(dataSources.keySet());
    }
    
    private Collection<String> createBroadcastTables(final Collection<String> broadcastTables) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(broadcastTables);
        return result;
    }
    
    private TableNamesMapper createTableMapper() {
        TableNamesMapper result = new TableNamesMapper();
        tables.forEach(result::put);
        return result;
    }
    
    private Map<String, Collection<DataNode>> createShardingTableDataNodes(final Collection<String> dataSourceNames, final Collection<String> tables) {
        Map<String, Collection<DataNode>> result = new HashMap<>(tables.size(), 1F);
        for (String each : tables) {
            result.put(each.toLowerCase(), generateDataNodes(each, dataSourceNames));
        }
        return result;
    }
    
    private Collection<DataNode> generateDataNodes(final String logicTable, final Collection<String> dataSourceNames) {
        Collection<DataNode> result = new LinkedList<>();
        for (String each : dataSourceNames) {
            result.add(new DataNode(each, logicTable));
        }
        return result;
    }
    
    @Override
    public String getType() {
        return BroadcastRule.class.getSimpleName();
    }
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        return tableDataNodes;
    }
    
    @Override
    public Collection<DataNode> getDataNodesByTableName(final String tableName) {
        return tableDataNodes.getOrDefault(tableName, Collections.emptyList());
    }
    
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return tableDataNodes.containsKey(logicTable.toLowerCase()) ? Optional.of(logicTable) : Optional.empty();
    }
    
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return !isAllBroadcastTables(tables);
    }
    
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return tableDataNodes.containsKey(actualTable.toLowerCase()) ? Optional.of(actualTable) : Optional.empty();
    }
    
    @Override
    public Optional<String> findActualTableByCatalog(final String catalog, final String logicTable) {
        if (!tableDataNodes.containsKey(logicTable.toLowerCase())) {
            return Optional.empty();
        }
        if (tableDataNodes.get(logicTable.toLowerCase()).stream().noneMatch(each -> each.getDataSourceName().equalsIgnoreCase(catalog))) {
            return Optional.empty();
        }
        return Optional.of(logicTable);
    }
    
    /**
     * Get broadcast rule table names.
     * 
     * @param logicTableNames logic table names
     * @return broadcast rule table names.
     */
    public Collection<String> getBroadcastRuleTableNames(final Collection<String> logicTableNames) {
        return logicTableNames.stream().filter(tables::contains).collect(Collectors.toSet());
    }
    
    /**
     * Judge whether logic table is all broadcast tables or not.
     *
     * @param logicTableNames logic table names
     * @return whether logic table is all broadcast tables or not
     */
    public boolean isAllBroadcastTables(final Collection<String> logicTableNames) {
        return !logicTableNames.isEmpty() && tables.containsAll(logicTableNames);
    }
    
    /**
     * Get available datasource names.
     * @return datasource names
     */
    public Collection<String> getAvailableDataSourceNames() {
        return dataSourceNames;
    }
    
    @Override
    public TableNamesMapper getLogicTableMapper() {
        return logicalTableMapper;
    }
    
    @Override
    public TableNamesMapper getActualTableMapper() {
        return new TableNamesMapper();
    }
    
    @Override
    public TableNamesMapper getDistributedTableMapper() {
        return getLogicTableMapper();
    }
    
    @Override
    public TableNamesMapper getEnhancedTableMapper() {
        return new TableNamesMapper();
    }
}
