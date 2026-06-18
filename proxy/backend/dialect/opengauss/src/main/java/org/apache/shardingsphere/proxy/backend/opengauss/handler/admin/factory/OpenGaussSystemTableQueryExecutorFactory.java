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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.factory;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.SystemSchemaManager;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectDatCompatibilityExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * System table query executor factory for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussSystemTableQueryExecutorFactory {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private static final Map<String, Collection<String>> SCHEMA_TABLES = new CaseInsensitiveMap<>();
    
    static {
        SCHEMA_TABLES.put("shardingsphere", new CaseInsensitiveSet<>(Collections.singletonList("cluster_information")));
    }
    
    /**
     * Create new instance of system table query executor.
     *
     * @param sqlStatementContext select statement context
     * @param sql SQL
     * @param parameters SQL parameters
     * @return created instance
     */
    public static Optional<DatabaseAdminExecutor> newInstance(final SelectStatementContext sqlStatementContext, final String sql, final List<Object> parameters) {
        Map<String, Collection<String>> selectedSchemaTables = getSelectedSchemaTables(sqlStatementContext);
        if (isSelectSystemTable(selectedSchemaTables) && isSelectDatCompatibility(sqlStatementContext)) {
            return Optional.of(new OpenGaussSelectDatCompatibilityExecutor());
        }
        if (isSelectedStatisticsSystemTable(selectedSchemaTables) || isSelectedShardingSphereSystemTable(selectedSchemaTables)) {
            return Optional.empty();
        }
        if (isSelectSystemTable(selectedSchemaTables)) {
            return Optional.of(new DatabaseMetaDataExecutor(sql, parameters));
        }
        return Optional.empty();
    }
    
    private static Map<String, Collection<String>> getSelectedSchemaTables(final SelectStatementContext selectStatementContext) {
        Map<String, Collection<String>> result = new CaseInsensitiveMap<>();
        for (SimpleTableSegment each : selectStatementContext.getTablesContext().getSimpleTables()) {
            TableNameSegment tableNameSegment = each.getTableName();
            String tableName = tableNameSegment.getIdentifier().getValue();
            String schemaName = tableNameSegment.getTableBoundInfo().map(TableSegmentBoundInfo::getOriginalSchema).map(IdentifierValue::getValue).orElseGet(() -> getOwnerSchemaName(each));
            Optional.ofNullable(schemaName).ifPresent(optional -> result.computeIfAbsent(optional, key -> new CaseInsensitiveSet<>()).add(tableName));
        }
        return result;
    }
    
    private static String getOwnerSchemaName(final SimpleTableSegment tableSegment) {
        return tableSegment.getOwner().map(OwnerSegment::getIdentifier).map(IdentifierValue::getValue).orElse(null);
    }
    
    private static boolean isSelectSystemTable(final Map<String, Collection<String>> selectedSchemaTables) {
        if (selectedSchemaTables.isEmpty()) {
            return false;
        }
        for (Entry<String, Collection<String>> each : selectedSchemaTables.entrySet()) {
            if (!SystemSchemaManager.isSystemTable("openGauss", each.getKey(), each.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isSelectDatCompatibility(final SelectStatementContext selectStatementContext) {
        TablesContext tablesContext = selectStatementContext.getTablesContext();
        boolean isSelectFromPgDatabase = 1 == tablesContext.getSimpleTables().size()
                && "pg_database".equalsIgnoreCase(tablesContext.getSimpleTables().iterator().next().getTableName().getIdentifier().getValue());
        Collection<Projection> projections = selectStatementContext.getProjectionsContext().getProjections();
        if (isSelectFromPgDatabase && 1 == projections.size() && projections.iterator().next() instanceof ColumnProjection) {
            ColumnProjection columnProjection = (ColumnProjection) projections.iterator().next();
            ColumnSegmentBoundInfo columnBoundInfo = columnProjection.getColumnBoundInfo();
            return null != columnBoundInfo
                    && "pg_database".equalsIgnoreCase(columnBoundInfo.getOriginalTable().getValue())
                    && "datcompatibility".equalsIgnoreCase(columnProjection.getName().getValue());
        }
        return false;
    }
    
    private static boolean isSelectedStatisticsSystemTable(final Map<String, Collection<String>> selectedSchemaTables) {
        Optional<DialectDatabaseStatisticsCollector> dialectStatisticsCollector = DatabaseTypedSPILoader.findService(DialectDatabaseStatisticsCollector.class, DATABASE_TYPE);
        return dialectStatisticsCollector.map(dialectDatabaseStatisticsCollector -> dialectDatabaseStatisticsCollector.isStatisticsTables(selectedSchemaTables)).orElse(false);
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
}
