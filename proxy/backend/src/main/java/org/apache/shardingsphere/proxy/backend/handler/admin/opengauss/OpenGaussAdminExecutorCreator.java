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

package org.apache.shardingsphere.proxy.backend.handler.admin.opengauss;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.handler.admin.postgresql.PostgreSQLAdminExecutorCreator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Database admin executor creator for openGauss.
 */
public final class OpenGaussAdminExecutorCreator implements DatabaseAdminExecutorCreator {
    
    private static final Set<String> SYSTEM_CATALOG_QUERY_EXPRESSIONS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    
    static {
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("VERSION()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("intervaltonum(gs_password_deadline())");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("gs_password_notifytime()");
    }
    
    private static final String OG_DATABASE = "pg_database";
    
    private final PostgreSQLAdminExecutorCreator delegated = new PostgreSQLAdminExecutorCreator();
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext<?> sqlStatementContext) {
        return delegated.create(sqlStatementContext);
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext<?> sqlStatementContext, final String sql, final String databaseName) {
        if (isSystemCatalogQuery(sqlStatementContext)) {
            return Optional.of(new OpenGaussSystemCatalogAdminQueryExecutor(sql));
        }
        return delegated.create(sqlStatementContext, sql, databaseName);
    }
    
    private boolean isSystemCatalogQuery(final SQLStatementContext<?> sqlStatementContext) {
        if (sqlStatementContext.getTablesContext().getTableNames().contains(OG_DATABASE)) {
            return true;
        }
        if (!(sqlStatementContext.getSqlStatement() instanceof SelectStatement)) {
            return false;
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatementContext.getSqlStatement();
        Collection<ProjectionSegment> projections = selectStatement.getProjections().getProjections();
        return 1 == projections.size() && projections.iterator().next() instanceof ExpressionProjectionSegment
                && SYSTEM_CATALOG_QUERY_EXPRESSIONS.contains(((ExpressionProjectionSegment) projections.iterator().next()).getText());
    }
    
    @Override
    public String getType() {
        return "openGauss";
    }
}
