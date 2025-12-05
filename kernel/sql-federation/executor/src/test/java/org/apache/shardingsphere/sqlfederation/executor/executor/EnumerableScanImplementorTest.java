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

package org.apache.shardingsphere.sqlfederation.executor.executor;

import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.ScanImplementorContext;
import org.apache.shardingsphere.sqlfederation.executor.context.ExecutorContext;
import org.apache.shardingsphere.sqlfederation.executor.enumerable.implementor.EnumerableScanImplementor;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnumerableScanImplementorTest {
    
    @Test
    void assertImplementWithStatistics() {
        CompilerContext compilerContext = mock(CompilerContext.class, RETURNS_DEEP_STUBS);
        ExecutorContext executorContext = mock(ExecutorContext.class);
        when(executorContext.getCurrentDatabaseName()).thenReturn("foo_db");
        when(executorContext.getCurrentSchemaName()).thenReturn("pg_catalog");
        ShardingSphereStatistics statistics = mockStatistics();
        when(executorContext.getStatistics()).thenReturn(statistics);
        ShardingSphereTable table = mock(ShardingSphereTable.class, RETURNS_DEEP_STUBS);
        when(table.getName()).thenReturn("test");
        when(table.getAllColumns()).thenReturn(Collections.singleton(new ShardingSphereColumn("id", Types.INTEGER, true, false, "int", false, false, true, false)));
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        SelectStatementContext selectStatementContext = mockSelectStatementContext();
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        Enumerable<Object> enumerable = new EnumerableScanImplementor(queryContext, compilerContext, executorContext).implement(table, mock(ScanImplementorContext.class));
        try (Enumerator<Object> actual = enumerable.enumerator()) {
            actual.moveNext();
            Object row = actual.current();
            assertThat(row, isA(Object[].class));
            assertThat(((Object[]) row)[0], is(1));
        }
    }
    
    private ShardingSphereStatistics mockStatistics() {
        ShardingSphereStatistics result = mock(ShardingSphereStatistics.class, RETURNS_DEEP_STUBS);
        DatabaseStatistics databaseStatistics = mock(DatabaseStatistics.class, RETURNS_DEEP_STUBS);
        when(result.getDatabaseStatistics("foo_db")).thenReturn(databaseStatistics);
        SchemaStatistics schemaStatistics = mock(SchemaStatistics.class, RETURNS_DEEP_STUBS);
        when(databaseStatistics.getSchemaStatistics("pg_catalog")).thenReturn(schemaStatistics);
        TableStatistics tableStatistics = mock(TableStatistics.class);
        when(tableStatistics.getRows()).thenReturn(Collections.singletonList(new RowStatistics(Collections.singletonList(1))));
        when(schemaStatistics.getTableStatistics("test")).thenReturn(tableStatistics);
        return result;
    }
    
    private SelectStatementContext mockSelectStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        when(result.getTablesContext().getSchemaNames()).thenReturn(Collections.singletonList("pg_catalog"));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.of("foo_db"));
        when(result.getTablesContext().getSchemaName()).thenReturn(Optional.of("pg_catalog"));
        return result;
    }
}
