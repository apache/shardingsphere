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

package org.apache.shardingsphere.infra.context.kernel;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.binder.LogicSQL;

import java.util.Collection;

/**
 * Kernel processor.
 */
public final class KernelProcessor {
    
    /**
     * Generate execution context.
     *
     * @param logicSQL logic SQL
     * @param schema ShardingSphere schema
     * @param props configuration properties
     * @return execution context
     */
    public ExecutionContext generateExecutionContext(final LogicSQL logicSQL, final ShardingSphereSchema schema, final ConfigurationProperties props) {
        Collection<ShardingSphereRule> rules = schema.getRules();
        SQLRouteEngine sqlRouteEngine = new SQLRouteEngine(rules, props);
        SQLStatementContext<?> sqlStatementContext = logicSQL.getSqlStatementContext();
        RouteContext routeContext = sqlRouteEngine.route(logicSQL, schema);
        SQLRewriteEntry rewriteEntry = new SQLRewriteEntry(schema.getMetaData().getSchemaMetaData(), props, rules);
        SQLRewriteResult rewriteResult = rewriteEntry.rewrite(logicSQL.getSql(), logicSQL.getParameters(), sqlStatementContext, routeContext);
        Collection<ExecutionUnit> executionUnits = ExecutionContextBuilder.build(schema.getMetaData(), rewriteResult, sqlStatementContext);
        return new ExecutionContext(sqlStatementContext, executionUnits, routeContext);
    }
}
