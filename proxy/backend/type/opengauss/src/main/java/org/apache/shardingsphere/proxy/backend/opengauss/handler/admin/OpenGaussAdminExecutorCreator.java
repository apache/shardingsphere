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

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.SystemSchemaManager;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetaDataExecutor.DefaultDatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.PostgreSQLAdminExecutorCreator;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Database admin executor creator for openGauss.
 */
public final class OpenGaussAdminExecutorCreator implements DatabaseAdminExecutorCreator {
    
    private static final Collection<String> SYSTEM_CATALOG_QUERY_EXPRESSIONS = new CaseInsensitiveSet<>();
    
    private static final Map<String, Collection<String>> SCHEMA_TABLES = new CaseInsensitiveMap<>();
    
    static {
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("VERSION()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("opengauss_version()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("gs_password_deadline()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("intervaltonum()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("intervaltonum(gs_password_deadline())");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("gs_password_notifytime()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("pg_catalog.gs_password_deadline()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("pg_catalog.intervaltonum()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("pg_catalog.intervaltonum(pg_catalog.gs_password_deadline())");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("pg_catalog.gs_password_notifytime()");
        SCHEMA_TABLES.put("pg_catalog", new CaseInsensitiveSet<>(Arrays.asList("pg_class", "pg_namespace", "pg_database", "pg_tables", "pg_roles")));
        SCHEMA_TABLES.put("shardingsphere", new CaseInsensitiveSet<>(Arrays.asList("cluster_information", "sharding_table_statistics")));
    }
    
    private final PostgreSQLAdminExecutorCreator delegated = new PostgreSQLAdminExecutorCreator();
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof ShowStatement) {
            return Optional.of(new OpenGaussShowVariableExecutor((ShowStatement) sqlStatement));
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext, final String sql, final String databaseName, final List<Object> parameters) {
        Map<String, Collection<String>> selectedSchemaTables = sqlStatementContext instanceof TableAvailable ? getSelectedSchemaTables(sqlStatementContext) : Collections.emptyMap();
        if (isSQLFederationSystemCatalogQuery(selectedSchemaTables) || isSQLFederationSystemCatalogQueryExpressions(sqlStatementContext)) {
            return Optional.of(new OpenGaussSystemCatalogAdminQueryExecutor(sqlStatementContext, sql, databaseName, parameters));
        }
        if (isPassThroughSystemCatalogQuery(selectedSchemaTables)) {
            return Optional.of(new DefaultDatabaseMetaDataExecutor(sql, parameters));
        }
        return delegated.create(sqlStatementContext, sql, databaseName, parameters);
    }
    
    private Map<String, Collection<String>> getSelectedSchemaTables(final SQLStatementContext sqlStatementContext) {
        Map<String, Collection<String>> result = new CaseInsensitiveMap<>();
        for (SimpleTableSegment each : ((TableAvailable) sqlStatementContext).getTablesContext().getSimpleTables()) {
            TableNameSegment tableNameSegment = each.getTableName();
            String schemaName = tableNameSegment.getTableBoundInfo().map(optional -> optional.getOriginalSchema().getValue()).orElse(null);
            schemaName = Strings.isNullOrEmpty(schemaName) ? each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(null) : schemaName;
            if (!Strings.isNullOrEmpty(schemaName)) {
                Collection<String> tables = result.getOrDefault(schemaName, new CaseInsensitiveSet<>());
                tables.add(tableNameSegment.getIdentifier().getValue());
                result.put(schemaName, tables);
            }
        }
        return result;
    }
    
    private boolean isSQLFederationSystemCatalogQuery(final Map<String, Collection<String>> selectedSchemaTables) {
        if (isSelectedStatisticsSystemTable(selectedSchemaTables)) {
            return true;
        }
        if (selectedSchemaTables.isEmpty()) {
            return false;
        }
        for (Entry<String, Collection<String>> each : selectedSchemaTables.entrySet()) {
            if (!SCHEMA_TABLES.containsKey(each.getKey())) {
                return false;
            }
            if (!SCHEMA_TABLES.get(each.getKey()).containsAll(each.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSelectedStatisticsSystemTable(final Map<String, Collection<String>> selectedSchemaTables) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
        Optional<DialectDatabaseStatisticsCollector> dialectStatisticsCollector = DatabaseTypedSPILoader.findService(DialectDatabaseStatisticsCollector.class, databaseType);
        return dialectStatisticsCollector.map(optional -> optional.isStatisticsTables(selectedSchemaTables)).orElse(false);
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
    
    private boolean isPassThroughSystemCatalogQuery(final Map<String, Collection<String>> selectedSchemaTables) {
        if (selectedSchemaTables.isEmpty()) {
            return false;
        }
        for (Entry<String, Collection<String>> each : selectedSchemaTables.entrySet()) {
            if (!SystemSchemaManager.isSystemTable("opengauss", each.getKey(), each.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
