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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
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
     * Get column meta data list.
     *
     * @return column meta data list
     * @throws SQLException SQL exception
     */
    public Collection<ColumnMetaData> getColumnMetaDataList() throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection()) {
            if (isTableExist(connection)) {
                result = getExistColumnMeta(connection);
            }
        }
        return result;
    }

    /**
     * Get column metadata by Sharding Connection.
     *
     * @param connection connection
     * @return column metadata List
     * @throws SQLException SQL exception
     */
    public Collection<ColumnMetaData> getColumnMetaDataList(final Connection connection) throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        if (isTableExist(connection)) {
            result = getExistColumnMeta(connection);
        }
        return result;
    }

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
