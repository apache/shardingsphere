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
import org.apache.shardingsphere.infra.binder.context.available.IndexContextAvailable;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ConstraintSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

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
        String result = decorateActualSQL(targetDatabaseName, targetTableName, parserEngine, databaseType, sql.trim());
        // TODO remove it after set search_path is supported.
        if ("openGauss".equals(databaseType.getType())) {
            return decorateOpenGauss(targetDatabaseName, schemaName, result, parserEngine, databaseType);
        }
        return Optional.of(result);
    }
    
    private String decorateActualSQL(final String databaseName, final String targetTableName, final SQLParserEngine parserEngine, final DatabaseType databaseType, final String sql) {
        SQLStatementContext sqlStatementContext = parseSQL(databaseName, parserEngine, databaseType, sql);
        Map<SQLSegment, String> replaceMap = new TreeMap<>(Comparator.comparing(SQLSegment::getStartIndex));
        if (sqlStatementContext instanceof CreateTableStatementContext) {
            appendFromIndexAndConstraint(replaceMap, targetTableName, sqlStatementContext);
            appendFromTable(replaceMap, targetTableName, sqlStatementContext);
        }
        if (sqlStatementContext.getSqlStatement() instanceof CommentStatement) {
            appendFromTable(replaceMap, targetTableName, sqlStatementContext);
        }
        if (sqlStatementContext instanceof CreateIndexStatementContext) {
            appendFromTable(replaceMap, targetTableName, sqlStatementContext);
            appendFromIndexAndConstraint(replaceMap, targetTableName, sqlStatementContext);
        }
        if (sqlStatementContext instanceof AlterTableStatementContext) {
            appendFromIndexAndConstraint(replaceMap, targetTableName, sqlStatementContext);
            appendFromTable(replaceMap, targetTableName, sqlStatementContext);
        }
        return doDecorateActualTable(replaceMap, sql);
    }
    
    private SQLStatementContext parseSQL(final String currentDatabaseName, final SQLParserEngine parserEngine, final DatabaseType databaseType, final String sql) {
        return new SQLBindEngine(metaData, currentDatabaseName, new HintValueContext()).bind(databaseType, parserEngine.parse(sql, true), Collections.emptyList());
    }
    
    private void appendFromIndexAndConstraint(final Map<SQLSegment, String> replaceMap, final String targetTableName, final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext.getTablesContext().getSimpleTables().isEmpty()) {
            return;
        }
        TableNameSegment tableNameSegment = sqlStatementContext.getTablesContext().getSimpleTables().iterator().next().getTableName();
        if (!tableNameSegment.getIdentifier().getValue().equals(targetTableName)) {
            SQLStatementAttributes attributes = sqlStatementContext.getSqlStatement().getAttributes();
            if (sqlStatementContext instanceof IndexContextAvailable) {
                for (IndexSegment each : ((IndexContextAvailable) sqlStatementContext).getIndexes()) {
                    String logicIndexName = IndexMetaDataUtils.getLogicIndexName(each.getIndexName().getIdentifier().getValue(), tableNameSegment.getIdentifier().getValue());
                    replaceMap.put(each.getIndexName(), logicIndexName);
                }
            }
            for (ConstraintSegment each : attributes.findAttribute(ConstraintSQLStatementAttribute.class).map(ConstraintSQLStatementAttribute::getConstraints).orElse(Collections.emptyList())) {
                String logicConstraint = IndexMetaDataUtils.getLogicIndexName(each.getIdentifier().getValue(), tableNameSegment.getIdentifier().getValue());
                replaceMap.put(each, logicConstraint);
            }
        }
    }
    
    private void appendFromTable(final Map<SQLSegment, String> replaceMap, final String targetTableName, final SQLStatementContext sqlStatementContext) {
        for (SimpleTableSegment each : sqlStatementContext.getTablesContext().getSimpleTables()) {
            if (!targetTableName.equals(each.getTableName().getIdentifier().getValue())) {
                replaceMap.put(each.getTableName(), targetTableName);
            }
        }
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
    private Optional<String> decorateOpenGauss(final String databaseName, final String schemaName, final String queryContext,
                                               final SQLParserEngine parserEngine, final DatabaseType databaseType) {
        if (queryContext.toLowerCase().startsWith(SET_SEARCH_PATH_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(replaceTableNameWithPrefix(queryContext, schemaName, databaseName, parserEngine, databaseType));
    }
    
    private String replaceTableNameWithPrefix(final String sql, final String schemaName, final String databaseName, final SQLParserEngine parserEngine, final DatabaseType databaseType) {
        SQLStatementContext sqlStatementContext = parseSQL(databaseName, parserEngine, databaseType, sql);
        if (sqlStatementContext instanceof CreateTableStatementContext || sqlStatementContext.getSqlStatement() instanceof CommentStatement
                || sqlStatementContext instanceof CreateIndexStatementContext || sqlStatementContext instanceof AlterTableStatementContext) {
            if (sqlStatementContext.getTablesContext().getSimpleTables().isEmpty()) {
                return sql;
            }
            Optional<String> sqlSchemaName = sqlStatementContext.getTablesContext().getSchemaName();
            if (sqlSchemaName.isPresent() && sqlSchemaName.get().equals(schemaName)) {
                return sql;
            }
            Map<SQLSegment, String> replaceMap = new TreeMap<>(Comparator.comparing(SQLSegment::getStartIndex));
            TableNameSegment tableNameSegment = sqlStatementContext.getTablesContext().getSimpleTables().iterator().next().getTableName();
            replaceMap.put(tableNameSegment, schemaName + "." + tableNameSegment.getIdentifier().getValue());
            return doDecorateActualTable(replaceMap, sql);
        }
        return sql;
    }
}
