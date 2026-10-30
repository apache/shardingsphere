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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory;

import org.apache.shardingsphere.database.connector.core.resultset.ResultSetMapper;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class QueryResultDataRowLoaderTest {
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    private final ResultSet resultSet = mock(ResultSet.class);
    
    @Test
    void assertLoadEmptyResult() throws SQLException {
        try (MockedConstruction<ResultSetMapper> mocked = mockConstruction(ResultSetMapper.class)) {
            Collection<MemoryQueryResultDataRow> actual = new QueryResultDataRowLoader(databaseType).load(2, resultSet);
            assertTrue(actual.isEmpty());
            assertTrue(mocked.constructed().isEmpty());
        }
    }
    
    @Test
    void assertLoadRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.wasNull()).thenReturn(false, true, false, false);
        try (MockedConstruction<ResultSetMapper> mocked = mockConstruction(ResultSetMapper.class, (mock, context) -> {
            when(mock.load(resultSet, 1)).thenReturn("foo_first", "foo_second");
            when(mock.load(resultSet, 2)).thenReturn(0, "foo_value");
        })) {
            Collection<MemoryQueryResultDataRow> actual = new QueryResultDataRowLoader(databaseType).load(2, resultSet);
            assertThat(actual.size(), is(2));
            Iterator<MemoryQueryResultDataRow> actualRows = actual.iterator();
            assertThat(actualRows.next().getValue(), is(Arrays.asList("foo_first", null)));
            assertThat(actualRows.next().getValue(), is(Arrays.asList("foo_second", "foo_value")));
            assertThat(mocked.constructed().size(), is(1));
        }
    }
}
