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

package io.shardingjdbc.core.jdbc.metadata.dialect;

import io.shardingjdbc.core.metadata.ColumnMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL table metadata handler.
 *
 * @author panjuan
 * @author zhaojun
 */
public final class H2ShardingMetaDataHandler extends ShardingMetaDataHandler {
    
    public H2ShardingMetaDataHandler(final DataSource dataSource, final String actualTableName) {
        super(dataSource, actualTableName);
    }
    
    @Override
    public boolean isTableExist(final Connection connection) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, getActualTableName(), null)) {
            return resultSet.next();
        }
    }

    @Override
    public List<ColumnMetaData> getExistColumnMeta(final Connection connection) throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery(String.format("show columns from %s;", getActualTableName()));
            try (ResultSet resultSet = statement.getResultSet()) {
                while (resultSet.next()) {
                    result.add(new ColumnMetaData(resultSet.getString("FIELD"), resultSet.getString("TYPE"), resultSet.getString("KEY")));
                }
            }
            return result;
        }
    }
}
