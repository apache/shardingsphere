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

package org.apache.shardingsphere.broadcast.route.engine;

import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastDatabaseBroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastTableBroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.unicast.BroadcastUnicastRouteEngine;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings(SQLStatementContextExtractor.class)
class BroadcastRouteEngineFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private BroadcastRule rule;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private QueryContext queryContext;
    
    @BeforeEach
    void setUp() {
        when(rule.getBroadcastTableNames(Collections.singleton("foo_tbl"))).thenReturn(Collections.singleton("foo_tbl"));
    }
    
    @Test
    void assertNewInstanceWithTCLStatement() {
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(TCLStatement.class));
        assertThat(BroadcastRouteEngineFactory.newInstance(queryContext, Collections.emptyList()), isA(BroadcastDatabaseBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceWithCursorContextAvailableAndIsAllBroadcastTables() {
        CursorHeldSQLStatementContext sqlStatementContext = mock(CursorHeldSQLStatementContext.class, RETURNS_DEEP_STUBS);
        CloseStatement closeStatement = new CloseStatement(databaseType, mock(), false);
        closeStatement.buildAttributes();
        when(sqlStatementContext.getSqlStatement()).thenReturn(closeStatement);
        when(sqlStatementContext.getTablesContext()).thenReturn(createTablesContext());
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertThat(BroadcastRouteEngineFactory.newInstance(queryContext, Collections.emptyList()), isA(BroadcastUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceWithDDLStatementAndIsAllBroadcastTables() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DDLStatement.class, RETURNS_DEEP_STUBS));
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(SQLStatementContextExtractor.getTableNames(database, sqlStatementContext)).thenReturn(Collections.singleton("foo_tbl"));
        assertThat(BroadcastRouteEngineFactory.newInstance(queryContext, Collections.emptyList()), isA(BroadcastTableBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceWithDALStatement() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getTablesContext()).thenReturn(createTablesContext());
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DALStatement.class));
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertThat(BroadcastRouteEngineFactory.newInstance(queryContext, Collections.emptyList()), isA(BroadcastTableBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceWithDCLStatementWithBroadcastTables() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getTablesContext()).thenReturn(createTablesContext());
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DCLStatement.class));
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertThat(BroadcastRouteEngineFactory.newInstance(queryContext, Collections.emptyList()), isA(BroadcastTableBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceWithSelectStatementAndIsAllBroadcastTables() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getTablesContext()).thenReturn(createTablesContext());
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertThat(BroadcastRouteEngineFactory.newInstance(queryContext, Collections.emptyList()), isA(BroadcastUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceWithUpdateStatementAndIsAllBroadcastTables() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getTablesContext()).thenReturn(createTablesContext());
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(UpdateStatement.class));
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertThat(BroadcastRouteEngineFactory.newInstance(queryContext, Collections.emptyList()), isA(BroadcastDatabaseBroadcastRouteEngine.class));
    }
    
    private TablesContext createTablesContext() {
        return new TablesContext(Collections.singleton(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))), null);
    }
}
