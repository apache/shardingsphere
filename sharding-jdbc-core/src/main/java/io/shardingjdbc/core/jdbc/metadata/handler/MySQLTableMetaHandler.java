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

package io.shardingjdbc.core.jdbc.metadata.handler;

import io.shardingjdbc.core.jdbc.metadata.entity.ColumnMeta;
import io.shardingjdbc.core.jdbc.metadata.entity.TableMeta;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * The MySQL table metadata handler.
 *
 * @author panjuan
 */
public final class MySQLTableMetaHandler extends AbstractTableMetaHandler {
    
    public MySQLTableMetaHandler(final DataSource dataSource, final String actualTableName) throws SQLException {
        super(dataSource, actualTableName);
    }
    
    /**
     * To get metadata of actual table of MySQL.
     *
     * @return table metadata
     * @throws SQLException SQL exception.
     */
    public TableMeta getActualTableMeta() throws SQLException {
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.executeQuery(String.format("desc %s;", actualTableName));
            ResultSet resultSet = statement.getResultSet();
            List<ColumnMeta> columnMetaList = new ArrayList<>();
            while (resultSet.next()) {
                String columnName = resultSet.getString("Field");
                String columnType = resultSet.getString("Type");
                String columnKey = resultSet.getString("Key");
                columnMetaList.add(new ColumnMeta(columnName, columnType, columnKey));
            }
            return new TableMeta(columnMetaList);
        }
    }
}
