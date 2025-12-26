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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.withoutfrom;

import org.apache.shardingsphere.database.connector.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.MySQLSystemVariableQueryExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.NoResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowConnectionIdExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowCurrentDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowCurrentUserExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.ShowVersionExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.UnicastResourceShowExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLSelectWithoutFromAdminExecutorFactoryTest {
    
    @Test
    void assertCreateSystemVariableExecutor() {
        VariableSegment variableSegment = new VariableSegment(0, 0, "max_connections");
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(0, 0, "@@max_connections", variableSegment);
        SelectStatement selectStatement = createSelectStatement(Collections.singletonList(projection));
        Optional<DatabaseAdminExecutor> actual = MySQLSelectWithoutFromAdminExecutorFactory.newInstance(
                mockSelectStatementContext(selectStatement), "", null, mock(ShardingSphereMetaData.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLSystemVariableQueryExecutor.class));
    }
    
    @Test
    void assertCreateShowConnectionIdExecutor() {
        SelectStatement selectStatement = createSelectStatement(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "connection_id()")));
        Optional<DatabaseAdminExecutor> actual = MySQLSelectWithoutFromAdminExecutorFactory.newInstance(
                mockSelectStatementContext(selectStatement), "", null, mock(ShardingSphereMetaData.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowConnectionIdExecutor.class));
    }
    
    @Test
    void assertCreateShowVersionExecutor() {
        SelectStatement selectStatement = createSelectStatement(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "version()")));
        Optional<DatabaseAdminExecutor> actual = MySQLSelectWithoutFromAdminExecutorFactory.newInstance(
                mockSelectStatementContext(selectStatement), "", null, mock(ShardingSphereMetaData.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowVersionExecutor.class));
    }
    
    @Test
    void assertCreateShowCurrentUserExecutor() {
        SelectStatement selectStatement = createSelectStatement(Collections.singletonList(new ExpressionProjectionSegment(0, 0, ShowCurrentUserExecutor.FUNCTION_NAME_ALIAS)));
        Optional<DatabaseAdminExecutor> actual = MySQLSelectWithoutFromAdminExecutorFactory.newInstance(
                mockSelectStatementContext(selectStatement), "", null, mock(ShardingSphereMetaData.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowCurrentUserExecutor.class));
    }
    
    @Test
    void assertCreateShowCurrentDatabaseExecutor() {
        SelectStatement selectStatement = createSelectStatement(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "database()")));
        Optional<DatabaseAdminExecutor> actual = MySQLSelectWithoutFromAdminExecutorFactory.newInstance(
                mockSelectStatementContext(selectStatement), "", null, mock(ShardingSphereMetaData.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowCurrentDatabaseExecutor.class));
    }
    
    @Test
    void assertCreateNoResourceExecutorWhenEmptyResource() {
        SelectStatement selectStatement = createSelectStatement(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "other()")));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getAllDatabases()).thenReturn(Collections.emptyList());
        Optional<DatabaseAdminExecutor> actual = MySQLSelectWithoutFromAdminExecutorFactory.newInstance(
                mockSelectStatementContext(selectStatement), "", null, metaData);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(NoResourceShowExecutor.class));
    }
    
    @Test
    void assertReturnEmptyWhenMultipleProjectionsAndNoDatabase() {
        ExpressionProjectionSegment firstProjection = new ExpressionProjectionSegment(0, 0, "col1");
        ExpressionProjectionSegment secondProjection = new ExpressionProjectionSegment(0, 0, "col2");
        SelectStatement selectStatement = createSelectStatement(Arrays.asList(firstProjection, secondProjection));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsDataSource()).thenReturn(true);
        when(metaData.getAllDatabases()).thenReturn(Collections.singletonList(database));
        assertFalse(MySQLSelectWithoutFromAdminExecutorFactory.newInstance(mockSelectStatementContext(selectStatement), "", null, metaData).isPresent());
    }
    
    @Test
    void assertReturnEmptyWhenFirstProjectionIsNotExpression() {
        ProjectionSegment shorthandProjection = mock(ProjectionSegment.class);
        SelectStatement selectStatement = createSelectStatement(Collections.singletonList(shorthandProjection));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsDataSource()).thenReturn(true);
        when(metaData.getAllDatabases()).thenReturn(Collections.singletonList(database));
        Optional<DatabaseAdminExecutor> actual = MySQLSelectWithoutFromAdminExecutorFactory.newInstance(mockSelectStatementContext(selectStatement), "", null, metaData);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(UnicastResourceShowExecutor.class));
    }
    
    @Test
    void assertReturnEmptyWhenDatabaseSpecified() {
        SelectStatement selectStatement = createSelectStatement(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "col1")));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsDataSource()).thenReturn(true);
        when(metaData.getAllDatabases()).thenReturn(Collections.singletonList(database));
        assertFalse(MySQLSelectWithoutFromAdminExecutorFactory.newInstance(mockSelectStatementContext(selectStatement), "", "logic_db", metaData).isPresent());
    }
    
    @Test
    void assertCreateUnicastResourceShowExecutor() {
        SelectStatement selectStatement = createSelectStatement(Collections.singletonList(new ExpressionProjectionSegment(0, 0, "col1")));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsDataSource()).thenReturn(true);
        when(metaData.getAllDatabases()).thenReturn(Collections.singletonList(database));
        Optional<DatabaseAdminExecutor> actual = MySQLSelectWithoutFromAdminExecutorFactory.newInstance(
                mockSelectStatementContext(selectStatement), "", null, metaData);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(UnicastResourceShowExecutor.class));
    }
    
    @Test
    void assertCreateShowCurrentUserExecutorWithAliasName() {
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(0, 0, "current_user");
        SelectStatement selectStatement = createSelectStatement(Collections.singletonList(projection));
        Optional<DatabaseAdminExecutor> actual = MySQLSelectWithoutFromAdminExecutorFactory.newInstance(
                mockSelectStatementContext(selectStatement), "", null, mock(ShardingSphereMetaData.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowCurrentUserExecutor.class));
    }
    
    private SelectStatement createSelectStatement(final Iterable<ProjectionSegment> projections) {
        SelectStatement result = new SelectStatement(new MySQLDatabaseType());
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projections.forEach(projectionsSegment.getProjections()::add);
        result.setProjections(projectionsSegment);
        return result;
    }
    
    private SelectStatementContext mockSelectStatementContext(final SelectStatement selectStatement) {
        SelectStatementContext result = mock(SelectStatementContext.class);
        when(result.getSqlStatement()).thenReturn(selectStatement);
        return result;
    }
}
