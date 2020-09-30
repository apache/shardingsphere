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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.sql.LogicSQLContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sql.parser.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import java.util.Collection;

/**
 * Kernel processor.
 */
public final class KernelProcessor {
    
    /**
     * Generate execution context.
     *
     * @param logicSQLContext logic SQL context
     * @param props configuration properties
     * @return execution context
     */
    public ExecutionContext generateExecutionContext(final LogicSQLContext logicSQLContext, final ConfigurationProperties props) {
        Collection<ShardingSphereRule> rules = logicSQLContext.getSchemaContext().getSchema().getRules();
        SQLRouteEngine sqlRouteEngine = new SQLRouteEngine(logicSQLContext.getSchemaContext().getSchema().getMetaData(), props, rules);
        SchemaMetaData schemaMetaData = logicSQLContext.getSchemaContext().getSchema().getMetaData().getRuleSchemaMetaData().getSchemaMetaData();
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(schemaMetaData, logicSQLContext.getParameters(), logicSQLContext.getSqlStatement());
        RouteContext routeContext = sqlRouteEngine.route(sqlStatementContext, logicSQLContext.getSql(), logicSQLContext.getParameters());
        SQLRewriteEntry rewriteEntry = new SQLRewriteEntry(logicSQLContext.getSchemaContext().getSchema().getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData(), props, rules);
        SQLRewriteResult rewriteResult = rewriteEntry.rewrite(logicSQLContext.getSql(), logicSQLContext.getParameters(), sqlStatementContext, routeContext);
        Collection<ExecutionUnit> executionUnits = ExecutionContextBuilder.build(logicSQLContext.getSchemaContext().getSchema().getMetaData(), rewriteResult, sqlStatementContext);
        return new ExecutionContext(sqlStatementContext, executionUnits, routeContext);
    }
}
