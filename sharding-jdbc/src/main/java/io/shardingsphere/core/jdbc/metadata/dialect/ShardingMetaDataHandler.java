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
import io.shardingsphere.core.metadata.TableMetaData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Abstract table meta handler.
 *
 * @author panjuan
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class ShardingMetaDataHandler {

    private final DataSource dataSource;

    private final String actualTableName;

    /**
     * Get table meta data.
     *
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public TableMetaData getTableMetaData() throws SQLException {
        TableMetaData result = new TableMetaData();
        try (Connection connection = dataSource.getConnection()) {
            if (isTableExist(connection)) {
                result.getColumnMetaData().addAll(getExistColumnMeta(connection));
            }
        }
        return result;
    }

    /**
     * Get table metadata by Sharding Connection.
     *
     * @param connection connection
     * @return table metadata
     * @throws SQLException SQL exception
     */
    public TableMetaData getTableMetaData(final Connection connection) throws SQLException {
        TableMetaData result = new TableMetaData();
        if (isTableExist(connection)) {
            result.getColumnMetaData().addAll(getExistColumnMeta(connection));
        }
        return result;
    }
    
    /**
     * Get table names from default data source.
     *
     * @return Table names from default data source
     * @throws SQLException SQL exception.
     */
    public abstract Collection<String> getTableNamesFromDefaultDataSource() throws SQLException;

    /**
     * Judge whether table exist or not.
     *
     * @param connection jdbc connection
     * @return true or false
     * @throws SQLException SQL exception
     */
    public abstract boolean isTableExist(Connection connection) throws SQLException;

    /**
     * Get exit table's column metadata list.
     *
     * @param connection jdbc connection
     * @return column metadata list
     * @throws SQLException SQL exception
     */
    public abstract List<ColumnMetaData> getExistColumnMeta(Connection connection) throws SQLException;
}
