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

package org.apache.shardingsphere.infra.binder.engine.segment.from.type;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
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
     * @return bound simple table segment
     */
    public static SimpleTableSegment bind(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext, final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        fillPivotColumnNamesInBinderContext(segment, binderContext);
        IdentifierValue databaseName = getDatabaseName(segment, binderContext);
        ShardingSpherePreconditions.checkNotNull(databaseName.getValue(), NoDatabaseSelectedException::new);
        IdentifierValue schemaName = getSchemaName(segment, binderContext);
        IdentifierValue tableName = segment.getTableName().getIdentifier();
        checkTableExists(binderContext, databaseName.getValue(), schemaName.getValue(), tableName.getValue());
        ShardingSphereSchema schema = binderContext.getMetaData().getDatabase(databaseName.getValue()).getSchema(schemaName.getValue());
        tableBinderContexts.putIfAbsent(
                (segment.getAliasName().orElseGet(tableName::getValue)).toLowerCase(), createSimpleTableBinderContext(segment, schema, databaseName, schemaName, binderContext));
        TableNameSegment tableNameSegment = new TableNameSegment(segment.getTableName().getStartIndex(), segment.getTableName().getStopIndex(), tableName);
        SimpleTableSegment result = new SimpleTableSegment(tableNameSegment);
        segment.getOwner().ifPresent(result::setOwner);
        segment.getAliasSegment().ifPresent(result::setAlias);
        return result;
    }
    
    private static void fillPivotColumnNamesInBinderContext(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext) {
        segment.getPivot().ifPresent(optional -> optional.getPivotColumns().forEach(each -> binderContext.getPivotColumnNames().add(each.getIdentifier().getValue().toLowerCase())));
    }
    
    private static IdentifierValue getDatabaseName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(binderContext.getDatabaseType()).getDialectDatabaseMetaData();
        Optional<OwnerSegment> owner = dialectDatabaseMetaData.getDefaultSchema().isPresent() ? segment.getOwner().flatMap(OwnerSegment::getOwner) : segment.getOwner();
        return new IdentifierValue(owner.map(optional -> optional.getIdentifier().getValue()).orElse(binderContext.getCurrentDatabaseName()));
    }
    
    private static IdentifierValue getSchemaName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext) {
        if (segment.getOwner().isPresent()) {
            return segment.getOwner().get().getIdentifier();
        }
        // TODO getSchemaName according to search path
        DatabaseType databaseType = binderContext.getDatabaseType();
        if ((databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) && SYSTEM_CATALOG_TABLES.contains(segment.getTableName().getIdentifier().getValue())) {
            return new IdentifierValue(PG_CATALOG);
        }
        return new IdentifierValue(new DatabaseTypeRegistry(databaseType).getDefaultSchemaName(binderContext.getCurrentDatabaseName()));
    }
    
    private static void checkTableExists(final SQLStatementBinderContext binderContext, final String databaseName, final String schemaName, final String tableName) {
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
    
    private static SimpleTableSegmentBinderContext createSimpleTableBinderContext(final SimpleTableSegment segment, final ShardingSphereSchema schema, final IdentifierValue databaseName,
                                                                                  final IdentifierValue schemaName, final SQLStatementBinderContext binderContext) {
        IdentifierValue tableName = segment.getTableName().getIdentifier();
        if (binderContext.getMetaData().getDatabase(databaseName.getValue()).getSchema(schemaName.getValue()).containsTable(tableName.getValue())) {
            return createSimpleTableSegmentBinderContextWithMetaData(segment, schema, databaseName, schemaName, binderContext, tableName);
        }
        return new SimpleTableSegmentBinderContext(Collections.emptyList());
    }
    
    private static SimpleTableSegmentBinderContext createSimpleTableSegmentBinderContextWithMetaData(final SimpleTableSegment segment, final ShardingSphereSchema schema,
                                                                                                     final IdentifierValue databaseName, final IdentifierValue schemaName,
                                                                                                     final SQLStatementBinderContext binderContext, final IdentifierValue tableName) {
        Collection<ProjectionSegment> projectionSegments = new LinkedList<>();
        QuoteCharacter quoteCharacter = new DatabaseTypeRegistry(binderContext.getDatabaseType()).getDialectDatabaseMetaData().getQuoteCharacter();
        for (ShardingSphereColumn each : schema.getTable(tableName.getValue()).getColumnValues()) {
            ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(createColumnSegment(segment, databaseName, schemaName, each, quoteCharacter, tableName));
            columnProjectionSegment.setVisible(each.isVisible());
            projectionSegments.add(columnProjectionSegment);
        }
        return new SimpleTableSegmentBinderContext(projectionSegments);
    }
    
    private static ColumnSegment createColumnSegment(final SimpleTableSegment segment, final IdentifierValue databaseName, final IdentifierValue schemaName,
                                                     final ShardingSphereColumn column, final QuoteCharacter quoteCharacter, final IdentifierValue tableName) {
        ColumnSegment result = new ColumnSegment(0, 0, new IdentifierValue(column.getName(), quoteCharacter));
        result.setOwner(new OwnerSegment(0, 0, segment.getAlias().orElse(tableName)));
        result.setColumnBoundInfo(new ColumnSegmentBoundInfo(databaseName, schemaName, tableName, new IdentifierValue(column.getName(), quoteCharacter)));
        return result;
    }
}
