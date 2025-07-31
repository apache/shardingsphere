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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin;

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetaDataExecutor.DefaultDatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectDatCompatibilityExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenGaussSystemTableQueryExecutorCreatorTest {
    
    @Test
    void assertSelectDatCompatibilityFromPgDatabase() {
        String sql = "select datcompatibility from pg_database where datname='sharding_db'";
        SQLStatementContext sqlStatementContext = mockSelectStatementContext("pg_catalog", "pg_database", "datcompatibility");
        OpenGaussSystemTableQueryExecutorCreator creator = new OpenGaussSystemTableQueryExecutorCreator(sqlStatementContext, sql, Collections.emptyList());
        assertTrue(creator.accept());
        Optional<DatabaseAdminExecutor> actual = creator.create();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSelectDatCompatibilityExecutor.class));
    }
    
    @Test
    void assertSelectFromNotCollectedTable() {
        String sql = "select name from pg_type'";
        SQLStatementContext sqlStatementContext = mockSelectStatementContext("pg_catalog", "pg_type", "name");
        OpenGaussSystemTableQueryExecutorCreator creator = new OpenGaussSystemTableQueryExecutorCreator(sqlStatementContext, sql, Collections.emptyList());
        assertTrue(creator.accept());
        Optional<DatabaseAdminExecutor> actual = creator.create();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(DefaultDatabaseMetaDataExecutor.class));
    }
    
    private SQLStatementContext mockSelectStatementContext(final String schemaName, final String tableName, final String columnName) {
        TableSegmentBoundInfo tableSegmentBoundInfo = mock(TableSegmentBoundInfo.class, RETURNS_DEEP_STUBS);
        when(tableSegmentBoundInfo.getOriginalSchema().getValue()).thenReturn(schemaName);
        TableNameSegment tableNameSegment = mock(TableNameSegment.class, RETURNS_DEEP_STUBS);
        when(tableNameSegment.getIdentifier().getValue()).thenReturn(tableName);
        when(tableNameSegment.getTableBoundInfo()).thenReturn(Optional.of(tableSegmentBoundInfo));
        SimpleTableSegment tableSegment = mock(SimpleTableSegment.class, RETURNS_DEEP_STUBS);
        when(tableSegment.getTableName()).thenReturn(tableNameSegment);
        TablesContext tablesContext = mock(TablesContext.class, RETURNS_DEEP_STUBS);
        when(tablesContext.getSimpleTables()).thenReturn(Collections.singletonList(tableSegment));
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext()).thenReturn(tablesContext);
        if (null != columnName) {
            ColumnSegmentBoundInfo columnSegmentBoundInfo = mock(ColumnSegmentBoundInfo.class, RETURNS_DEEP_STUBS);
            when(columnSegmentBoundInfo.getOriginalTable().getValue()).thenReturn(tableName);
            ColumnProjection columnProjection = mock(ColumnProjection.class, RETURNS_DEEP_STUBS);
            when(columnProjection.getName()).thenReturn(new IdentifierValue(columnName));
            when(columnProjection.getColumnBoundInfo()).thenReturn(columnSegmentBoundInfo);
            when(result.getProjectionsContext().getProjections()).thenReturn(Collections.singletonList(columnProjection));
        }
        return result;
    }
}
