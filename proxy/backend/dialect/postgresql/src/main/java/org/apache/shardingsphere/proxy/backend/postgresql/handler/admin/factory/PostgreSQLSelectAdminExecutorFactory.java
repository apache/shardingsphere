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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.factory;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.SystemSchemaManager;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseMetaDataExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Select admin executor factory for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLSelectAdminExecutorFactory {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private static final Map<String, Collection<String>> SCHEMA_TABLES = new CaseInsensitiveMap<>();
    
    static {
        SCHEMA_TABLES.put("shardingsphere", new CaseInsensitiveSet<>(Collections.singletonList("cluster_information")));
    }
    
    /**
     * Create new instance of database admin executor.
     *
     * @param selectStatementContext select statement context
     * @param sql SQL
     * @param parameters SQL parameters
     * @return created instance
     */
    public static Optional<DatabaseAdminExecutor> newInstance(final SelectStatementContext selectStatementContext, final String sql, final List<Object> parameters) {
        Map<String, Collection<String>> selectedSchemaTables = getSelectedSchemaTables(selectStatementContext.getSqlStatement());
        if (isSelectedStatisticsSystemTable(selectedSchemaTables) || isSelectedShardingSphereSystemTable(selectedSchemaTables)) {
            return Optional.empty();
        }
        if (isSelectSystemTable(selectedSchemaTables)) {
            return Optional.of(new DatabaseMetaDataExecutor(sql, parameters));
        }
        return Optional.empty();
    }
    
    private static Map<String, Collection<String>> getSelectedSchemaTables(final SelectStatement sqlStatement) {
        Map<String, Collection<String>> result = new CaseInsensitiveMap<>();
        for (TableSegment each : extractTables(sqlStatement)) {
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
    
    private static Collection<TableSegment> extractTables(final SelectStatement sqlStatement) {
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(sqlStatement);
        Collection<TableSegment> result = new LinkedList<>(tableExtractor.getTableContext());
        for (TableSegment each : tableExtractor.getTableContext()) {
            if (each instanceof SubqueryTableSegment) {
                TableExtractor subTableExtractor = new TableExtractor();
                subTableExtractor.extractTablesFromSelect(((SubqueryTableSegment) each).getSubquery().getSelect());
                result.addAll(subTableExtractor.getTableContext());
            }
        }
        return result;
    }
    
    private static boolean isSelectedStatisticsSystemTable(final Map<String, Collection<String>> selectedSchemaTables) {
        return DatabaseTypedSPILoader.findService(DialectDatabaseStatisticsCollector.class, DATABASE_TYPE).map(optional -> optional.isStatisticsTables(selectedSchemaTables)).orElse(false);
    }
    
    private static boolean isSelectedShardingSphereSystemTable(final Map<String, Collection<String>> selectedSchemaTables) {
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
    
    private static boolean isSelectSystemTable(final Map<String, Collection<String>> selectedSchemaTables) {
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
}
