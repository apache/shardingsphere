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

package org.apache.shardingsphere.infra.route.engine;

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.fixture.decider.RouteAllSQLStatementFixture;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteFailureRuleFixture;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteRuleFixture;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLRouteEngineTest {
    
    @Test
    void assertRouteToPartial() {
        ShardingSphereDatabase database = createDatabase();
        QueryContext queryContext = mockQueryContext(database, mock(SQLStatement.class));
        RouteContext actual = new SQLRouteEngine(Collections.singleton(new RouteRuleFixture()), new ConfigurationProperties(new Properties())).route(queryContext, mock(RuleMetaData.class), database);
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_0"));
        assertTrue(routeUnit.getTableMappers().isEmpty());
    }
    
    @Test
    void assertRouteToAll() {
        ShardingSphereDatabase database = createDatabase();
        QueryContext queryContext = mockQueryContext(database, new RouteAllSQLStatementFixture());
        RouteContext actual = new SQLRouteEngine(Collections.singleton(new RouteRuleFixture()), new ConfigurationProperties(new Properties())).route(queryContext, mock(RuleMetaData.class), database);
        assertTrue(actual.getRouteUnits().isEmpty());
    }
    
    @Test
    void assertRouteFailed() {
        ShardingSphereDatabase database = createDatabase();
        QueryContext queryContext = mockQueryContext(database, mock(SQLStatement.class));
        assertThrows(UnsupportedOperationException.class, () -> new SQLRouteEngine(
                Collections.singleton(new RouteFailureRuleFixture()), new ConfigurationProperties(new Properties())).route(queryContext, mock(RuleMetaData.class), database));
    }
    
    private ShardingSphereDatabase createDatabase() {
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(new RouteRuleFixture()));
        return new ShardingSphereDatabase("logic_schema", mock(DatabaseType.class), mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
    }
    
    private QueryContext mockQueryContext(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("logic_schema"));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDatabase("logic_schema")).thenReturn(database);
        when(metaData.containsDatabase("logic_schema")).thenReturn(true);
        SQLStatementContext sqlStatementContext = mock(CommonSQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        return new QueryContext(sqlStatementContext, "SELECT 1", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
    }
}
