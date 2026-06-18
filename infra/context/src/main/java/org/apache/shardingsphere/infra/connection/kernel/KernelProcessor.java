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

package org.apache.shardingsphere.infra.connection.kernel;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.checker.SupportedSQLCheckEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.infra.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.session.query.QueryContext;

/**
 * Kernel processor.
 */
@HighFrequencyInvocation
public final class KernelProcessor {
    
    /**
     * Generate execution context.
     *
     * @param queryContext query context
     * @param globalRuleMetaData global rule meta data
     * @param props configuration properties
     * @return execution context
     */
    public ExecutionContext generateExecutionContext(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ConfigurationProperties props) {
        check(queryContext);
        RouteContext routeContext = route(queryContext, globalRuleMetaData, props);
        SQLRewriteResult rewriteResult = rewrite(queryContext, globalRuleMetaData, props, routeContext);
        ExecutionContext result = createExecutionContext(queryContext, routeContext, rewriteResult);
        logSQL(queryContext, props, result);
        return result;
    }
    
    private void check(final QueryContext queryContext) {
        if (queryContext.getHintValueContext().isSkipMetadataValidate()) {
            return;
        }
        ShardingSphereDatabase database = queryContext.getUsedDatabase();
        new SupportedSQLCheckEngine().checkSQL(database.getRuleMetaData().getRules(), queryContext.getSqlStatementContext(), database);
    }
    
    private RouteContext route(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ConfigurationProperties props) {
        ShardingSphereDatabase database = queryContext.getUsedDatabase();
        return new SQLRouteEngine(database.getRuleMetaData().getRules(), props).route(queryContext, globalRuleMetaData, database);
    }
    
    private SQLRewriteResult rewrite(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ConfigurationProperties props, final RouteContext routeContext) {
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(queryContext.getUsedDatabase(), globalRuleMetaData, props);
        return sqlRewriteEntry.rewrite(queryContext, routeContext);
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext, final RouteContext routeContext, final SQLRewriteResult rewriteResult) {
        return new ExecutionContext(queryContext, ExecutionContextBuilder.build(queryContext.getUsedDatabase(), rewriteResult, queryContext.getSqlStatementContext()), routeContext);
    }
    
    private void logSQL(final QueryContext queryContext, final ConfigurationProperties props, final ExecutionContext executionContext) {
        if (props.<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(queryContext, props.<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
    }
}
