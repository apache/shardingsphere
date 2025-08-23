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

package org.apache.shardingsphere.data.pipeline.core.checker;

import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithTargetTableNotEmptyException;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineDataSourceCheckEngineTest {
    
    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource dataSource;
    
    private PipelineDataSourceCheckEngine pipelineDataSourceCheckEngine;
    
    private Collection<DataSource> dataSources;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @BeforeEach
    void setUp() {
        pipelineDataSourceCheckEngine = new PipelineDataSourceCheckEngine(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        dataSources = new LinkedList<>();
        dataSources.add(dataSource);
    }
    
    @Test
    void assertCheckConnection() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        pipelineDataSourceCheckEngine.checkConnection(dataSources);
        verify(dataSource).getConnection();
    }
    
    @Test
    void assertCheckConnectionFailed() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("error"));
        assertThrows(SQLWrapperException.class, () -> pipelineDataSourceCheckEngine.checkConnection(dataSources));
    }
    
    @Test
    void assertCheckSourceDataSources() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        pipelineDataSourceCheckEngine.checkSourceDataSources(dataSources);
        verify(dataSource).getConnection();
    }
    
    @Test
    void assertCheckTargetDataSources() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM t_order LIMIT 1")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        ImporterConfiguration importerConfig = mock(ImporterConfiguration.class, RETURNS_DEEP_STUBS);
        when(importerConfig.getTableAndSchemaNameMapper().getQualifiedTables()).thenReturn(Collections.singleton(new QualifiedTable(null, "t_order")));
        assertDoesNotThrow(() -> pipelineDataSourceCheckEngine.checkTargetDataSources(dataSources, importerConfig));
    }
    
    @Test
    void assertCheckTargetDataSourcesFailed() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM t_order LIMIT 1")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        ImporterConfiguration importerConfig = mock(ImporterConfiguration.class, RETURNS_DEEP_STUBS);
        when(importerConfig.getTableAndSchemaNameMapper().getQualifiedTables()).thenReturn(Collections.singleton(new QualifiedTable(null, "t_order")));
        assertThrows(PrepareJobWithTargetTableNotEmptyException.class, () -> pipelineDataSourceCheckEngine.checkTargetDataSources(dataSources, importerConfig));
    }
    
    @Test
    void assertCheckTargetDataSourcesWhenSQLExceptionThrown() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException(""));
        assertThrows(SQLWrapperException.class, () -> pipelineDataSourceCheckEngine.checkTargetDataSources(dataSources, mock(ImporterConfiguration.class)));
    }
}
