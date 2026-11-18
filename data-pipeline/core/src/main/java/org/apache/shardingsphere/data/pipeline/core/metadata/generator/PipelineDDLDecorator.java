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

package org.apache.shardingsphere.data.pipeline.core.metadata.generator;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ConstraintSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Pipeline DDL decorator.
 */
@AllArgsConstructor
public final class PipelineDDLDecorator {
    
    private static final String SET_SEARCH_PATH_PREFIX = "set search_path";
    
    private final ShardingSphereMetaData metaData;
    
    /**
     * Decorate SQL.
     *
     * @param databaseType database type
     * @param targetDatabaseName target database name
     * @param schemaName schema name
     * @param targetTableName target table name
     * @param parserEngine parser engine
     * @param sql SQL
     * @return decorated SQL
     */
    public Optional<String> decorate(final DatabaseType databaseType, final String targetDatabaseName, final String schemaName, final String targetTableName,
                                     final SQLParserEngine parserEngine, final String sql) {
        if (Strings.isNullOrEmpty(sql)) {
            return Optional.empty();
        }
        String result = decorateActualSQL(targetDatabaseName, schemaName, targetTableName, parserEngine, sql.trim());
        // TODO remove it after set search_path is supported.
        if ("openGauss".equals(databaseType.getType())) {
            return decorateOpenGauss(schemaName, result, parserEngine);
        }
        return Optional.of(result);
    }
    
    private String decorateActualSQL(final String databaseName, final String schemaName, final String targetTableName, final SQLParserEngine parserEngine, final String sql) {
        SQLStatementContext sqlStatementContext = parseSQL(databaseName, parserEngine, sql);
        Map<SQLSegment, String> replaceMap = new TreeMap<>(Comparator.comparing(SQLSegment::getStartIndex));
        if (sqlStatementContext.getSqlStatement() instanceof CreateTableStatement) {
            appendFromIndexAndConstraint(replaceMap, databaseName, schemaName, targetTableName, sqlStatementContext);
            appendFromTable(replaceMap, schemaName, targetTableName, sqlStatementContext);
        }
        if (sqlStatementContext.getSqlStatement() instanceof CommentStatement) {
            appendFromTable(replaceMap, schemaName, targetTableName, sqlStatementContext);
        }
        if (sqlStatementContext.getSqlStatement() instanceof CreateIndexStatement) {
            appendFromTable(replaceMap, schemaName, targetTableName, sqlStatementContext);
            appendFromIndexAndConstraint(replaceMap, databaseName, schemaName, targetTableName, sqlStatementContext);
        }
        if (sqlStatementContext.getSqlStatement() instanceof AlterTableStatement) {
            appendFromIndexAndConstraint(replaceMap, databaseName, schemaName, targetTableName, sqlStatementContext);
            appendFromTable(replaceMap, schemaName, targetTableName, sqlStatementContext);
        }
        appendFromColumns(replaceMap, databaseName, targetTableName, sqlStatementContext.getSqlStatement());
        return doDecorateActualTable(replaceMap, sql);
    }
    
    private SQLStatementContext parseSQL(final String currentDatabaseName, final SQLParserEngine parserEngine, final String sql) {
        return SQLStatementContextFactory.newInstance(metaData, parserEngine.parse(sql, true), currentDatabaseName);
    }
    
    private void appendFromIndexAndConstraint(final Map<SQLSegment, String> replaceMap, final String databaseName, final String schemaName,
                                              final String targetTableName, final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext.getTablesContext().getSimpleTables().isEmpty()) {
            return;
        }
        String sourceTableName = sqlStatementContext.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue();
        if (!sourceTableName.equals(targetTableName)) {
            SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
            Collection<String> candidateLogicIndexNames = findCandidateLogicIndexNames(databaseName, schemaName, targetTableName);
            sqlStatement.getAttributes().findAttribute(IndexSQLStatementAttribute.class).map(IndexSQLStatementAttribute::getIndexes).orElse(Collections.emptyList()).forEach(each -> {
                String logicIndexName = IndexMetaDataUtils.findGeneratedLogicIndexName(
                        each.getIndexName().getIdentifier().getValue(), sourceTableName, candidateLogicIndexNames).orElse(each.getIndexName().getIdentifier().getValue());
                replaceMap.put(each.getIndexName(), logicIndexName);
            });
            for (ConstraintSegment each : sqlStatement.getAttributes().findAttribute(ConstraintSQLStatementAttribute.class).map(ConstraintSQLStatementAttribute::getConstraints)
                    .orElse(Collections.emptyList())) {
                String logicConstraint = IndexMetaDataUtils.getLogicIndexName(each.getIdentifier().getValue(), sourceTableName);
                replaceMap.put(each, logicConstraint);
            }
        }
    }
    
    private Collection<String> findCandidateLogicIndexNames(final String databaseName, final String schemaName, final String targetTableName) {
        if (Strings.isNullOrEmpty(databaseName)) {
            return Collections.emptyList();
        }
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        if (null == database) {
            return Collections.emptyList();
        }
        if (!Strings.isNullOrEmpty(schemaName)) {
            return database.containsSchema(schemaName) && database.getSchema(schemaName).containsTable(targetTableName)
                    ? database.getSchema(schemaName).getTable(targetTableName).getAllIndexes().stream().map(each -> each.getName()).collect(Collectors.toList())
                    : Collections.emptyList();
        }
        Collection<ShardingSphereSchema> matchedSchemas = database.getAllSchemas().stream().filter(each -> each.containsTable(targetTableName)).collect(Collectors.toList());
        return 1 == matchedSchemas.size() ? matchedSchemas.iterator().next().getTable(targetTableName).getAllIndexes().stream()
                .map(each -> each.getName()).collect(Collectors.toList()) : Collections.emptyList();
    }
    
    private void appendFromTable(final Map<SQLSegment, String> replaceMap, final String schemaName, final String targetTableName, final SQLStatementContext sqlStatementContext) {
        Collection<SimpleTableSegment> tables = sqlStatementContext.getTablesContext().getSimpleTables();
        if (tables.isEmpty()) {
            return;
        }
        SimpleTableSegment sourceTable = tables.iterator().next();
        PipelineSQLSegmentBuilder sqlSegmentBuilder = new PipelineSQLSegmentBuilder(sqlStatementContext.getSqlStatement().getDatabaseType());
        for (SimpleTableSegment each : tables) {
            if (!isSameTable(sourceTable, each)) {
                continue;
            }
            if (!Strings.isNullOrEmpty(schemaName)) {
                each.getOwner().ifPresent(owner -> replaceMap.put(owner, sqlSegmentBuilder.getEscapedActualIdentifier(schemaName)));
            }
            if (!targetTableName.equals(each.getTableName().getIdentifier().getValue())) {
                replaceMap.put(each.getTableName(), targetTableName);
            }
        }
    }
    
    private boolean isSameTable(final SimpleTableSegment sourceTable, final SimpleTableSegment referencedTable) {
        return sourceTable.getTableName().getIdentifier().getValue().equals(referencedTable.getTableName().getIdentifier().getValue())
                && sourceTable.getOwner().map(optional -> optional.getIdentifier().getValue()).equals(referencedTable.getOwner().map(optional -> optional.getIdentifier().getValue()));
    }
    
    private void appendFromColumns(final Map<SQLSegment, String> replaceMap, final String databaseName, final String targetTableName, final SQLStatement sqlStatement) {
        if (!"PostgreSQL".equals(sqlStatement.getDatabaseType().getType())) {
            return;
        }
        Collection<ColumnSegment> columns = new LinkedList<>();
        if (sqlStatement instanceof CreateTableStatement) {
            CreateTableStatement createTableStatement = (CreateTableStatement) sqlStatement;
            columns.addAll(createTableStatement.getColumnDefinitions().stream().map(each -> each.getColumnName()).collect(Collectors.toList()));
            columns.addAll(createTableStatement.getColumnReferences());
            appendConstraintColumnReferences(columns, createTableStatement.getTable(), createTableStatement.getConstraintDefinitions());
        }
        if (sqlStatement instanceof AlterTableStatement) {
            AlterTableStatement alterTableStatement = (AlterTableStatement) sqlStatement;
            appendConstraintColumnReferences(columns, alterTableStatement.getTable(),
                    alterTableStatement.getAddConstraintDefinitions().stream().map(each -> each.getConstraintDefinition()).collect(Collectors.toList()));
        }
        if (sqlStatement instanceof CreateIndexStatement) {
            columns.addAll(((CreateIndexStatement) sqlStatement).getColumnReferences());
        }
        if (sqlStatement instanceof CommentStatement && null != ((CommentStatement) sqlStatement).getColumn()) {
            columns.add(((CommentStatement) sqlStatement).getColumn());
        }
        columns.addAll(sqlStatement.getAttributes().findAttribute(IndexSQLStatementAttribute.class).map(IndexSQLStatementAttribute::getIndexColumns).orElse(Collections.emptyList()));
        if (!columns.isEmpty()) {
            PipelineSQLSegmentBuilder sqlSegmentBuilder = new PipelineSQLSegmentBuilder(sqlStatement.getDatabaseType(), metaData.getDatabase(databaseName).getIdentifierContext());
            for (ColumnSegment each : columns) {
                replaceMap.put(each, getColumnReplacement(sqlSegmentBuilder, targetTableName, each));
            }
        }
    }
    
    private void appendConstraintColumnReferences(final Collection<ColumnSegment> columns, final SimpleTableSegment sourceTable, final Collection<ConstraintDefinitionSegment> constraints) {
        for (ConstraintDefinitionSegment each : constraints) {
            columns.addAll(each.getPrimaryKeyColumns());
            columns.addAll(each.getColumnReferences());
            if (each.getReferencedTable().filter(referencedTable -> isSameTable(sourceTable, referencedTable)).isPresent()) {
                columns.addAll(each.getReferencedColumns());
            }
        }
    }
    
    private String getColumnReplacement(final PipelineSQLSegmentBuilder sqlSegmentBuilder, final String targetTableName, final ColumnSegment column) {
        return "*".equals(column.getIdentifier().getValue()) && QuoteCharacter.NONE == column.getIdentifier().getQuoteCharacter()
                ? sqlSegmentBuilder.getEscapedActualIdentifier(targetTableName) + ".*"
                : sqlSegmentBuilder.getEscapedIdentifier(IdentifierScope.COLUMN, new IdentifierValue(column.getIdentifier().getValue(), QuoteCharacter.NONE));
    }
    
    private String doDecorateActualTable(final Map<SQLSegment, String> replaceMap, final String sql) {
        StringBuilder result = new StringBuilder();
        int lastStopIndex = 0;
        for (Entry<SQLSegment, String> entry : replaceMap.entrySet()) {
            result.append(sql, lastStopIndex, entry.getKey().getStartIndex());
            result.append(entry.getValue());
            lastStopIndex = entry.getKey().getStopIndex() + 1;
        }
        if (lastStopIndex < sql.length()) {
            result.append(sql, lastStopIndex, sql.length());
        }
        return result.toString();
    }
    
    // TODO remove it after set search_path is supported.
    private Optional<String> decorateOpenGauss(final String schemaName, final String queryContext, final SQLParserEngine parserEngine) {
        if (queryContext.toLowerCase().startsWith(SET_SEARCH_PATH_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(replaceTableNameWithPrefix(queryContext, schemaName, parserEngine));
    }
    
    private String replaceTableNameWithPrefix(final String sql, final String schemaName, final SQLParserEngine parserEngine) {
        SQLStatement sqlStatement = parserEngine.parse(sql, true);
        if (sqlStatement instanceof CreateTableStatement || sqlStatement instanceof CreateIndexStatement
                || sqlStatement instanceof AlterTableStatement || sqlStatement instanceof CommentStatement) {
            TableExtractor tableExtractor = new TableExtractor();
            tableExtractor.extractTablesFromSQLStatement(sqlStatement);
            Optional<SimpleTableSegment> simpleTableSegment = tableExtractor.getTableContext().stream().filter(SimpleTableSegment.class::isInstance).map(SimpleTableSegment.class::cast).findFirst();
            if (!simpleTableSegment.isPresent()) {
                return sql;
            }
            Optional<String> sqlSchemaName = simpleTableSegment.get().getOwner().map(optional -> optional.getIdentifier().getValue());
            if (sqlSchemaName.isPresent() && sqlSchemaName.get().equals(schemaName)) {
                return sql;
            }
            Map<SQLSegment, String> replaceMap = new TreeMap<>(Comparator.comparing(SQLSegment::getStartIndex));
            replaceMap.put(simpleTableSegment.get(), schemaName + "." + simpleTableSegment.get().getTableName().getIdentifier().getValue());
            return doDecorateActualTable(replaceMap, sql);
        }
        return sql;
    }
}
