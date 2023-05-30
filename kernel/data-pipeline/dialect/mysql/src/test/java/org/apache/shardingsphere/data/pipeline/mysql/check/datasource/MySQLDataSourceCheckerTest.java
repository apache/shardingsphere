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

package org.apache.shardingsphere.data.pipeline.mysql.check.datasource;

import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithCheckPrivilegeFailedException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithInvalidSourceDataSourceException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithoutEnoughPrivilegeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLDataSourceCheckerTest {
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    private Collection<DataSource> dataSources;
    
    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection().prepareStatement(anyString())).thenReturn(preparedStatement);
        dataSources = Collections.singleton(dataSource);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }
    
    @Test
    void assertCheckPrivilegeWithParticularSuccess() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO '%'@'%'");
        new MySQLDataSourceChecker().checkPrivilege(dataSources);
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    void assertCheckPrivilegeWithAllSuccess() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("GRANT ALL PRIVILEGES CLIENT ON *.* TO '%'@'%'");
        new MySQLDataSourceChecker().checkPrivilege(dataSources);
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    void assertCheckPrivilegeLackPrivileges() {
        assertThrows(PrepareJobWithoutEnoughPrivilegeException.class, () -> new MySQLDataSourceChecker().checkPrivilege(dataSources));
    }
    
    @Test
    void assertCheckPrivilegeFailure() throws SQLException {
        when(resultSet.next()).thenThrow(new SQLException(""));
        assertThrows(PrepareJobWithCheckPrivilegeFailedException.class, () -> new MySQLDataSourceChecker().checkPrivilege(dataSources));
    }
    
    @Test
    void assertCheckVariableSuccess() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString(1)).thenReturn("LOG_BIN", "BINLOG_FORMAT", "BINLOG_ROW_IMAGE");
        when(resultSet.getString(2)).thenReturn("ON", "ROW", "FULL");
        assertDoesNotThrow(() -> new MySQLDataSourceChecker().checkVariable(dataSources));
        verify(preparedStatement, times(1)).executeQuery();
    }
    
    @Test
    void assertCheckVariableWithWrongVariable() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(1)).thenReturn("BINLOG_FORMAT", "LOG_BIN");
        when(resultSet.getString(2)).thenReturn("ROW", "OFF");
        assertThrows(PrepareJobWithInvalidSourceDataSourceException.class, () -> new MySQLDataSourceChecker().checkVariable(dataSources));
    }
    
    @Test
    void assertCheckVariableFailure() throws SQLException {
        when(resultSet.next()).thenThrow(new SQLException(""));
        assertThrows(PrepareJobWithCheckPrivilegeFailedException.class, () -> new MySQLDataSourceChecker().checkVariable(dataSources));
    }
}
