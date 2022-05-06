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

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteFailureRuleFixture;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteRuleFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class SQLRouteEngineTest {
    
    @Mock
    private ConfigurationProperties props;
    
    @Test
    public void assertRouteSuccess() {
        LogicSQL logicSQL = new LogicSQL(mock(CommonSQLStatementContext.class), "SELECT 1", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(new RouteRuleFixture()));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        SQLRouteEngine sqlRouteEngine = new SQLRouteEngine(Collections.singleton(new RouteRuleFixture()), props);
        RouteContext actual = sqlRouteEngine.route(logicSQL, metaData);
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_0"));
        assertTrue(routeUnit.getTableMappers().isEmpty());
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertRouteFailure() {
        LogicSQL logicSQL = new LogicSQL(mock(CommonSQLStatementContext.class), "SELECT 1", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(new RouteRuleFixture()));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        SQLRouteEngine sqlRouteEngine = new SQLRouteEngine(Collections.singleton(new RouteFailureRuleFixture()), props);
        sqlRouteEngine.route(logicSQL, metaData);
    }
}
