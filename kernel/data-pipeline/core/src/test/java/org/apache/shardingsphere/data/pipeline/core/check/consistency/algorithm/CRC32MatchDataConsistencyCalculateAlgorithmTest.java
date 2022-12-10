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

package org.apache.shardingsphere.data.pipeline.core.check.consistency.algorithm;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculateParameter;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCalculatedResult;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineTableDataConsistencyCheckLoadingFailedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CRC32MatchDataConsistencyCalculateAlgorithmTest {
    
    private DataConsistencyCalculateParameter parameter;
    
    @Mock
    private PipelineDataSourceWrapper pipelineDataSource;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Connection connection;
    
    @Before
    public void setUp() throws SQLException {
        PipelineColumnMetaData uniqueKey = new PipelineColumnMetaData(1, "id", Types.INTEGER, "integer", false, true, true);
        parameter = new DataConsistencyCalculateParameter(pipelineDataSource, null,
                "foo_tbl", Arrays.asList("foo_col", "bar_col"), "FIXTURE", "FIXTURE", uniqueKey, Collections.emptyMap());
        when(pipelineDataSource.getConnection()).thenReturn(connection);
    }
    
    @Test
    public void assertCalculateSuccess() throws SQLException {
        PreparedStatement preparedStatement0 = mockPreparedStatement(123L, 10);
        when(connection.prepareStatement("SELECT CRC32(foo_col) FROM foo_tbl")).thenReturn(preparedStatement0);
        PreparedStatement preparedStatement1 = mockPreparedStatement(456L, 10);
        when(connection.prepareStatement("SELECT CRC32(bar_col) FROM foo_tbl")).thenReturn(preparedStatement1);
        Iterator<DataConsistencyCalculatedResult> actual = new CRC32MatchDataConsistencyCalculateAlgorithm().calculate(parameter).iterator();
        assertThat(actual.next().getRecordsCount(), is(10));
        assertFalse(actual.hasNext());
    }
    
    private PreparedStatement mockPreparedStatement(final long expectedCRC32Result, final int expectedRecordsCount) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        PreparedStatement result = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(result.executeQuery()).thenReturn(resultSet);
        when(resultSet.getLong(1)).thenReturn(expectedCRC32Result);
        when(resultSet.getInt(2)).thenReturn(expectedRecordsCount);
        return result;
    }
    
    @Test(expected = PipelineTableDataConsistencyCheckLoadingFailedException.class)
    public void assertCalculateFailed() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException());
        new CRC32MatchDataConsistencyCalculateAlgorithm().calculate(parameter);
    }
}
