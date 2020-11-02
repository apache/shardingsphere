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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.impl.AllSQLRouteExecutor;
import org.apache.shardingsphere.infra.route.engine.impl.PartialSQLRouteExecutor;
import org.apache.shardingsphere.infra.route.hook.SPIRoutingHook;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;

import java.util.Collection;

/**
 * SQL route engine.
 */
@RequiredArgsConstructor
public final class SQLRouteEngine {
    
    private final Collection<ShardingSphereRule> rules;
    
    private final ConfigurationProperties props;
    
    private final SPIRoutingHook routingHook = new SPIRoutingHook();
    
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
            SQLRouteExecutor executor = isNeedAllSchemas(logicSQL.getSqlStatementContext().getSqlStatement()) ? new AllSQLRouteExecutor() : new PartialSQLRouteExecutor(rules, props);
            RouteContext result = executor.route(logicSQL, schema);
            routingHook.finishSuccess(result, schema.getMetaData().getSchemaMetaData().getConfiguredSchemaMetaData());
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            routingHook.finishFailure(ex);
            throw ex;
        }
    }
    
    // TODO use dynamic config to judge UnconfiguredSchema
    private boolean isNeedAllSchemas(final SQLStatement sqlStatement) {
        return sqlStatement instanceof MySQLShowTablesStatement;
    }
}
