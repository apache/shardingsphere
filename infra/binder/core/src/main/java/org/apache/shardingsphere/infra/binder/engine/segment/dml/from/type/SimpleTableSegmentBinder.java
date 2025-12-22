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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.util.AlterTableMetadataCheckUtils;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.DuplicateIndexException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.IndexNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.SystemSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.PivotSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Simple table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleTableSegmentBinder {
    
    /**
     * Bind simple table segment.
     *
     * @param segment simple table segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @return bound simple table segment
     */
    public static SimpleTableSegment bind(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext,
                                          final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        binderContext.getPivotColumnNames().addAll(segment.getPivot().map(PivotSegment::getPivotColumnNames).orElse(Collections.emptyList()));
        IdentifierValue databaseName = getDatabaseName(segment, binderContext);
        Optional<IdentifierValue> schemaName = getSchemaName(segment, binderContext, databaseName);
        IdentifierValue tableName = segment.getTableName().getIdentifier();
        Optional<ShardingSphereSchema> schema = schemaName.map(identifierValue -> binderContext.getMetaData().getDatabase(databaseName.getValue()).getSchema(identifierValue.getValue()));
        checkTableExists(binderContext, schema, tableName.getValue());
        checkTableMetadata(binderContext, schema.orElse(null), schemaName.map(IdentifierValue::getValue).orElse(null), tableName.getValue());
        tableBinderContexts.put(new CaseInsensitiveString(segment.getAliasName().orElseGet(tableName::getValue)),
                createSimpleTableBinderContext(segment, schema, databaseName, schemaName, binderContext));
        TableNameSegment tableNameSegment = new TableNameSegment(segment.getTableName().getStartIndex(), segment.getTableName().getStopIndex(), tableName);
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(databaseName, schemaName.orElse(null)));
        SimpleTableSegment result = new SimpleTableSegment(tableNameSegment);
        segment.getOwner().ifPresent(result::setOwner);
        segment.getAliasSegment().ifPresent(result::setAlias);
        return result;
    }
    
    private static IdentifierValue getDatabaseName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(binderContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData();
        Optional<OwnerSegment> owner = dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent() ? segment.getOwner().flatMap(OwnerSegment::getOwner) : segment.getOwner();
        IdentifierValue result = new IdentifierValue(owner.map(optional -> optional.getIdentifier().getValue()).orElse(binderContext.getCurrentDatabaseName()));
        ShardingSpherePreconditions.checkNotNull(result.getValue(), NoDatabaseSelectedException::new);
        ShardingSpherePreconditions.checkState(binderContext.getMetaData().containsDatabase(result.getValue()), () -> new UnknownDatabaseException(result.getValue()));
        return result;
    }
    
    private static Optional<IdentifierValue> getSchemaName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext, final IdentifierValue databaseName) {
        Optional<IdentifierValue> result = getSchemaName(segment, binderContext);
        result.ifPresent(identifierValue -> ShardingSpherePreconditions.checkState(binderContext.getMetaData().getDatabase(databaseName.getValue()).containsSchema(identifierValue.getValue()),
                () -> new SchemaNotFoundException(identifierValue.getValue())));
        return result;
    }
    
    private static Optional<IdentifierValue> getSchemaName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext) {
        if (segment.getOwner().isPresent()) {
            return Optional.ofNullable(segment.getOwner().get().getIdentifier());
        }
        DatabaseType databaseType = binderContext.getSqlStatement().getDatabaseType();
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(databaseType);
        DialectDatabaseMetaData dialectDatabaseMetaData = databaseTypeRegistry.getDialectDatabaseMetaData();
        Optional<String> defaultSystemSchema = dialectDatabaseMetaData.getSchemaOption().getDefaultSystemSchema();
        if (defaultSystemSchema.isPresent() && SystemSchemaManager.isSystemTable(databaseType.getType(), defaultSystemSchema.get(), segment.getTableName().getIdentifier().getValue())) {
            return Optional.of(new IdentifierValue(defaultSystemSchema.get()));
        }
        return Optional.of(new IdentifierValue(databaseTypeRegistry.getDefaultSchemaName(binderContext.getCurrentDatabaseName())));
    }
    
    private static void checkTableExists(final SQLStatementBinderContext binderContext, final Optional<ShardingSphereSchema> schema, final String tableName) {
        // TODO refactor table exists check with spi @duanzhengqiang
        if (binderContext.getSqlStatement() instanceof CreateTableStatement && isCreateTable(((CreateTableStatement) binderContext.getSqlStatement()).getTable(), tableName)) {
            ShardingSpherePreconditions.checkState(binderContext.getHintValueContext().isSkipMetadataValidate()
                    || ((CreateTableStatement) binderContext.getSqlStatement()).isIfNotExists() || !schema.isPresent() || !schema.get().containsTable(tableName),
                    () -> new TableExistsException(tableName));
            return;
        }
        if (binderContext.getSqlStatement() instanceof AlterTableStatement && isRenameTable((AlterTableStatement) binderContext.getSqlStatement(), tableName)) {
            ShardingSpherePreconditions.checkState(binderContext.getHintValueContext().isSkipMetadataValidate() || !schema.isPresent() || !schema.get().containsTable(tableName),
                    () -> new TableExistsException(tableName));
            return;
        }
        if (binderContext.getSqlStatement() instanceof DropTableStatement) {
            ShardingSpherePreconditions.checkState(((DropTableStatement) binderContext.getSqlStatement()).isIfExists() || schema.isPresent() && schema.get().containsTable(tableName),
                    () -> new TableNotFoundException(tableName));
            return;
        }
        if (binderContext.getSqlStatement() instanceof RenameTableStatement && isRenameTable((RenameTableStatement) binderContext.getSqlStatement(), tableName)) {
            ShardingSpherePreconditions.checkState(binderContext.getHintValueContext().isSkipMetadataValidate() || !schema.isPresent() || !schema.get().containsTable(tableName),
                    () -> new TableExistsException(tableName));
            return;
        }
        if (binderContext.getSqlStatement() instanceof CreateViewStatement && isCreateTable(((CreateViewStatement) binderContext.getSqlStatement()).getView(), tableName)) {
            ShardingSpherePreconditions.checkState(binderContext.getHintValueContext().isSkipMetadataValidate()
                    || ((CreateViewStatement) binderContext.getSqlStatement()).isReplaceView() || !schema.isPresent() || !schema.get().containsTable(tableName),
                    () -> new TableExistsException(tableName));
            return;
        }
        if (binderContext.getSqlStatement() instanceof AlterViewStatement && isRenameView((AlterViewStatement) binderContext.getSqlStatement(), tableName)) {
            ShardingSpherePreconditions.checkState(binderContext.getHintValueContext().isSkipMetadataValidate() || !schema.isPresent() || !schema.get().containsTable(tableName),
                    () -> new TableExistsException(tableName));
            return;
        }
        if (binderContext.getSqlStatement() instanceof DropViewStatement) {
            ShardingSpherePreconditions.checkState(((DropViewStatement) binderContext.getSqlStatement()).isIfExists() || schema.isPresent() && schema.get().containsTable(tableName),
                    () -> new TableNotFoundException(tableName));
            return;
        }
        if ("DUAL".equalsIgnoreCase(tableName)) {
            return;
        }
        if (schema.isPresent() && SystemSchemaManager.isSystemTable(schema.get().getName(), tableName)) {
            return;
        }
        if (binderContext.getExternalTableBinderContexts().containsKey(new CaseInsensitiveString(tableName))) {
            return;
        }
        if (binderContext.getCommonTableExpressionsSegmentsUniqueAliases().contains(tableName)) {
            return;
        }
        ShardingSpherePreconditions.checkState(schema.isPresent() && schema.get().containsTable(tableName), () -> new TableNotFoundException(tableName));
    }
    
    private static boolean isCreateTable(final SimpleTableSegment simpleTableSegment, final String tableName) {
        return simpleTableSegment.getTableName().getIdentifier().getValue().equalsIgnoreCase(tableName);
    }
    
    private static boolean isRenameTable(final AlterTableStatement alterTableStatement, final String tableName) {
        return alterTableStatement.getRenameTable().isPresent() && alterTableStatement.getRenameTable().get().getTableName().getIdentifier().getValue().equalsIgnoreCase(tableName);
    }
    
    private static boolean isRenameTable(final RenameTableStatement renameTableStatement, final String tableName) {
        for (RenameTableDefinitionSegment each : renameTableStatement.getRenameTables()) {
            if (each.getRenameTable().getTableName().getIdentifier().getValue().equalsIgnoreCase(tableName)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isRenameView(final AlterViewStatement alterViewStatement, final String tableName) {
        return alterViewStatement.getRenameView().isPresent() && alterViewStatement.getRenameView().get().getTableName().getIdentifier().getValue().equalsIgnoreCase(tableName);
    }
    
    private static void checkTableMetadata(final SQLStatementBinderContext binderContext, final ShardingSphereSchema schema, final String schemaName, final String tableName) {
        if (binderContext.getHintValueContext().isSkipMetadataValidate() || null == schema) {
            return;
        }
        ShardingSphereTable shardingSphereTable = schema.getTable(tableName);
        if (binderContext.getSqlStatement() instanceof AlterTableStatement) {
            if (isRenameTable((AlterTableStatement) binderContext.getSqlStatement(), tableName)) {
                return;
            }
            ShardingSpherePreconditions.checkState(schema.containsTable(tableName), () -> new TableNotFoundException(tableName));
            AlterTableMetadataCheckUtils.checkAlterTable((AlterTableStatement) binderContext.getSqlStatement(), shardingSphereTable);
            return;
        }
        if (binderContext.getSqlStatement() instanceof CreateIndexStatement) {
            ShardingSpherePreconditions.checkState(schema.containsTable(tableName), () -> new TableNotFoundException(tableName));
            String indexName = ((CreateIndexStatement) binderContext.getSqlStatement()).getIndex().getIndexName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(!shardingSphereTable.containsIndex(indexName), () -> new DuplicateIndexException(indexName));
            return;
        }
        if (binderContext.getSqlStatement() instanceof DropIndexStatement) {
            ShardingSpherePreconditions.checkState(schema.containsTable(tableName), () -> new TableNotFoundException(tableName));
            ((DropIndexStatement) binderContext.getSqlStatement()).getIndexes().forEach(each -> {
                String indexName = each.getIndexName().getIdentifier().getValue();
                ShardingSpherePreconditions.checkState(shardingSphereTable.containsIndex(indexName), () -> new IndexNotFoundException(schemaName, indexName));
            });
        }
    }
    
    private static SimpleTableSegmentBinderContext createSimpleTableBinderContext(final SimpleTableSegment segment, final Optional<ShardingSphereSchema> schema, final IdentifierValue databaseName,
                                                                                  final Optional<IdentifierValue> schemaName, final SQLStatementBinderContext binderContext) {
        IdentifierValue tableName = segment.getTableName().getIdentifier();
        if (schema.isPresent() && schema.get().containsTable(tableName.getValue())) {
            return createSimpleTableSegmentBinderContextWithMetaData(segment, schema.get(), databaseName, schemaName.get(), binderContext, tableName);
        }
        if (binderContext.getSqlStatement() instanceof CreateTableStatement) {
            Collection<ProjectionSegment> projectionSegments = createProjectionSegments((CreateTableStatement) binderContext.getSqlStatement(), databaseName, schemaName.orElse(null), tableName);
            return new SimpleTableSegmentBinderContext(projectionSegments, TableSourceType.PHYSICAL_TABLE);
        }
        CaseInsensitiveString caseInsensitiveTableName = new CaseInsensitiveString(tableName.getValue());
        if (binderContext.getExternalTableBinderContexts().containsKey(caseInsensitiveTableName)) {
            TableSegmentBinderContext tableSegmentBinderContext = binderContext.getExternalTableBinderContexts().get(caseInsensitiveTableName).iterator().next();
            Collection<ProjectionSegment> subqueryProjections =
                    SubqueryTableBindUtils.createSubqueryProjections(tableSegmentBinderContext.getProjectionSegments(), tableName, binderContext.getSqlStatement().getDatabaseType(),
                            TableSourceType.TEMPORARY_TABLE);
            return new SimpleTableSegmentBinderContext(subqueryProjections, TableSourceType.TEMPORARY_TABLE);
        }
        return new SimpleTableSegmentBinderContext(Collections.emptyList(), TableSourceType.TEMPORARY_TABLE);
    }
    
    private static Collection<ProjectionSegment> createProjectionSegments(final CreateTableStatement sqlStatement, final IdentifierValue databaseName,
                                                                          final IdentifierValue schemaName, final IdentifierValue tableName) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        for (ColumnDefinitionSegment each : sqlStatement.getColumnDefinitions()) {
            each.getColumnName().setColumnBoundInfo(
                    new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(databaseName, schemaName), tableName, each.getColumnName().getIdentifier(), TableSourceType.TEMPORARY_TABLE));
            result.add(new ColumnProjectionSegment(each.getColumnName()));
        }
        return result;
    }
    
    private static SimpleTableSegmentBinderContext createSimpleTableSegmentBinderContextWithMetaData(final SimpleTableSegment segment, final ShardingSphereSchema schema,
                                                                                                     final IdentifierValue databaseName, final IdentifierValue schemaName,
                                                                                                     final SQLStatementBinderContext binderContext, final IdentifierValue tableName) {
        Collection<ProjectionSegment> projectionSegments = new LinkedList<>();
        QuoteCharacter quoteCharacter = new DatabaseTypeRegistry(binderContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData().getQuoteCharacter();
        for (ShardingSphereColumn each : schema.getTable(tableName.getValue()).getAllColumns()) {
            ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(createColumnSegment(segment, databaseName, schemaName, each, quoteCharacter, tableName));
            columnProjectionSegment.setVisible(each.isVisible());
            projectionSegments.add(columnProjectionSegment);
        }
        return new SimpleTableSegmentBinderContext(projectionSegments, TableSourceType.PHYSICAL_TABLE);
    }
    
    private static ColumnSegment createColumnSegment(final SimpleTableSegment segment, final IdentifierValue databaseName, final IdentifierValue schemaName,
                                                     final ShardingSphereColumn column, final QuoteCharacter quoteCharacter, final IdentifierValue tableName) {
        ColumnSegment result = new ColumnSegment(0, 0, new IdentifierValue(column.getName(), quoteCharacter));
        result.setOwner(new OwnerSegment(0, 0, segment.getAlias().orElse(tableName)));
        result.setColumnBoundInfo(
                new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(databaseName, schemaName), tableName, new IdentifierValue(column.getName(), quoteCharacter), TableSourceType.PHYSICAL_TABLE));
        return result;
    }
}
