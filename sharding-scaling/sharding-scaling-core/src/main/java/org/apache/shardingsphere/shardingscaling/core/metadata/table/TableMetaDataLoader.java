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

package org.apache.shardingsphere.shardingscaling.core.metadata.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import javax.sql.DataSource;

import org.apache.shardingsphere.shardingscaling.core.metadata.MetaDataLoader;
import org.apache.shardingsphere.shardingscaling.core.metadata.column.ColumnMetaDataLoader;

/**
 * Table meta data loader.
 */
public final class TableMetaDataLoader implements MetaDataLoader<TableMetaData> {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private final DataSource dataSource;
    
    private final ColumnMetaDataLoader columnMetaDataLoader;
    
    public TableMetaDataLoader(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.columnMetaDataLoader = new ColumnMetaDataLoader(dataSource);
    }
    
    @Override
    public TableMetaData load(final String tableName) {
        TableMetaData result = new TableMetaData();
        result.addAllColumnMetaData(columnMetaDataLoader.load(tableName));
        result.addAllPrimaryKey(getPrimaryKeys(tableName));
        return result;
    }
    
    private Collection<String> getPrimaryKeys(final String tableName) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                ResultSet rs = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), connection.getSchema(), tableName);
                Collection<String> primaryKeys = new LinkedList<>();
                while (rs.next()) {
                    primaryKeys.add(rs.getString(COLUMN_NAME));
                }
                return primaryKeys;
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("get primary key form table [%s] error", tableName), e);
        }
    }
}
