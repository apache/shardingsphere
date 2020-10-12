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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.UnconfiguredSchemaSQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.hook.SPIRoutingHook;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.infra.binder.LogicSQL;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL route engine.
 */
public final class SQLRouteEngine {
    
    static {
        ShardingSphereServiceLoader.register(SQLRouter.class);
    }
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLRouter> routers;
    
    private final SPIRoutingHook routingHook;
    
    public SQLRouteEngine(final ConfigurationProperties props, final Collection<ShardingSphereRule> rules) {
        this.props = props;
        routers = OrderedSPIRegistry.getRegisteredServices(rules, SQLRouter.class);
        routingHook = new SPIRoutingHook();
    }
    
    /**
     * Route SQL.
     *
     * @param logicSQL logic SQL
     * @param schema ShardingSphere schema
     * @return route context
     */
    public RouteContext route(final LogicSQL logicSQL, final ShardingSphereSchema schema) {
        routingHook.start(logicSQL.getSql());
        try {
            RouteContext result = doRoute(logicSQL, schema);
            routingHook.finishSuccess(result, schema.getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData());
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            routingHook.finishFailure(ex);
            throw ex;
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private RouteContext doRoute(final LogicSQL logicSQL, final ShardingSphereSchema schema) {
        RouteContext result = new RouteContext();
        for (Entry<ShardingSphereRule, SQLRouter> entry : routers.entrySet()) {
            if (result.getRouteUnits().isEmpty()) {
                result = entry.getValue().createRouteContext(logicSQL, schema, entry.getKey(), props);
            } else {
                entry.getValue().decorateRouteContext(result, logicSQL, schema, entry.getKey(), props);
            }
        }
        new UnconfiguredSchemaSQLRouter().decorate(result, logicSQL, schema);
        return result;
    }
}
