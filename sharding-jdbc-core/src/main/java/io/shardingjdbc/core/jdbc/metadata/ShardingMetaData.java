/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.jdbc.metadata;

import io.shardingjdbc.core.jdbc.metadata.entity.ActualTableInformation;
import io.shardingjdbc.core.jdbc.metadata.entity.TableMeta;
import io.shardingjdbc.core.jdbc.metadata.handler.TableMetaHandlerFactory;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;


/**
 * The metadata of sharding tables.
 *
 * @author panjuan
 */
@NoArgsConstructor
@Getter
public final class ShardingMetaData {
    
    private Map<String, TableMeta> logicTableStructureMap;
    
    private Map<String, List<ActualTableInformation>> logicTableActualTablesMap;
    
    private void initialize(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        Collection<TableRule> tableRules = shardingRule.getTableRules();
        calculateLogicTableActualTablesMap(dataSourceMap, tableRules);
        calculateLogicTableStructureMap();
    }
    
    private void calculateLogicTableActualTablesMap(final Map<String, DataSource> dataSourceMap, final Collection<TableRule> tableRules)
        throws SQLException {
        logicTableActualTablesMap = new HashMap<>(tableRules.size(), 1);
        for (TableRule each : tableRules) {
            String logicTable = each.getLogicTable();
            List<DataNode> actualDataNodes = each.getActualDataNodes();
            List<ActualTableInformation> actualTableInformationList = calActualTableInformationList(actualDataNodes, dataSourceMap);
            logicTableActualTablesMap.put(logicTable, actualTableInformationList);
        }
    }
    
    private void calculateLogicTableStructureMap() {
        logicTableStructureMap = new HashMap<>(logicTableStructureMap.size(), 1);
        for (Entry<String, List<ActualTableInformation>> entry : logicTableActualTablesMap.entrySet()) {
            if (isAllTableMetaSame(entry.getValue())) {
                logicTableStructureMap.put(entry.getKey(), entry.getValue().get(0).getTableMeta());
            } else {
                throw new ShardingJdbcException("Cannot get uniformed table structure for %s.", entry.getKey());
            }
        }
    }
    
    private List<ActualTableInformation> calActualTableInformationList(final List<DataNode> actualDataNodes, final Map<String, DataSource> dataSourceMap)
        throws SQLException {
        List<ActualTableInformation> actualTableInformationList = new ArrayList<>();
        for (DataNode dataNode : actualDataNodes) {
            TableMeta tableMeta = TableMetaHandlerFactory.newInstance(dataSourceMap.get(dataNode.getDataSourceName()), dataNode.getTableName()).getActualTableMeta();
            actualTableInformationList.add(new ActualTableInformation(dataNode, tableMeta));
        }
        return actualTableInformationList;
    }
    
    private boolean isAllTableMetaSame(final List<ActualTableInformation> actualTableInformationList) {
        List<TableMeta> tableMetaList = new ArrayList<>();
        for (ActualTableInformation each : actualTableInformationList) {
            tableMetaList.add(each.getTableMeta());
        }
        final Set<TableMeta> tableMetaSet = new HashSet<>(tableMetaList);
        return 1 == tableMetaSet.size();
    }
    
    /**
     * To get logic table metadata by logic table name.
     *
     * @param logicTable logic table name.
     * @return table metadata.
     */
    public TableMeta getLogicTableMeta(final String logicTable) {
        return logicTableStructureMap.get(logicTable);
    }
    
    /**
     * To get actual table information list by logic table name.
     *
     * @param logicTable logic table name.
     * @return actual table information list.
     */
    public List<ActualTableInformation> getActualTableInformationList(final String logicTable) {
        return logicTableActualTablesMap.get(logicTable);
    }
}
