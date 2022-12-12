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

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowDeleteStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowInsertStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowSelectStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowUpdateStatementRoutingEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowRouteEngineFactoryTest {
    
    @Test
    public void assertNewInstance() {
        ShadowRouteEngine shadowInsertRouteEngine = ShadowRouteEngineFactory.newInstance(new QueryContext(createInsertSqlStatementContext(), "", Collections.emptyList()));
        assertThat(shadowInsertRouteEngine, instanceOf(ShadowInsertStatementRoutingEngine.class));
        ShadowRouteEngine shadowUpdateRouteEngine = ShadowRouteEngineFactory.newInstance(new QueryContext(createUpdateSqlStatementContext(), "", Collections.emptyList()));
        assertThat(shadowUpdateRouteEngine, instanceOf(ShadowUpdateStatementRoutingEngine.class));
        ShadowRouteEngine shadowDeleteRouteEngine = ShadowRouteEngineFactory.newInstance(new QueryContext(createDeleteSqlStatementContext(), "", Collections.emptyList()));
        assertThat(shadowDeleteRouteEngine, instanceOf(ShadowDeleteStatementRoutingEngine.class));
        ShadowRouteEngine shadowSelectRouteEngine = ShadowRouteEngineFactory.newInstance(new QueryContext(createSelectSqlStatementContext(), "", Collections.emptyList()));
        assertThat(shadowSelectRouteEngine, instanceOf(ShadowSelectStatementRoutingEngine.class));
    }
    
    private SQLStatementContext<InsertStatement> createInsertSqlStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(InsertStatement.class));
        return result;
    }
    
    private SQLStatementContext<UpdateStatement> createUpdateSqlStatementContext() {
        UpdateStatementContext result = mock(UpdateStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(UpdateStatement.class));
        return result;
    }
    
    private SQLStatementContext<DeleteStatement> createDeleteSqlStatementContext() {
        DeleteStatementContext result = mock(DeleteStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(DeleteStatement.class));
        return result;
    }
    
    private SQLStatementContext<SelectStatement> createSelectSqlStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        return result;
    }
}
