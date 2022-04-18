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

package org.apache.shardingsphere.data.pipeline.core.spi.check.consistency;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineDataConsistencyCheckFailedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CRC32MatchDataConsistencyCalculateAlgorithmTest {
    
    @Mock
    private DataConsistencyCalculateParameter parameter;
    
    private PipelineDataSourceWrapper pipelineDataSource;
    
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @Before
    public void setUp() throws SQLException {
        pipelineDataSource = mock(PipelineDataSourceWrapper.class, RETURNS_DEEP_STUBS);
        connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        Collection<String> columnNames = Arrays.asList("fieldOne", "fieldTwo", "fieldThree");
        when(parameter.getLogicTableName()).thenReturn("tableName");
        when(parameter.getColumnNames()).thenReturn(columnNames);
        when(parameter.getDataSource()).thenReturn(pipelineDataSource);
        when(parameter.getDatabaseType()).thenReturn("FIXTURE");
    }
    
    @Test
    public void assertCalculateSuccess() {
        Iterable<Object> calculate = new CRC32MatchDataConsistencyCalculateAlgorithm().calculate(parameter);
        long actualDatabaseTypesSize = StreamSupport.stream(calculate.spliterator(), false).count();
        long expectedDatabaseTypesSize = parameter.getColumnNames().size();
        assertThat(actualDatabaseTypesSize, is(expectedDatabaseTypesSize));
    }
    
    @Test
    public void assertCalculateWithQuerySuccess() throws SQLException {
        String sqlCommandForFieldOne = "SELECT CRC32(fieldOne) FROM tableName";
        String sqlCommandForFieldTwo = "SELECT CRC32(fieldTwo) FROM tableName";
        String sqlCommandForFieldThree = "SELECT CRC32(fieldThree) FROM tableName";
        when(pipelineDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sqlCommandForFieldOne)).thenReturn(preparedStatement);
        when(connection.prepareStatement(sqlCommandForFieldTwo)).thenReturn(preparedStatement);
        when(connection.prepareStatement(sqlCommandForFieldThree)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Iterable<Object> calculate = new CRC32MatchDataConsistencyCalculateAlgorithm().calculate(parameter);
        long actualDatabaseTypesSize = StreamSupport.stream(calculate.spliterator(), false).count();
        long expectedDatabaseTypesSize = parameter.getColumnNames().size();
        assertThat(actualDatabaseTypesSize, is(expectedDatabaseTypesSize));
    }
    
    @Test(expected = PipelineDataConsistencyCheckFailedException.class)
    public void assertCalculateFailed() throws SQLException {
        when(pipelineDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException());
        new CRC32MatchDataConsistencyCalculateAlgorithm().calculate(parameter);
    }
}
