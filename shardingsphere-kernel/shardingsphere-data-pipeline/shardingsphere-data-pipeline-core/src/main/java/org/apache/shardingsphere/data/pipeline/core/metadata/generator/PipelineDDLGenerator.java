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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.CreateTableSQLGeneratorFactory;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CommentStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.type.ConstraintAvailable;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Pipeline DDL generator.
 */
@Slf4j
public final class PipelineDDLGenerator {
    
    private static final String DELIMITER = ";";
    
    private static final String SET_SEARCH_PATH_PREFIX = "set search_path";
    
    /**
     * Generate logic DDL.
     * 
     * @param databaseType database type
     * @param sourceDataSource source data source
     * @param schemaName schema name
     * @param logicTableName table name
     * @param actualTableName actual table name
     * @param parserEngine parser engine
     * @return DDL
     * @throws SQLException SQL exception 
     */
    public String generateLogicDDL(final DatabaseType databaseType, final DataSource sourceDataSource,
                                   final String schemaName, final String logicTableName, final String actualTableName, final ShardingSphereSQLParserEngine parserEngine) throws SQLException {
        log.info("generateLogicDDLSQL, databaseType={}, schemaName={}, tableName={}", databaseType.getType(), schemaName, logicTableName);
        StringBuilder result = new StringBuilder();
        for (String each : CreateTableSQLGeneratorFactory.getInstance(databaseType).generate(sourceDataSource, schemaName, actualTableName)) {
            Optional<String> queryContext = decorate(databaseType, sourceDataSource, schemaName, logicTableName, parserEngine, each);
            queryContext.ifPresent(ddlSQL -> result.append(ddlSQL).append(DELIMITER).append(System.lineSeparator()));
        }
        return result.toString();
    }
    
    private Optional<String> decorate(final DatabaseType databaseType, final DataSource dataSource, final String schemaName, final String logicTableName,
                                      final ShardingSphereSQLParserEngine parserEngine, final String sql) throws SQLException {
        if (sql.trim().isEmpty()) {
            return Optional.empty();
        }
        String databaseName;
        try (Connection connection = dataSource.getConnection()) {
            databaseName = connection.getCatalog();
        }
        String result = decorateActualSQL(databaseName, logicTableName, parserEngine, sql.trim());
        // TODO remove it after set search_path is supported.
        if ("openGauss".equals(databaseType.getType())) {
            return decorateOpenGauss(databaseName, schemaName, result, parserEngine);
        }
        return Optional.of(result);
    }
    
    private String decorateActualSQL(final String databaseName, final String logicTableName, final ShardingSphereSQLParserEngine parserEngine, final String sql) {
        QueryContext queryContext = getQueryContext(databaseName, parserEngine, sql);
        SQLStatementContext<?> sqlStatementContext = queryContext.getSqlStatementContext();
        Map<SQLSegment, String> replaceMap = new TreeMap<>(Comparator.comparing(SQLSegment::getStartIndex));
        if (sqlStatementContext instanceof CreateTableStatementContext) {
            appendFromIndexAndConstraint(replaceMap, logicTableName, sqlStatementContext);
            appendFromTable(replaceMap, logicTableName, (TableAvailable) sqlStatementContext);
        }
        if (sqlStatementContext instanceof CommentStatementContext) {
            appendFromTable(replaceMap, logicTableName, (TableAvailable) sqlStatementContext);
        }
        if (sqlStatementContext instanceof CreateIndexStatementContext) {
            appendFromTable(replaceMap, logicTableName, (TableAvailable) sqlStatementContext);
            appendFromIndexAndConstraint(replaceMap, logicTableName, sqlStatementContext);
        }
        if (sqlStatementContext instanceof AlterTableStatementContext) {
            appendFromIndexAndConstraint(replaceMap, logicTableName, sqlStatementContext);
            appendFromTable(replaceMap, logicTableName, (TableAvailable) sqlStatementContext);
        }
        return doDecorateActualTable(replaceMap, sql);
    }
    
    private QueryContext getQueryContext(final String databaseName, final ShardingSphereSQLParserEngine parserEngine, final String sql) {
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(null, parserEngine.parse(sql, false), databaseName);
        return new QueryContext(sqlStatementContext, sql, Collections.emptyList());
    }
    
    private void appendFromIndexAndConstraint(final Map<SQLSegment, String> replaceMap, final String logicTableName, final SQLStatementContext<?> sqlStatementContext) {
        if (!(sqlStatementContext instanceof TableAvailable) || ((TableAvailable) sqlStatementContext).getTablesContext().getTables().isEmpty()) {
            return;
        }
        TableNameSegment tableNameSegment = ((TableAvailable) sqlStatementContext).getTablesContext().getTables().iterator().next().getTableName();
        if (!tableNameSegment.getIdentifier().getValue().equals(logicTableName)) {
            if (sqlStatementContext instanceof IndexAvailable) {
                for (IndexSegment each : ((IndexAvailable) sqlStatementContext).getIndexes()) {
                    String logicIndexName = IndexMetaDataUtil.getLogicIndexName(each.getIndexName().getIdentifier().getValue(), tableNameSegment.getIdentifier().getValue());
                    replaceMap.put(each.getIndexName(), logicIndexName);
                }
            }
            if (sqlStatementContext instanceof ConstraintAvailable) {
                for (ConstraintSegment each : ((ConstraintAvailable) sqlStatementContext).getConstraints()) {
                    String logicConstraint = IndexMetaDataUtil.getLogicIndexName(each.getIdentifier().getValue(), tableNameSegment.getIdentifier().getValue());
                    replaceMap.put(each, logicConstraint);
                }
            }
        }
    }
    
    private void appendFromTable(final Map<SQLSegment, String> replaceMap, final String logicTableName, final TableAvailable sqlStatementContext) {
        for (SimpleTableSegment each : sqlStatementContext.getAllTables()) {
            if (!logicTableName.equals(each.getTableName().getIdentifier().getValue())) {
                replaceMap.put(each.getTableName(), logicTableName);
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
                                               final ShardingSphereSQLParserEngine parserEngine) {
        if (queryContext.toLowerCase().startsWith(SET_SEARCH_PATH_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(replaceTableNameWithPrefix(queryContext, schemaName + ".", databaseName, parserEngine));
    }
    
    private String replaceTableNameWithPrefix(final String sql, final String prefix, final String databaseName, final ShardingSphereSQLParserEngine parserEngine) {
        QueryContext queryContext = getQueryContext(databaseName, parserEngine, sql);
        SQLStatementContext<?> sqlStatementContext = queryContext.getSqlStatementContext();
        if (sqlStatementContext instanceof CreateTableStatementContext || sqlStatementContext instanceof CommentStatementContext
                || sqlStatementContext instanceof CreateIndexStatementContext || sqlStatementContext instanceof AlterTableStatementContext) {
            if (!sqlStatementContext.getTablesContext().getTables().isEmpty()) {
                TableNameSegment tableNameSegment = sqlStatementContext.getTablesContext().getTables().iterator().next().getTableName();
                Map<SQLSegment, String> replaceMap = new TreeMap<>(Comparator.comparing(SQLSegment::getStartIndex));
                replaceMap.put(tableNameSegment, prefix + tableNameSegment.getIdentifier().getValue());
                return doDecorateActualTable(replaceMap, sql);
            }
        }
        return sql;
    }
}
