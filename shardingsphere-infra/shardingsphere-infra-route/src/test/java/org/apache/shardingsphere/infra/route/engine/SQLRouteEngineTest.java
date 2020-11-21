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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteFailureRuleFixture;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteRuleFixture;
import org.apache.shardingsphere.infra.route.hook.SPIRoutingHook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class SQLRouteEngineTest {
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private ConfigurationProperties props;
    
    @Mock
    private SPIRoutingHook routingHook;
    
    @Test
    public void assertRouteSuccess() {
        LogicSQL logicSQL = new LogicSQL(mock(SQLStatementContext.class), "SELECT 1", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(new RouteRuleFixture()));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, schema);
        SQLRouteEngine sqlRouteEngine = new SQLRouteEngine(Collections.singleton(new RouteRuleFixture()), props);
        setSPIRoutingHook(sqlRouteEngine);
        RouteContext actual = sqlRouteEngine.route(logicSQL, metaData);
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_0"));
        assertTrue(routeUnit.getTableMappers().isEmpty());
        verify(routingHook).start("SELECT 1");
        verify(routingHook).finishSuccess(actual, schema);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertRouteFailure() {
        LogicSQL logicSQL = new LogicSQL(mock(SQLStatementContext.class), "SELECT 1", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(new RouteRuleFixture()));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, schema);
        SQLRouteEngine sqlRouteEngine = new SQLRouteEngine(Collections.singleton(new RouteFailureRuleFixture()), props);
        setSPIRoutingHook(sqlRouteEngine);
        try {
            sqlRouteEngine.route(logicSQL, metaData);
        } catch (final UnsupportedOperationException ex) {
            verify(routingHook).start("SELECT 1");
            verify(routingHook).finishFailure(ex);
            throw ex;
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setSPIRoutingHook(final SQLRouteEngine sqlRouteEngine) {
        Field field = SQLRouteEngine.class.getDeclaredField("routingHook");
        field.setAccessible(true);
        field.set(sqlRouteEngine, routingHook);
    }
}
