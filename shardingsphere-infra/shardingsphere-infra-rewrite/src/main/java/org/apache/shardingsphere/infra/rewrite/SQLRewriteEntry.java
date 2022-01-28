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

package org.apache.shardingsphere.infra.rewrite;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.engine.GenericSQLRewriteEngine;
import org.apache.shardingsphere.infra.rewrite.engine.RouteSQLRewriteEngine;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL rewrite entry.
 */
public final class SQLRewriteEntry {
    
    static {
        ShardingSphereServiceLoader.register(SQLRewriteContextDecorator.class);
    }
    
    private final String schemaName;
    
    private final ShardingSphereSchema schema;
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLRewriteContextDecorator> decorators;
    
    public SQLRewriteEntry(final String schemaName, final ShardingSphereSchema schema, final ConfigurationProperties props, final Collection<ShardingSphereRule> rules) {
        this.schemaName = schemaName;
        this.schema = schema;
        this.props = props;
        decorators = OrderedSPIRegistry.getRegisteredServices(SQLRewriteContextDecorator.class, rules);
    }
    
    /**
     * Rewrite.
     * 
     * @param sql SQL
     * @param parameters SQL parameters
     * @param sqlStatementContext SQL statement context
     * @param routeContext route context
     * @return route unit and SQL rewrite result map
     */
    public SQLRewriteResult rewrite(final String sql, final List<Object> parameters, final SQLStatementContext<?> sqlStatementContext, final RouteContext routeContext) {
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sql, parameters, sqlStatementContext, routeContext);
        return routeContext.getRouteUnits().isEmpty()
                ? new GenericSQLRewriteEngine().rewrite(sqlRewriteContext) : new RouteSQLRewriteEngine().rewrite(sqlRewriteContext, routeContext);
    }
    
    private SQLRewriteContext createSQLRewriteContext(final String sql, final List<Object> parameters, final SQLStatementContext<?> sqlStatementContext, final RouteContext routeContext) {
        SQLRewriteContext result = new SQLRewriteContext(schemaName, schema, sqlStatementContext, sql, parameters);
        decorate(decorators, result, routeContext);
        result.generateSQLTokens();
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void decorate(final Map<ShardingSphereRule, SQLRewriteContextDecorator> decorators, final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        for (Entry<ShardingSphereRule, SQLRewriteContextDecorator> entry : decorators.entrySet()) {
            entry.getValue().decorate(entry.getKey(), props, sqlRewriteContext, routeContext);
        }
    }
}
