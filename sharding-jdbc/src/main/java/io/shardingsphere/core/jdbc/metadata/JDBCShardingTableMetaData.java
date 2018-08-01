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

package io.shardingsphere.core.jdbc.metadata;

import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.rule.DataNode;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Sharding table meta data for JDBC.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class JDBCShardingTableMetaData extends ShardingTableMetaData {
    
    private final Map<String, DataSource> dataSourceMap;
    
    public JDBCShardingTableMetaData(final ListeningExecutorService executorService, final Map<String, DataSource> dataSourceMap) {
        super(executorService);
        this.dataSourceMap = dataSourceMap;
    }
    
    @Override
    protected Connection getConnection(final String dataSourceName) throws SQLException {
        return dataSourceMap.get(dataSourceName).getConnection();
    }
    
    @Override
    public TableMetaData loadTableMetaData(final DataNode dataNode, final Map<String, Connection> connectionMap) throws SQLException {
        if (connectionMap.containsKey(dataNode.getDataSourceName())) {
            return getTableMetaData(connectionMap.get(dataNode.getDataSourceName()), dataNode.getTableName());
        }
        return getTableMetaData(dataSourceMap.get(dataNode.getDataSourceName()), dataNode.getTableName());
    }
    
    private TableMetaData getTableMetaData(final Connection connection, final String actualTableName) throws SQLException {
        return new TableMetaData(isTableExist(connection, actualTableName) ? getColumnMetaDataList(connection, actualTableName) : Collections.<ColumnMetaData>emptyList());
    }
    
    private TableMetaData getTableMetaData(final DataSource dataSource, final String actualTableName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return getTableMetaData(connection, actualTableName);
        }
    }
}
