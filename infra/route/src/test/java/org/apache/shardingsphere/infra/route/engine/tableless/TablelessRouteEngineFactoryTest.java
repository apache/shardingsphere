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

package org.apache.shardingsphere.infra.route.engine.tableless;

import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.engine.tableless.type.broadcast.TablelessDataSourceBroadcastRouteEngine;
import org.apache.shardingsphere.infra.route.engine.tableless.type.broadcast.TablelessInstanceBroadcastRouteEngine;
import org.apache.shardingsphere.infra.route.engine.tableless.type.unicast.TablelessDataSourceUnicastRouteEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.CreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.SetResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.TCLStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TablelessRouteEngineFactoryTest {
    
    @Mock(extraInterfaces = TableAvailable.class)
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private TablesContext tablesContext;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(((TableAvailable) sqlStatementContext).getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getTableNames()).thenReturn(new ArrayList<>());
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    @Test
    void assertNewInstanceForSetResourceGroup() {
        SetResourceGroupStatement resourceGroupStatement = mock(SetResourceGroupStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(resourceGroupStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, mock(ShardingSphereDatabase.class));
        assertThat(actual, instanceOf(TablelessInstanceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDALShow() {
        DALStatement dalStatement = mock(ShowDatabasesStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, instanceOf(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForTCL() {
        TCLStatement tclStatement = mock(TCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(tclStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, instanceOf(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDALSetForMySQL() {
        SetStatement setStatement = mock(SetStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(setStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, instanceOf(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForCreateResourceGroup() {
        CreateResourceGroupStatement resourceGroupStatement = mock(CreateResourceGroupStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(resourceGroupStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, instanceOf(TablelessInstanceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForCloseAllStatement() {
        CloseStatementContext closeStatementContext = mock(CloseStatementContext.class, RETURNS_DEEP_STUBS);
        CloseStatement closeStatement = mock(CloseStatement.class);
        when(closeStatement.isCloseAll()).thenReturn(true);
        when(closeStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        when(closeStatementContext.getSqlStatement()).thenReturn(closeStatement);
        QueryContext queryContext = new QueryContext(closeStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, instanceOf(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForCreateSchemaStatement() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(CreateSchemaStatement.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, instanceOf(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForAlterSchemaStatement() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(AlterSchemaStatement.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, instanceOf(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDropSchemaStatement() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(DropSchemaStatement.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, instanceOf(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForSelectStatement() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(mock(SelectStatementContext.class));
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(SelectStatement.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, instanceOf(TablelessDataSourceUnicastRouteEngine.class));
    }
}
