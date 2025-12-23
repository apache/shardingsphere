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

package org.apache.shardingsphere.proxy.backend.response.header.query;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.exception.kernel.syntax.ColumnIndexOutOfRangeException;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class QueryHeaderBuilderEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBuildWithoutProjections() throws SQLException {
        QueryResultMetaData queryResultMetaData = mock(QueryResultMetaData.class);
        when(queryResultMetaData.getColumnName(1)).thenReturn("col_name");
        when(queryResultMetaData.getColumnLabel(1)).thenReturn("col_label");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        QueryHeader expectedQueryHeader = mock(QueryHeader.class);
        try (MockedStatic<DatabaseTypedSPILoader> spiLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            QueryHeaderBuilder queryHeaderBuilder = mock(QueryHeaderBuilder.class);
            when(queryHeaderBuilder.build(queryResultMetaData, database, "col_name", "col_label", 1)).thenReturn(expectedQueryHeader);
            spiLoader.when(() -> DatabaseTypedSPILoader.getService(QueryHeaderBuilder.class, databaseType)).thenReturn(queryHeaderBuilder);
            QueryHeader actualQueryHeader = new QueryHeaderBuilderEngine(databaseType).build(queryResultMetaData, database, 1);
            assertThat(actualQueryHeader, is(expectedQueryHeader));
        }
    }
    
    @Test
    void assertBuildWithProjections() throws SQLException {
        Projection projection = mock(Projection.class);
        when(projection.getColumnName()).thenReturn("c1");
        when(projection.getColumnLabel()).thenReturn("l1");
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singleton(projection));
        QueryResultMetaData queryResultMetaData = mock(QueryResultMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        QueryHeader expectedQueryHeader = mock(QueryHeader.class);
        try (MockedStatic<DatabaseTypedSPILoader> spiLoader = mockStatic(DatabaseTypedSPILoader.class)) {
            QueryHeaderBuilder queryHeaderBuilder = mock(QueryHeaderBuilder.class);
            when(queryHeaderBuilder.build(queryResultMetaData, database, "c1", "l1", 1)).thenReturn(expectedQueryHeader);
            spiLoader.when(() -> DatabaseTypedSPILoader.getService(QueryHeaderBuilder.class, databaseType)).thenReturn(queryHeaderBuilder);
            QueryHeader actualQueryHeader = new QueryHeaderBuilderEngine(databaseType).build(projectionsContext, queryResultMetaData, database, 1);
            assertThat(actualQueryHeader, is(expectedQueryHeader));
        }
    }
    
    @Test
    void assertBuildWithProjectionsColumnIndexOutOfRange() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singleton(mock(Projection.class)));
        assertThrows(ColumnIndexOutOfRangeException.class, () -> new QueryHeaderBuilderEngine(databaseType).build(projectionsContext, mock(), mock(), 2));
    }
}
