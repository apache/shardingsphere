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

package org.apache.shardingsphere.readwritesplitting.route.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.transaction.TransactionHolder;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;

/**
 * Data source router for readwrite-splitting.
 */
@RequiredArgsConstructor
public final class ReadwriteSplittingDataSourceRouter {
    
    private static final ThreadLocal<String> SLAVE_ROUTE_HOLDER = new ThreadLocal<>();
    
    private final ReadwriteSplittingDataSourceRule rule;
    
    /**
     * Route.
     * 
     * @param sqlStatementContext SQL statement context
     * @return data source name
     */
    public String route(final SQLStatementContext<?> sqlStatementContext) {
        if (TransactionHolder.isTransaction()) {
            return routeInTransaction(sqlStatementContext);
        }
        return routeNotInTransaction(sqlStatementContext);
    }
    
    private String routeInTransaction(final SQLStatementContext<?> sqlStatementContext) {
        if (rule.getRouteMode() == 0) {
            return rule.getReadwriteSplittingStrategy().getWriteDataSource();
        }
        if (rule.getRouteMode() == 1) {
            if (!TransactionHolder.isTransactionReadOnly() && isPrimaryRoute(sqlStatementContext)) {
                return rule.getReadwriteSplittingStrategy().getWriteDataSource();
            }
            if (null == SLAVE_ROUTE_HOLDER.get()) {
                SLAVE_ROUTE_HOLDER.set(rule.getLoadBalancer().getDataSource(rule.getName(), rule.getWriteDataSource(), rule.getReadDataSourceNames()));
            }
            return SLAVE_ROUTE_HOLDER.get();
        }
        if (rule.getRouteMode() == 2) {
            if (!TransactionHolder.isTransactionReadOnly() && isPrimaryRoute(sqlStatementContext)) {
                return rule.getReadwriteSplittingStrategy().getWriteDataSource();
            }
            return rule.getLoadBalancer().getDataSource(rule.getName(), rule.getWriteDataSource(), rule.getReadDataSourceNames());
        }
        throw new UnsupportedOperationException(String.format("RouteMode: %d not support yet", rule.getRouteMode()));
    }
    
    private String routeNotInTransaction(final SQLStatementContext<?> sqlStatementContext) {
        if (isPrimaryRoute(sqlStatementContext)) {
            return rule.getReadwriteSplittingStrategy().getWriteDataSource();
        }
        if (1 == rule.getReadDataSourceNames().size()) {
            return rule.getReadDataSourceNames().get(0);
        }
        return rule.getLoadBalancer().getDataSource(rule.getName(), rule.getWriteDataSource(), rule.getReadDataSourceNames());
    }
    
    private boolean isPrimaryRoute(final SQLStatementContext<?> sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        return containsLockSegment(sqlStatement) || !(sqlStatement instanceof SelectStatement) || isHintWriteRouteOnly(sqlStatementContext);
    }
    
    private boolean isHintWriteRouteOnly(final SQLStatementContext<?> sqlStatementContext) {
        return HintManager.isWriteRouteOnly() || (sqlStatementContext instanceof CommonSQLStatementContext && ((CommonSQLStatementContext<?>) sqlStatementContext).isHintWriteRouteOnly());
    }
    
    private boolean containsLockSegment(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement && SelectStatementHandler.getLockSegment((SelectStatement) sqlStatement).isPresent();
    }
}
