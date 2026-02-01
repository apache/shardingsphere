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

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.engine.tableless.type.broadcast.TablelessDataSourceBroadcastRouteEngine;
import org.apache.shardingsphere.infra.route.engine.tableless.type.broadcast.TablelessInstanceBroadcastRouteEngine;
import org.apache.shardingsphere.infra.route.engine.tableless.type.unicast.TablelessDataSourceUnicastRouteEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TablelessDataSourceBroadcastRouteSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class TablelessRouteEngineFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private TablesContext tablesContext;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getTableNames()).thenReturn(new LinkedList<>());
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    @Test
    void assertNewInstanceForSetStatement() {
        SetStatement setStatement = mock(SetStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(setStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, isA(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForTablelessDataSourceBroadcastRouteSQLStatementAttribute() {
        DALStatement dalStatement = mock(DALStatement.class);
        when(dalStatement.getAttributes()).thenReturn(new SQLStatementAttributes(new TablelessDataSourceBroadcastRouteSQLStatementAttribute()));
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, isA(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForTCL() {
        TCLStatement tclStatement = mock(TCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(tclStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, isA(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDataSourceBroadcastRoute() {
        DALStatement sqlStatement = mock(DALStatement.class);
        when(sqlStatement.getDatabaseType()).thenReturn(databaseType);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes());
        DialectDALStatementBroadcastRouteDecider dialectDALStatementBroadcastRouteDecider = mock(DialectDALStatementBroadcastRouteDecider.class);
        when(dialectDALStatementBroadcastRouteDecider.isDataSourceBroadcastRoute(sqlStatement)).thenReturn(true);
        when(DatabaseTypedSPILoader.findService(DialectDALStatementBroadcastRouteDecider.class, databaseType)).thenReturn(Optional.of(dialectDALStatementBroadcastRouteDecider));
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, isA(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForInstanceBroadcastRoute() {
        DALStatement sqlStatement = mock(DALStatement.class);
        when(sqlStatement.getDatabaseType()).thenReturn(databaseType);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes());
        DialectDALStatementBroadcastRouteDecider dialectDALStatementBroadcastRouteDecider = mock(DialectDALStatementBroadcastRouteDecider.class);
        when(dialectDALStatementBroadcastRouteDecider.isInstanceBroadcastRoute(sqlStatement)).thenReturn(true);
        when(DatabaseTypedSPILoader.findService(DialectDALStatementBroadcastRouteDecider.class, databaseType)).thenReturn(Optional.of(dialectDALStatementBroadcastRouteDecider));
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, isA(TablelessInstanceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForCloseAllStatement() {
        CursorHeldSQLStatementContext closeStatementContext = mock(CursorHeldSQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(closeStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        CloseStatement closeStatement = new CloseStatement(databaseType, null, true);
        closeStatement.buildAttributes();
        when(closeStatementContext.getSqlStatement()).thenReturn(closeStatement);
        QueryContext queryContext = new QueryContext(closeStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, isA(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForCreateSchemaStatement() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(CreateSchemaStatement.class, RETURNS_DEEP_STUBS));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, isA(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForAlterSchemaStatement() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(AlterSchemaStatement.class, RETURNS_DEEP_STUBS));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, isA(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDropSchemaStatement() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(DropSchemaStatement.class, RETURNS_DEEP_STUBS));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, isA(TablelessDataSourceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForSelectStatement() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(mock(SelectStatementContext.class));
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(SelectStatement.class));
        TablelessRouteEngine actual = TablelessRouteEngineFactory.newInstance(queryContext, database);
        assertThat(actual, isA(TablelessDataSourceUnicastRouteEngine.class));
    }
}
