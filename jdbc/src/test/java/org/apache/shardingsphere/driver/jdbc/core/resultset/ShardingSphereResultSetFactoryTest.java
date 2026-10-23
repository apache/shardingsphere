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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingSphereResultSetFactoryTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("columnMappingArguments")
    void assertNewInstance(final String name, final Map<String, Integer> columnLabelAndIndexMap, final Map<String, Integer> expected) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getJDBCResultSet()).thenReturn(Optional.of(resultSet));
        List<QueryResult> queryResults = Collections.singletonList(queryResult);
        QueryContext queryContext = mock(QueryContext.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        MergedResult mergedResult = mock(MergedResult.class);
        ShardingSphereResultSetFactory factory = new ShardingSphereResultSetFactory(mock(ConnectionContext.class), mock(ShardingSphereMetaData.class), mock(ConfigurationProperties.class),
                Collections.emptyList());
        try (
                MockedStatic<ShardingSphereResultSetUtils> mockedResultSetUtils = mockStatic(ShardingSphereResultSetUtils.class);
                MockedConstruction<MergeEngine> ignored = mockConstruction(MergeEngine.class, (mock, context) -> when(mock.merge(queryResults, queryContext)).thenReturn(mergedResult))) {
            if (null == columnLabelAndIndexMap) {
                mockedResultSetUtils.when(() -> ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(sqlStatementContext, resultSetMetaData)).thenReturn(expected);
            }
            ShardingSphereResultSet actual =
                    (ShardingSphereResultSet) factory.newInstance(mock(ShardingSphereDatabase.class), queryContext, queryResults, mock(Statement.class), columnLabelAndIndexMap);
            assertThat(actual.getColumnLabelAndIndexMap(), sameInstance(expected));
            verify(resultSet).getMetaData();
        }
    }
    
    private static Stream<Arguments> columnMappingArguments() {
        Map<String, Integer> supplied = Collections.singletonMap("foo_label", 2);
        Map<String, Integer> empty = Collections.emptyMap();
        return Stream.of(
                Arguments.of("absent mapping", null, Collections.singletonMap("foo_label", 1)),
                Arguments.of("supplied mapping", supplied, supplied),
                Arguments.of("empty mapping", empty, empty));
    }
}
