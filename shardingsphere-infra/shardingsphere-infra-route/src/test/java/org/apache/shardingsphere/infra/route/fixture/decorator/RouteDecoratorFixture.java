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

package org.apache.shardingsphere.infra.route.fixture.decorator;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteRuleFixture;

import java.util.Collections;

public final class RouteDecoratorFixture implements RouteDecorator<RouteRuleFixture> {
    
    @Override
    public RouteContext decorate(final RouteContext routeContext, final ShardingSphereMetaData metaData, final RouteRuleFixture rule, final ConfigurationProperties props) {
        RouteResult routeResult = new RouteResult();
        routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.emptyList()));
        return new RouteContext(routeContext.getSqlStatementContext(), routeContext.getParameters(), routeResult);
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
    
    @Override
    public Class<RouteRuleFixture> getTypeClass() {
        return RouteRuleFixture.class;
    }
}
