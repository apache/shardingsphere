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

package org.apache.shardingsphere.infra.rule.single;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.level.KernelRule;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Single table rule.
 */
@Getter
public final class SingleTableRule implements KernelRule, SchemaRule, DataNodeContainedRule {
    
    private final Collection<String> dataSourceNames;
    
    private final Map<String, SingleTableDataNode> singleTableDataNodes;
    
    public SingleTableRule(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        Map<String, DataSource> aggregateDataSourceMap = getAggregateDataSourceMap(dataSourceMap, rules);
        dataSourceNames = aggregateDataSourceMap.keySet();
        singleTableDataNodes = SingleTableDataNodeLoader.load(databaseType, aggregateDataSourceMap, getExcludedTables(rules));
    }
    
    private Map<String, DataSource> getAggregateDataSourceMap(final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        Map<String, DataSource> result = new HashMap<>(dataSourceMap);
        for (ShardingSphereRule each : rules) {
            if (each instanceof DataSourceContainedRule) {
                result = getAggregateDataSourceMap(result, (DataSourceContainedRule) each);
            }
        }
        return result;
    }
    
    private Map<String, DataSource> getAggregateDataSourceMap(final Map<String, DataSource> dataSourceMap, final DataSourceContainedRule rule) {
        Map<String, DataSource> result = new HashMap<>();
        for (Entry<String, Collection<String>> entry : rule.getDataSourceMapper().entrySet()) {
            for (String each : entry.getValue()) {
                if (dataSourceMap.containsKey(each)) {
                    result.put(entry.getKey(), dataSourceMap.remove(each));
                }
            }
        }
        result.putAll(dataSourceMap);
        return result;
    }
    
    /**
     * Judge whether single table is in same data source or not.
     *
     * @param logicTableNames logic table names
     * @return whether single table is in same data source or not
     */
    public boolean isSingleTableInSameDataSource(final Collection<String> logicTableNames) {
        Set<String> singleTableNames = new HashSet<>(getSingleTableNames(logicTableNames));
        long dataSourceCount = singleTableDataNodes.keySet().stream().filter(singleTableNames::contains).map(each -> singleTableDataNodes.get(each).getDataSourceName())
                .collect(Collectors.toSet()).size();
        return dataSourceCount <= 1;
    }
    
    /**
     * Get sharding logic table names.
     *
     * @param logicTableNames logic table names
     * @return sharding logic table names
     */
    public Collection<String> getSingleTableNames(final Collection<String> logicTableNames) {
        return logicTableNames.stream().filter(singleTableDataNodes::containsKey).collect(Collectors.toCollection(LinkedList::new));
    }
    
    /**
     * Add single table data node.
     * 
     * @param tableName table name
     * @param dataSourceName data source name
     */
    public void addSingleTableDataNode(final String tableName, final String dataSourceName) {
        if (dataSourceNames.contains(dataSourceName) && !singleTableDataNodes.containsKey(tableName)) {
            singleTableDataNodes.put(tableName, new SingleTableDataNode(tableName, dataSourceName));
        }
    }
    
    /**
     * Drop single table data node.
     *
     * @param tableName table name
     */
    public void dropSingleTableDataNode(final String tableName) {
        singleTableDataNodes.remove(tableName);
    }
    
    private Collection<String> getExcludedTables(final Collection<ShardingSphereRule> rules) {
        return rules.stream().filter(each -> each instanceof DataNodeContainedRule).flatMap(each -> ((DataNodeContainedRule) each).getAllTables().stream()).collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        Map<String, Collection<DataNode>> result = new LinkedHashMap<>();
        singleTableDataNodes.forEach((key, value) -> result.put(key, Collections.singleton(new DataNode(value.getDataSourceName(), value.getTableName()))));
        return result;
    }
    
    @Override
    public Collection<String> getAllActualTables() {
        return Collections.emptyList();
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
        return singleTableDataNodes.keySet();
    }
}
