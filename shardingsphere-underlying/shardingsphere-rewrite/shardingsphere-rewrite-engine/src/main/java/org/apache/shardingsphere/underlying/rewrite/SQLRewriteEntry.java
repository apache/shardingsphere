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

package org.apache.shardingsphere.underlying.rewrite;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.aware.RouteContextAware;
import org.apache.shardingsphere.underlying.route.context.RouteContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL rewrite entry.
 */
@RequiredArgsConstructor
public final class SQLRewriteEntry {
    
    private final SchemaMetaData schemaMetaData;
    
    private final ConfigurationProperties properties;
    
    private final Map<BaseRule, SQLRewriteContextDecorator> decorators = new LinkedHashMap<>();
    
    /**
     * Register route decorator.
     *
     * @param rule rule
     * @param decorator SQL rewrite context decorator
     */
    public void registerDecorator(final BaseRule rule, final SQLRewriteContextDecorator decorator) {
        decorators.put(rule, decorator);
    }
    
    /**
     * Create SQL rewrite context.
     * 
     * @param sql SQL
     * @param parameters parameters
     * @param sqlStatementContext SQL statement context
     * @param routeContext route context
     * @return SQL rewrite context
     */
    public SQLRewriteContext createSQLRewriteContext(final String sql, final List<Object> parameters, final SQLStatementContext sqlStatementContext, final RouteContext routeContext) {
        SQLRewriteContext result = new SQLRewriteContext(schemaMetaData, sqlStatementContext, sql, parameters);
        decorate(decorators, result, routeContext);
        result.generateSQLTokens();
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void decorate(final Map<BaseRule, SQLRewriteContextDecorator> decorators, final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        for (Entry<BaseRule, SQLRewriteContextDecorator> entry : decorators.entrySet()) {
            BaseRule rule = entry.getKey();
            SQLRewriteContextDecorator decorator = entry.getValue();
            if (decorator instanceof RouteContextAware) {
                ((RouteContextAware) decorator).setRouteContext(routeContext);
            }
            decorator.decorate(rule, properties, sqlRewriteContext);
        }
    }
}
