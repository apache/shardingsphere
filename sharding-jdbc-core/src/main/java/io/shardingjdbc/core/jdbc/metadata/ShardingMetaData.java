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

import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.jdbc.metadata.dialect.TableMetaHandlerFactory;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sharding metadata.
 *
 * @author panjuan
 */
@Getter
public final class ShardingMetaData {
    
    private Map<String, TableMetaData> tableMetaDataMap;
    
    /**
     * Initialize sharding meta data.
     * 
     * @param dataSourceMap data source map
     * @param shardingRule sharding rule
     * @throws SQLException SQL exception
     */
    public void init(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        tableMetaDataMap = new HashMap<>(shardingRule.getTableRules().size(), 1);
        for (TableRule each : shardingRule.getTableRules()) {
            tableMetaDataMap.put(each.getLogicTable(), getTableMetaData(each.getLogicTable(), each.getActualDataNodes(), dataSourceMap));
        }
    }
    
    private TableMetaData getTableMetaData(final String logicTableName, final List<DataNode> actualDataNodes, final Map<String, DataSource> dataSourceMap) throws SQLException {
        Collection<ColumnMetaData> result = null;
        for (DataNode each : actualDataNodes) {
            Collection<ColumnMetaData> columnMetaDataList = TableMetaHandlerFactory.newInstance(dataSourceMap.get(each.getDataSourceName()), each.getTableName()).getColumnMetaDataList();
            if (null == result) {
                result = columnMetaDataList;
            }
            if (!result.equals(columnMetaDataList)) {
                throw new ShardingJdbcException("Cannot get uniformed table structure for '%s'.", logicTableName);
            }
        }
        return new TableMetaData(result);
    }
}
