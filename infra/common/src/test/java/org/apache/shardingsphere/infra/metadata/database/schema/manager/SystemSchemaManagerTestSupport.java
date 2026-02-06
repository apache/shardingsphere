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

package org.apache.shardingsphere.infra.metadata.database.schema.manager;

import org.apache.shardingsphere.database.connector.core.GlobalDataSourceRegistry;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SystemSchemaManagerTestSupport {
    
    private static final Map<String, Collection<String>> SCHEMA_TABLES = new LinkedHashMap<>();
    
    private static final AtomicReference<String> SCHEMA_NAME_REF = new AtomicReference<>();
    
    private static final AtomicReference<MockedDataSource> DATA_SOURCE_REF = new AtomicReference<>();
    
    private SystemSchemaManagerTestSupport() {
    }
    
    /**
     * Set up MySQL system schema data source.
     *
     * @param schemaName schema name
     * @param tableNames table names
     * @throws SQLException SQL exception
     */
    public static void setUpMySQLSystemSchemaDataSource(final String schemaName, final Collection<String> tableNames) throws SQLException {
        if (null == DATA_SOURCE_REF.get()) {
            initMySQLSystemSchemaDataSource();
        }
        SCHEMA_TABLES.put(schemaName, tableNames);
    }
    
    /**
     * Build table result set.
     *
     * @param tableNames table names
     * @return table result set
     * @throws SQLException SQL exception
     */
    public static ResultSet buildTableResultSet(final Collection<String> tableNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        List<String> names = new ArrayList<>(tableNames);
        AtomicReference<String> current = new AtomicReference<>();
        when(result.next()).thenAnswer(invocation -> {
            if (names.isEmpty()) {
                return false;
            }
            current.set(names.remove(0));
            return true;
        });
        when(result.getString("TABLE_NAME")).thenAnswer(invocation -> current.get());
        return result;
    }
    
    private static ResultSet buildTableTypeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_TYPE")).thenReturn("BASE TABLE");
        return result;
    }
    
    private static void initMySQLSystemSchemaDataSource() throws SQLException {
        GlobalDataSourceRegistry.getInstance().getCachedDataSources().clear();
        Connection connection = mock(Connection.class);
        MockedDataSource dataSource = new MockedDataSource(connection);
        PreparedStatement tableStatement = mock(PreparedStatement.class);
        PreparedStatement typeStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=?")).thenReturn(tableStatement);
        when(connection.prepareStatement("SELECT TABLE_TYPE FROM information_schema.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME=?")).thenReturn(typeStatement);
        doAnswer(invocation -> {
            SCHEMA_NAME_REF.set(invocation.getArgument(1, String.class));
            return null;
        }).when(tableStatement).setString(eq(1), anyString());
        doNothing().when(typeStatement).setString(eq(1), anyString());
        doNothing().when(typeStatement).setString(eq(2), anyString());
        when(tableStatement.executeQuery()).thenAnswer(invocation -> buildTableResultSet(SCHEMA_TABLES.getOrDefault(SCHEMA_NAME_REF.get(), Collections.emptyList())));
        when(typeStatement.executeQuery()).thenAnswer(invocation -> buildTableTypeResultSet());
        GlobalDataSourceRegistry.getInstance().getCachedDataSources().put("mysql", dataSource);
        DATA_SOURCE_REF.set(dataSource);
    }
}
