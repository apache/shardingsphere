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

package org.apache.shardingsphere.test.it.rewriter.engine.mocker.type;

import lombok.SneakyThrows;
import org.apache.shardingsphere.test.it.rewriter.engine.mocker.DialectStorageUnitMetaDataMocker;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MySQL storage unit meta data mocker.
 */
public final class MySQLStorageUnitMetaDataMocker implements DialectStorageUnitMetaDataMocker {
    
    @Override
    @SneakyThrows(SQLException.class)
    public void mockStorageUnitMetaData(final Connection connection, final DatabaseMetaData databaseMetaData) {
        mockIdentifierCasePolicy(connection);
        mockDataTypeInfo(databaseMetaData);
    }
    
    private void mockIdentifierCasePolicy(final Connection connection) throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement("SELECT @@lower_case_table_names")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);
    }
    
    private void mockDataTypeInfo(final DatabaseMetaData databaseMetaData) throws SQLException {
        Map<String, Integer> dataTypes = new LinkedHashMap<>(30, 1F);
        dataTypes.put("TINYINT", Types.TINYINT);
        dataTypes.put("SMALLINT", Types.SMALLINT);
        dataTypes.put("MEDIUMINT", Types.INTEGER);
        dataTypes.put("INT", Types.INTEGER);
        dataTypes.put("INTEGER", Types.INTEGER);
        dataTypes.put("BIGINT", Types.BIGINT);
        dataTypes.put("DECIMAL", Types.DECIMAL);
        dataTypes.put("NUMERIC", Types.NUMERIC);
        dataTypes.put("FLOAT", Types.FLOAT);
        dataTypes.put("REAL", Types.REAL);
        dataTypes.put("DOUBLE", Types.DOUBLE);
        dataTypes.put("BIT", Types.BIT);
        dataTypes.put("DATE", Types.DATE);
        dataTypes.put("DATETIME", Types.TIMESTAMP);
        dataTypes.put("TIMESTAMP", Types.TIMESTAMP);
        dataTypes.put("TIME", Types.TIME);
        dataTypes.put("CHAR", Types.CHAR);
        dataTypes.put("VARCHAR", Types.VARCHAR);
        dataTypes.put("BINARY", Types.BINARY);
        dataTypes.put("VARBINARY", Types.VARBINARY);
        dataTypes.put("TINYBLOB", Types.BLOB);
        dataTypes.put("BLOB", Types.BLOB);
        dataTypes.put("MEDIUMBLOB", Types.BLOB);
        dataTypes.put("LONGBLOB", Types.BLOB);
        dataTypes.put("TINYTEXT", Types.LONGVARCHAR);
        dataTypes.put("TEXT", Types.LONGVARCHAR);
        dataTypes.put("MEDIUMTEXT", Types.LONGVARCHAR);
        dataTypes.put("LONGTEXT", Types.LONGVARCHAR);
        dataTypes.put("ENUM", Types.VARCHAR);
        dataTypes.put("SET", Types.VARCHAR);
        ResultSet resultSet = mock(ResultSet.class);
        AtomicReference<Entry<String, Integer>> current = new AtomicReference<>();
        when(resultSet.next()).thenAnswer(invocation -> {
            if (dataTypes.isEmpty()) {
                return false;
            }
            Entry<String, Integer> next = dataTypes.entrySet().iterator().next();
            dataTypes.remove(next.getKey());
            current.set(next);
            return true;
        });
        when(resultSet.getString("TYPE_NAME")).thenAnswer(invocation -> current.get().getKey());
        when(resultSet.getInt("DATA_TYPE")).thenAnswer(invocation -> current.get().getValue());
        when(databaseMetaData.getTypeInfo()).thenReturn(resultSet);
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
