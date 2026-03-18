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

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectDatCompatibilityExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectPasswordDeadlineExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectPasswordNotifyTimeExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectVersionExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class OpenGaussAdminExecutorCreatorTest {
    
    @Test
    void assertCreateExecutorForSelectDatCompatibilityFromPgDatabase() {
        String sql = "SELECT datcompatibility FROM pg_database WHERE datname='sharding_db'";
        String databaseName = "postgres";
        SelectStatementContext selectStatementContext = mockSelectStatementContext("pg_catalog", "pg_database", "datcompatibility");
        Optional<DatabaseAdminExecutor> actual = new OpenGaussAdminExecutorCreator().create(selectStatementContext, sql, databaseName, Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(OpenGaussSelectDatCompatibilityExecutor.class));
    }
    
    @Test
    void assertCreateExecutorForSelectFromCollectedTable() {
        initDialectDatabaseStatisticsCollector(true);
        String sql = "SELECT relname FROM pg_class";
        String databaseName = "postgres";
        SelectStatementContext selectStatementContext = mockSelectStatementContext("pg_catalog", "pg_class", null);
        Optional<DatabaseAdminExecutor> actual = new OpenGaussAdminExecutorCreator().create(selectStatementContext, sql, databaseName, Collections.emptyList());
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateExecutorForSelectFromNotCollectedTable() {
        initDialectDatabaseStatisticsCollector(false);
        String sql = "SELECT * FROM pg_type";
        SelectStatementContext selectStatementContext = mockSelectStatementContext("pg_catalog", "pg_type", null);
        Optional<DatabaseAdminExecutor> actual = new OpenGaussAdminExecutorCreator().create(selectStatementContext, sql, "postgres", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(DatabaseMetaDataExecutor.class));
    }
    
    @Test
    void assertCreateExecutorForSelectVersion() {
        initDialectDatabaseStatisticsCollector(false);
        String sql = "SELECT VERSION()";
        String expression = "VERSION()";
        assertCreateExecutorForFunction(sql, expression, OpenGaussSelectVersionExecutor.class);
    }
    
    @Test
    void assertCreateExecutorForSelectGsPasswordDeadline() {
        initDialectDatabaseStatisticsCollector(false);
        String sql = "SELECT pg_catalog.intervaltonum(pg_catalog.gs_password_deadline())";
        String expression = "pg_catalog.intervaltonum(pg_catalog.gs_password_deadline())";
        assertCreateExecutorForFunction(sql, expression, OpenGaussSelectPasswordDeadlineExecutor.class);
    }
    
    @Test
    void assertCreateExecutorForSelectGsPasswordNotifyTime() {
        initDialectDatabaseStatisticsCollector(false);
        String sql = "SELECT pg_catalog.gs_password_notifytime()";
        String expression = "pg_catalog.gs_password_notifytime()";
        assertCreateExecutorForFunction(sql, expression, OpenGaussSelectPasswordNotifyTimeExecutor.class);
    }
    
    private void initDialectDatabaseStatisticsCollector(final boolean isStatisticsTables) {
        DialectDatabaseStatisticsCollector statisticsCollector = mock(DialectDatabaseStatisticsCollector.class);
        when(statisticsCollector.isStatisticsTables(anyMap())).thenReturn(isStatisticsTables);
        when(DatabaseTypedSPILoader.findService(DialectDatabaseStatisticsCollector.class, TypedSPILoader.getService(DatabaseType.class, "openGauss"))).thenReturn(Optional.of(statisticsCollector));
        when(DatabaseTypedSPILoader.findService(DialectDatabaseStatisticsCollector.class, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))).thenReturn(Optional.of(statisticsCollector));
    }
    
    private void assertCreateExecutorForFunction(final String sql, final String expression, final Class<?> type) {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getProjections().getProjections())
                .thenReturn(Collections.singletonList(new ExpressionProjectionSegment(-1, -1, expression)));
        Optional<DatabaseAdminExecutor> actual = new OpenGaussAdminExecutorCreator().create(selectStatementContext, sql, "postgres", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(type));
    }
    
    private SelectStatementContext mockSelectStatementContext(final String schemaName, final String tableName, final String columnName) {
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
