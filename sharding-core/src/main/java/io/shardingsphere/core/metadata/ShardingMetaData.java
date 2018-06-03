/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.metadata;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingDataSourceNames;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Abstract Sharding metadata.
 *
 * @author panjuan
 * @author zhaojun
 */
@Getter
@Setter
@Slf4j
public abstract class ShardingMetaData {
    
    private final ListeningExecutorService executorService;
    
    private Map<String, TableMetaData> tableMetaDataMap;
    
    public ShardingMetaData(final ListeningExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Initialize sharding metadata.
     *
     * @param shardingRule sharding rule
     */
    public void init(final ShardingRule shardingRule) {
        tableMetaDataMap = new HashMap<>(shardingRule.getTableRules().size(), 1);
        for (TableRule each : shardingRule.getTableRules()) {
            refresh(each, shardingRule);
        }
    }

    /**
     * refresh each tableMetaData by TableRule.
     *
     * @param each table rule
     * @param shardingRule sharding rule
     */
    public void refresh(final TableRule each, final ShardingRule shardingRule) {
        refresh(each, shardingRule, Collections.<String, Connection>emptyMap());
    }

    /**
     * refresh each tableMetaData by TableRule.
     *
     * @param each table rule
     * @param shardingRule sharding rule
     * @param connectionMap connection map passing from sharding connection
     */
    public void refresh(final TableRule each, final ShardingRule shardingRule, final Map<String, Connection> connectionMap) {
        tableMetaDataMap.put(each.getLogicTable(), getFinalTableMetaData(each.getLogicTable(), each.getActualDataNodes(), shardingRule.getShardingDataSourceNames(), connectionMap));
    }

    private TableMetaData getFinalTableMetaData(final String logicTableName, final List<DataNode> actualDataNodes,
                                                final ShardingDataSourceNames shardingDataSourceNames, final Map<String, Connection> connectionMap) {
        List<TableMetaData> actualTableMetaDataList = getAllActualTableMetaData(actualDataNodes, shardingDataSourceNames, connectionMap);
        for (int i = 0; i < actualTableMetaDataList.size(); i++) {
            if (actualTableMetaDataList.size() - 1 == i) {
                return actualTableMetaDataList.get(i);
            }
            if (!actualTableMetaDataList.get(i).equals(actualTableMetaDataList.get(i + 1))) {
                throw new ShardingException(getErrorMsgOfTableMetaData(logicTableName, actualTableMetaDataList.get(i), actualTableMetaDataList.get(i + 1)));
            }
        }
        return new TableMetaData();
    }
    
    private List<TableMetaData> getAllActualTableMetaData(final List<DataNode> actualDataNodes, final ShardingDataSourceNames shardingDataSourceNames,
                                                          final Map<String, Connection> connectionMap) {
        List<ListenableFuture<TableMetaData>> result = new ArrayList<>();
        for (final DataNode each : actualDataNodes) {
            result.add(executorService.submit(new Callable<TableMetaData>() {
                public TableMetaData call() throws Exception {
                    return getTableMetaData(each, shardingDataSourceNames, connectionMap);
                }
            }));
        }
        try {
            return Futures.allAsList(result).get();
        } catch (final InterruptedException | ExecutionException ex) {
            throw new ShardingException(ex);
        }
    }
    
    /**d
     * Get column metadata implementing by concrete handler.
     *
     * @param dataNode DataNode
     * @param shardingDataSourceNames ShardingDataSourceNames
     * @param connectionMap connection map from sharding connection
     * @return ColumnMetaData
     * @throws SQLException SQL exception
     */
    public abstract TableMetaData getTableMetaData(DataNode dataNode, ShardingDataSourceNames shardingDataSourceNames, Map<String, Connection> connectionMap) throws SQLException;

    private String getErrorMsgOfTableMetaData(final String logicTableName, final TableMetaData oldTableMetaData, final TableMetaData newTableMetaData) {
        return String.format("Cannot get uniformed table structure for %s. The different metadata of actual tables is as follows:\n%s\n%s.",
                logicTableName, oldTableMetaData.toString(), newTableMetaData.toString());
    }
}
