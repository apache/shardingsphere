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

package org.apache.shardingsphere.shardingscaling.core.metadata.column;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingscaling.core.metadata.MetaDataLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of column meta data loader by JDBC.
 */
@RequiredArgsConstructor
public final class ColumnMetaDataLoader implements MetaDataLoader<List<ColumnMetaData>> {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String TYPE_NAME = "TYPE_NAME";
    
    private static final String DATA_TYPE = "DATA_TYPE";
    
    private final DataSource dataSource;
    
    @Override
    public List<ColumnMetaData> load(final String tableName) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                List<ColumnMetaData> result = new ArrayList<>();
                ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableName, "%");
                while (resultSet.next()) {
                    ColumnMetaData columnMetaData = new ColumnMetaData();
                    columnMetaData.setColumnName(resultSet.getString(COLUMN_NAME));
                    columnMetaData.setColumnType(resultSet.getInt(DATA_TYPE));
                    columnMetaData.setColumnTypeName(resultSet.getString(TYPE_NAME));
                    result.add(columnMetaData);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("get column meta data from table [%s] error", tableName), e);
        }
    }
}
