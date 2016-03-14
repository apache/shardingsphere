/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.google.common.base.Splitter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 表规则配置对象.
 * 
 * @author zhangliang
 */
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@ToString
public final class TableRule {
    
    private final String logicTable;
    
    private final List<DataNode> actualTables;
    
    private DatabaseShardingStrategy databaseShardingStrategy;
    
    private TableShardingStrategy tableShardingStrategy;
    
    public TableRule(final String logicTable, final List<String> actualTables, final DataSourceRule dataSourceRule,
                     final DatabaseShardingStrategy databaseShardingStrategy, final TableShardingStrategy tableShardingStrategy) {
        this(logicTable, new ArrayList<DataNode>(actualTables.size() * dataSourceRule.getDataSourceNames().size()), databaseShardingStrategy, tableShardingStrategy);
        generateDataNodes(actualTables, dataSourceRule);
    }
    
    public TableRule(final String logicTable, final List<String> actualTables, final DataSourceRule dataSourceRule) {
        this(logicTable, actualTables, dataSourceRule, null, null);
    }
    
    public TableRule(final String logicTable, final List<String> actualTables, final DataSourceRule dataSourceRule, final DatabaseShardingStrategy databaseShardingStrategy) {
        this(logicTable, actualTables, dataSourceRule, databaseShardingStrategy, null);
    }
    
    public TableRule(final String logicTable, final List<String> actualTables, final DataSourceRule dataSourceRule, final TableShardingStrategy tableShardingStrategy) {
        this(logicTable, actualTables, dataSourceRule, null, tableShardingStrategy);
    }
    
    private void generateDataNodes(final List<String> actualTables, final DataSourceRule dataSourceRule) {
        for (String actualTable : actualTables) {
            if (actualTable.contains(".")) {
                List<String> actualDatabaseTable = Splitter.on(".").splitToList(actualTable);
                this.actualTables.add(new DataNode(actualDatabaseTable.get(0), actualDatabaseTable.get(1)));
            } else {
                for (String dataSourceName : dataSourceRule.getDataSourceNames()) {
                    this.actualTables.add(new DataNode(dataSourceName, actualTable));
                }
            }
        }
    }
    
    /**
     * 根据数据源名称过滤获取真实数据单元.
     * 
     * @param targetDataSources 数据源名称集合
     * @param targetTables 真实表名称集合
     * @return 真实数据单元
     */
    public Collection<DataNode> getActualDataNodes(final Collection<String> targetDataSources, final Collection<String> targetTables) {
        Collection<DataNode> result = new LinkedHashSet<>(actualTables.size());
        for (DataNode each : actualTables) {
            if (targetDataSources.contains(each.getDataSourceName()) && targetTables.contains(each.getTableName())) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * 获取真实数据源.
     *
     * @return 真实表名称
     */
    public Collection<String> getActualDatasourceNames() {
        Collection<String> result = new LinkedHashSet<>(actualTables.size());
        for (DataNode each : actualTables) {
            result.add(each.getDataSourceName());
        }
        return result;
    }
    
    /**
     * 根据数据源名称过滤获取真实表名称.
     * 
     * @param targetDataSources 数据源名称
     * @return 真实表名称
     */
    public Collection<String> getActualTableNames(final Collection<String> targetDataSources) {
        Collection<String> result = new LinkedHashSet<>(actualTables.size());
        for (DataNode each : actualTables) {
            if (targetDataSources.contains(each.getDataSourceName())) {
                result.add(each.getTableName());
            }
        }
        return result;
    }
    
    /**
     * 根据数据源和真实表名称查找真实表顺序.
     * 
     * @param dataSourceName 数据源名称
     * @param actualTableName 真实表名称
     * @return 真实表顺序
     */
    public int findActualTableIndex(final String dataSourceName, final String actualTableName) {
        int result = 0;
        for (DataNode each : actualTables) {
            if (each.getDataSourceName().equals(dataSourceName) && each.getTableName().equals(actualTableName)) {
                return result;
            }
            result++;
        }
        return -1;
    }
}
