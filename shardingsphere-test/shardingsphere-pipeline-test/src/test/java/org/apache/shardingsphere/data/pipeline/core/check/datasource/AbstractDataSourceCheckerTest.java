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

package org.apache.shardingsphere.data.pipeline.core.check.datasource;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithInvalidConnectionException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithTargetTableNotEmptyException;
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AbstractDataSourceCheckerTest {
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource dataSource;
    
    private AbstractDataSourceChecker dataSourceChecker;
    
    private Collection<DataSource> dataSources;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Before
    public void setUp() {
        dataSourceChecker = new AbstractDataSourceChecker() {
            
            @Override
            protected String getDatabaseType() {
                return "H2";
            }
            
            @Override
            public void checkPrivilege(final Collection<? extends DataSource> dataSources) {
            }
            
            @Override
            public void checkVariable(final Collection<? extends DataSource> dataSources) {
            }
        };
        dataSources = new LinkedList<>();
        dataSources.add(dataSource);
    }
    
    @SneakyThrows
    @Test
    public void assertCheckConnection() {
        when(dataSource.getConnection()).thenReturn(connection);
        dataSourceChecker.checkConnection(dataSources);
        verify(dataSource).getConnection();
    }
    
    @Test(expected = PrepareJobWithInvalidConnectionException.class)
    public void assertCheckConnectionFailed() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("error"));
        dataSourceChecker.checkConnection(dataSources);
    }
    
    @Test
    public void assertCheckTargetTable() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM t_order LIMIT 1")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        dataSourceChecker.checkTargetTable(dataSources, new TableNameSchemaNameMapping(Collections.emptyMap()), Collections.singletonList("t_order"));
    }
    
    @Test(expected = PrepareJobWithTargetTableNotEmptyException.class)
    public void assertCheckTargetTableFailed() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM t_order LIMIT 1")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        dataSourceChecker.checkTargetTable(dataSources, new TableNameSchemaNameMapping(Collections.emptyMap()), Collections.singletonList("t_order"));
    }
}
