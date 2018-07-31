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

package io.shardingsphere.core.jdbc.metadata.dialect;

import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.jdbc.metadata.JDBCShardingTableMetaData;
import io.shardingsphere.core.metadata.table.ColumnMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Sharding table meta data for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLShardingTableMetaData extends JDBCShardingTableMetaData {
    
    private static final String SHOW_TABLES = "SHOW TABLES";
    
    private static final String SHOW_TABLES_LIKE = "SHOW TABLES LIKE '%s'";
    
    private static final String DESC = "DESC `%s`";
    
    public MySQLShardingTableMetaData(final ListeningExecutorService executorService, final Map<String, DataSource> dataSourceMap) {
        super(executorService, dataSourceMap);
    }
    
    @Override
    protected String getAllTableNamesSQL() {
        return SHOW_TABLES;
    }
    
    @Override
    protected boolean isTableExist(final Connection connection, final String actualTableName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format(SHOW_TABLES_LIKE, actualTableName));
            try (ResultSet resultSet = statement.getResultSet()) {
                return resultSet.next();
            }
        }
    }
    
    @Override
    protected List<ColumnMetaData> getColumnMetaDataList(final Connection connection, final String actualTableName) throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format(DESC, actualTableName));
            try (ResultSet resultSet = statement.getResultSet()) {
                while (resultSet.next()) {
                    result.add(new ColumnMetaData(resultSet.getString("Field"), resultSet.getString("Type"), resultSet.getString("Key")));
                }
            }
            return result;
        }
    }
}
