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
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecoratorFactory;
import org.apache.shardingsphere.infra.rewrite.engine.GenericSQLRewriteEngine;
import org.apache.shardingsphere.infra.rewrite.engine.RouteSQLRewriteEngine;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL rewrite entry.
 */
public final class SQLRewriteEntry {
    
    private final ShardingSphereDatabase database;
    
    private final ShardingSphereRuleMetaData globalRuleMetaData;
    
    private final ConfigurationProperties props;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLRewriteContextDecorator> decorators;
    
    public SQLRewriteEntry(final ShardingSphereDatabase database, final ShardingSphereRuleMetaData globalRuleMetaData, final ConfigurationProperties props) {
        this.database = database;
        this.globalRuleMetaData = globalRuleMetaData;
        this.props = props;
        decorators = SQLRewriteContextDecoratorFactory.getInstance(database.getRuleMetaData().getRules());
    }
    
    /**
     * Rewrite.
     * 
     * @param sql SQL
     * @param parameters SQL parameters
     * @param sqlStatementContext SQL statement context
     * @param routeContext route context
     * @param connectionContext connection context
     * @return route unit and SQL rewrite result map
     */
    public SQLRewriteResult rewrite(final String sql, final List<Object> parameters, final SQLStatementContext<?> sqlStatementContext,
                                    final RouteContext routeContext, final ConnectionContext connectionContext) {
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sql, parameters, sqlStatementContext, routeContext, connectionContext);
        SQLTranslatorRule rule = globalRuleMetaData.getSingleRule(SQLTranslatorRule.class);
        DatabaseType protocolType = database.getProtocolType();
        DatabaseType storageType = database.getResourceMetaData().getDatabaseType();
        return routeContext.getRouteUnits().isEmpty()
                ? new GenericSQLRewriteEngine(rule, protocolType, storageType).rewrite(sqlRewriteContext)
                : new RouteSQLRewriteEngine(rule, protocolType, storageType).rewrite(sqlRewriteContext, routeContext);
    }
    
    private SQLRewriteContext createSQLRewriteContext(final String sql, final List<Object> parameters, final SQLStatementContext<?> sqlStatementContext,
                                                      final RouteContext routeContext, final ConnectionContext connectionContext) {
        SQLRewriteContext result = new SQLRewriteContext(database.getName(), database.getSchemas(), sqlStatementContext, sql, parameters, connectionContext);
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
