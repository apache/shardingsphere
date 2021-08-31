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

package org.apache.shardingsphere.shadow.route.future.engine;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.route.future.engine.dml.ShadowInsertStatementRoutingEngine;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AbstractShadowRouteEngineTest {
    
    @Test
    public void assertDoShadowDecorate() {
        AbstractShadowRouteEngine abstractShadowRouteEngine = new ShadowInsertStatementRoutingEngine();
        abstractShadowRouteEngine.shadowDMLStatementRouteDecorate(createRouteContext(), createShadowRule());
    }
    
    private ShadowRule createShadowRule() {
        ShadowRule shadowRule = mock(ShadowRule.class);
        when(shadowRule.getShadowDataSourceMappings()).thenReturn(createShadowDataSourceMappings());
        return shadowRule;
    }
    
    private Map<String, String> createShadowDataSourceMappings() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("ds", "ds_shadow");
        result.put("ds1", "ds1_shadow");
        return result;
    }
    
    private RouteContext createRouteContext() {
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.getRouteUnits()).thenReturn(createRouteUnits());
        return routeContext;
    }
    
    private Collection<RouteUnit> createRouteUnits() {
        Collection<RouteUnit> result = new LinkedList<>();
        result.add(new RouteUnit(new RouteMapper("logic_ds", "ds"), Lists.newArrayList()));
        return result;
    }
}
