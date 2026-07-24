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
import com.google.common.collect.LinkedHashMultimap;
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
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Optional<ShardingSphereSchema> schema = schemaName.map(identifierValue -> binderContext.getMetaData().getDatabase(databaseName).getSchema(identifierValue));
        if (isUpdateTargetTableAlias(binderContext, tableBinderContexts, schemaName, tableName.getValue(), segment)) {
            return bindUpdateTargetTableAlias(segment, binderContext, tableBinderContexts, databaseName, schemaName, tableName);
        }
        checkTableExists(binderContext, schema.orElse(null), schemaName, tableName, segment, tableBinderContexts);
        checkTableMetadata(binderContext, schema.orElse(null), schemaName.map(IdentifierValue::getValue).orElse(null), tableName);
        String tableAliasOrName = segment.getAliasName().orElseGet(tableName::getValue);
        Optional<SimpleTableSegmentBinderContext> tableBinderContext = createSimpleTableBinderContext(segment, schema.orElse(null), databaseName, schemaName.orElse(null), binderContext);
        tableBinderContext.ifPresent(context -> tableBinderContexts.put(CaseInsensitiveString.of(tableAliasOrName), context));
        TableNameSegment tableNameSegment = new TableNameSegment(segment.getTableName().getStartIndex(), segment.getTableName().getStopIndex(), tableName);
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(databaseName, schemaName.orElse(null)));
        SimpleTableSegment result = new SimpleTableSegment(tableNameSegment);
        segment.getOwner().ifPresent(result::setOwner);
        segment.getAliasSegment().ifPresent(result::setAlias);
        segment.getTableSampleExpression().map(optional -> ExpressionSegmentBinder.bind(optional, SegmentType.JOIN_ON, binderContext, tableBinderContexts, LinkedHashMultimap.create()))
                .ifPresent(result::setTableSampleExpression);
        tableBinderContext.flatMap(context -> segment.getPivot()
                .map(optional -> PivotSegmentBinder.bind(optional, binderContext, createTableBinderContexts(tableAliasOrName, context), LinkedHashMultimap.create()))).ifPresent(result::setPivot);
        return result;
    }
    
    private static Multimap<CaseInsensitiveString, TableSegmentBinderContext> createTableBinderContexts(final String tableAliasOrName, final TableSegmentBinderContext tableBinderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> result = LinkedHashMultimap.create();
        result.put(CaseInsensitiveString.of(tableAliasOrName), tableBinderContext);
        return result;
    }
    
    private static IdentifierValue getDatabaseName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(binderContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData();
        Optional<IdentifierValue> ownerDatabaseName = getOwnerDatabaseName(segment, binderContext, dialectDatabaseMetaData);
        Optional<OwnerSegment> owner = dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent() ? segment.getOwner().flatMap(OwnerSegment::getOwner) : segment.getOwner();
        IdentifierValue result = ownerDatabaseName.orElseGet(() -> new IdentifierValue(owner.map(optional -> optional.getIdentifier().getValue()).orElse(binderContext.getCurrentDatabaseName())));
        ShardingSpherePreconditions.checkNotNull(result.getValue(), NoDatabaseSelectedException::new);
        ShardingSpherePreconditions.checkState(binderContext.getMetaData().containsDatabase(result), () -> new UnknownDatabaseException(result.getValue()));
        return result;
    }
    
    private static Optional<IdentifierValue> getOwnerDatabaseName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext,
                                                                  final DialectDatabaseMetaData dialectDatabaseMetaData) {
        if (!dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent() || !segment.getOwner().isPresent() || segment.getOwner().get().getOwner().isPresent()) {
            return Optional.empty();
        }
        IdentifierValue owner = segment.getOwner().get().getIdentifier();
        ShardingSphereDatabase currentDatabase = binderContext.getMetaData().getDatabase(binderContext.getCurrentDatabaseName());
        if (null != currentDatabase && currentDatabase.containsSchema(owner)) {
            return Optional.empty();
        }
        return binderContext.getMetaData().containsDatabase(owner) ? Optional.of(owner) : Optional.empty();
    }
    
    private static Optional<IdentifierValue> getSchemaName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext, final IdentifierValue databaseName) {
        Optional<IdentifierValue> result = getSchemaNameValue(segment, binderContext, databaseName);
        result.ifPresent(identifierValue -> ShardingSpherePreconditions.checkState(binderContext.getMetaData().getDatabase(databaseName).containsSchema(identifierValue),
                () -> new SchemaNotFoundException(identifierValue.getValue())));
        return result;
    }
    
    private static Optional<IdentifierValue> getSchemaNameValue(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext, final IdentifierValue databaseName) {
        DatabaseType databaseType = binderContext.getSqlStatement().getDatabaseType();
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(databaseType);
        DialectDatabaseMetaData dialectDatabaseMetaData = databaseTypeRegistry.getDialectDatabaseMetaData();
        if (segment.getOwner().isPresent() && !isOwnerDatabaseName(segment, binderContext, dialectDatabaseMetaData)) {
            return Optional.ofNullable(segment.getOwner().get().getIdentifier());
        }
        Optional<String> defaultSystemSchema = dialectDatabaseMetaData.getSchemaOption().getDefaultSystemSchema();
        if (defaultSystemSchema.isPresent() && SystemSchemaManager.isSystemTable(databaseType.getType(), defaultSystemSchema.get(), segment.getTableName().getIdentifier().getValue())) {
            return Optional.of(new IdentifierValue(defaultSystemSchema.get()));
        }
        if (dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent()) {
            ShardingSphereDatabase database = binderContext.getMetaData().getDatabase(databaseName);
            if (binderContext.getSqlStatement() instanceof CreateTableStatement || binderContext.getSqlStatement() instanceof CreateViewStatement
                    || binderContext.getSqlStatement() instanceof CreateIndexStatement) {
                return Optional.ofNullable(database.getDefaultSchemaName()).map(IdentifierValue::new);
            }
            Optional<IdentifierValue> result = findUniqueSchemaIdentifierByTableName(database, segment.getTableName().getIdentifier());
            return result.isPresent() ? result : Optional.ofNullable(database.getDefaultSchemaName()).map(IdentifierValue::new);
        }
        ShardingSphereDatabase database = binderContext.getMetaData().getDatabase(binderContext.getCurrentDatabaseName());
        return Optional.ofNullable(database.getDefaultSchemaName()).map(IdentifierValue::new);
    }
    
    private static Optional<IdentifierValue> findUniqueSchemaIdentifierByTableName(final ShardingSphereDatabase database, final IdentifierValue tableName) {
        IdentifierValue result = null;
        for (ShardingSphereSchema each : database.getAllSchemas()) {
            if (!each.containsTable(tableName)) {
                continue;
            }
            if (null != result) {
                return Optional.empty();
            }
            result = new IdentifierValue(each.getName());
        }
        return Optional.ofNullable(result);
    }
    
    private static boolean isOwnerDatabaseName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext, final DialectDatabaseMetaData dialectDatabaseMetaData) {
        return getOwnerDatabaseName(segment, binderContext, dialectDatabaseMetaData).isPresent();
    }
    
    private static boolean isUpdateTargetTableAlias(final SQLStatementBinderContext binderContext, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                    final Optional<IdentifierValue> schemaName, final String tableNameValue, final SimpleTableSegment segment) {
        if (!(binderContext.getSqlStatement() instanceof UpdateStatement)) {
            return false;
        }
        UpdateStatement updateStatement = (UpdateStatement) binderContext.getSqlStatement();
        if (!updateStatement.getFrom().isPresent()) {
            return false;
        }
        if (!updateStatement.isTargetTableIsFromAlias()) {
            return false;
        }
        if (!(updateStatement.getTable() instanceof SimpleTableSegment)) {
            return false;
        }
        if (!((SimpleTableSegment) updateStatement.getTable()).getTableName().getIdentifier().getValue().equalsIgnoreCase(tableNameValue)) {
            return false;
        }
        if (segment.getAliasName().isPresent()) {
            return false;
        }
        return !segment.getOwner().isPresent() && tableBinderContexts.containsKey(CaseInsensitiveString.of(tableNameValue))
                || tableBinderContexts.values().stream().anyMatch(each -> isSameUpdateTargetTableContext(segment, schemaName, tableNameValue, each));
    }
    
    private static SimpleTableSegment bindUpdateTargetTableAlias(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext,
                                                                 final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts, final IdentifierValue databaseName,
                                                                 final Optional<IdentifierValue> schemaName, final IdentifierValue tableName) {
        Collection<TableSegmentBinderContext> fromTableContexts = !segment.getOwner().isPresent() && tableBinderContexts.containsKey(CaseInsensitiveString.of(tableName.getValue()))
                ? tableBinderContexts.get(CaseInsensitiveString.of(tableName.getValue()))
                : tableBinderContexts.values().stream()
                        .filter(each -> isSameUpdateTargetTableContext(segment, schemaName, tableName.getValue(), each)).collect(Collectors.toList());
        IdentifierValue originalTableName = fromTableContexts.stream()
                .map(TableSegmentBinderContext::getOriginalTableName).filter(Optional::isPresent).map(Optional::get).findFirst().orElse(tableName);
        Optional<OwnerSegment> fromTableOwner = fromTableContexts.stream()
                .map(TableSegmentBinderContext::getOriginalOwner).filter(Optional::isPresent).map(Optional::get).findFirst();
        IdentifierValue resolvedSchemaName = fromTableOwner.map(OwnerSegment::getIdentifier).orElseGet(() -> schemaName.orElse(null));
        TableNameSegment tableNameSegment = new TableNameSegment(segment.getTableName().getStartIndex(), segment.getTableName().getStopIndex(), originalTableName);
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(databaseName, resolvedSchemaName));
        SimpleTableSegment result = new SimpleTableSegment(tableNameSegment);
        fromTableOwner.ifPresent(result::setOwner);
        result.setAlias(segment.getAliasSegment().orElseGet(() -> new AliasSegment(segment.getTableName().getStartIndex(), segment.getTableName().getStopIndex(), tableName)));
        segment.getTableSampleExpression().map(optional -> ExpressionSegmentBinder.bind(optional, SegmentType.JOIN_ON, binderContext, tableBinderContexts, LinkedHashMultimap.create()))
                .ifPresent(result::setTableSampleExpression);
        return result;
    }
    
    private static boolean isSameUpdateTargetTableContext(final SimpleTableSegment targetTable, final Optional<IdentifierValue> schemaName, final String tableName,
                                                          final TableSegmentBinderContext tableBinderContext) {
        return tableBinderContext.getOriginalTableName().map(each -> each.getValue().equalsIgnoreCase(tableName)).orElse(false)
                && isSameUpdateTargetOwner(targetTable, schemaName, tableBinderContext);
    }
    
    private static boolean isSameUpdateTargetOwner(final SimpleTableSegment targetTable, final Optional<IdentifierValue> schemaName,
                                                   final TableSegmentBinderContext tableBinderContext) {
        Optional<OwnerSegment> originalOwner = tableBinderContext.getOriginalOwner();
        if (!targetTable.getOwner().isPresent()) {
            return !originalOwner.isPresent() || schemaName.map(optional -> originalOwner.get().getIdentifier().getValue().equalsIgnoreCase(optional.getValue())).orElse(false);
        }
        return originalOwner.isPresent() && isSameOwner(targetTable.getOwner().get(), originalOwner.get());
    }
    
    private static boolean isSameOwner(final OwnerSegment targetOwner, final OwnerSegment originalOwner) {
        return targetOwner.getIdentifier().getValue().equalsIgnoreCase(originalOwner.getIdentifier().getValue())
                && targetOwner.getOwner().isPresent() == originalOwner.getOwner().isPresent()
                && (!targetOwner.getOwner().isPresent() || isSameOwner(targetOwner.getOwner().get(), originalOwner.getOwner().get()));
    }
    
    private static void checkTableExists(final SQLStatementBinderContext binderContext, final ShardingSphereSchema schema, final Optional<IdentifierValue> schemaName,
                                         final IdentifierValue tableName, final SimpleTableSegment segment,
                                         final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        String tableNameValue = tableName.getValue();
        if (isUpdateTargetTableAlias(binderContext, tableBinderContexts, schemaName, tableNameValue, segment)) {
            return;
        }
        // TODO refactor table exists check with spi @duanzhengqiang
        if (binderContext.getSqlStatement() instanceof CreateTableStatement && isCreateTable(((CreateTableStatement) binderContext.getSqlStatement()).getTable(), tableNameValue)) {
            ShardingSpherePreconditions.checkState(binderContext.getHintValueContext().isSkipMetadataValidate()
                    || ((CreateTableStatement) binderContext.getSqlStatement()).isIfNotExists() || null == schema || !schema.containsTable(tableName),
                    () -> new TableExistsException(tableNameValue));
            return;
        }
        if (binderContext.getSqlStatement() instanceof AlterTableStatement && isRenameTable((AlterTableStatement) binderContext.getSqlStatement(), tableNameValue)) {
            ShardingSpherePreconditions.checkState(binderContext.getHintValueContext().isSkipMetadataValidate() || null == schema || !schema.containsTable(tableName),
                    () -> new TableExistsException(tableNameValue));
            return;
        }
        if (binderContext.getSqlStatement() instanceof DropTableStatement) {
            ShardingSpherePreconditions.checkState(((DropTableStatement) binderContext.getSqlStatement()).isIfExists() || null != schema && schema.containsTable(tableName),
                    () -> new TableNotFoundException(tableNameValue));
            return;
        }
        if (binderContext.getSqlStatement() instanceof RenameTableStatement && isRenameTable((RenameTableStatement) binderContext.getSqlStatement(), tableNameValue)) {
            ShardingSpherePreconditions.checkState(binderContext.getHintValueContext().isSkipMetadataValidate() || null == schema || !schema.containsTable(tableName),
                    () -> new TableExistsException(tableNameValue));
            return;
        }
        if (binderContext.getSqlStatement() instanceof CreateViewStatement && isCreateTable(((CreateViewStatement) binderContext.getSqlStatement()).getView(), tableNameValue)) {
            ShardingSpherePreconditions.checkState(binderContext.getHintValueContext().isSkipMetadataValidate()
                    || ((CreateViewStatement) binderContext.getSqlStatement()).isReplaceView() || null == schema || !schema.containsTable(tableName),
                    () -> new TableExistsException(tableNameValue));
            return;
        }
        if (binderContext.getSqlStatement() instanceof AlterViewStatement && isRenameView((AlterViewStatement) binderContext.getSqlStatement(), tableNameValue)) {
            ShardingSpherePreconditions.checkState(binderContext.getHintValueContext().isSkipMetadataValidate() || null == schema || !schema.containsTable(tableName),
                    () -> new TableExistsException(tableNameValue));
            return;
        }
        if (binderContext.getSqlStatement() instanceof DropViewStatement) {
            ShardingSpherePreconditions.checkState(((DropViewStatement) binderContext.getSqlStatement()).isIfExists() || null != schema && schema.containsTable(tableName),
                    () -> new TableNotFoundException(tableNameValue));
            return;
        }
        if ("DUAL".equalsIgnoreCase(tableNameValue)) {
            return;
        }
        if (null != schema && SystemSchemaManager.isSystemTable(schema.getName(), tableNameValue)) {
            return;
        }
        if (segment.getDbLink().isPresent()) {
            return;
        }
        if (isVariableTable(binderContext, segment)) {
            return;
        }
        if (binderContext.getExternalTableBinderContexts().containsKey(CaseInsensitiveString.of(tableNameValue))) {
            return;
        }
        if (binderContext.getCommonTableExpressionsSegmentsUniqueAliases().contains(tableNameValue)) {
            return;
        }
        ShardingSpherePreconditions.checkState(null != schema && schema.containsTable(tableName), () -> new TableNotFoundException(tableNameValue));
    }
    
    private static boolean isVariableTable(final SQLStatementBinderContext binderContext, final SimpleTableSegment segment) {
        if (segment.getOwner().isPresent()) {
            return false;
        }
        IdentifierValue tableName = segment.getTableName().getIdentifier();
        if (QuoteCharacter.NONE != tableName.getQuoteCharacter()) {
            return false;
        }
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(binderContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData();
        return dialectDatabaseMetaData.getVariableTableNamePrefix().filter(tableName.getValue()::startsWith).isPresent();
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
    
    private static void checkTableMetadata(final SQLStatementBinderContext binderContext, final ShardingSphereSchema schema, final String schemaName, final IdentifierValue tableName) {
        if (binderContext.getHintValueContext().isSkipMetadataValidate() || null == schema) {
            return;
        }
        ShardingSphereTable shardingSphereTable = schema.getTable(tableName);
        if (binderContext.getSqlStatement() instanceof AlterTableStatement) {
            if (isRenameTable((AlterTableStatement) binderContext.getSqlStatement(), tableName.getValue())) {
                return;
            }
            ShardingSpherePreconditions.checkState(schema.containsTable(tableName), () -> new TableNotFoundException(tableName.getValue()));
            AlterTableMetadataCheckUtils.checkAlterTable((AlterTableStatement) binderContext.getSqlStatement(), shardingSphereTable);
            return;
        }
        if (binderContext.getSqlStatement() instanceof CreateIndexStatement) {
            ShardingSpherePreconditions.checkState(schema.containsTable(tableName), () -> new TableNotFoundException(tableName.getValue()));
            IdentifierValue indexName = ((CreateIndexStatement) binderContext.getSqlStatement()).getIndex().getIndexName().getIdentifier();
            ShardingSpherePreconditions.checkState(!shardingSphereTable.containsIndex(indexName), () -> new DuplicateIndexException(indexName.getValue()));
            return;
        }
        if (binderContext.getSqlStatement() instanceof DropIndexStatement) {
            ShardingSpherePreconditions.checkState(schema.containsTable(tableName), () -> new TableNotFoundException(tableName.getValue()));
            ((DropIndexStatement) binderContext.getSqlStatement()).getIndexes().forEach(each -> {
                IdentifierValue indexName = each.getIndexName().getIdentifier();
                ShardingSpherePreconditions.checkState(shardingSphereTable.containsIndex(indexName), () -> new IndexNotFoundException(schemaName, indexName.getValue()));
            });
        }
    }
    
    private static Optional<SimpleTableSegmentBinderContext> createSimpleTableBinderContext(final SimpleTableSegment segment, final ShardingSphereSchema schema, final IdentifierValue databaseName,
                                                                                            final IdentifierValue schemaName, final SQLStatementBinderContext binderContext) {
        IdentifierValue tableName = segment.getTableName().getIdentifier();
        Optional<SimpleTableSegmentBinderContext> externalTableBinderContext = createExternalTableBinderContext(segment, tableName, binderContext);
        if (externalTableBinderContext.isPresent()) {
            return externalTableBinderContext;
        }
        if (null != schema && schema.containsTable(tableName)) {
            return createSimpleTableSegmentBinderContextWithMetaData(segment, schema, databaseName, schemaName, binderContext, tableName);
        }
        if (binderContext.getSqlStatement() instanceof CreateTableStatement) {
            Collection<ProjectionSegment> projectionSegments = createProjectionSegments((CreateTableStatement) binderContext.getSqlStatement(), databaseName, schemaName, tableName);
            return Optional.of(new SimpleTableSegmentBinderContext(projectionSegments, TableSourceType.PHYSICAL_TABLE));
        }
        SimpleTableSegmentBinderContext result = new SimpleTableSegmentBinderContext(Collections.emptyList(), TableSourceType.TEMPORARY_TABLE);
        segment.getDbLink().ifPresent(optional -> result.setContainsDBLink(true));
        if (isVariableTable(binderContext, segment)) {
            result.setContainsTableVariable(true);
        }
        return Optional.of(result);
    }
    
    private static Optional<SimpleTableSegmentBinderContext> createExternalTableBinderContext(final SimpleTableSegment segment, final IdentifierValue tableName,
                                                                                              final SQLStatementBinderContext binderContext) {
        if (segment.getOwner().isPresent()) {
            return Optional.empty();
        }
        CaseInsensitiveString caseInsensitiveTableName = CaseInsensitiveString.of(tableName.getValue());
        if (!binderContext.getExternalTableBinderContexts().containsKey(caseInsensitiveTableName)) {
            return Optional.empty();
        }
        TableSegmentBinderContext tableSegmentBinderContext = binderContext.getExternalTableBinderContexts().get(caseInsensitiveTableName).iterator().next();
        Collection<ProjectionSegment> subqueryProjections = SubqueryTableBindUtils.createSubqueryProjections(
                tableSegmentBinderContext.getProjectionSegments(), tableName, binderContext.getSqlStatement().getDatabaseType(), TableSourceType.TEMPORARY_TABLE);
        SimpleTableSegmentBinderContext result = new SimpleTableSegmentBinderContext(subqueryProjections, TableSourceType.TEMPORARY_TABLE);
        if (tableSegmentBinderContext instanceof SimpleTableSegmentBinderContext && ((SimpleTableSegmentBinderContext) tableSegmentBinderContext).isFromWithSegment()) {
            result.setFromWithSegment(true);
        }
        return Optional.of(result);
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
    
    private static Optional<SimpleTableSegmentBinderContext> createSimpleTableSegmentBinderContextWithMetaData(final SimpleTableSegment segment, final ShardingSphereSchema schema,
                                                                                                               final IdentifierValue databaseName, final IdentifierValue schemaName,
                                                                                                               final SQLStatementBinderContext binderContext, final IdentifierValue tableName) {
        Collection<ProjectionSegment> projectionSegments = new LinkedList<>();
        QuoteCharacter quoteCharacter = new DatabaseTypeRegistry(binderContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData().getQuoteCharacter();
        for (ShardingSphereColumn each : schema.getTable(tableName).getAllColumns()) {
            ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(createColumnSegment(segment, databaseName, schemaName, each, quoteCharacter, tableName));
            columnProjectionSegment.setVisible(each.isVisible());
            projectionSegments.add(columnProjectionSegment);
        }
        SimpleTableSegmentBinderContext result = new SimpleTableSegmentBinderContext(projectionSegments, TableSourceType.PHYSICAL_TABLE, tableName);
        segment.getOwner().ifPresent(result::setOriginalOwner);
        return Optional.of(result);
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
