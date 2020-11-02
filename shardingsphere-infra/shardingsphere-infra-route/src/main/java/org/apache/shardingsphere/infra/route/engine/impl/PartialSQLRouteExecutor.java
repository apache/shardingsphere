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

package org.apache.shardingsphere.infra.route.engine.impl;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteExecutor;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Partial SQL route executor.
 */
public final class PartialSQLRouteExecutor implements SQLRouteExecutor {
    
    static {
        ShardingSphereServiceLoader.register(SQLRouter.class);
    }
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLRouter> routers;
    
    public PartialSQLRouteExecutor(final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) {
        this.props = props;
        routers = OrderedSPIRegistry.getRegisteredServices(rules, SQLRouter.class);
    }
    
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RouteContext route(final LogicSQL logicSQL, final ShardingSphereSchema schema) {
        RouteContext result = new RouteContext();
        for (Entry<ShardingSphereRule, SQLRouter> entry : routers.entrySet()) {
            if (result.getRouteUnits().isEmpty()) {
                result = entry.getValue().createRouteContext(logicSQL, schema, entry.getKey(), props);
            } else {
                entry.getValue().decorateRouteContext(result, logicSQL, schema, entry.getKey(), props);
            }
        }
        return result;
    }
}
