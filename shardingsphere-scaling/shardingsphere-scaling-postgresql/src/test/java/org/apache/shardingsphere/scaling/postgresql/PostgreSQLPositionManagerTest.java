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

package org.apache.shardingsphere.scaling.postgresql;

import org.apache.shardingsphere.scaling.postgresql.wal.WalPosition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.replication.LogSequenceNumber;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLPositionManagerTest {
    
    private static final String POSTGRESQL_96_LSN = "0/14EFDB8";
    
    private static final String POSTGRESQL_10_LSN = "0/1634520";
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Before
    public void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        PreparedStatement postgreSQL96LsnPs = mockPostgreSQL96Lsn();
        when(connection.prepareStatement("SELECT * FROM pg_create_logical_replication_slot('sharding_scaling', 'test_decoding')"))
                .thenReturn(mock(PreparedStatement.class));
        when(connection.prepareStatement("SELECT PG_CURRENT_XLOG_LOCATION()")).thenReturn(postgreSQL96LsnPs);
        PreparedStatement postgreSQL10LsnPs = mockPostgreSQL10Lsn();
        when(connection.prepareStatement("SELECT PG_CURRENT_WAL_LSN()")).thenReturn(postgreSQL10LsnPs);
    }
    
    @Test
    public void assertGetCurrentPositionOnPostgreSQL96() throws SQLException {
        PostgreSQLPositionManager postgreSQLPositionManager = new PostgreSQLPositionManager(dataSource);
        when(databaseMetaData.getDatabaseMajorVersion()).thenReturn(9);
        when(databaseMetaData.getDatabaseMinorVersion()).thenReturn(6);
        WalPosition actual = postgreSQLPositionManager.getPosition();
        assertThat(actual.getLogSequenceNumber(), is(LogSequenceNumber.valueOf(POSTGRESQL_96_LSN)));
    }
    
    @Test
    public void assertGetCurrentPositionOnPostgreSQL10() throws SQLException {
        PostgreSQLPositionManager postgreSQLPositionManager = new PostgreSQLPositionManager(dataSource);
        when(databaseMetaData.getDatabaseMajorVersion()).thenReturn(10);
        WalPosition actual = postgreSQLPositionManager.getPosition();
        assertThat(actual.getLogSequenceNumber(), is(LogSequenceNumber.valueOf(POSTGRESQL_10_LSN)));
    }
    
    @Test(expected = RuntimeException.class)
    public void assertGetCurrentPositionThrowException() throws SQLException {
        PostgreSQLPositionManager postgreSQLPositionManager = new PostgreSQLPositionManager(dataSource);
        when(databaseMetaData.getDatabaseMajorVersion()).thenReturn(9);
        when(databaseMetaData.getDatabaseMinorVersion()).thenReturn(4);
        postgreSQLPositionManager.getPosition();
    }
    
    @Test
    public void assertUpdateCurrentPosition() {
        PostgreSQLPositionManager postgreSQLPositionManager = new PostgreSQLPositionManager(dataSource);
        WalPosition expected = new WalPosition(LogSequenceNumber.valueOf(POSTGRESQL_96_LSN));
        postgreSQLPositionManager.setPosition(expected);
        assertThat(postgreSQLPositionManager.getPosition(), is(expected));
    }
    
    private PreparedStatement mockPostgreSQL96Lsn() throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(POSTGRESQL_96_LSN);
        return result;
    }
    
    private PreparedStatement mockPostgreSQL10Lsn() throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(POSTGRESQL_10_LSN);
        return result;
    }
}
