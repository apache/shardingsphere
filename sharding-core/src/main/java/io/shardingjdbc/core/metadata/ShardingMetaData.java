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

package io.shardingjdbc.core.metadata;

import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingDataSourceNames;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import lombok.Getter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract Sharding metadata.
 *
 * @author panjuan
 * @author zhaojun
 */
@Getter
public abstract class ShardingMetaData {
    
    private Map<String, TableMetaData> tableMetaDataMap;
    
    /**
     * Initialize sharding meta data.
     *
     * @param shardingRule sharding rule
     * @throws SQLException SQL exception
     */
    public void init(final ShardingRule shardingRule) throws SQLException {
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
     * @throws SQLException SQL Exception
     */
    public void refresh(final TableRule each, final ShardingRule shardingRule) throws SQLException {
        tableMetaDataMap.put(each.getLogicTable(), getTableMetaData(each.getLogicTable(), each.getActualDataNodes(), shardingRule.getShardingDataSourceNames()));
    }
    
    private TableMetaData getTableMetaData(final String logicTableName, final List<DataNode> actualDataNodes, final ShardingDataSourceNames shardingDataSourceNames) throws SQLException {
        Collection<ColumnMetaData> result = null;
        for (DataNode each : actualDataNodes) {
            Collection<ColumnMetaData> columnMetaDataList = getColumnMetaDataList(each, shardingDataSourceNames);
            if (null == result) {
                result = columnMetaDataList;
            }
            if (!result.equals(columnMetaDataList)) {
                throw new ShardingJdbcException("Cannot get uniformed table structure for '%s'.", logicTableName);
            }
        }
        return new TableMetaData(result);
    }
    
    protected abstract Collection<ColumnMetaData> getColumnMetaDataList(DataNode dataNode, ShardingDataSourceNames shardingDataSourceNames) throws SQLException;

}


