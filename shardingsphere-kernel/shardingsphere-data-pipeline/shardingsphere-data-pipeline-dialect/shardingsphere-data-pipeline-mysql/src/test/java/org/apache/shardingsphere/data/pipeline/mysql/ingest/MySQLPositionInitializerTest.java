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

package org.apache.shardingsphere.data.pipeline.mysql.ingest;

import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogPosition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLPositionInitializerTest {
    
    private static final String LOG_FILE_NAME = "binlog-000001";
    
    private static final long LOG_POSITION = 4L;
    
    private static final long SERVER_ID = 555555;
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Before
    public void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        PreparedStatement positionStatement = mockPositionStatement();
        when(connection.prepareStatement("SHOW MASTER STATUS")).thenReturn(positionStatement);
        PreparedStatement serverIdStatement = mockServerIdStatement();
        when(connection.prepareStatement("SHOW VARIABLES LIKE 'server_id'")).thenReturn(serverIdStatement);
    }
    
    @Test
    public void assertGetCurrentPosition() throws SQLException {
        MySQLPositionInitializer mySQLPositionInitializer = new MySQLPositionInitializer();
        BinlogPosition actual = mySQLPositionInitializer.init(dataSource);
        assertThat(actual.getServerId(), is(SERVER_ID));
        assertThat(actual.getFilename(), is(LOG_FILE_NAME));
        assertThat(actual.getPosition(), is(LOG_POSITION));
    }
    
    private PreparedStatement mockPositionStatement() throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(LOG_FILE_NAME);
        when(resultSet.getLong(2)).thenReturn(LOG_POSITION);
        return result;
    }
    
    private PreparedStatement mockServerIdStatement() throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong(2)).thenReturn(SERVER_ID);
        return result;
    }
}

