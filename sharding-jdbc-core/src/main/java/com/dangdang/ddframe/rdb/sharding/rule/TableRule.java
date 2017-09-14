/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.rule;

import com.dangdang.ddframe.rdb.sharding.keygen.KeyGenerator;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import lombok.Getter;
import lombok.ToString;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Table rule configuration.
 * 
 * @author zhangliang
 */
@Getter
@ToString
public final class TableRule {
    
    private final String logicTable;
    
    private final boolean dynamic;
    
    private final List<DataNode> actualDataNodes;
    
    private final ShardingStrategy databaseShardingStrategy;
    
    private final ShardingStrategy tableShardingStrategy;
    
    private final String generateKeyColumn;
    
    private final KeyGenerator keyGenerator;
    
    public TableRule(final String logicTable, final boolean dynamic, final List<String> actualTables, final List<String> actualDataSources, final Map<String, DataSource> dataSourceMap,
                     final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy, final String generateKeyColumn, final KeyGenerator keyGenerator) {
        this.logicTable = logicTable;
        this.dynamic = dynamic;
        if (dynamic) {
            actualDataNodes = generateDataNodes(dataSourceMap);
        } else if (null == actualTables || actualTables.isEmpty()) {
            actualDataNodes = generateDataNodes(Collections.singletonList(logicTable), dataSourceMap, actualDataSources);
        } else {
            actualDataNodes = generateDataNodes(actualTables, dataSourceMap, actualDataSources);
        }
        this.databaseShardingStrategy = databaseShardingStrategy;
        this.tableShardingStrategy = tableShardingStrategy;
        this.generateKeyColumn = generateKeyColumn;
        this.keyGenerator = keyGenerator;
    }
    
    private List<DataNode> generateDataNodes(final Map<String, DataSource> dataSourceMap) {
        List<DataNode> result = new ArrayList<>(dataSourceMap.size());
        for (String each : dataSourceMap.keySet()) {
            result.add(new DynamicDataNode(each));
        }
        return result;
    }
    
    private List<DataNode> generateDataNodes(final List<String> actualTables, final Map<String, DataSource> dataSourceMap, final Collection<String> actualDataSourceNames) {
        Collection<String> dataSourceNames = getDataSourceNames(dataSourceMap, actualDataSourceNames);
        List<DataNode> result = new ArrayList<>(actualTables.size() * (dataSourceNames.isEmpty() ? 1 : dataSourceNames.size()));
        for (String actualTable : actualTables) {
            if (DataNode.isValidDataNode(actualTable)) {
                result.add(new DataNode(actualTable));
            } else {
                for (String dataSourceName : dataSourceNames) {
                    result.add(new DataNode(dataSourceName, actualTable));
                }
            }
        }
        return result;
    }
    
    private Collection<String> getDataSourceNames(final Map<String, DataSource> dataSourceMap, final Collection<String> actualDataSourceNames) {
        if (null == dataSourceMap) {
            return Collections.emptyList();
        }
        if (null == actualDataSourceNames || actualDataSourceNames.isEmpty()) {
            return dataSourceMap.keySet();
        }
        return actualDataSourceNames;
    }
    
    /**
     * Get actual data source names.
     *
     * @return actual data source names
     */
    public Collection<String> getActualDatasourceNames() {
        Collection<String> result = new LinkedHashSet<>(actualDataNodes.size());
        for (DataNode each : actualDataNodes) {
            result.add(each.getDataSourceName());
        }
        return result;
    }
    
    /**
     * Get actual table names via target data source name.
     *
     * @param targetDataSource target data source name
     * @return names of actual tables
     */
    public Collection<String> getActualTableNames(final String targetDataSource) {
        Collection<String> result = new LinkedHashSet<>(actualDataNodes.size());
        for (DataNode each : actualDataNodes) {
            if (targetDataSource.equals(each.getDataSourceName())) {
                result.add(each.getTableName());
            }
        }
        return result;
    }
    
    int findActualTableIndex(final String dataSourceName, final String actualTableName) {
        int result = 0;
        for (DataNode each : actualDataNodes) {
            if (each.getDataSourceName().equalsIgnoreCase(dataSourceName) && each.getTableName().equalsIgnoreCase(actualTableName)) {
                return result;
            }
            result++;
        }
        return -1;
    }
}
