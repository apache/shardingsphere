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

package org.apache.shardingsphere.proxy.backend.handler.admin.opengauss;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OpenGaussAdminExecutorCreatorTest {
    
    @Test
    public void assertCreateExecutorForSelectDatabase() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getTableNames().contains("pg_database")).thenReturn(true);
        Optional<DatabaseAdminExecutor> actual = new OpenGaussAdminExecutorCreator()
                .create(selectStatementContext, "select datname, datcompatibility from pg_database where datname = 'sharding_db'", "postgres");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSystemCatalogAdminQueryExecutor.class));
    }
    
    @Test
    public void assertCreateExecutorForSelectVersion() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getProjections().getProjections()).thenReturn(Collections.singletonList(new ExpressionProjectionSegment(-1, -1, "VERSION()")));
        Optional<DatabaseAdminExecutor> actual = new OpenGaussAdminExecutorCreator().create(selectStatementContext, "select VERSION()", "postgres");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSystemCatalogAdminQueryExecutor.class));
    }
    
    @Test
    public void assertCreateOtherExecutor() {
        OpenGaussAdminExecutorCreator creator = new OpenGaussAdminExecutorCreator();
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.emptyList());
        assertThat(creator.create(sqlStatementContext), is(Optional.empty()));
        assertThat(creator.create(sqlStatementContext, "", ""), is(Optional.empty()));
    }
    
    @Test
    public void assertGetType() {
        assertThat(new OpenGaussAdminExecutorCreator().getType(), is("openGauss"));
    }
}
