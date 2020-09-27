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

package org.apache.shardingsphere.infra.route;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;
import org.apache.shardingsphere.infra.route.decorator.UnconfiguredSchemaRouteDecorator;
import org.apache.shardingsphere.infra.route.hook.SPIRoutingHook;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data node router.
 */
public final class DataNodeRouter {
    
    static {
        ShardingSphereServiceLoader.register(RouteDecorator.class);
    }
    
    private final ShardingSphereMetaData metaData;
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, RouteDecorator> decorators;
    
    private final SPIRoutingHook routingHook;
    
    public DataNodeRouter(final ShardingSphereMetaData metaData, final ConfigurationProperties props, final Collection<ShardingSphereRule> rules) {
        this.metaData = metaData;
        this.props = props;
        decorators = OrderedSPIRegistry.getRegisteredServices(rules, RouteDecorator.class);
        routingHook = new SPIRoutingHook();
    }
    
    /**
     * Route SQL.
     *
     * @param sqlStatementContext SQL statement context
     * @param sql SQL
     * @param parameters SQL parameters
     * @return route context
     */
    public RouteContext route(final SQLStatementContext<?> sqlStatementContext, final String sql, final List<Object> parameters) {
        routingHook.start(sql);
        try {
            RouteContext result = doRoute(sqlStatementContext, parameters);
            routingHook.finishSuccess(result, metaData.getRuleSchemaMetaData().getConfiguredSchemaMetaData());
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            routingHook.finishFailure(ex);
            throw ex;
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private RouteContext doRoute(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters) {
        RouteContext result = new RouteContext(sqlStatementContext, parameters);
        for (Entry<ShardingSphereRule, RouteDecorator> entry : decorators.entrySet()) {
            entry.getValue().decorate(result, sqlStatementContext, parameters, metaData, entry.getKey(), props);
        }
        new UnconfiguredSchemaRouteDecorator().decorate(result, sqlStatementContext, metaData);
        return result;
    }
}
