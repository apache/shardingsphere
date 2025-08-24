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
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectPasswordDeadlineExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectPasswordNotifyTimeExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectVersionExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenGaussSystemFunctionQueryExecutorCreatorTest {
    
    @Test
    void assertVersion() {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext("VERSION()");
        OpenGaussSystemFunctionQueryExecutorCreator creator = new OpenGaussSystemFunctionQueryExecutorCreator(sqlStatementContext);
        assertTrue(creator.accept());
        Optional<DatabaseAdminExecutor> actual = creator.create();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSelectVersionExecutor.class));
    }
    
    @Test
    void assertGsPasswordDeadline() {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext("gs_password_deadline()");
        OpenGaussSystemFunctionQueryExecutorCreator creator = new OpenGaussSystemFunctionQueryExecutorCreator(sqlStatementContext);
        assertTrue(creator.accept());
        Optional<DatabaseAdminExecutor> actual = creator.create();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSelectPasswordDeadlineExecutor.class));
    }
    
    @Test
    void assertGsPasswordNotifyTime() {
        SQLStatementContext sqlStatementContext = mockSQLStatementContext("gs_password_notifytime()");
        OpenGaussSystemFunctionQueryExecutorCreator creator = new OpenGaussSystemFunctionQueryExecutorCreator(sqlStatementContext);
        assertTrue(creator.accept());
        Optional<DatabaseAdminExecutor> actual = creator.create();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSelectPasswordNotifyTimeExecutor.class));
    }
    
    private SQLStatementContext mockSQLStatementContext(final String functionName) {
        ExpressionProjectionSegment expressionProjection = mock(ExpressionProjectionSegment.class, RETURNS_DEEP_STUBS);
        when(expressionProjection.getText()).thenReturn(functionName);
        ProjectionsSegment projections = mock(ProjectionsSegment.class, RETURNS_DEEP_STUBS);
        when(projections.getProjections().size()).thenReturn(1);
        when(projections.getProjections().iterator().next()).thenReturn(expressionProjection);
        SelectStatement selectStatement = mock(SelectStatement.class, RETURNS_DEEP_STUBS);
        when(selectStatement.getProjections()).thenReturn(projections);
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(selectStatement);
        return result;
    }
}
