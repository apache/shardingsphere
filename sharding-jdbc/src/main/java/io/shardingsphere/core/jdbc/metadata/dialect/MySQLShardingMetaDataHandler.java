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

import io.shardingsphere.core.metadata.ColumnMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL table metadata handler.
 *
 * @author panjuan
 * @author zhaojun
 */
public final class MySQLShardingMetaDataHandler extends ShardingMetaDataHandler {

    public MySQLShardingMetaDataHandler(final DataSource dataSource, final String actualTableName) {
        super(dataSource, actualTableName);
    }

    @Override
    public boolean isTableExist(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("show tables like '%s'", getActualTableName()));
            try (ResultSet resultSet = statement.getResultSet()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public List<ColumnMetaData> getExistColumnMeta(final Connection connection) throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("desc %s;", getActualTableName()));
            try (ResultSet resultSet = statement.getResultSet()) {
                while (resultSet.next()) {
                    result.add(new ColumnMetaData(resultSet.getString("Field"), resultSet.getString("Type"), resultSet.getString("Key")));
                }
            }
            return result;
        }
    }
    
    @Override
    public Collection<String> getTableNamesFromDefaultDataSource() throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (Connection connection = getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("show tables;");
            try (ResultSet resultSet = statement.getResultSet()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString(1));
                }
            }
            return result;
        }
    }
}
