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

package org.apache.shardingsphere.data.pipeline.mysql.check.consistency;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataCalculateParameter;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@RunWith(MockitoJUnitRunner.class)
public final class CRC32MatchMySQLSingleTableDataCalculatorTest {

    @Mock
    private DataCalculateParameter dataCalculateParameter;

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
        when(dataCalculateParameter.getLogicTableName()).thenReturn("tableName");
        when(dataCalculateParameter.getColumnNames()).thenReturn(columnNames);
        when(dataCalculateParameter.getDataSource()).thenReturn(pipelineDataSource);
    }

    @Test
    public void assertCRC32MatchMySQLSingleTableDataCalculatorSuccess() {
        String actualAlgorithmType = new CRC32MatchMySQLSingleTableDataCalculator().getAlgorithmType();
        String expectedAlgorithmType = "CRC32_MATCH";
        assertThat(actualAlgorithmType, is(expectedAlgorithmType));
    }

    @Test
    public void assertGetDatabaseTypesSuccess() {
        Collection<String> actualDatabaseTypes = new CRC32MatchMySQLSingleTableDataCalculator().getDatabaseTypes();
        long actualDatabaseTypesSize = actualDatabaseTypes.size();
        long expectedDatabaseTypesSize = new Long(1);
        String actualDatabaseTypesFirstElement = actualDatabaseTypes.stream().findFirst().get();
        String expectedDatabaseTypesFirstElement = "MySQL";
        assertThat(actualDatabaseTypesSize, is(expectedDatabaseTypesSize));
        assertThat(actualDatabaseTypesFirstElement, is(expectedDatabaseTypesFirstElement));
    }

    @Test
    public void assertCalculateSuccess() {
        Iterable<Object> calculate = new CRC32MatchMySQLSingleTableDataCalculator().calculate(dataCalculateParameter);
        long actualDatabaseTypesSize = StreamSupport.stream(calculate.spliterator(), false).count();
        long expectedDatabaseTypesSize = dataCalculateParameter.getColumnNames().size();
        assertThat(actualDatabaseTypesSize, is(expectedDatabaseTypesSize));
    }

    @Test
    public void assertCalculateWithQuerySuccess() throws SQLException {
        String sqlCommandForFieldOne = "SELECT BIT_XOR(CAST(CRC32(`fieldOne`) AS UNSIGNED)) AS checksum FROM `tableName`";
        String sqlCommandForFieldTwo = "SELECT BIT_XOR(CAST(CRC32(`fieldTwo`) AS UNSIGNED)) AS checksum FROM `tableName`";
        String sqlCommandForFieldThree = "SELECT BIT_XOR(CAST(CRC32(`fieldThree`) AS UNSIGNED)) AS checksum FROM `tableName`";
        when(pipelineDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sqlCommandForFieldOne)).thenReturn(preparedStatement);
        when(connection.prepareStatement(sqlCommandForFieldTwo)).thenReturn(preparedStatement);
        when(connection.prepareStatement(sqlCommandForFieldThree)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        Iterable<Object> calculate = new CRC32MatchMySQLSingleTableDataCalculator().calculate(dataCalculateParameter);
        long actualDatabaseTypesSize = StreamSupport.stream(calculate.spliterator(), false).count();
        long expectedDatabaseTypesSize = dataCalculateParameter.getColumnNames().size();
        assertThat(actualDatabaseTypesSize, is(expectedDatabaseTypesSize));
    }

    @Test(expected = PipelineDataConsistencyCheckFailedException.class)
    public void assertCalculateFailed() throws SQLException {
        when(pipelineDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException());
        new CRC32MatchMySQLSingleTableDataCalculator().calculate(dataCalculateParameter);
    }
}
