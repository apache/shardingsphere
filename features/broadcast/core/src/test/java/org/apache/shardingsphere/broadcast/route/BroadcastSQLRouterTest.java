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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Properties;

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
        when(rule.getBroadcastTableNames(Collections.singleton("t_config"))).thenReturn(Collections.singleton("t_config"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        BroadcastRouteEngine routeEngine = mock(BroadcastRouteEngine.class);
        when(BroadcastRouteEngineFactory.newInstance(queryContext, Collections.singleton("t_config"))).thenReturn(routeEngine);
        getSQLRouter(rule).createRouteContext(queryContext, mock(RuleMetaData.class), database, rule, Collections.singleton("t_config"), new ConfigurationProperties(new Properties()));
        verify(routeEngine).route(rule);
    }
    
    private BroadcastSQLRouter getSQLRouter(final BroadcastRule rule) {
        return (BroadcastSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(rule)).get(rule);
    }
}
