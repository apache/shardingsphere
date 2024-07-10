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

package org.apache.shardingsphere.infra.binder.segment.from.impl;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.SystemSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bounded.ColumnSegmentBoundedInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bounded.TableSegmentBoundedInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Simple table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleTableSegmentBinder {
    
    private static final Collection<String> SYSTEM_CATALOG_TABLES = new CaseInsensitiveSet<>(4, 1F);
    
    private static final String PG_CATALOG = "pg_catalog";
    
    static {
        SYSTEM_CATALOG_TABLES.add("pg_database");
        SYSTEM_CATALOG_TABLES.add("pg_tables");
        SYSTEM_CATALOG_TABLES.add("pg_roles");
        SYSTEM_CATALOG_TABLES.add("pg_settings");
    }
    
    /**
     * Bind simple table segment.
     *
     * @param segment simple table segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @return bounded simple table segment
     */
    public static SimpleTableSegment bind(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext, final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        fillPivotColumnNamesInBinderContext(segment, binderContext);
        IdentifierValue originalDatabase = getDatabaseName(segment, binderContext);
        IdentifierValue originalSchema = getSchemaName(segment, binderContext);
        ShardingSpherePreconditions.checkNotNull(originalDatabase.getValue(), NoDatabaseSelectedException::new);
        checkTableExists(segment.getTableName().getIdentifier().getValue(), binderContext, originalDatabase.getValue(), originalSchema.getValue());
        ShardingSphereSchema schema = binderContext.getMetaData().getDatabase(originalDatabase.getValue()).getSchema(originalSchema.getValue());
        tableBinderContexts.putIfAbsent((segment.getAliasName().orElseGet(() -> segment.getTableName().getIdentifier().getValue())).toLowerCase(),
                createSimpleTableBinderContext(segment, schema, originalDatabase, originalSchema, binderContext));
        TableNameSegment tableNameSegment = new TableNameSegment(segment.getTableName().getStartIndex(), segment.getTableName().getStopIndex(), segment.getTableName().getIdentifier());
        tableNameSegment.setTableBoundedInfo(new TableSegmentBoundedInfo(originalDatabase, originalSchema));
        SimpleTableSegment result = new SimpleTableSegment(tableNameSegment);
        segment.getOwner().ifPresent(result::setOwner);
        segment.getAliasSegment().ifPresent(result::setAlias);
        return result;
    }
    
    private static void fillPivotColumnNamesInBinderContext(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext) {
        segment.getPivot().ifPresent(optional -> optional.getPivotColumns().forEach(each -> binderContext.getPivotColumnNames().add(each.getIdentifier().getValue().toLowerCase())));
    }
    
    private static IdentifierValue getDatabaseName(final SimpleTableSegment tableSegment, final SQLStatementBinderContext binderContext) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(binderContext.getDatabaseType()).getDialectDatabaseMetaData();
        Optional<OwnerSegment> owner = dialectDatabaseMetaData.getDefaultSchema().isPresent() ? tableSegment.getOwner().flatMap(OwnerSegment::getOwner) : tableSegment.getOwner();
        return new IdentifierValue(owner.map(optional -> optional.getIdentifier().getValue()).orElse(binderContext.getCurrentDatabaseName()));
    }
    
    private static IdentifierValue getSchemaName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext) {
        if (segment.getOwner().isPresent()) {
            return segment.getOwner().get().getIdentifier();
        }
        // TODO getSchemaName according to search path
        DatabaseType databaseType = binderContext.getDatabaseType();
        if ((databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType)
                && SYSTEM_CATALOG_TABLES.contains(segment.getTableName().getIdentifier().getValue())) {
            return new IdentifierValue(PG_CATALOG);
        }
        return new IdentifierValue(new DatabaseTypeRegistry(databaseType).getDefaultSchemaName(binderContext.getCurrentDatabaseName()));
    }
    
    private static SimpleTableSegmentBinderContext createSimpleTableBinderContext(final SimpleTableSegment segment, final ShardingSphereSchema schema, final IdentifierValue originalDatabase,
                                                                                  final IdentifierValue originalSchema, final SQLStatementBinderContext binderContext) {
        Collection<ShardingSphereColumn> columnNames = Optional.ofNullable(
                schema.getTable(segment.getTableName().getIdentifier().getValue())).map(ShardingSphereTable::getColumnValues).orElseGet(Collections::emptyList);
        Collection<ProjectionSegment> projectionSegments = new LinkedList<>();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(binderContext.getDatabaseType()).getDialectDatabaseMetaData();
        for (ShardingSphereColumn each : columnNames) {
            ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue(each.getName(), dialectDatabaseMetaData.getQuoteCharacter()));
            columnSegment.setOwner(new OwnerSegment(0, 0, segment.getAlias().orElse(segment.getTableName().getIdentifier())));
            columnSegment.setColumnBoundedInfo(new ColumnSegmentBoundedInfo(originalDatabase, originalSchema, segment.getTableName().getIdentifier(),
                    new IdentifierValue(each.getName(), dialectDatabaseMetaData.getQuoteCharacter())));
            ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(columnSegment);
            columnProjectionSegment.setVisible(each.isVisible());
            projectionSegments.add(columnProjectionSegment);
        }
        return new SimpleTableSegmentBinderContext(projectionSegments);
    }
    
    private static void checkTableExists(final String tableName, final SQLStatementBinderContext binderContext, final String databaseName, final String schemaName) {
        if ("dual".equalsIgnoreCase(tableName)) {
            return;
        }
        if (SystemSchemaManager.isSystemTable(schemaName, tableName)) {
            return;
        }
        if (binderContext.getExternalTableBinderContexts().containsKey(tableName)) {
            return;
        }
        ShardingSpherePreconditions.checkState(binderContext.getMetaData().containsDatabase(databaseName)
                && binderContext.getMetaData().getDatabase(databaseName).containsSchema(schemaName)
                && binderContext.getMetaData().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName),
                () -> new TableNotFoundException(tableName));
    }
}
