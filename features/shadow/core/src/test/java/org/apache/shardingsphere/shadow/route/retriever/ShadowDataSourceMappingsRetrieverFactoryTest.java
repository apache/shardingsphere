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

package org.apache.shardingsphere.shadow.route.retriever;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.route.retriever.dml.ShadowDMLStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowDataSourceMappingsRetrieverFactoryTest {
    
    @Test
    void assertNewInstance() {
        ShadowDataSourceMappingsRetriever retriever = ShadowDataSourceMappingsRetrieverFactory.newInstance(
                new QueryContext(createInsertSqlStatementContext(), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)));
        assertThat(retriever, isA(ShadowDMLStatementDataSourceMappingsRetriever.class));
        ShadowDataSourceMappingsRetriever shadowUpdateRouteEngine = ShadowDataSourceMappingsRetrieverFactory.newInstance(
                new QueryContext(createUpdateSqlStatementContext(), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)));
        assertThat(shadowUpdateRouteEngine, isA(ShadowDMLStatementDataSourceMappingsRetriever.class));
        ShadowDataSourceMappingsRetriever shadowDeleteRouteEngine = ShadowDataSourceMappingsRetrieverFactory.newInstance(
                new QueryContext(createDeleteSqlStatementContext(), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)));
        assertThat(shadowDeleteRouteEngine, isA(ShadowDMLStatementDataSourceMappingsRetriever.class));
        ShadowDataSourceMappingsRetriever shadowSelectRouteEngine = ShadowDataSourceMappingsRetrieverFactory.newInstance(
                new QueryContext(createSelectSqlStatementContext(), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)));
        assertThat(shadowSelectRouteEngine, isA(ShadowDMLStatementDataSourceMappingsRetriever.class));
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    private SQLStatementContext createInsertSqlStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(InsertStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        return result;
    }
    
    private SQLStatementContext createUpdateSqlStatementContext() {
        UpdateStatementContext result = mock(UpdateStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(UpdateStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        return result;
    }
    
    private SQLStatementContext createDeleteSqlStatementContext() {
        DeleteStatementContext result = mock(DeleteStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(DeleteStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        return result;
    }
    
    private SQLStatementContext createSelectSqlStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        return result;
    }
}
