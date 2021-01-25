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

import org.apache.shardingsphere.infra.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.infra.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;

import java.sql.SQLException;

/**
 * Kernel processor.
 */
public final class KernelProcessor {
    
    /**
     * Generate execution context.
     *
     * @param logicSQL logic SQL
     * @param metaData ShardingSphere meta data
     * @param props configuration properties
     * @return execution context
     * @throws SQLException SQL exception
     */
    public ExecutionContext generateExecutionContext(final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ConfigurationProperties props) throws SQLException {
        audit(logicSQL, metaData);
        RouteContext routeContext = route(logicSQL, metaData, props);
        SQLRewriteResult rewriteResult = rewrite(logicSQL, metaData, props, routeContext);
        ExecutionContext result = createExecutionContext(logicSQL, metaData, routeContext, rewriteResult);
        logSQL(logicSQL, props, result);
        return result;
    }
    
    private void audit(final LogicSQL logicSQL, final ShardingSphereMetaData metaData) throws SQLException {
        new SQLAuditEngine().audit(logicSQL.getSqlStatementContext().getSqlStatement(), logicSQL.getParameters(), metaData.getName(), metaData.getRuleMetaData().getRules());
    }
    
    private RouteContext route(final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ConfigurationProperties props) {
        return new SQLRouteEngine(metaData.getRuleMetaData().getRules(), props).route(logicSQL, metaData);
    }
    
    private SQLRewriteResult rewrite(final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ConfigurationProperties props, final RouteContext routeContext) {
        return new SQLRewriteEntry(
                metaData.getSchema(), props, metaData.getRuleMetaData().getRules()).rewrite(logicSQL.getSql(), logicSQL.getParameters(), logicSQL.getSqlStatementContext(), routeContext);
    }
    
    private ExecutionContext createExecutionContext(final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final RouteContext routeContext, final SQLRewriteResult rewriteResult) {
        return new ExecutionContext(logicSQL.getSqlStatementContext(), ExecutionContextBuilder.build(metaData, rewriteResult, logicSQL.getSqlStatementContext()), routeContext);
    }
    
    private void logSQL(final LogicSQL logicSQL, final ConfigurationProperties props, final ExecutionContext executionContext) {
        if (props.<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(logicSQL, props.<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
    }
}
