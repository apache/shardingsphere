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

package io.shardingsphere.proxy.metadata;

import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.proxy.backend.jdbc.datasource.JDBCBackendDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Sharding table meta data for proxy.
 *
 * @author panjuan
 */
@Getter
public final class ProxyShardingTableMetaData extends ShardingTableMetaData {
    
    private static final String SHOW_TABLES = "SHOW TABLES";
    
    private static final String DESC = "DESC `%s`";
    
    private final JDBCBackendDataSource backendDataSource;
    
    public ProxyShardingTableMetaData(final ListeningExecutorService executorService, final JDBCBackendDataSource backendDataSource) {
        super(executorService);
        this.backendDataSource = backendDataSource;
    }
    
    @Override
    public Collection<String> getAllTableNames(final String dataSourceName) throws SQLException {
        try (Connection connection = backendDataSource.getDataSource(dataSourceName).getConnection();
             Statement statement = connection.createStatement()) {
            return getAllTableNames(statement);
        }
    }
    
    private Collection<String> getAllTableNames(final Statement statement) throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (ResultSet resultSet = statement.executeQuery(SHOW_TABLES)) {
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        }
        return result;
    }
    
    @Override
    public TableMetaData loadTableMetaData(final DataNode dataNode, final Map<String, Connection> connectionMap) throws SQLException {
        try (Connection connection = backendDataSource.getDataSource(dataNode.getDataSourceName()).getConnection();
             Statement statement = connection.createStatement()) {
            return isTableExist(connection, dataNode.getTableName()) ? new TableMetaData(getColumnMetaDataList(statement, dataNode.getTableName())) : new TableMetaData();
        }
    }
    
    private boolean isTableExist(final Connection connection, final String actualTableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, actualTableName, null)) {
            return resultSet.next();
        }
    }
    
    // TODO :panjuan use connection.getMetaData().getColumns
    private List<ColumnMetaData> getColumnMetaDataList(final Statement statement, final String actualTableName) throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        try (ResultSet resultSet = statement.executeQuery(String.format(DESC, actualTableName))) {
            while (resultSet.next()) {
                result.add(new ColumnMetaData(resultSet.getString("Field"), resultSet.getString("Type"), resultSet.getString("Key")));
            }
        }
        return result;
    }
}
