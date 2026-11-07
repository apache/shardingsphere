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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.position.slot;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgreSQLSlotManagerTest {
    
    private static final String DECODE_PLUGIN = "test_decoding";
    
    private static final String LOAD_SQL = "SELECT slot_name, database FROM pg_replication_slots WHERE slot_name=? AND plugin=?";
    
    private static final String CREATE_SQL = "SELECT * FROM pg_create_logical_replication_slot(?, ?)";
    
    private static final String DROP_SQL = "SELECT pg_drop_replication_slot(?)";
    
    private final PostgreSQLSlotManager slotManager = new PostgreSQLSlotManager(DECODE_PLUGIN);
    
    @Test
    void assertCreateWhenSlotNotPresent() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getCatalog()).thenReturn("foo_catalog");
        PreparedStatement loadPreparedStatement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(connection.prepareStatement(LOAD_SQL)).thenReturn(loadPreparedStatement);
        PreparedStatement createPreparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(CREATE_SQL)).thenReturn(createPreparedStatement);
        slotManager.create(connection, "foo_slot");
        String slotName = PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, "foo_slot");
        verify(loadPreparedStatement).setString(1, slotName);
        verify(loadPreparedStatement).setString(2, DECODE_PLUGIN);
        verify(loadPreparedStatement).executeQuery();
        verify(createPreparedStatement).setString(1, slotName);
        verify(createPreparedStatement).setString(2, DECODE_PLUGIN);
        verify(createPreparedStatement).execute();
    }
    
    @Test
    void assertCreateWhenSlotDatabaseIsNull() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getCatalog()).thenReturn("bar_catalog");
        PreparedStatement loadPreparedStatement = mock(PreparedStatement.class);
        ResultSet loadResultSet = mock(ResultSet.class);
        when(connection.prepareStatement(LOAD_SQL)).thenReturn(loadPreparedStatement);
        when(loadPreparedStatement.executeQuery()).thenReturn(loadResultSet);
        when(loadResultSet.next()).thenReturn(true);
        String slotName = PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, "bar_slot");
        when(loadResultSet.getString(1)).thenReturn(slotName);
        when(loadResultSet.getString(2)).thenReturn(null);
        PreparedStatement dropPreparedStatement = mock(PreparedStatement.class);
        PreparedStatement createPreparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(DROP_SQL)).thenReturn(dropPreparedStatement);
        when(connection.prepareStatement(CREATE_SQL)).thenReturn(createPreparedStatement);
        slotManager.create(connection, "bar_slot");
        verify(dropPreparedStatement).setString(1, slotName);
        verify(dropPreparedStatement).execute();
        verify(createPreparedStatement).setString(1, slotName);
        verify(createPreparedStatement).setString(2, DECODE_PLUGIN);
        verify(createPreparedStatement).execute();
    }
    
    @Test
    void assertCreateWhenSlotDatabaseExistsDoNothing() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getCatalog()).thenReturn("baz_catalog");
        PreparedStatement loadPreparedStatement = mock(PreparedStatement.class);
        ResultSet loadResultSet = mock(ResultSet.class);
        when(connection.prepareStatement(LOAD_SQL)).thenReturn(loadPreparedStatement);
        when(loadPreparedStatement.executeQuery()).thenReturn(loadResultSet);
        when(loadResultSet.next()).thenReturn(true);
        String slotName = PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, "baz_slot");
        when(loadResultSet.getString(1)).thenReturn(slotName);
        when(loadResultSet.getString(2)).thenReturn("baz_db");
        slotManager.create(connection, "baz_slot");
        verify(connection, never()).prepareStatement(DROP_SQL);
        verify(connection, never()).prepareStatement(CREATE_SQL);
    }
    
    @Test
    void assertCreateHandleDuplicateAndOtherSQLState() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getCatalog()).thenReturn("dup_catalog");
        PreparedStatement loadPreparedStatement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(connection.prepareStatement(LOAD_SQL)).thenReturn(loadPreparedStatement);
        PreparedStatement createPreparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(CREATE_SQL)).thenReturn(createPreparedStatement);
        when(createPreparedStatement.execute()).thenThrow(new SQLException("duplicate", "42710")).thenThrow(new SQLException("error", "99999"));
        slotManager.create(connection, "dup_slot");
        assertThrows(SQLException.class, () -> slotManager.create(connection, "dup_slot"));
        String slotName = PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, "dup_slot");
        verify(createPreparedStatement, times(2)).setString(1, slotName);
        verify(createPreparedStatement, times(2)).setString(2, DECODE_PLUGIN);
    }
    
    @Test
    void assertDropIfExistedBranches() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getCatalog()).thenReturn("drop_catalog");
        PreparedStatement loadPreparedStatement = mock(PreparedStatement.class);
        ResultSet loadResultSet = mock(ResultSet.class);
        when(connection.prepareStatement(LOAD_SQL)).thenReturn(loadPreparedStatement);
        when(loadPreparedStatement.executeQuery()).thenReturn(loadResultSet);
        String slotName = PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, "drop_slot");
        when(loadResultSet.next()).thenReturn(false, true);
        when(loadResultSet.getString(1)).thenReturn(slotName);
        when(loadResultSet.getString(2)).thenReturn("drop_db");
        PreparedStatement dropPreparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(DROP_SQL)).thenReturn(dropPreparedStatement);
        slotManager.dropIfExisted(connection, "drop_slot");
        slotManager.dropIfExisted(connection, "drop_slot");
        verify(loadPreparedStatement, times(2)).setString(1, slotName);
        verify(loadPreparedStatement, times(2)).setString(2, DECODE_PLUGIN);
        verify(dropPreparedStatement).setString(1, slotName);
        verify(dropPreparedStatement).execute();
    }
}
