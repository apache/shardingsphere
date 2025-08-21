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

package org.apache.shardingsphere.sharding.route.engine.type.unicast;

import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingTableRuleNotFoundException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class ShardingUnicastRouteEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private ShardingRule rule;
    
    @BeforeEach
    void setUp() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..2}"));
        rule = new ShardingRule(shardingRuleConfig,
                Maps.of("ds_0", new MockedDataSource(), "ds_1", new MockedDataSource(), "ds_2", new MockedDataSource()), mock(ComputeNodeInstanceContext.class), Collections.emptyList());
    }
    
    @Test
    void assertRoutingForShardingTable() {
        RouteContext actual = new ShardingUnicastRouteEngine(mock(SQLStatement.class, RETURNS_DEEP_STUBS), Collections.singleton("t_order"), new ConnectionContext(Collections::emptySet)).route(rule);
        assertThat(actual.getRouteUnits().size(), is(1));
        assertFalse("ds_2".equalsIgnoreCase(actual.getRouteUnits().iterator().next().getDataSourceMapper().getLogicName()));
    }
    
    @Test
    void assertRoutingForBroadcastTable() {
        RouteContext actual = new ShardingUnicastRouteEngine(
                mock(SQLStatement.class, RETURNS_DEEP_STUBS), Collections.singleton("t_config"), new ConnectionContext(Collections::emptySet)).route(rule);
        assertThat(actual.getRouteUnits().size(), is(1));
    }
    
    @Test
    void assertRoutingForNoTable() {
        RouteContext actual = new ShardingUnicastRouteEngine(mock(SQLStatement.class, RETURNS_DEEP_STUBS), Collections.emptyList(), new ConnectionContext(Collections::emptySet)).route(rule);
        assertThat(actual.getRouteUnits().size(), is(1));
    }
    
    @Test
    void assertRouteForWithNoIntersection() {
        assertThrows(ShardingTableRuleNotFoundException.class, () -> new ShardingUnicastRouteEngine(
                mock(SQLStatement.class, RETURNS_DEEP_STUBS), Arrays.asList("t_order", "t_config", "t_product"), new ConnectionContext(Collections::emptySet)).route(rule));
    }
    
    @Test
    void assertRoutingForTableWithoutTableRule() {
        RouteContext actual = new ShardingUnicastRouteEngine(mock(SQLStatement.class, RETURNS_DEEP_STUBS), Collections.singleton("t_other"), new ConnectionContext(Collections::emptySet)).route(rule);
        assertThat(actual.getRouteUnits().size(), is(1));
    }
    
    @Test
    void assertRoutingForBroadcastTableWithCursorStatement() {
        CursorStatement sqlStatement = new CursorStatement(databaseType, null, null);
        RouteContext actual = new ShardingUnicastRouteEngine(sqlStatement, Collections.singleton("t_config"), new ConnectionContext(Collections::emptySet)).route(rule);
        assertThat(actual.getRouteUnits().size(), is(1));
        assertThat(actual.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_0"));
    }
    
    @Test
    void assertRoutingForBroadcastTableWithPreferredDataSource() {
        ConnectionContext connectionContext = new ConnectionContext(() -> Collections.singleton("ds_1"));
        RouteContext actual = new ShardingUnicastRouteEngine(mock(SelectStatement.class, RETURNS_DEEP_STUBS), Collections.singleton("t_config"), connectionContext).route(rule);
        assertThat(actual.getRouteUnits().size(), is(1));
        assertThat(actual.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_1"));
    }
}
