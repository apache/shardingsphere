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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class OpenGaussAdminExecutorCreatorTest {
    
    @Test
    void assertCreateExecutorForSelectDatabase() {
        initDialectDatabaseStatisticsCollector(true);
        SelectStatementContext selectStatementContext = mockSelectStatementContext("pg_database");
        Optional<DatabaseAdminExecutor> actual = new OpenGaussAdminExecutorCreator()
                .create(selectStatementContext, "select datname, datcompatibility from pg_database where datname = 'sharding_db'", "postgres", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSystemCatalogAdminQueryExecutor.class));
    }
    
    @Test
    void assertCreateExecutorForSelectTables() {
        initDialectDatabaseStatisticsCollector(true);
        SelectStatementContext selectStatementContext = mockSelectStatementContext("pg_tables");
        Optional<DatabaseAdminExecutor> actual = new OpenGaussAdminExecutorCreator()
                .create(selectStatementContext, "select schemaname, tablename from pg_tables where schemaname = 'sharding_db'", "postgres", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSystemCatalogAdminQueryExecutor.class));
    }
    
    @Test
    void assertCreateExecutorForSelectRoles() {
        initDialectDatabaseStatisticsCollector(true);
        SelectStatementContext selectStatementContext = mockSelectStatementContext("pg_roles");
        Optional<DatabaseAdminExecutor> actual = new OpenGaussAdminExecutorCreator()
                .create(selectStatementContext, "select rolname from pg_roles", "postgres", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSystemCatalogAdminQueryExecutor.class));
    }
    
    @Test
    void assertCreateExecutorForSelectVersion() {
        initDialectDatabaseStatisticsCollector(false);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getProjections().getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(-1, -1, "VERSION()")));
        Optional<DatabaseAdminExecutor> actual = new OpenGaussAdminExecutorCreator().create(selectStatementContext, "select VERSION()", "postgres", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSystemCatalogAdminQueryExecutor.class));
    }
    
    @Test
    void assertCreateOtherExecutor() {
        initDialectDatabaseStatisticsCollector(false);
        OpenGaussAdminExecutorCreator creator = new OpenGaussAdminExecutorCreator();
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.emptyList());
        assertThat(creator.create(sqlStatementContext), is(Optional.empty()));
        assertThat(creator.create(sqlStatementContext, "", "", Collections.emptyList()), is(Optional.empty()));
    }
    
    private void initDialectDatabaseStatisticsCollector(final boolean isStatisticsTables) {
        DialectDatabaseStatisticsCollector statisticsCollector = mock(DialectDatabaseStatisticsCollector.class);
        when(statisticsCollector.isStatisticsTables(anyMap())).thenReturn(isStatisticsTables);
        when(DatabaseTypedSPILoader.findService(DialectDatabaseStatisticsCollector.class, TypedSPILoader.getService(DatabaseType.class, "openGauss"))).thenReturn(Optional.of(statisticsCollector));
    }
    
    private SelectStatementContext mockSelectStatementContext(final String tableName) {
        SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class, RETURNS_DEEP_STUBS);
        when(simpleTableSegment.getTableName().getIdentifier().getValue()).thenReturn(tableName);
        TableSegmentBoundInfo tableSegmentBoundInfo = mock(TableSegmentBoundInfo.class, RETURNS_DEEP_STUBS);
        when(tableSegmentBoundInfo.getOriginalSchema().getValue()).thenReturn("pg_catalog");
        when(simpleTableSegment.getTableName().getTableBoundInfo()).thenReturn(Optional.of(tableSegmentBoundInfo));
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getSimpleTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(result.getTablesContext().getTableNames()).thenReturn(Collections.singletonList(tableName));
        return result;
    }
}
