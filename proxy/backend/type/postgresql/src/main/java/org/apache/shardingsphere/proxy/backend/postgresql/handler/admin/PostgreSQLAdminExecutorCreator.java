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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.SystemSchemaManager;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetaDataExecutor.DefaultDatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.PostgreSQLResetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.PostgreSQLSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.PostgreSQLShowVariableExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ResetParameterStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Database admin executor creator for PostgreSQL.
 */
public final class PostgreSQLAdminExecutorCreator implements DatabaseAdminExecutorCreator {
    
    private static final Map<String, Collection<String>> SCHEMA_TABLES = new CaseInsensitiveMap<>();
    
    static {
        SCHEMA_TABLES.put("shardingsphere", new CaseInsensitiveSet<>(Arrays.asList("cluster_information", "sharding_table_statistics")));
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof ShowStatement) {
            return Optional.of(new PostgreSQLShowVariableExecutor((ShowStatement) sqlStatement));
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext sqlStatementContext, final String sql, final String databaseName, final List<Object> parameters) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof SelectStatement) {
            Map<String, Collection<String>> selectedSchemaTables = getSelectedSchemaTables((SelectStatement) sqlStatement);
            if (isSelectedStatisticsSystemTable(selectedSchemaTables) || isSelectedShardingSphereSystemTable(selectedSchemaTables)) {
                return Optional.empty();
            }
            if (isSelectSystemTable(selectedSchemaTables)) {
                return Optional.of(new DefaultDatabaseMetaDataExecutor(sql, parameters));
            }
        }
        if (sqlStatement instanceof SetStatement) {
            return Optional.of(new PostgreSQLSetVariableAdminExecutor((SetStatement) sqlStatement));
        }
        if (sqlStatement instanceof ResetParameterStatement) {
            return Optional.of(new PostgreSQLResetVariableAdminExecutor((ResetParameterStatement) sqlStatement));
        }
        return Optional.empty();
    }
    
    private Map<String, Collection<String>> getSelectedSchemaTables(final SelectStatement sqlStatement) {
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromSelect(sqlStatement);
        List<TableSegment> extracted = new LinkedList<>(extractor.getTableContext());
        for (TableSegment each : extractor.getTableContext()) {
            if (each instanceof SubqueryTableSegment) {
                TableExtractor subExtractor = new TableExtractor();
                subExtractor.extractTablesFromSelect(((SubqueryTableSegment) each).getSubquery().getSelect());
                extracted.addAll(subExtractor.getTableContext());
            }
        }
        Map<String, Collection<String>> result = new CaseInsensitiveMap<>();
        for (TableSegment each : extracted) {
            if (each instanceof SimpleTableSegment) {
                Optional<OwnerSegment> ownerSegment = ((SimpleTableSegment) each).getOwner();
                if (ownerSegment.isPresent()) {
                    Collection<String> tables = result.getOrDefault(ownerSegment.get().getIdentifier().getValue(), new CaseInsensitiveSet<>());
                    tables.add(((SimpleTableSegment) each).getTableName().getIdentifier().getValue());
                    result.put(ownerSegment.get().getIdentifier().getValue(), tables);
                }
            }
        }
        return result;
    }
    
    private boolean isSelectedStatisticsSystemTable(final Map<String, Collection<String>> selectedSchemaTables) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        Optional<DialectDatabaseStatisticsCollector> dialectStatisticsCollector = DatabaseTypedSPILoader.findService(DialectDatabaseStatisticsCollector.class, databaseType);
        return dialectStatisticsCollector.map(dialectDatabaseStatisticsCollector -> dialectDatabaseStatisticsCollector.isStatisticsTables(selectedSchemaTables)).orElse(false);
    }
    
    private boolean isSelectedShardingSphereSystemTable(final Map<String, Collection<String>> selectedSchemaTables) {
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
    
    private boolean isSelectSystemTable(final Map<String, Collection<String>> selectedSchemaTables) {
        if (selectedSchemaTables.isEmpty()) {
            return false;
        }
        for (Entry<String, Collection<String>> each : selectedSchemaTables.entrySet()) {
            if (!SystemSchemaManager.isSystemTable("postgresql", each.getKey(), each.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
