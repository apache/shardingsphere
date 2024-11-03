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

package org.apache.shardingsphere.broadcast.route;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.route.engine.BroadcastRouteEngineFactory;
import org.apache.shardingsphere.broadcast.route.engine.type.BroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(BroadcastRouteEngineFactory.class)
class BroadcastSQLRouterTest {
    
    @Test
    void assertCreateRouteContext() {
        QueryContext queryContext = mock(QueryContext.class);
        BroadcastRule rule = mock(BroadcastRule.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        BroadcastRouteEngine routeEngine = mock(BroadcastRouteEngine.class);
        when(BroadcastRouteEngineFactory.newInstance(rule, database, queryContext)).thenReturn(routeEngine);
        getSQLRouter(rule).createRouteContext(queryContext, mock(RuleMetaData.class), database, rule, new ConfigurationProperties(new Properties()));
        verify(routeEngine).route(any(), eq(rule));
    }
    
    @Test
    void assertDecorateBroadcastRouteContextWithSingleDataSource() {
        BroadcastRuleConfiguration currentConfig = mock(BroadcastRuleConfiguration.class);
        when(currentConfig.getTables()).thenReturn(Collections.singleton("t_order"));
        BroadcastRule rule = new BroadcastRule(currentConfig, Collections.singletonMap("foo_ds", new MockedDataSource()), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), Lists.newArrayList()));
        BroadcastSQLRouter sqlRouter = getSQLRouter(rule);
        sqlRouter.decorateRouteContext(routeContext, createQueryContext(), mockSingleDatabase(), rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = routeContext.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is("foo_ds"));
    }
    
    private BroadcastSQLRouter getSQLRouter(final BroadcastRule rule) {
        return (BroadcastSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(rule)).get(rule);
    }
    
    private ShardingSphereDatabase mockSingleDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        when(result.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singletonList("foo_ds"));
        return result;
    }
    
    private QueryContext createQueryContext() {
        CreateTableStatement createTableStatement = new MySQLCreateTableStatement(false);
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        return new QueryContext(new CreateTableStatementContext(createTableStatement, DefaultDatabase.LOGIC_NAME), "CREATE TABLE", new LinkedList<>(), new HintValueContext(),
                mockConnectionContext(), mock(ShardingSphereMetaData.class));
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of(DefaultDatabase.LOGIC_NAME));
        return result;
    }
}
