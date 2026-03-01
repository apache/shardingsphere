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

import org.apache.shardingsphere.broadcast.route.engine.BroadcastRouteEngineFactory;
import org.apache.shardingsphere.broadcast.route.engine.type.BroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.SQLRouter.Type;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(BroadcastRouteEngineFactory.class)
class BroadcastSQLRouterTest {
    
    private final BroadcastRule rule = mock(BroadcastRule.class);
    
    private final BroadcastSQLRouter sqlRouter = (BroadcastSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(rule)).get(rule);
    
    @Test
    void assertCreateRouteContextWhenBroadcastTableNamesAreNotEmpty() {
        QueryContext queryContext = mock(QueryContext.class);
        when(rule.getBroadcastTableNames(Collections.singleton("t_config"))).thenReturn(Collections.singleton("t_config"));
        BroadcastRouteEngine routeEngine = mock(BroadcastRouteEngine.class);
        when(BroadcastRouteEngineFactory.newInstance(queryContext, Collections.singleton("t_config"))).thenReturn(routeEngine);
        RouteContext expectedRouteContext = mock(RouteContext.class);
        when(routeEngine.route(rule)).thenReturn(expectedRouteContext);
        assertThat(sqlRouter.createRouteContext(queryContext, mock(), mock(), rule, Collections.singleton("t_config"), new ConfigurationProperties(new Properties())), is(expectedRouteContext));
        assertThat(sqlRouter.getType(), is(Type.DATA_NODE));
    }
    
    @Test
    void assertCreateRouteContextWhenBroadcastTableNamesAreEmpty() {
        when(rule.getBroadcastTableNames(Collections.singleton("t_config"))).thenReturn(Collections.emptySet());
        assertTrue(sqlRouter.createRouteContext(mock(), mock(), mock(), rule, Collections.singleton("t_config"), new ConfigurationProperties(new Properties())).getRouteUnits().isEmpty());
    }
}
