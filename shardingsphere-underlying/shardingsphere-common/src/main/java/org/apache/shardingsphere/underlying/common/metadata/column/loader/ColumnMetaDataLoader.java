/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.underlying.common.metadata.column.loader;

import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Column meta data loader.
 *
 * @author zhangliang
 */
public final class ColumnMetaDataLoader {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String TYPE_NAME = "TYPE_NAME";
    
    /**
     * Load column meta data list.
     * 
     * @param connection connection
     * @param catalog catalog name
     * @param table table name
     * @return column meta data list
     * @throws SQLException SQL exception
     */
    public Collection<ColumnMetaData> load(final Connection connection, final String catalog, final String table) throws SQLException {
        Collection<ColumnMetaData> result = new LinkedList<>();
        Collection<String> primaryKeys = loadPrimaryKeys(connection, catalog, table);
        try (ResultSet resultSet = connection.getMetaData().getColumns(catalog, null, table, "%")) {
            while (resultSet.next()) {
                String columnName = resultSet.getString(COLUMN_NAME);
                String columnType = resultSet.getString(TYPE_NAME);
                boolean isPrimaryKey = primaryKeys.contains(columnName);
                result.add(new ColumnMetaData(columnName, columnType, isPrimaryKey));
            }
        }
        return result;
    }
    
    private Collection<String> loadPrimaryKeys(final Connection connection, final String catalog, final String table) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(catalog, null, table)) {
            while (resultSet.next()) {
                result.add(resultSet.getString(COLUMN_NAME));
            }
        }
        return result;
    }
}
