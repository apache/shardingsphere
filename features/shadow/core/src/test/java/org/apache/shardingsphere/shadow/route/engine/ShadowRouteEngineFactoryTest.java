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

package org.apache.shardingsphere.shadow.route.engine;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowDeleteStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowInsertStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowSelectStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowUpdateStatementRoutingEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowRouteEngineFactoryTest {
    
    @Test
    void assertNewInstance() {
        ShadowRouteEngine shadowInsertRouteEngine = ShadowRouteEngineFactory.newInstance(
                new QueryContext(createInsertSqlStatementContext(), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)));
        assertThat(shadowInsertRouteEngine, instanceOf(ShadowInsertStatementRoutingEngine.class));
        ShadowRouteEngine shadowUpdateRouteEngine = ShadowRouteEngineFactory.newInstance(
                new QueryContext(createUpdateSqlStatementContext(), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)));
        assertThat(shadowUpdateRouteEngine, instanceOf(ShadowUpdateStatementRoutingEngine.class));
        ShadowRouteEngine shadowDeleteRouteEngine = ShadowRouteEngineFactory.newInstance(
                new QueryContext(createDeleteSqlStatementContext(), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)));
        assertThat(shadowDeleteRouteEngine, instanceOf(ShadowDeleteStatementRoutingEngine.class));
        ShadowRouteEngine shadowSelectRouteEngine = ShadowRouteEngineFactory.newInstance(
                new QueryContext(createSelectSqlStatementContext(), "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class)));
        assertThat(shadowSelectRouteEngine, instanceOf(ShadowSelectStatementRoutingEngine.class));
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of(DefaultDatabase.LOGIC_NAME));
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
