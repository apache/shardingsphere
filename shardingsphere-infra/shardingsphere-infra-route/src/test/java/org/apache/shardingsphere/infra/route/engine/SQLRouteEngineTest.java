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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteFailureRuleFixture;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteRuleFixture;
import org.apache.shardingsphere.infra.route.hook.SPIRoutingHook;
import org.apache.shardingsphere.infra.sql.LogicSQL;
import org.apache.shardingsphere.infra.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SQLRouteEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ConfigurationProperties props;
    
    @Mock
    private SPIRoutingHook routingHook;
    
    @Before
    public void setUp() {
        when(metaData.getRuleSchemaMetaData().getSchemaMetaData()).thenReturn(mock(SchemaMetaData.class));
    }
    
    @Test
    public void assertRouteSuccess() {
        ShardingSphereSchema schema = new ShardingSphereSchema("logic_schema", Collections.emptyList(), Collections.singleton(new RouteRuleFixture()), Collections.emptyMap(), metaData);
        LogicSQL logicSQL = new LogicSQL(schema, mock(SQLStatementContext.class), "SELECT 1", Collections.emptyList());
        SQLRouteEngine sqlRouteEngine = new SQLRouteEngine(props, Collections.singleton(new RouteRuleFixture()));
        setSPIRoutingHook(sqlRouteEngine);
        RouteContext actual = sqlRouteEngine.route(logicSQL);
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_0"));
        assertTrue(routeUnit.getTableMappers().isEmpty());
        verify(routingHook).start("SELECT 1");
        verify(routingHook).finishSuccess(actual, metaData.getRuleSchemaMetaData().getConfiguredSchemaMetaData());
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertRouteFailure() {
        ShardingSphereSchema schema = new ShardingSphereSchema("logic_schema", Collections.emptyList(), Collections.singleton(new RouteRuleFixture()), Collections.emptyMap(), metaData);
        LogicSQL logicSQL = new LogicSQL(schema, mock(SQLStatementContext.class), "SELECT 1", Collections.emptyList());
        SQLRouteEngine sqlRouteEngine = new SQLRouteEngine(props, Collections.singleton(new RouteFailureRuleFixture()));
        setSPIRoutingHook(sqlRouteEngine);
        try {
            sqlRouteEngine.route(logicSQL);
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
