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

package org.apache.shardingsphere.database.connector.postgresql.metadata.database.option;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class PostgreSQLDataTypeOptionTest {
    
    @BeforeEach
    void setUp() {
        // Clear the static cache before each test to prevent test pollution
        PostgreSQLDataTypeOption.clearCache();
    }
    
    @Test
    void assertGetExtraDataTypes() {
        PostgreSQLDataTypeOption option = new PostgreSQLDataTypeOption();
        Map<String, Integer> extraDataTypes = option.getExtraDataTypes();
        
        assertThat(extraDataTypes.get("VARBIT"), is(java.sql.Types.OTHER));
        assertThat(extraDataTypes.get("BIT VARYING"), is(java.sql.Types.OTHER));
        assertThat(extraDataTypes.get("INTEGER"), is(java.sql.Types.INTEGER));
    }
    
    @Test
    void assertLoadUDTTypes() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        
        // Mock the connection methods needed for cache key generation
        when(connection.getCatalog()).thenReturn("test_catalog");
        when(connection.getSchema()).thenReturn("test_schema");
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Two rows then end
        when(resultSet.getString("udt_name")).thenReturn("custom_type1", "custom_type2");
        // Note: setString returns void, so we don't mock the return value
        doNothing().when(preparedStatement).setString(1, "public");
        
        PostgreSQLDataTypeOption option = new PostgreSQLDataTypeOption();
        Map<String, Integer> udtTypes = option.loadUDTTypes(connection);
        
        assertThat(udtTypes.size(), is(2));
        assertThat(udtTypes.get("custom_type1"), is(java.sql.Types.OTHER));
        assertThat(udtTypes.get("custom_type2"), is(java.sql.Types.OTHER));
    }
    
    @Test
    void assertLoadUDTTypesWithSchemaFilter() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        
        // Mock the connection methods needed for cache key generation
        when(connection.getCatalog()).thenReturn("test_catalog");
        when(connection.getSchema()).thenReturn("test_schema");
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Two rows then end
        when(resultSet.getString("udt_name")).thenReturn("custom_type1", "custom_type2");
        // Note: setString returns void, so we don't mock the return value
        doNothing().when(preparedStatement).setString(1, "my_schema");
        
        PostgreSQLDataTypeOption option = new PostgreSQLDataTypeOption();
        Map<String, Integer> udtTypes = option.loadUDTTypesWithSchema(connection, "my_schema");
        
        assertThat(udtTypes.size(), is(2));
        assertThat(udtTypes.get("custom_type1"), is(java.sql.Types.OTHER));
        assertThat(udtTypes.get("custom_type2"), is(java.sql.Types.OTHER));
    }
    
    @Test
    void assertLoadUDTTypesFromAllSchemas() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        
        // Mock the connection methods needed for cache key generation
        when(connection.getCatalog()).thenReturn("test_catalog");
        when(connection.getSchema()).thenReturn("test_schema");
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false); // Three rows then end
        when(resultSet.getString("udt_name")).thenReturn("type1", "type2", "type3");
        
        PostgreSQLDataTypeOption option = new PostgreSQLDataTypeOption();
        // Passing null should load from all schemas (no parameter setting happens in this case)
        Map<String, Integer> udtTypes = option.loadUDTTypesWithSchema(connection, null);
        
        assertThat(udtTypes.size(), is(3));
        assertThat(udtTypes.get("type1"), is(java.sql.Types.OTHER));
        assertThat(udtTypes.get("type2"), is(java.sql.Types.OTHER));
        assertThat(udtTypes.get("type3"), is(java.sql.Types.OTHER));
    }
    
    @Test
    void assertLoadUDTTypesEmptyResult() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        
        // Mock the connection methods needed for cache key generation
        when(connection.getCatalog()).thenReturn("test_catalog");
        when(connection.getSchema()).thenReturn("test_schema");
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No rows
        doNothing().when(preparedStatement).setString(1, "public");
        
        PostgreSQLDataTypeOption option = new PostgreSQLDataTypeOption();
        Map<String, Integer> udtTypes = option.loadUDTTypes(connection);
        
        assertThat(udtTypes.isEmpty(), is(true));
    }
}