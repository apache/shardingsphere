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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilderRule;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetaDataExecutor.DefaultDatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.PostgreSQLAdminExecutorCreator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Database admin executor creator for openGauss.
 */
public final class OpenGaussAdminExecutorCreator implements DatabaseAdminExecutorCreator {
    
    private static final Set<String> SYSTEM_CATALOG_QUERY_EXPRESSIONS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    
    private static final Set<String> SYSTEM_CATALOG_TABLES = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    
    static {
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("VERSION()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("opengauss_version()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("intervaltonum(gs_password_deadline())");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("gs_password_notifytime()");
        SYSTEM_CATALOG_TABLES.add("pg_class");
        SYSTEM_CATALOG_TABLES.add("pg_namespace");
        SYSTEM_CATALOG_TABLES.add("pg_database");
        SYSTEM_CATALOG_TABLES.add("pg_tables");
        SYSTEM_CATALOG_TABLES.add("pg_roles");
    }
    
    private final PostgreSQLAdminExecutorCreator delegated = new PostgreSQLAdminExecutorCreator();
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext) {
        return delegated.create(sqlStatementContext);
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext, final String sql, final String databaseName, final List<Object> parameters) {
        if (isSQLFederationSystemCatalogQuery(sqlStatementContext) || isSQLFederationSystemCatalogQueryExpressions(sqlStatementContext)) {
            return Optional.of(new OpenGaussSystemCatalogAdminQueryExecutor(sqlStatementContext, sql, databaseName, parameters));
        }
        if (isPassThroughSystemCatalogQuery(sqlStatementContext)) {
            return Optional.of(new DefaultDatabaseMetaDataExecutor(sql, parameters));
        }
        return delegated.create(sqlStatementContext, sql, databaseName, parameters);
    }
    
    private boolean isSQLFederationSystemCatalogQuery(final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        return !tableNames.isEmpty() && SYSTEM_CATALOG_TABLES.containsAll(tableNames);
    }
    
    private boolean isSQLFederationSystemCatalogQueryExpressions(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext.getSqlStatement() instanceof SelectStatement)) {
            return false;
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatementContext.getSqlStatement();
        Collection<ProjectionSegment> projections = selectStatement.getProjections().getProjections();
        return 1 == projections.size() && projections.iterator().next() instanceof ExpressionProjectionSegment
                && SYSTEM_CATALOG_QUERY_EXPRESSIONS.contains(((ExpressionProjectionSegment) projections.iterator().next()).getText());
    }
    
    private boolean isPassThroughSystemCatalogQuery(final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        return !tableNames.isEmpty() && (SystemSchemaBuilderRule.OPEN_GAUSS_INFORMATION_SCHEMA.getTables().containsAll(tableNames)
                || SystemSchemaBuilderRule.OPEN_GAUSS_PG_CATALOG.getTables().containsAll(tableNames));
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
